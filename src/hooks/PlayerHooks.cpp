#include "PlayerHooks.h"
#include "../il2cpp/Il2CppResolver.h"
#include "../mod/ModMenu.h"
#include <android/log.h>
#include <dlfcn.h>
#include <unistd.h>
#include <sys/mman.h>
#include <cstring>
#include <cstdint>
#include <cmath>

#define TAG "GGD-MOD"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

// ──────────────────────────────────────────────────────────────
//  ARM64 Inline Hook — Dobby gerektirmez
// ──────────────────────────────────────────────────────────────

static bool patchPage(void* addr, void* src, size_t len) {
    uintptr_t page = (uintptr_t)addr & ~(uintptr_t)(getpagesize() - 1);
    if (mprotect((void*)page, getpagesize() * 2, PROT_READ | PROT_WRITE | PROT_EXEC) != 0)
        return false;
    memcpy(addr, src, len);
    __builtin___clear_cache((char*)addr, (char*)addr + len);
    return true;
}

// ARM64 B instruction (yakın menzil, ±128 MB)
static uint32_t armB(uintptr_t from, uintptr_t to) {
    int64_t offset = ((int64_t)to - (int64_t)from) >> 2;
    return (uint32_t)(0x14000000 | (offset & 0x3FFFFFF));
}

// ARM64 uzak atlama: LDR X17,#8 ; BR X17 ; <addr64>
static bool applyFarHook(void* target, void* replacement) {
    uint8_t tramp[16];
    // LDR X17, #8
    *(uint32_t*)(tramp + 0) = 0x58000051;
    // BR X17
    *(uint32_t*)(tramp + 4) = 0xD61F0220;
    // Hedef adres (64-bit)
    *(uint64_t*)(tramp + 8) = (uint64_t)replacement;
    return patchPage(target, tramp, 16);
}

static bool hookFunction(void* target, void* replacement) {
    if (!target || !replacement) return false;
    uintptr_t from = (uintptr_t)target;
    uintptr_t to   = (uintptr_t)replacement;
    int64_t   off  = (int64_t)to - (int64_t)from;

    if (off >= -(1 << 27) && off < (1 << 27)) {
        // Yakın menzil — tek B instruction
        uint32_t instr = armB(from, to);
        return patchPage(target, &instr, 4);
    } else {
        // Uzak menzil — 16-byte trampolin
        return applyFarHook(target, replacement);
    }
}

// ──────────────────────────────────────────────────────────────
//  Orijinal fonksiyon pointer'ları
// ──────────────────────────────────────────────────────────────
static int   (*orig_GetIsAlive)(void* self)            = nullptr;
static int   (*orig_GetIsVisible)(void* self)          = nullptr;
static int   (*orig_GetHasKilledThisRound)(void* self) = nullptr;
static float (*orig_GetMoveSpeed)(void* self)          = nullptr;
static float (*orig_GetVisionRange)(void* self)        = nullptr;
static int   (*orig_GetRole)(void* self)               = nullptr;
static float (*orig_GetKillCooldown)(void* self)       = nullptr;

// ── HOOK Implementasyonları ───────────────────────────────────

// Ölü oyuncuları canlı göster (ESP + seeDeadPlayers)
static int hook_GetIsAlive(void* self) {
    if (ModMenu::seeDeadPlayers) return 1;
    return orig_GetIsAlive ? orig_GetIsAlive(self) : 1;
}

// Tüm oyuncuları görünür yap (ESP)
static int hook_GetIsVisible(void* self) {
    if (ModMenu::espEnabled) return 1;
    return orig_GetIsVisible ? orig_GetIsVisible(self) : 0;
}

// Kill cooldown sıfırla
static float hook_GetKillCooldown(void* self) {
    if (ModMenu::noKillCooldown) return 0.0f;
    return orig_GetKillCooldown ? orig_GetKillCooldown(self) : 25.0f;
}

// Hız hack — ×5 veya ×10 çarpanı uygula
static float hook_GetMoveSpeed(void* self) {
    float base = orig_GetMoveSpeed ? orig_GetMoveSpeed(self) : 2.5f;
    if (ModMenu::speedHackX5 || ModMenu::speedHackX10) {
        return base * ModMenu::speedMultiplier;
    }
    return base;
}

