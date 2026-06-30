package com.flex.ggdmod;

import android.app.Activity;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.*;
import android.view.animation.*;
import android.widget.*;

/**
 * FlexClient GGD v1.2 — Activity DecorView Overlay
 *
 * SYSTEM_ALERT_WINDOW izni GEREKMEZ.
 * Overlay, oyunun kendi Activity penceresine eklenir.
 *
 * Katmanlar:
 *  ① HUD watermark  → sol üst "Flex Client v1.2"
 *  ② Yuvarlak [F]   → sürüklenebilir, tap = menü aç/kapat
 *  ③ Menü paneli   → özellik toggle'ları (fade animasyonlu)
 */
public class ModMenuOverlay {

    // ── JNI ──────────────────────────────────────────────────
    static { System.loadLibrary("ggdmod"); }
    public static native void nativeInit(Activity activity);
    public static native void nativeToggleFeature(String featureName, boolean enabled);

    // ── Özellikler ────────────────────────────────────────────
    private static final String[][] FEATURES = {
        {"ESP",               "Tüm oyuncuları haritada göster",         "esp"},
        {"Rolleri Göster",    "Her oyuncunun rolünü görüntüle",          "showRoles"},
        {"Ölüleri Gör",       "Öldürülen oyuncular görünür kalır",       "seeDeadPlayers"},
        {"Kill Cooldown Yok", "Öldürdükten hemen sonra tekrar öldür",    "noKillCooldown"},
        {"Her Zaman Katil",   "Her zaman katil rolü al [Deneysel]",      "alwaysImpostor"},
        {"Sonsuz Görüş",      "Görüş menzilini kaldır",                  "infiniteVision"},
        {"Hız Hack ×5",       "Hareket hızını 5 katına çıkar",           "speedHackX5"},
        {"Hız Hack ×10",      "Hareket hızını 10 katına çıkar",          "speedHackX10"},
        {"Oto Kill",          "Yakındaki herkesi otomatik öldür",        "autoKill"},
    };

    // ── Renkler ───────────────────────────────────────────────
    private static final int COL_ACCENT  = 0xFFFF2D55;
    private static final int COL_ACCENT2 = 0xFF7B2FFF;
    private static final int COL_GREEN   = 0xFF00E676;
    private static final int COL_WHITE   = 0xFFFFFFFF;
    private static final int COL_GRAY    = 0xFFAAAAAA;
    private static final int COL_DIVIDER = 0x22FFFFFF;

    // ── State ─────────────────────────────────────────────────
    private static FrameLayout root;       // Activity decorView'e eklenen kök
    private static View        hudView;
    private static View        circleView;
    private static View        menuPanel;
    private static boolean     menuOpen    = false;
    private static boolean     initialized = false;

