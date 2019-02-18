cd "$(dirname "$0")"

# 0. Make directories
mkdir ../../outputs
mkdir ../../outputs/configs

# 1. Zipalign & sign apk
./zipalign -f -v 4 ../build/outputs/apk/release/app-release-unsigned.apk aligned.apk
java -jar apksigner.jar sign --ks smoothappsstudio --ks-pass pass:zdhszyzs --out "$1".apk aligned.apk

# 2. Copy signed APK to output directory
mv "$1".apk ../../outputs/message_"$1".apk

# 3. Mapping & configs
mv ../build/outputs/mapping/release/mapping.txt ../../outputs/"$1"-code-mapping.txt
cp ../build/intermediates/assets/release/alerts.tl ../../outputs/configs/alerts-"$1".tl

# 4. Cleanup
rm aligned.apk
rm -r ../build/outputs/apk/release
