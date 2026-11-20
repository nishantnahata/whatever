package com.beertracker.app;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

/**
 * The drink log, shared by the app page (via {@link WebBridge}) and the home-screen widget.
 * Stored as the same JSON array the page uses: [{id, ts, ml, abv, name, size}, ...].
 */
final class BeerStore {
    private static final String PREFS = "beer";
    private static final String KEY = "entries";
    static final double WEEKLY_LIMIT = 14.0;
    private static final Object LOCK = new Object();

    private BeerStore() {}

    private static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** Raw JSON, or null if the app has never stored anything natively (triggers migration in the page). */
    static String getJson(Context c) {
        synchronized (LOCK) {
            return prefs(c).getString(KEY, null);
        }
    }

    static void setJson(Context c, String json) {
        synchronized (LOCK) {
            prefs(c).edit().putString(KEY, json).commit();
        }
        BeerWidget.refreshAll(c);
    }

    static JSONArray load(Context c) {
        String json = getJson(c);
        if (json == null) return new JSONArray();
        try {
            return new JSONArray(json);
        } catch (JSONException e) {
            return new JSONArray();
        }
    }

    /** Logs a pint now, at the strength of the most recent drink (or 4.5%). Returns its units. */
    static double addPint(Context c) {
        double units;
        synchronized (LOCK) {
            JSONArray arr = load(c);
            double abv = 4.5;
            long latest = Long.MIN_VALUE;
            for (int i = 0; i < arr.length(); i++) {
                JSONObject e = arr.optJSONObject(i);
                if (e != null && e.optLong("ts") > latest && e.optDouble("abv", -1) > 0) {
                    latest = e.optLong("ts");
                    abv = e.optDouble("abv");
                }
            }
            long now = System.currentTimeMillis();
            JSONObject e = new JSONObject();
            try {
                e.put("id", Long.toString(now, 36) + Integer.toString((int) (Math.random() * 1e6), 36));
                e.put("ts", now);
                e.put("ml", 568);
                e.put("abv", abv);
                e.put("name", "");
                e.put("size", "pint");
            } catch (JSONException ignored) {
            }
            arr.put(e);
            prefs(c).edit().putString(KEY, arr.toString()).commit();
            units = 568 * abv / 1000.0;
        }
        BeerWidget.refreshAll(c);
        return units;
    }

    static final class Week {
        int drinks;
        int today;
        double units;
    }

    /** Totals for the current Monday-to-Sunday week, in local time. */
    static Week thisWeek(Context c) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long todayStart = cal.getTimeInMillis();
        int sinceMonday = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7;
        cal.add(Calendar.DAY_OF_MONTH, -sinceMonday);
        long weekStart = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        long weekEnd = cal.getTimeInMillis();
        Calendar t = Calendar.getInstance();
        t.setTimeInMillis(todayStart);
        t.add(Calendar.DAY_OF_MONTH, 1);
        long todayEnd = t.getTimeInMillis();

        Week w = new Week();
        JSONArray arr = load(c);
        for (int i = 0; i < arr.length(); i++) {
            JSONObject e = arr.optJSONObject(i);
            if (e == null) continue;
            long ts = e.optLong("ts");
            if (ts >= weekStart && ts < weekEnd) {
                w.drinks++;
                w.units += e.optDouble("ml", 0) * e.optDouble("abv", 0) / 1000.0;
                if (ts >= todayStart && ts < todayEnd) w.today++;
            }
        }
        return w;
    }
}