    // ─────────────────────────────────────────────────────────
    //  Giriş — C++ hackThread çağırır, Activity thread'inde çalışır
    // ─────────────────────────────────────────────────────────
    public static void show(Activity activity) {
        if (initialized) return;
        activity.runOnUiThread(() -> {
            try {
                nativeInit(activity);

                // Activity'nin kök ViewGroup'unu al — izin gerekmez
                ViewGroup decor = (ViewGroup) activity.getWindow().getDecorView();

                root = new FrameLayout(activity);
                root.setLayoutParams(new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT));
                root.setClickable(false);

                decor.addView(root);
                initialized = true;

                addHudLabel(activity);
                addCircleButton(activity);

                android.util.Log.i("GGD-GUI", "Flex Client v1.2 overlay başlatıldı");
            } catch (Exception e) {
                android.util.Log.e("GGD-GUI", "Overlay hatası: " + e.getMessage());
            }
        });
    }

    public static void destroy() {
        if (root != null) {
            try {
                ViewGroup parent = (ViewGroup) root.getParent();
                if (parent != null) parent.removeView(root);
            } catch (Exception ignored) {}
        }
        root        = null;
        hudView     = null;
        circleView  = null;
        menuPanel   = null;
        menuOpen    = false;
        initialized = false;
    }

    // ─────────────────────────────────────────────────────────
    //  ① HUD Watermark
    // ─────────────────────────────────────────────────────────
    private static void addHudLabel(Activity activity) {
        TextView tv = new TextView(activity);
        tv.setText("⚡ Flex Client v1.2");
        tv.setTextColor(COL_WHITE);
        tv.setTextSize(11f);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setPadding(20, 8, 20, 8);

        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xCCFF2D55, 0xCC7B2FFF});
        bg.setCornerRadius(14f);
        setBackground(tv, bg);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.TOP | Gravity.START;
        lp.setMargins(16, 48, 0, 0);

        hudView = tv;
        root.addView(tv, lp);
    }

    // ─────────────────────────────────────────────────────────
    //  ② Yuvarlak [F] Butonu
    // ─────────────────────────────────────────────────────────
    private static void addCircleButton(Activity activity) {
        TextView btn = new TextView(activity) {
            @Override protected void onDraw(Canvas c) {
                float cx = getWidth() / 2f, cy = getHeight() / 2f, r = cx - 6f;
                Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);
                glow.setColor(COL_ACCENT);
                glow.setMaskFilter(new BlurMaskFilter(20, BlurMaskFilter.Blur.OUTER));
                c.drawCircle(cx, cy, r - 2, glow);
                Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
                fill.setShader(new RadialGradient(cx, cy, r,
                        COL_ACCENT2, COL_ACCENT, Shader.TileMode.CLAMP));
                c.drawCircle(cx, cy, r, fill);
                super.onDraw(c);
            }
        };
        btn.setText("F");
        btn.setTextColor(COL_WHITE);
        btn.setTextSize(22f);
        btn.setTypeface(Typeface.DEFAULT_BOLD);
        btn.setGravity(Gravity.CENTER);
        btn.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        final FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(110, 110);
        lp.gravity = Gravity.TOP | Gravity.END;
        lp.setMargins(0, 200, 20, 0);

        final int[] lx = {0}, ly = {0};
        final boolean[] dragged = {false};

        btn.setOnTouchListener((v, e) -> {
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
                        lp.setMargins(
                            Math.max(0, lp.leftMargin + dx),
                            Math.max(0, lp.topMargin  + dy),
                            lp.rightMargin, lp.bottomMargin);
                        lx[0] = (int) e.getRawX();
                        ly[0] = (int) e.getRawY();
                        v.setLayoutParams(lp);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (!dragged[0]) toggleMenu(activity);
                    break;
            }
            return true;
        });

        circleView = btn;
        root.addView(btn, lp);
    }

    // ─────────────────────────────────────────────────────────
    //  ③ Menü Paneli
    // ─────────────────────────────────────────────────────────
    private static void toggleMenu(Activity activity) {
        if (menuOpen) {
            closeMenu();
            return;
        }
        menuOpen = true;

        LinearLayout panel = new LinearLayout(activity);
        panel.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable panelBg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{0xF2090E1A, 0xF2120820});
        panelBg.setCornerRadius(20f);
        panelBg.setStroke(2, COL_ACCENT2);
        setBackground(panel, panelBg);

        // Başlık
        LinearLayout header = new LinearLayout(activity);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setPadding(24, 18, 24, 18);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = new TextView(activity);
        title.setText("⚡ FLEX CLIENT v1.2");
        title.setTextColor(COL_WHITE);
        title.setTextSize(13f);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams tlp =
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        title.setLayoutParams(tlp);

        TextView sub = new TextView(activity);
        sub.setText("GGD v4.09");
        sub.setTextColor(COL_GRAY);
        sub.setTextSize(10f);

        header.addView(title);
        header.addView(sub);
        panel.addView(header);
        panel.addView(divider(activity));

        // Özellik satırları
        for (int i = 0; i < FEATURES.length; i++) {
            panel.addView(featureRow(activity, FEATURES[i]));
            if (i < FEATURES.length - 1) panel.addView(divider(activity));
        }

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(420,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.TOP | Gravity.START;
        lp.setMargins(40, 140, 0, 0);

        menuPanel = panel;
        root.addView(panel, lp);

        AlphaAnimation anim = new AlphaAnimation(0f, 1f);
        anim.setDuration(200);
        anim.setFillAfter(true);
        panel.startAnimation(anim);
    }

    private static void closeMenu() {
        if (menuPanel == null) return;
        AlphaAnimation anim = new AlphaAnimation(1f, 0f);
        anim.setDuration(150);
        anim.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation a) {}
            public void onAnimationRepeat(Animation a) {}
            public void onAnimationEnd(Animation a) {
                root.removeView(menuPanel);
                menuPanel = null;
                menuOpen = false;
            }
        });
        menuPanel.startAnimation(anim);
    }

    private static View featureRow(Activity activity, String[] feat) {
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(24, 14, 24, 14);
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

    private static View divider(Activity activity) {
        View v = new View(activity);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        lp.setMargins(16, 0, 16, 0);
        v.setLayoutParams(lp);
        v.setBackgroundColor(COL_DIVIDER);
        return v;
    }

    @SuppressWarnings("deprecation")
    private static void setBackground(View v, GradientDrawable d) {
        if (Build.VERSION.SDK_INT >= 16) v.setBackground(d);
        else v.setBackgroundDrawable(d);
    }
}
