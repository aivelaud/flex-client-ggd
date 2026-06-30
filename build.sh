#!/data/data/com.termux/files/usr/bin/bash
# ============================================================
#  GGD Mod - Termux Build Script
#  Çalıştır: bash build.sh
# ============================================================

set -e   # hata olursa dur

JAVA_SRC="./java"
OUT_DIR="./build"
DEX_OUT="$OUT_DIR/classes.dex"
SMALI_OUT="./smali_output"

echo "=== [1/5] Bağımlılıkları kontrol ediyorum ==="
for tool in javac d8 baksmali; do
    if ! command -v $tool &>/dev/null; then
        echo "HATA: '$tool' bulunamadı."
        echo "Termux'ta şunu çalıştır: pkg install openjdk-17 && pip install d8 || apt install d8"
        echo "Baksmali için: https://github.com/JesusFreke/smali/releases adresinden indir"
        exit 1
    fi
done

echo "=== [2/5] Java derleyici (javac) ==="
mkdir -p "$OUT_DIR/classes"

# Android SDK stub'larına ihtiyacımız var — Termux'taki android.jar yolunu yaz:
ANDROID_JAR="$HOME/android-sdk/platforms/android-33/android.jar"
if [ ! -f "$ANDROID_JAR" ]; then
    echo "UYARI: android.jar bulunamadı: $ANDROID_JAR"
    echo "İndir: https://dl.google.com/android/repository/platform-33-ext4_r01.zip"
    echo "Veya sadece smali_manual klasörünü kullan (ADIMLAR.md Adım 4b)"
    # Stub olmadan compile edemeyiz, manuel smali yoluna yönlendir
    exit 1
fi

javac -source 8 -target 8 \
    -cp "$ANDROID_JAR" \
    -d "$OUT_DIR/classes" \
    "$JAVA_SRC/NativeGGD.java" \
    "$JAVA_SRC/HackOverlayService.java"

echo "=== [3/5] DEX oluşturuluyor (d8) ==="
d8 --output "$OUT_DIR" \
   --lib "$ANDROID_JAR" \
   $(find "$OUT_DIR/classes" -name "*.class")

echo "=== [4/5] Smali üretiliyor (baksmali) ==="
mkdir -p "$SMALI_OUT"
baksmali d "$DEX_OUT" -o "$SMALI_OUT"

echo "=== [5/5] Tamamlandı! ==="
echo ""
echo "Üretilen smali dosyaları:"
find "$SMALI_OUT" -name "*.smali" | while read f; do echo "  $f"; done
echo ""
echo "Sonraki adım: ADIMLAR.md → Adım 5 (MT Manager enjeksiyon)"
