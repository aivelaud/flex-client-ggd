# GGD MOD — Adım Adım Kurulum Rehberi

> **Araçlar:** Termux + MT Manager (her ikisi de yüklü olmalı)

---

## 📋 GENEL AKIŞ

```
APK indir → Termux'ta derle → Smali üret → MT Manager ile enjekte et → İmzala → Yükle
```

---

## ADIM 1 — Termux Hazırlık

Termux'u aç ve şunları kur:

```bash
pkg update -y
pkg install openjdk-17 git wget unzip -y
```

Baksmali + Smali araçlarını indir:

```bash
cd ~
wget https://github.com/JesusFreke/smali/releases/download/v2.5.2/baksmali-2.5.2.jar
wget https://github.com/JesusFreke/smali/releases/download/v2.5.2/smali-2.5.2.jar

# Kısayol yap
echo 'alias baksmali="java -jar ~/baksmali-2.5.2.jar"' >> ~/.bashrc
echo 'alias smali="java -jar ~/smali-2.5.2.jar"' >> ~/.bashrc
source ~/.bashrc
```

Android SDK (android.jar) indir:

```bash
mkdir -p ~/android-sdk/platforms
cd ~/android-sdk/platforms
wget https://dl.google.com/android/repository/platform-33_r02.zip
unzip platform-33_r02.zip
mv android-33 android-33
# android.jar artık: ~/android-sdk/platforms/android-33/android.jar
```

---

## ADIM 2 — Repo'yu Çek

```bash
cd ~
git clone https://github.com/aivelaud/flex-client-ggd
cd flex-client-ggd
```

---

## ADIM 3 — Java Kaynak Kodunu Derle

```bash
cd ~/flex-client-ggd

ANDROID_JAR="$HOME/android-sdk/platforms/android-33/android.jar"
mkdir -p build/classes

javac -source 8 -target 8 \
  -cp "$ANDROID_JAR" \
  -d build/classes \
  java/NativeGGD.java \
  java/HackOverlayService.java

echo "Derleme başarılı!"
```

---

## ADIM 4 — DEX Oluştur

```bash
# d8 (Android Build Tools içinde gelir, yok ise manuel yol kullan)
# Eğer d8 yoksa Adım 4b'ye git

d8 --output build/ \
   --lib "$ANDROID_JAR" \
   $(find build/classes -name "*.class")
```

### ADIM 4b — d8 yoksa (Alternatif: dx kullan)

```bash
dx --dex \
   --output=build/classes.dex \
   build/classes/
```

---

## ADIM 5 — Smali Üret

```bash
baksmali d build/classes.dex -o smali_output/

# Üretilen dosyaları gör:
ls smali_output/com/unity3d/player/
# Görmesi gerekenler:
#   HackOverlayService.smali
#   HackOverlayService$1.smali   (touch listener)
#   NativeGGD.smali
```

---

## ADIM 6 — MT Manager ile APK'ya Enjekte Et

### 6.1 APK'yı Aç

1. MT Manager'ı aç
2. GGD APK dosyasına git (genellikle `/data/app/...` veya İndirilenler)
3. APK'ya uzun bas → **"Aç"** seç → **"APK Editör"**

### 6.2 Smali Dosyalarını Kopyala

1. APK Editör içinde **"Dex Düzenle"** → `classes.dex` seç → **"Smali Dosyaları"**
2. Sağ üst köşe **"+"** → **"Dosya İçe Aktar"**
3. Şu dosyaları tek tek içe aktar:
   ```
   smali_output/com/unity3d/player/HackOverlayService.smali
   smali_output/com/unity3d/player/HackOverlayService$1.smali
   smali_output/com/unity3d/player/NativeGGD.smali
   ```
4. Kaydet

### 6.3 AndroidManifest.xml Düzenle

1. APK Editör ana ekranında **"AndroidManifest.xml"** seç
2. Düzenle moduna gir

**`<uses-permission>` satırlarının yanına ekle:**
```xml
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
```

**`</application>` kapanmadan hemen ÖNCE ekle:**
```xml
<service
    android:name="com.unity3d.player.HackOverlayService"
    android:enabled="true"
    android:exported="false" />
```

3. Kaydet

### 6.4 UnityPlayerActivity.smali Düzenle

1. `classes.dex` → `com/unity3d/player/UnityPlayerActivity.smali` aç
2. `onCreate` metodunu bul
3. `return-void`'dan ÖNCE şu smali bloğunu ekle:

