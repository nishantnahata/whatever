#!/usr/bin/env bash
# Builds a signed, installable APK without Gradle or the Google SDK manager.
# Needs (Ubuntu/Debian): apt-get install aapt apksigner zipalign dalvik-exchange android-sdk-platform-23 default-jdk
set -euo pipefail
cd "$(dirname "$0")"

ANDROID_JAR=${ANDROID_JAR:-/usr/lib/android-sdk/platforms/android-23/android.jar}
KEYSTORE=${KEYSTORE:-$HOME/.android/beer-tracker.keystore}
OUT=build
APK=dist/beer-tracker.apk

rm -rf "$OUT" && mkdir -p "$OUT/gen" "$OUT/classes" "$OUT/dex" dist

echo "1/5 resources"
aapt package -f -m -J "$OUT/gen" -M AndroidManifest.xml -S res -I "$ANDROID_JAR"

echo "2/5 compile"
javac -source 8 -target 8 -Xlint:-options -encoding UTF-8 \
  -bootclasspath "$ANDROID_JAR" -d "$OUT/classes" \
  $(find src "$OUT/gen" -name '*.java')

echo "3/5 dex"
dalvik-exchange --dex --min-sdk-version=23 --output="$OUT/dex/classes.dex" "$OUT/classes"

echo "4/5 package"
aapt package -f -M AndroidManifest.xml -S res -A assets -I "$ANDROID_JAR" -F "$OUT/unsigned.apk"
(cd "$OUT/dex" && aapt add ../unsigned.apk classes.dex >/dev/null)
zipalign -f -p 4 "$OUT/unsigned.apk" "$OUT/aligned.apk"

echo "5/5 sign"
if [ ! -f "$KEYSTORE" ]; then
  mkdir -p "$(dirname "$KEYSTORE")"
  keytool -genkeypair -keystore "$KEYSTORE" -storepass beertracker -keypass beertracker \
    -alias beer -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Beer Tracker" >/dev/null 2>&1
fi
apksigner sign --ks "$KEYSTORE" --ks-pass pass:beertracker --key-pass pass:beertracker \
  --min-sdk-version 23 --out "$APK" "$OUT/aligned.apk"
apksigner verify "$APK"
echo "Built $APK ($(du -h "$APK" | cut -f1))"
