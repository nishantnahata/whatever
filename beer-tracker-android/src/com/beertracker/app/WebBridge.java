package com.beertracker.app;

import android.webkit.JavascriptInterface;

/** Exposed to the page as window.BeerBridge: the shared drink log, plus backup export/import. */
final class WebBridge {
    private final MainActivity activity;

    WebBridge(MainActivity activity) {
        this.activity = activity;
    }

    @JavascriptInterface
    public String getEntries() {
        return BeerStore.getJson(activity);
    }

    @JavascriptInterface
    public void setEntries(String json) {
        BeerStore.setJson(activity, json);
    }

    /** Asks where to save, then writes {@code json}. Result arrives via window.onNativeExport(ok, error). */
    @JavascriptInterface
    public void exportData(String json, String fileName) {
        activity.startExport(json, fileName);
    }

    /** Asks for a file. Result arrives via window.onNativeImport(ok, textOrError). */
    @JavascriptInterface
    public void importData() {
        activity.startImport();
    }
}
