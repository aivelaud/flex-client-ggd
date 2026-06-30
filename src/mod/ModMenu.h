#pragma once
#include <string>
#include <vector>
#include <functional>

// ──────────────────────────────────────────────────────────────
//  GGD Mod Menü — Bayraklar & API  (v1.2)
// ──────────────────────────────────────────────────────────────

namespace ModMenu {

    // ── Görünürlük / Bilgi ────────────────────────────────────
    inline bool espEnabled       = false;   // Tüm oyuncuları haritada göster
    inline bool showRoles        = false;   // Rolleri isimlerin üzerinde göster
    inline bool seeDeadPlayers   = false;   // Ölü oyuncuları görünür yap
    inline bool infiniteVision   = false;   // Görüş menzili kaldır

    // ── Kill / Rol ────────────────────────────────────────────
    inline bool noKillCooldown   = false;   // Kill cooldown = 0
    inline bool alwaysImpostor   = false;   // Her round katil ol [Deneysel]
    inline bool autoKill         = false;   // Yakındaki oyuncuları otomatik öldür

    // ── Hareket ──────────────────────────────────────────────
    inline bool speedHackX5      = false;   // Hız ×5
    inline bool speedHackX10     = false;   // Hız ×10
    inline float speedMultiplier = 1.0f;    // Gerçek çarpan (hook tarafından okunur)

    // ── Menü ─────────────────────────────────────────────────
    inline bool menuVisible = false;

    // ── API ───────────────────────────────────────────────────
    void init(void* jni_env, void* activity);
    void toggle();
    void render();
    void shutdown();
    void showToast(void* env, const char* msg);
}
