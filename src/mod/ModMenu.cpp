#include "ModMenu.h"
#include <android/log.h>
#include <jni.h>
#include <pthread.h>
#include <unistd.h>
#include <string>

#define TAG "GGD-MOD-MENU"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  TAG, __VA_ARGS__)

// ──────────────────────────────────────────────────────────────
//  Mod Menü Implementation  (v1.2)
// ──────────────────────────────────────────────────────────────

static JavaVM* gJavaVM   = nullptr;
static jobject gActivity = nullptr;

// ── Overlay thread — Java katmanını başlatır ──────────────────
static void* overlayThread(void*) {
    JNIEnv* env;
    gJavaVM->AttachCurrentThread(&env, nullptr);

    LOGI("Overlay thread başlatıldı");

    jclass overlayClass = env->FindClass("com/flex/ggdmod/ModMenuOverlay");
    if (!overlayClass) {
        LOGI("ModMenuOverlay class bulunamadı — DEX inject yapıldı mı?");
        gJavaVM->DetachCurrentThread();
        return nullptr;
    }

    jmethodID showMenu = env->GetStaticMethodID(
            overlayClass, "show", "(Landroid/app/Activity;)V");
    if (showMenu && gActivity) {
        env->CallStaticVoidMethod(overlayClass, showMenu, gActivity);
        LOGI("ModMenuOverlay.show() çağrıldı");
    } else {
        LOGI("show() metodu bulunamadı veya activity null");
    }

    gJavaVM->DetachCurrentThread();
    return nullptr;
}

void ModMenu::init(void* jni_env, void* activity) {
    JNIEnv* env = (JNIEnv*)jni_env;
    env->GetJavaVM(&gJavaVM);
    gActivity = env->NewGlobalRef((jobject)activity);
    LOGI("ModMenu::init — activity=%p", activity);

    pthread_t tid;
    pthread_create(&tid, nullptr, overlayThread, nullptr);
    pthread_detach(tid);
}

void ModMenu::toggle() {
    menuVisible = !menuVisible;
    LOGI("Menü %s", menuVisible ? "AÇIK" : "KAPALI");
}

void ModMenu::render() {
    if (!menuVisible) return;
    LOGI("[ESP=%d] [Roller=%d] [Ölüler=%d] [Cooldown=%d] [Katil=%d] [Görüş=%d] [Hız×%.0f] [OtoKill=%d]",
        espEnabled, showRoles, seeDeadPlayers, noKillCooldown,
        alwaysImpostor, infiniteVision, speedMultiplier, autoKill);
}

void ModMenu::shutdown() {
    if (gActivity && gJavaVM) {
        JNIEnv* env;
        gJavaVM->AttachCurrentThread(&env, nullptr);
        env->DeleteGlobalRef(gActivity);
        gJavaVM->DetachCurrentThread();
    }
}

void ModMenu::showToast(void* env, const char* msg) {
    JNIEnv* jenv = (JNIEnv*)env;
    jclass  toast = jenv->FindClass("android/widget/Toast");
    if (!toast) return;
    jmethodID makeText = jenv->GetStaticMethodID(toast, "makeText",
        "(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;");
    jmethodID show = jenv->GetMethodID(toast, "show", "()V");
    jstring   jmsg = jenv->NewStringUTF(msg);
    jobject   t    = jenv->CallStaticObjectMethod(toast, makeText, gActivity, jmsg, 1);
    jenv->CallVoidMethod(t, show);
}