// Sonsuz görüş — görüş menzilini çok büyük yap
static float hook_GetVisionRange(void* self) {
    if (ModMenu::infiniteVision) return 99999.0f;
    return orig_GetVisionRange ? orig_GetVisionRange(self) : 2.0f;
}

// Her zaman katil — rol ID'sini 1 (Impostor) döndür
static int hook_GetRole(void* self) {
    if (ModMenu::alwaysImpostor) return 1; // 1 = Impostor/Goose (katil)
    return orig_GetRole ? orig_GetRole(self) : 0;
}

// Kill control (autoKill için HasKilledThisRound her zaman false döner)
static int hook_GetHasKilledThisRound(void* self) {
    if (ModMenu::autoKill || ModMenu::noKillCooldown) return 0;
    return orig_GetHasKilledThisRound ? orig_GetHasKilledThisRound(self) : 0;
}

// ──────────────────────────────────────────────────────────────
//  Hook kurulum
// ──────────────────────────────────────────────────────────────
void PlayerHooks::init() {
    if (!IL2CPP.init()) {
        LOGE("IL2CPP API init başarısız!");
        return;
    }
    LOGI("IL2CPP API hazır, hook'lar kuruluyor...");

    struct Entry {
        const char* ns;
        const char* cls;
        const char* method;
        int         argc;
        void*       hookFn;
        void**      origFn;
    };

    Entry entries[] = {
        // Yaşam durumu
        {"", "PlayerData", "GetIsAlive",            0, (void*)hook_GetIsAlive,
                                                        (void**)&orig_GetIsAlive},
        // Görünürlük
        {"", "PlayerData", "GetIsVisible",           0, (void*)hook_GetIsVisible,
                                                        (void**)&orig_GetIsVisible},
        // Kill cooldown
        {"", "PlayerData", "GetKillCooldown",        0, (void*)hook_GetKillCooldown,
                                                        (void**)&orig_GetKillCooldown},
        // Kill durumu (cooldown bypass için)
        {"", "PlayerData", "GetHasKilledThisRound",  0, (void*)hook_GetHasKilledThisRound,
                                                        (void**)&orig_GetHasKilledThisRound},
        // Hareket hızı
        {"", "PlayerMovement", "GetMoveSpeed",       0, (void*)hook_GetMoveSpeed,
                                                        (void**)&orig_GetMoveSpeed},
        // Görüş menzili
        {"", "PlayerData", "GetVisionRange",         0, (void*)hook_GetVisionRange,
                                                        (void**)&orig_GetVisionRange},
        // Rol
        {"", "PlayerData", "GetRole",                0, (void*)hook_GetRole,
                                                        (void**)&orig_GetRole},
    };

    int hooked = 0;
    for (auto& e : entries) {
        Il2CppClass* cls = FindClass(e.ns, e.cls);
        if (!cls) {
            LOGE("Class bulunamadı: %s", e.cls);
            continue;
        }
        auto method = IL2CPP.class_get_method_from_name(cls, e.method, e.argc);
        if (!method) {
            LOGE("Method bulunamadı: %s::%s", e.cls, e.method);
            continue;
        }
        // Method pointer IL2CPP'de: ilk alan = function pointer
        void** methodPtr = (void**)method;
        void*  fnPtr     = methodPtr[0];

        // Orijinali kaydet
        if (e.origFn) *e.origFn = fnPtr;

        if (hookFunction(fnPtr, e.hookFn)) {
            LOGI("Hook OK: %s::%s", e.cls, e.method);
            hooked++;
        } else {
            LOGE("Hook FAILED: %s::%s", e.cls, e.method);
        }
    }

    LOGI("Hook sonucu: %d/%zu başarılı", hooked, sizeof(entries)/sizeof(entries[0]));
}

void PlayerHooks::teardown() {
    // Orijinal byte'lar trampolin olmadan restore edilemiyor.
    // Şimdilik sadece bayrakları sıfırla — yeterli.
    ModMenu::espEnabled     = false;
    ModMenu::showRoles      = false;
    ModMenu::seeDeadPlayers = false;
    ModMenu::noKillCooldown = false;
    ModMenu::alwaysImpostor = false;
    ModMenu::infiniteVision = false;
    ModMenu::autoKill       = false;
    ModMenu::speedHackX5    = false;
    ModMenu::speedHackX10   = false;
    ModMenu::speedMultiplier= 1.0f;
    LOGI("Hook'lar devre dışı bırakıldı (bayraklar sıfırlandı)");
}
