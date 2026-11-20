# Beer Tracker (Android)

The weekly beer tracker as a small offline Android app. Log pints, halves, cans and bottles, and see
drinks, UK units against the 14/week guide, dry days, and the last 8 weeks.
Includes a home-screen widget (this week's drinks, units, today's count and a one-tap **+ Pint**).
Data is stored on the phone only. There is no account and no internet permission.

## Install

1. Copy `dist/beer-tracker.apk` to the phone (or download it from GitHub on the phone).
2. Open it. Android will ask to allow installs from that app (Chrome / Files): allow it.
3. Play Protect may say the app is unrecognised. Tap **More details → Install anyway**.

Needs Android 6.0 or newer.

Forgot to log something? Change the **Date** under Quick add (or set a date and time in
**Add with details**) to log it on any day up to a year back.

Switching phones? In the app's **Backup** section, tap **Export** on the old phone and save the file
somewhere the new phone can reach (Google Drive, a message to yourself). On the new phone, install the
app and tap **Import**. Importing adds to what's already there and skips drinks it already has.

To add the widget: long-press the home screen → **Widgets** → **Beer Tracker**.
The **+ Pint** button logs a 568ml pint at the strength of the last drink logged (4.5% if none).

## Google Play

See [`store/PLAY_STORE.md`](store/PLAY_STORE.md) for the full publishing guide, listing text and
store graphics. Play bundles are built by GitHub Actions (`.github/workflows/beer-tracker-android.yml`)
and signed with the upload key stored in repository secrets.

## Build

```
sudo apt-get install aapt apksigner zipalign dalvik-exchange android-sdk-platform-23 default-jdk
./build.sh
```

The app is `app/src/main/assets/index.html` inside a WebView (`app/src/main/java/.../MainActivity.java`). The drink log lives in
SharedPreferences (`BeerStore.java`), which the page reaches through `WebBridge.java` and the widget
(`BeerWidget.java`, `res/layout/widget.xml`) reads directly.
To ship an update, bump `versionCode` in `app/build.gradle` and sign it with the **same keystore**
(`~/.android/beer-tracker.keystore`). Otherwise the phone will refuse the update, and uninstalling
the old app first deletes the logged data.