```smali
# ── İzin kontrolü (SYSTEM_ALERT_WINDOW) ──
invoke-static {}, Landroid/os/Build$VERSION;->SDK_INT:I
sget v1, Landroid/os/Build$VERSION;->SDK_INT:I
const/16 v2, 0x17  # Android M = 23
if-lt v1, v2, :skip_perm_check

invoke-static {p0}, Landroid/provider/Settings;->canDrawOverlays(Landroid/content/Context;)Z
move-result v1
if-nez v1, :skip_perm_check

new-instance v1, Landroid/content/Intent;
const-string v2, "android.settings.action.MANAGE_OVERLAY_PERMISSION"
invoke-direct {v1, v2}, Landroid/content/Intent;-><init>(Ljava/lang/String;)V

invoke-virtual {p0}, Landroid/app/Activity;->getPackageName()Ljava/lang/String;
move-result-object v2
new-instance v3, Ljava/lang/StringBuilder;
invoke-direct {v3}, Ljava/lang/StringBuilder;-><init>()V
const-string v4, "package:"
invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
move-result-object v3
invoke-virtual {v3, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
move-result-object v3
invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
move-result-object v2
invoke-static {v2}, Landroid/net/Uri;->parse(Ljava/lang/String;)Landroid/net/Uri;
move-result-object v2
invoke-virtual {v1, v2}, Landroid/content/Intent;->setData(Landroid/net/Uri;)Landroid/content/Intent;

invoke-virtual {p0, v1}, Landroid/app/Activity;->startActivity(Landroid/content/Intent;)V

:skip_perm_check
```

5. `onResume` metodunu bul, `invoke-super` satırından SONRA ekle:

```smali
# ── Overlay servisi başlat ──
invoke-static {}, Landroid/os/Build$VERSION;->SDK_INT:I
sget v1, Landroid/os/Build$VERSION;->SDK_INT:I
const/16 v2, 0x17
if-lt v1, v2, :start_service

invoke-static {p0}, Landroid/provider/Settings;->canDrawOverlays(Landroid/content/Context;)Z
move-result v1
if-eqz v1, :no_service

:start_service
new-instance v1, Landroid/content/Intent;
const-class v2, Lcom/unity3d/player/HackOverlayService;
invoke-direct {v1, p0, v2}, Landroid/content/Intent;-><init>(Landroid/content/Context;Ljava/lang/Class;)V
invoke-virtual {p0, v1}, Landroid/app/Activity;->startService(Landroid/content/Intent;)Landroid/content/ComponentName;

:no_service
```

6. Kaydet

---

## ADIM 7 — APK'yı Derle ve İmzala

### MT Manager ile:
1. APK Editör → **"Derle"** (sağ üst)
2. Derleme tamamlanınca **"İmzala"** butonuna bas
3. Varsa mevcut imza ile imzala, yoksa **"Yeni Anahtar Oluştur"**
4. İmzalı APK'yı kaydet

### Termux ile (alternatif):
```bash
# apktool ile derle
apktool b modded_ggd_folder -o ggd_modded.apk

# uber-apk-signer ile imzala
java -jar uber-apk-signer.jar --apks ggd_modded.apk
```

---

## ADIM 8 — Yükle ve Test Et

1. Mevcut GGD'yi kaldır (varsa)
2. İmzalı APK'yı yükle
3. Oyunu aç
4. **"Ekranın Üzerinde Göster" (Overlay) izin ekranı açılmalı** → İzin ver
5. Oyuna gir → Sol üstte **"GGD" butonu** görünmeli
6. Butona bas → Menü açılır, toggle'lar çalışır

---

## 🚨 SORUN GİDERME

| Sorun | Çözüm |
|---|---|
| Overlay butonu görünmüyor | Settings → Uygulamalar → GGD → "Diğer uygulamaların üzerinde göster" → Aç |
| Uygulama crash oluyor | smali bloğunu doğru yere ekledin mi? `logcat` ile bak |
| Toggle'lar çalışmıyor | libggdmod.so'daki JNI metod isimleri eşleşmeli, bkz. NATIVE_BRIDGE.md |
| "Verify error" hatası | Smali sözdizimi hatası — bloğu yeniden kontrol et |

---

## 📁 Dosya Yapısı

```
flex-client-ggd/
├── java/
│   ├── HackOverlayService.java       ← Ana GUI servisi
│   ├── NativeGGD.java                ← Native köprü
│   └── UnityPlayerActivity_patch.java ← Referans (değişiklikler burada)
├── smali/
│   └── AndroidManifest_patch.xml     ← Manifest eklentileri
├── build.sh                          ← Otomatik derleme scripti
└── ADIMLAR.md                        ← Bu dosya
```
