package com.beertracker.app;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.Locale;

/** Home-screen widget: this week's drinks and units, today's count, and a one-tap "+ Pint". */
public class BeerWidget extends AppWidgetProvider {
    static final String ACTION_ADD_PINT = "com.beertracker.app.ADD_PINT";

    @Override
    public void onUpdate(Context c, AppWidgetManager mgr, int[] ids) {
        render(c, mgr, ids);
    }

    @Override
    public void onReceive(Context c, Intent intent) {
        String action = intent.getAction();
        if (ACTION_ADD_PINT.equals(action)) {
            double u = BeerStore.addPint(c);
            Toast.makeText(c, String.format(Locale.UK, "Pint added · %s units", fmt1(u)), Toast.LENGTH_SHORT).show();
            return;
        }
        if (Intent.ACTION_DATE_CHANGED.equals(action) || Intent.ACTION_TIME_CHANGED.equals(action)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
            refreshAll(c);
            return;
        }
        super.onReceive(c, intent);
    }

    static void refreshAll(Context c) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(c);
        int[] ids = mgr.getAppWidgetIds(new ComponentName(c, BeerWidget.class));
        if (ids != null && ids.length > 0) render(c, mgr, ids);
    }

    private static void render(Context c, AppWidgetManager mgr, int[] ids) {
        BeerStore.Week w = BeerStore.thisWeek(c);
        boolean over = w.units > BeerStore.WEEKLY_LIMIT;
        int pct = (int) Math.round(Math.min(1.0, w.units / BeerStore.WEEKLY_LIMIT) * 100);

        Intent open = new Intent(c, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent openPi = PendingIntent.getActivity(c, 0, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent add = new Intent(c, BeerWidget.class);
        add.setAction(ACTION_ADD_PINT);
        PendingIntent addPi = PendingIntent.getBroadcast(c, 1, add,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        for (int id : ids) {
            RemoteViews v = new RemoteViews(c.getPackageName(), R.layout.widget);
            v.setTextViewText(R.id.w_drinks, Integer.toString(w.drinks));
            v.setTextViewText(R.id.w_drinks_label, w.drinks == 1 ? "drink" : "drinks");
            v.setTextViewText(R.id.w_units, fmt1(w.units));
            v.setTextViewText(R.id.w_today, Integer.toString(w.today));
            v.setProgressBar(R.id.w_bar, 100, pct, false);
            v.setProgressBar(R.id.w_bar_over, 100, 100, false);
            v.setViewVisibility(R.id.w_bar, over ? View.GONE : View.VISIBLE);
            v.setViewVisibility(R.id.w_bar_over, over ? View.VISIBLE : View.GONE);
            v.setTextViewText(R.id.w_note, over
                    ? String.format(Locale.UK, "%s over the 14-unit guide", fmt1(w.units - BeerStore.WEEKLY_LIMIT))
                    : String.format(Locale.UK, "%s of 14 units", fmt1(w.units)));
            v.setOnClickPendingIntent(R.id.w_root, openPi);
            v.setOnClickPendingIntent(R.id.w_add, addPi);
            mgr.updateAppWidget(id, v);
        }
    }

    static String fmt1(double n) {
        double r = Math.round(n * 10) / 10.0;
        return r == Math.floor(r) ? Long.toString((long) r) : Double.toString(r);
    }
}
