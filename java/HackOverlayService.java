package com.unity3d.player;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

/**
 * GGD Hack Overlay Service
 * Oyunun üzerinde yüzen GUI menüsü gösterir.
 * SYSTEM_ALERT_WINDOW izni ile çalışır.
 */
public class HackOverlayService extends Service {

    private WindowManager windowManager;
    private View rootView;
    private LinearLayout menuPanel;
    private boolean menuVisible = false;

    // ──────────── TOGGLE DURUMLARI ────────────
    private boolean espEnabled      = true;
    private boolean speedEnabled    = true;
    private boolean cooldownEnabled = true;
    private boolean visionEnabled   = true;
    private boolean autoKillEnabled = false;
    private boolean alwaysKillEnabled = false;

    // Sürükleme için
    private int   initialX, initialY;
    private float initialTouchX, initialTouchY;
    private WindowManager.LayoutParams params;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        buildOverlay();
    }

    // ──────────── OVERLAY OLUŞTUR ────────────
    private void buildOverlay() {

        // ── Kök layout (FrameLayout: button + menü yan yana değil, üst üste)
        FrameLayout root = new FrameLayout(this);

        // ── Yüzen "GGD" butonu
        Button floatBtn = new Button(this);
        floatBtn.setText("GGD");
        floatBtn.setTextColor(Color.WHITE);
        floatBtn.setTextSize(13f);
        floatBtn.setBackgroundColor(0xFF1565C0); // koyu mavi
        floatBtn.setPadding(16, 8, 16, 8);

        FrameLayout.LayoutParams btnLP = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        btnLP.gravity = Gravity.TOP | Gravity.LEFT;
        root.addView(floatBtn, btnLP);

        // ── Menü paneli (başta gizli)
        menuPanel = buildMenuPanel();
        menuPanel.setVisibility(View.GONE);
        FrameLayout.LayoutParams menuLP = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        menuLP.gravity = Gravity.TOP | Gravity.LEFT;
        menuLP.leftMargin = 10;
        menuLP.topMargin  = 120; // Butunun altına
        root.addView(menuPanel, menuLP);

        // ── Butona tıklama → Menüyü aç/kapat
        floatBtn.setOnClickListener(v -> {
            menuVisible = !menuVisible;
            menuPanel.setVisibility(menuVisible ? View.VISIBLE : View.GONE);
        });

        // ── WindowManager parametreleri
        int overlayType = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                overlayType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 30;
        params.y = 200;

        // ── Sürükleme
        root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return false; // click da çalışsın diye false

                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int)(event.getRawX() - initialTouchX);
                        params.y = initialY + (int)(event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(rootView, params);
                        return true;
                }
                return false;
            }
        });

        windowManager.addView(root, params);
        rootView = root;
    }

    // ──────────── MENÜ İÇERİĞİ ────────────
    private LinearLayout buildMenuPanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setBackgroundColor(0xEE0A0A1A); // koyu lacivert, %93 opak
        panel.setPadding(20, 14, 20, 14);

        // Başlık
        TextView title = new TextView(this);
        title.setText("⚡ GGD MOD MENÜ");
        title.setTextColor(0xFF64B5F6);
        title.setTextSize(15f);
        title.setPadding(0, 0, 0, 10);
        panel.addView(title);

        // Ayraç
        panel.addView(makeDivider());

        // Toggle'lar
        panel.addView(makeToggle("ESP (Duvar Görüşü)",  espEnabled,      enabled -> {
            espEnabled = enabled;
            NativeGGD.setEsp(enabled);
        }));

        panel.addView(makeToggle("Hız ×5",              speedEnabled,    enabled -> {
            speedEnabled = enabled;
            NativeGGD.setSpeed(enabled);
        }));

        panel.addView(makeToggle("Cooldown Yok",        cooldownEnabled, enabled -> {
            cooldownEnabled = enabled;
            NativeGGD.setCooldown(enabled);
        }));

        panel.addView(makeToggle("Sonsuz Görüş",        visionEnabled,   enabled -> {
            visionEnabled = enabled;
            NativeGGD.setVision(enabled);
        }));

        panel.addView(makeToggle("Oto Kill",            autoKillEnabled, enabled -> {
            autoKillEnabled = enabled;
            NativeGGD.setAutoKill(enabled);
        }));

        panel.addView(makeToggle("Her Zaman Katil",     alwaysKillEnabled, enabled -> {
            alwaysKillEnabled = enabled;
            NativeGGD.setAlwaysKill(enabled);
        }));

        // Kapat butonu
        panel.addView(makeDivider());
        Button closeBtn = new Button(this);
        closeBtn.setText("✕ Kapat");
        closeBtn.setTextColor(Color.WHITE);
        closeBtn.setBackgroundColor(0xFF880E4F);
        closeBtn.setTextSize(12f);
        closeBtn.setOnClickListener(v -> {
            menuVisible = false;
            menuPanel.setVisibility(View.GONE);
        });
        panel.addView(closeBtn);

        return panel;
    }

    // ──────────── YARDIMCI: Toggle satırı ────────────
    private View makeToggle(String label, boolean initialState,
                            CompoundButton.OnCheckedChangeListener listener) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 6, 0, 6);

        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(13f);
        LinearLayout.LayoutParams tvLP = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        row.addView(tv, tvLP);

        Switch sw = new Switch(this);
        sw.setChecked(initialState);
        sw.setOnCheckedChangeListener(listener);
        row.addView(sw);

        return row;
    }

    // ──────────── YARDIMCI: Çizgi ────────────
    private View makeDivider() {
        View line = new View(this);
        line.setBackgroundColor(0x55FFFFFF);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        lp.setMargins(0, 6, 0, 6);
        line.setLayoutParams(lp);
        return line;
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (rootView != null) {
            windowManager.removeView(rootView);
            rootView = null;
        }
    }
}
