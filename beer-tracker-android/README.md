# Beer Tracker (Android)

The weekly beer tracker as a small offline Android app. Log pints, halves, cans and bottles, and see
drinks, UK units against the 14/week guide, dry days, and the last 8 weeks.
Data is stored on the phone only. There is no account and no internet permission.

## Install

1. Copy `dist/beer-tracker.apk` to the phone (or download it from GitHub on the phone).
2. Open it. Android will ask to allow installs from that app (Chrome / Files): allow it.
3. Play Protect may say the app is unrecognised. Tap **More details → Install anyway**.

Needs Android 6.0 or newer.

## Build

```
sudo apt-get install aapt apksigner zipalign dalvik-exchange android-sdk-platform-23 default-jdk
./build.sh
```

The app is `assets/index.html` inside a WebView (`src/.../MainActivity.java`).
To ship an update, bump `versionCode` in `AndroidManifest.xml` and sign it with the **same keystore**
(`~/.android/beer-tracker.keystore`). Otherwise the phone will refuse the update, and uninstalling
the old app first deletes the logged data.
