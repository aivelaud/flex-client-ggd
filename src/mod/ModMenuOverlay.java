package com.flex.ggdmod;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.*;
import android.view.animation.*;
import android.widget.*;

/**
 * FlexClient GGD v1.2 — Android Floating Overlay GUI
 *
 * Katmanlar:
 *  ① HUD watermark  → sol üst "Flex Client v1.2"  (dokunulamaz)
 *  ② Yuvarlak buton → sürüklenebilir [F] ikonu, tap = menü aç/kapat
 *  ③ Menü paneli   → başlık + özellik toggle'lar (fade animasyonlu)
 *
 * İzin:
 *  Android 6+ cihazlarda SYSTEM_ALERT_WINDOW gerekir.
 *  İzin yoksa kullanıcıya Settings sayfası açılır.
 *
 * Yaşam döngüsü:
 *  show()    — tek seferlik başlatır, tekrar çağrılırsa sessizce döner
 *  destroy() — tüm overlay view'larını temizler, state sıfırlar
 */
public class ModMenuOverlay {

    // ── JNI ──────────────────────────────────────────────────
    static { System.loadLibrary("ggdmod"); }
    public static native void nativeInit(Activity activity);
    public static native void nativeToggleFeature(String featureName, boolean enabled);

    // ── Özellik listesi: {ad, açıklama, jni key} ─────────────
    private static final String[][] FEATURES = {
        {"ESP",               "Tüm oyuncuları haritada göster",           "esp"},
        {"Rolleri Göster",    "Her oyuncunun rolünü görüntüle",            "showRoles"},
        {"Ölüleri Gör",       "Öldürülen oyuncular görünür kalır",         "seeDeadPlayers"},
        {"Kill Cooldown Yok", "Öldürdükten hemen sonra tekrar öldür",      "noKillCooldown"},
        {"Her Zaman Katil",   "Her zaman katil rolü al [Deneysel]",        "alwaysImpostor"},
        {"Sonsuz Görüş",      "Görüş menzilini kaldır, her yeri gör",      "infiniteVision"},
        {"Hız Hack ×5",       "Hareket hızını 5 katına çıkar",             "speedHackX5"},
        {"Hız Hack ×10",      "Hareket hızını 10 katına çıkar",            "speedHackX10"},
        {"Oto Kill",          "Yakındaki herkesi otomatik öldür",          "autoKill"},
    };

    // ── Renkler ───────────────────────────────────────────────
    private static final int COL_ACCENT  = 0xFFFF2D55;
    private static final int COL_ACCENT2 = 0xFF7B2FFF;
    private static final int COL_GREEN   = 0xFF00E676;
    private static final int COL_WHITE   = 0xFFFFFFFF;
    private static final int COL_GRAY    = 0xFFAAAAAA;
    private static final int COL_DIVIDER = 0x22FFFFFF;
    private static final int COL_BG      = 0xF2090E1A;

    // ── State ─────────────────────────────────────────────────
    private static WindowManager wm;
    private static View          hudView;
    private static View          circleView;
    private static View          menuPanel;
    private static boolean       menuOpen    = false;
    private static boolean       initialized = false;

