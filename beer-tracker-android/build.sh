#!/usr/bin/env bash
# Quick local APK build without Gradle or the Google SDK manager (for sideloading/testing).
# Google Play builds (.aab) come from Gradle in GitHub Actions: see .github/workflows/beer-tracker-android.yml.
# Needs (Ubuntu/Debian): apt-get install aapt apksigner zipalign dalvik-exchange android-sdk-platform-23 default-jdk
set -euo pipefail
cd "$(dirname "$0")"

ANDROID_JAR=${ANDROID_JAR:-/usr/lib/android-sdk/platforms/android-23/android.jar}
KEYSTORE=${KEYSTORE:-$HOME/.android/beer-tracker.keystore}
MAIN=app/src/main
OUT=build
APK=dist/beer-tracker.apk

# Single source of truth for identity and version: app/build.gradle
gradle_val() { sed -nE "s/^[[:space:]]*$1[[:space:]]+'?([^' ]+)'?.*/\1/p" app/build.gradle | head -1; }
APP_ID=$(gradle_val applicationId); NAMESPACE=$(gradle_val namespace)
MIN_SDK=$(gradle_val minSdk); TARGET_SDK=$(gradle_val targetSdk)
VERSION_CODE=$(gradle_val versionCode); VERSION_NAME=$(gradle_val versionName)

rm -rf "$OUT" && mkdir -p "$OUT/gen" "$OUT/classes" "$OUT/dex" dist
# aapt (v1) needs the package attribute in the manifest; Gradle takes it from build.gradle instead.
sed "s|<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\">|<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\" package=\"$NAMESPACE\">|" \
  "$MAIN/AndroidManifest.xml" > "$OUT/AndroidManifest.xml"
AAPT_OPTS=(-M "$OUT/AndroidManifest.xml" -S "$MAIN/res" -I "$ANDROID_JAR"
  --min-sdk-version "$MIN_SDK" --target-sdk-version "$TARGET_SDK"
  --version-code "$VERSION_CODE" --version-name "$VERSION_NAME" --rename-manifest-package "$APP_ID")

echo "1/5 resources ($APP_ID $VERSION_NAME, target $TARGET_SDK)"
aapt package -f -m -J "$OUT/gen" "${AAPT_OPTS[@]}"

echo "2/5 compile"
javac -source 8 -target 8 -Xlint:-options -encoding UTF-8 \
  -bootclasspath "$ANDROID_JAR" -d "$OUT/classes" \
  $(find "$MAIN/java" "$OUT/gen" -name '*.java')

echo "3/5 dex"
dalvik-exchange --dex --min-sdk-version="$MIN_SDK" --output="$OUT/dex/classes.dex" "$OUT/classes"

echo "4/5 package"
aapt package -f "${AAPT_OPTS[@]}" -A "$MAIN/assets" -F "$OUT/unsigned.apk"
(cd "$OUT/dex" && aapt add ../unsigned.apk classes.dex >/dev/null)
zipalign -f -p 4 "$OUT/unsigned.apk" "$OUT/aligned.apk"

echo "5/5 sign"
if [ ! -f "$KEYSTORE" ]; then
  mkdir -p "$(dirname "$KEYSTORE")"
  keytool -genkeypair -keystore "$KEYSTORE" -storepass beertracker -keypass beertracker \
    -alias beer -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Beer Tracker" >/dev/null 2>&1
fi
apksigner sign --ks "$KEYSTORE" --ks-pass pass:beertracker --key-pass pass:beertracker \
  --min-sdk-version "$MIN_SDK" --out "$APK" "$OUT/aligned.apk"
rm -f "$APK.idsig"
apksigner verify "$APK"
echo "Built $APK ($(du -h "$APK" | cut -f1))"
