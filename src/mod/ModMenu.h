#pragma once
#include <string>
#include <vector>

// ──────────────────────────────────────────────────────────────
//  GGD Mod Menü — Bayraklar (v1.2)
//  Özellikler varsayılan AÇIK — GUI olmadan test için
// ──────────────────────────────────────────────────────────────

namespace ModMenu {

    // Varsayılan AÇIK — anında etkili
    inline bool espEnabled       = true;    // ESP
    inline bool showRoles        = true;    // Rolleri göster
    inline bool seeDeadPlayers   = true;    // Ölüleri gör
    inline bool noKillCooldown   = true;    // Kill cooldown = 0
    inline bool alwaysImpostor   = false;   // Deneysel — kapalı
    inline bool infiniteVision   = true;    // Sonsuz görüş
    inline bool autoKill         = false;   // Kapalı
    inline bool speedHackX5      = true;    // Hız ×5 AÇIK
    inline bool speedHackX10     = false;
    inline float speedMultiplier = 5.0f;    // ×5

    inline bool menuVisible = false;

    void init(void* jni_env, void* activity);
    void toggle();
    void render();
    void shutdown();
    void showToast(void* env, const char* msg);
}