    // ─────────────────────────────────────────────────────────
    //  Giriş noktası — C++ hackThread çağırır (idempotent)
    // ─────────────────────────────────────────────────────────
    public static void show(Activity activity) {
        if (initialized) return;
        activity.runOnUiThread(() -> {
            // Android 6+ izin kontrolü
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(activity)) {
                    requestOverlayPermission(activity);
                    return;
                }
            }
            startOverlay(activity);
        });
    }

    // İzin yoksa Settings sayfasına yönlendir + Toast
    private static void requestOverlayPermission(Activity activity) {
        try {
            Toast.makeText(activity,
                "Flex Client: 'Diğer uygulamaların üzerinde göster' iznini ver!",
                Toast.LENGTH_LONG).show();

            Intent intent = new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + activity.getPackageName())
            );
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } catch (Exception e) {
            android.util.Log.e("GGD-GUI", "İzin ekranı açılamadı: " + e.getMessage());
        }
        // İzin verildikten sonra oyun yeniden başlatılınca overlay açılır
    }

    private static void startOverlay(Activity activity) {
        try {
            nativeInit(activity);
            wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
            initialized = true;
            safeAddView(addHudLabel(activity),     hudLabelParams());
            safeAddView(addCircleButton(activity), circleParams());
        } catch (Exception e) {
            android.util.Log.e("GGD-GUI", "Overlay başlatma hatası: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────
    //  Tam temizlik
    // ─────────────────────────────────────────────────────────
    public static void destroy() {
        removeView(menuPanel);
        removeView(circleView);
        removeView(hudView);
        menuPanel   = null;
        circleView  = null;
        hudView     = null;
        menuOpen    = false;
        initialized = false;
    }

    // ─────────────────────────────────────────────────────────
    //  ① HUD Watermark — sol üst
    // ─────────────────────────────────────────────────────────
    private static View addHudLabel(Activity activity) {
        TextView tv = new TextView(activity) {
            @Override protected void onDraw(Canvas c) {
                // Arka plan gradient çizgisi
                Paint bg = new Paint(Paint.ANTI_ALIAS_FLAG);
                bg.setShader(new LinearGradient(0, 0, getWidth(), 0,
                    COL_ACCENT, COL_ACCENT2, Shader.TileMode.CLAMP));
                bg.setAlpha(180);
                c.drawRoundRect(0, 0, getWidth(), getHeight(), 12, 12, bg);
                super.onDraw(c);
            }
        };
        tv.setText("⚡ Flex Client v1.2");
        tv.setTextColor(COL_WHITE);
        tv.setTextSize(11f);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setPadding(16, 6, 16, 6);
        tv.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        hudView = tv;
        return tv;
    }

    private static WindowManager.LayoutParams hudLabelParams() {
        WindowManager.LayoutParams p = baseParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
        p.gravity = Gravity.TOP | Gravity.START;
        p.x = 12; p.y = 40;
        p.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        return p;
    }

    // ─────────────────────────────────────────────────────────
    //  ② Yuvarlak [F] butonu — sürüklenebilir
    // ─────────────────────────────────────────────────────────
    private static View addCircleButton(Activity activity) {
        TextView circle = new TextView(activity) {
            @Override protected void onDraw(Canvas c) {
                float cx = getWidth() / 2f, cy = getHeight() / 2f, r = cx - 4;
                // Dış glow
                Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);
                glow.setColor(COL_ACCENT);
                glow.setMaskFilter(new BlurMaskFilter(18, BlurMaskFilter.Blur.OUTER));
                c.drawCircle(cx, cy, r - 2, glow);
                // Gradient dolgu
                Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
                fill.setShader(new RadialGradient(cx, cy, r,
                    COL_ACCENT2, COL_ACCENT, Shader.TileMode.CLAMP));
                c.drawCircle(cx, cy, r, fill);
                super.onDraw(c);
            }
        };
        circle.setText("F");
        circle.setTextColor(COL_WHITE);
        circle.setTextSize(22f);
        circle.setTypeface(Typeface.DEFAULT_BOLD);
        circle.setGravity(Gravity.CENTER);
        circle.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        final WindowManager.LayoutParams p = circleParams();
        final int[]     lx      = {0};
        final int[]     ly      = {0};
        final boolean[] dragged = {false};

        circle.setOnTouchListener((v, e) -> {
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lx[0] = (int) e.getRawX();
                    ly[0] = (int) e.getRawY();
                    dragged[0] = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    int dx = (int) e.getRawX() - lx[0];
                    int dy = (int) e.getRawY() - ly[0];
                    if (Math.abs(dx) > 8 || Math.abs(dy) > 8) {
                        dragged[0] = true;
                        p.x = clamp(p.x - dx, 0, screenWidth(activity)  - 120);
                        p.y = clamp(p.y + dy, 0, screenHeight(activity) - 120);
                        lx[0] = (int) e.getRawX();
                        ly[0] = (int) e.getRawY();
                        safeUpdateLayout(v, p);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (!dragged[0]) toggleMenu(activity);
                    break;
            }
            return true;
        });
        circleView = circle;
        return circle;
    }

    private static WindowManager.LayoutParams circleParams() {
        WindowManager.LayoutParams p = baseParams(116, 116);
        p.gravity = Gravity.TOP | Gravity.END;
        p.x = 20; p.y = 200;
        return p;
    }

    // ─────────────────────────────────────────────────────────
    //  ③ Mod Menü Paneli
    // ─────────────────────────────────────────────────────────
    private static void toggleMenu(Activity activity) {
        if (menuOpen) { closeMenu(); return; }
        menuOpen = true;

        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        applyBg(root, panelBg());

        root.addView(buildHeader(activity));
        root.addView(divider(activity));

        for (int i = 0; i < FEATURES.length; i++) {
            root.addView(featureRow(activity, FEATURES[i]));
            if (i < FEATURES.length - 1) root.addView(divider(activity));
        }

        // Panel header sürükleme
        final WindowManager.LayoutParams pp = panelParams();
        final int[] lx = {0}, ly = {0};
        final boolean[] dragged = {false};
        root.getChildAt(0).setOnTouchListener((v, e) -> {
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lx[0] = (int) e.getRawX(); ly[0] = (int) e.getRawY(); dragged[0] = false; break;
                case MotionEvent.ACTION_MOVE:
                    int dx = (int) e.getRawX() - lx[0];
                    int dy = (int) e.getRawY() - ly[0];
                    if (Math.abs(dx) > 5 || Math.abs(dy) > 5) {
                        dragged[0] = true;
                        pp.x = clamp(pp.x + dx, 0, screenWidth(activity)  - 420);
                        pp.y = clamp(pp.y + dy, 0, screenHeight(activity) - 100);
                        lx[0] = (int) e.getRawX(); ly[0] = (int) e.getRawY();
                        safeUpdateLayout(menuPanel, pp);
                    }
                    break;
            }
            return dragged[0];
        });

        menuPanel = root;
        safeAddView(root, pp);
        root.startAnimation(alphaAnim(0f, 1f, 200));
    }

    private static void closeMenu() {
        if (menuPanel == null) return;
        AlphaAnimation anim = alphaAnim(1f, 0f, 150);
        anim.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation a) {}
            public void onAnimationRepeat(Animation a) {}
            public void onAnimationEnd(Animation a) {
                removeView(menuPanel);
                menuPanel = null;
                menuOpen = false;
            }
        });
        menuPanel.startAnimation(anim);
    }

    private static LinearLayout buildHeader(Activity activity) {
        LinearLayout h = new LinearLayout(activity);
        h.setOrientation(LinearLayout.HORIZONTAL);
        h.setPadding(24, 20, 24, 20);
        h.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = new TextView(activity);
        title.setText("⚡ FLEX CLIENT v1.2");
        title.setTextColor(COL_WHITE);
        title.setTextSize(14f);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        title.setLayoutParams(lp);

        TextView sub = new TextView(activity);
        sub.setText("GGD v4.09");
        sub.setTextColor(COL_GRAY);
        sub.setTextSize(10f);

        h.addView(title);
        h.addView(sub);
        return h;
    }

    private static View featureRow(Activity activity, String[] feat) {
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(24, 16, 24, 16);
        row.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout info = new LinearLayout(activity);
        info.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        info.setLayoutParams(lp);

        TextView name = new TextView(activity);
        name.setText(feat[0]);
        name.setTextColor(COL_WHITE);
        name.setTextSize(13f);
        name.setTypeface(Typeface.DEFAULT_BOLD);

        TextView desc = new TextView(activity);
        desc.setText(feat[1]);
        desc.setTextColor(COL_GRAY);
        desc.setTextSize(10f);

        info.addView(name);
        info.addView(desc);

        Switch sw = new Switch(activity);
        sw.setChecked(false);
        final String key = feat[2];

        // Hız hack'leri birbirini dışlar
        sw.setOnCheckedChangeListener((btn, checked) -> {
            if (checked && (key.equals("speedHackX5") || key.equals("speedHackX10"))) {
                String other = key.equals("speedHackX5") ? "speedHackX10" : "speedHackX5";
                nativeToggleFeature(other, false);
            }
            nativeToggleFeature(key, checked);
            name.setTextColor(checked ? COL_GREEN : COL_WHITE);
        });

        row.addView(info);
        row.addView(sw);
        return row;
    }

    private static WindowManager.LayoutParams panelParams() {
        WindowManager.LayoutParams p = baseParams(420,
                WindowManager.LayoutParams.WRAP_CONTENT);
        p.gravity = Gravity.TOP | Gravity.START;
        p.x = 40; p.y = 120;
        return p;
    }

    // ─────────────────────────────────────────────────────────
    //  Yardımcılar
    // ─────────────────────────────────────────────────────────
    private static GradientDrawable panelBg() {
        GradientDrawable d = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{0xF2090E1A, 0xF2120820});
        d.setCornerRadius(20f);
        d.setStroke(2, COL_ACCENT2);
        return d;
    }

    private static View divider(Activity activity) {
        View v = new View(activity);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        lp.setMargins(16, 0, 16, 0);
        v.setLayoutParams(lp);
        v.setBackgroundColor(COL_DIVIDER);
        return v;
    }

    private static void applyBg(View v, GradientDrawable d) {
        if (Build.VERSION.SDK_INT >= 16) v.setBackground(d);
        else v.setBackgroundDrawable(d);
    }

    @SuppressWarnings("deprecation")
    private static WindowManager.LayoutParams baseParams(int w, int h) {
        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;
        return new WindowManager.LayoutParams(w, h, type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
    }

    private static void safeAddView(View v, WindowManager.LayoutParams p) {
        try { wm.addView(v, p); } catch (Exception e) {
            android.util.Log.e("GGD-GUI", "addView hata: " + e.getMessage());
        }
    }

    private static void safeUpdateLayout(View v, WindowManager.LayoutParams p) {
        try { if (v != null && v.getParent() != null) wm.updateViewLayout(v, p); }
        catch (Exception ignored) {}
    }

    private static void removeView(View v) {
        try { if (v != null && v.getParent() != null) wm.removeViewImmediate(v); }
        catch (Exception ignored) {}
    }

    private static AlphaAnimation alphaAnim(float from, float to, int ms) {
        AlphaAnimation a = new AlphaAnimation(from, to);
        a.setDuration(ms);
        if (to == 1f) a.setFillAfter(true);
        return a;
    }

    private static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    @SuppressWarnings("deprecation")
    private static int screenWidth(Activity a) {
        if (Build.VERSION.SDK_INT >= 30) {
            return a.getWindowManager().getCurrentWindowMetrics().getBounds().width();
        }
        android.util.DisplayMetrics dm = new android.util.DisplayMetrics();
        a.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    @SuppressWarnings("deprecation")
    private static int screenHeight(Activity a) {
        if (Build.VERSION.SDK_INT >= 30) {
            return a.getWindowManager().getCurrentWindowMetrics().getBounds().height();
        }
        android.util.DisplayMetrics dm = new android.util.DisplayMetrics();
        a.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }
}
