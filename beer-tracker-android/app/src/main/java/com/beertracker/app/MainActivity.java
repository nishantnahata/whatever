package com.beertracker.app;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/** Hosts the beer tracker page and handles saving/opening backup files for it. */
public class MainActivity extends Activity {
    private static final int REQ_EXPORT = 41;
    private static final int REQ_IMPORT = 42;
    private static final int MAX_IMPORT_BYTES = 10 * 1024 * 1024;

    private WebView web;
    private String pendingExport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int bg = getResources().getColor(R.color.bg, getTheme());
        web = new WebView(this);
        web.setBackgroundColor(bg);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setSupportZoom(false);
        web.addJavascriptInterface(new WebBridge(this), "BeerBridge");
        web.setWebViewClient(new WebViewClient());
        // Android 15+ draws the app behind the status/navigation bars (and, with adjustResize, the keyboard
        // insets arrive here too). Pad a container by those insets so the page never sits underneath them.
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(bg);
        root.addView(web, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        root.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            @SuppressWarnings("deprecation")
            public WindowInsets onApplyWindowInsets(View v, WindowInsets in) {
                v.setPadding(in.getSystemWindowInsetLeft(), in.getSystemWindowInsetTop(),
                        in.getSystemWindowInsetRight(), in.getSystemWindowInsetBottom());
                return in.consumeSystemWindowInsets();
            }
        });
        setContentView(root);
        if (savedInstanceState != null) {
            web.restoreState(savedInstanceState);
        } else {
            web.loadUrl("file:///android_asset/index.html");
        }
    }

    // --- Backup files (called from WebBridge on its own thread) ---

    /** Opens the system "save as" screen; the chosen file gets {@code json}. */
    void startExport(final String json, final String fileName) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pendingExport = json;
                Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("application/json");
                i.putExtra(Intent.EXTRA_TITLE, fileName);
                try {
                    startActivityForResult(i, REQ_EXPORT);
                } catch (ActivityNotFoundException e) {
                    pendingExport = null;
                    callJs("onNativeExport", false, "No file app available to save the backup");
                }
            }
        });
    }

    /** Opens the system file picker; the chosen file's text is handed to the page. */
    void startImport() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*"); // backups shared via chat/email often lose their JSON type
                try {
                    startActivityForResult(i, REQ_IMPORT);
                } catch (ActivityNotFoundException e) {
                    callJs("onNativeImport", false, "No file app available to open a backup");
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final Uri uri = (resultCode == RESULT_OK && data != null) ? data.getData() : null;
        if (requestCode == REQ_EXPORT) {
            final String json = pendingExport;
            pendingExport = null;
            if (uri == null || json == null) return; // cancelled
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        OutputStream out = getContentResolver().openOutputStream(uri, "w");
                        if (out == null) throw new java.io.IOException("no stream");
                        try {
                            out.write(json.getBytes("UTF-8"));
                        } finally {
                            out.close();
                        }
                        callJs("onNativeExport", true, "");
                    } catch (Exception e) {
                        callJs("onNativeExport", false, "Couldn’t save the backup file");
                    }
                }
            }).start();
        } else if (requestCode == REQ_IMPORT) {
            if (uri == null) return; // cancelled
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        InputStream in = getContentResolver().openInputStream(uri);
                        if (in == null) throw new java.io.IOException("no stream");
                        ByteArrayOutputStream buf = new ByteArrayOutputStream();
                        try {
                            byte[] b = new byte[16384];
                            int n;
                            while ((n = in.read(b)) > 0) {
                                buf.write(b, 0, n);
                                if (buf.size() > MAX_IMPORT_BYTES) {
                                    callJs("onNativeImport", false, "That file is too big to be a Beer Tracker backup");
                                    return;
                                }
                            }
                        } finally {
                            in.close();
                        }
                        callJs("onNativeImport", true, buf.toString("UTF-8"));
                    } catch (Exception e) {
                        callJs("onNativeImport", false, "Couldn’t open that file");
                    }
                }
            }).start();
        }
    }

    /** Calls window[fn](ok, text) in the page on the UI thread. */
    private void callJs(final String fn, final boolean ok, final String text) {
        final String js = "window." + fn + " && window." + fn + "(" + ok + "," + JSONObject.quote(text) + ")";
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (web != null) web.evaluateJavascript(js, null);
            }
        });
    }

    // --- Lifecycle ---

    @Override
    protected void onResume() {
        super.onResume();
        // Pick up pints logged from the widget while the app was in the background.
        web.evaluateJavascript("window.reloadFromNative && reloadFromNative()", null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        web.saveState(outState);
    }

    @Override
    protected void onDestroy() {
        if (web != null) web.destroy();
        web = null;
        super.onDestroy();
    }
}
