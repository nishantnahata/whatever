package com.beertracker.app;

import android.content.Context;
import android.webkit.JavascriptInterface;

/** Exposed to the page as window.BeerBridge so it shares the drink log with the widget. */
final class WebBridge {
    private final Context ctx;

    WebBridge(Context ctx) {
        this.ctx = ctx.getApplicationContext();
    }

    @JavascriptInterface
    public String getEntries() {
        return BeerStore.getJson(ctx);
    }

    @JavascriptInterface
    public void setEntries(String json) {
        BeerStore.setJson(ctx, json);
    }
}
