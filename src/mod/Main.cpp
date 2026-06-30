#include <jni.h>
#include <dlfcn.h>
#include <pthread.h>
#include <unistd.h>
#include <android/log.h>
#include "../mod/ModMenu.h"
#include "../hooks/PlayerHooks.h"
#include "../il2cpp/Il2CppResolver.h"

#define TAG "GGD-MOD"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

// ──────────────────────────────────────────────────────────────
//  FlexClient GGD Mod v1.2
//  Yenilikler: Hız ×5/×10, Her Zaman Katil, Sonsuz Görüş, Oto Kill
//              Runtime SYSTEM_ALERT_WINDOW izin isteği
// ──────────────────────────────────────────────────────────────

static void* hackThread(void*) {
    LOGI("=== FlexClient GGD Mod v1.2 ===");
    LOGI("GGD v4.09.00 — IL2CPP hook başlatılıyor...");

    // libil2cpp.so'nun yüklenmesini bekle (max 30s)
    for (int i = 0; i < 30; i++) {
        void* h = dlopen("libil2cpp.so", RTLD_NOW | RTLD_NOLOAD);
        if (h) { dlclose(h); break; }
        LOGI("libil2cpp bekleniyor... (%d/30)", i + 1);
        sleep(1);
    }

    // IL2CPP API başlat
    if (!IL2CPP.init()) {
        LOGE("IL2CPP init başarısız — mod çalışmayacak");
        return nullptr;
    }
    LOGI("IL2CPP API hazır");

    // Hook'ları kur
    PlayerHooks::init();
    LOGI("Player hook'ları kuruldu");

    LOGI("=== Mod hazır — logcat'i izle ===");
    return nullptr;
}

// JNI_OnLoad: .so inject edildiğinde otomatik çağrılır
extern "C" JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    LOGI("JNI_OnLoad — FlexClient GGD v1.2");

    pthread_t tid;
    pthread_create(&tid, nullptr, hackThread, nullptr);
    pthread_detach(tid);

    return JNI_VERSION_1_6;
}

// ──────────────────────────────────────────────────────────────
//  JNI bridge: Java ModMenuOverlay → C++ ModMenu
// ──────────────────────────────────────────────────────────────
extern "C" JNIEXPORT void JNICALL
Java_com_flex_ggdmod_ModMenuOverlay_nativeToggleFeature(
        JNIEnv* env, jclass, jstring featureName, jboolean enabled) {
    const char* name = env->GetStringUTFChars(featureName, nullptr);
    bool val = (bool)enabled;

    if      (strcmp(name, "esp")            == 0) ModMenu::espEnabled     = val;
    else if (strcmp(name, "showRoles")      == 0) ModMenu::showRoles      = val;
    else if (strcmp(name, "seeDeadPlayers") == 0) ModMenu::seeDeadPlayers = val;
    else if (strcmp(name, "noKillCooldown") == 0) ModMenu::noKillCooldown = val;
    else if (strcmp(name, "alwaysImpostor") == 0) ModMenu::alwaysImpostor = val;
    else if (strcmp(name, "infiniteVision") == 0) ModMenu::infiniteVision = val;
    else if (strcmp(name, "autoKill")       == 0) ModMenu::autoKill       = val;
    else if (strcmp(name, "speedHackX5")    == 0) {
        ModMenu::speedHackX5      = val;
        if (val) { ModMenu::speedHackX10 = false; ModMenu::speedMultiplier = 5.0f; }
        else if (!ModMenu::speedHackX10)           ModMenu::speedMultiplier = 1.0f;
    }
    else if (strcmp(name, "speedHackX10")   == 0) {
        ModMenu::speedHackX10     = val;
        if (val) { ModMenu::speedHackX5 = false; ModMenu::speedMultiplier = 10.0f; }
        else if (!ModMenu::speedHackX5)            ModMenu::speedMultiplier = 1.0f;
    }

    LOGI("Feature toggle: %s = %s", name, val ? "ON" : "OFF");
    env->ReleaseStringUTFChars(featureName, name);
}

extern "C" JNIEXPORT void JNICALL
Java_com_flex_ggdmod_ModMenuOverlay_nativeInit(
        JNIEnv* env, jclass, jobject activity) {
    ModMenu::init(env, activity);
}
