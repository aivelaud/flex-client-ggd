package com.unity3d.player;

/*
 * Bu dosya UnityPlayerActivity.java'nın patch'li halidir.
 * Orijinal smali'ye sadece 3 blok ekleniyor:
 *   1) import'lar (smali'de gerek yok, referans için burada)
 *   2) onCreate içinde izin kontrolü
 *   3) onResume içinde servis başlatma
 *
 * Smali düzenlemesi için bkz. ADIMLAR.md → Adım 4.
 */

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

public class UnityPlayerActivity extends Activity
        implements IUnityPlayerLifecycleEvents,
                   IUnityPermissionRequestSupport,
                   IUnityPlayerSupport {

    protected UnityPlayerForActivityOrService mUnityPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // libggdmod.so'yu yükle (zaten mevcut)
        System.loadLibrary("ggdmod");

        requestWindowFeature(1);
        super.onCreate(savedInstanceState);

        String cmd = getIntent().getStringExtra("unity");
        cmd = updateUnityCommandLineArguments(cmd);
        getIntent().putExtra("unity", cmd);

        mUnityPlayer = new UnityPlayerForActivityOrService(this, this);
        setContentView(mUnityPlayer.getFrameLayout());
        mUnityPlayer.getFrameLayout().requestFocus();

        // ─────────── YENİ EKLENEN KISIM ───────────
        // SYSTEM_ALERT_WINDOW iznini kontrol et
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                // İzin yoksa ayarlar ekranını aç
                Intent permIntent = new Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName())
                );
                startActivity(permIntent);
            }
        }
        // ──────────────────────────────────────────
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUnityPlayer.onResume();

        // ─────────── YENİ EKLENEN KISIM ───────────
        // İzin varsa overlay servisini başlat
        boolean canDraw = android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M
                || Settings.canDrawOverlays(this);
        if (canDraw) {
            startService(new Intent(this, HackOverlayService.class));
        }
        // ──────────────────────────────────────────
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUnityPlayer.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mUnityPlayer.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mUnityPlayer.onStart();
    }

    @Override
    protected void onDestroy() {
        mUnityPlayer.destroy();
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        mUnityPlayer.newIntent(intent);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mUnityPlayer.onTrimMemory(UnityPlayerForActivityOrService.MemoryUsage.Critical);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        switch (level) {
            case 5:  mUnityPlayer.onTrimMemory(UnityPlayerForActivityOrService.MemoryUsage.Medium);   break;
            case 10: mUnityPlayer.onTrimMemory(UnityPlayerForActivityOrService.MemoryUsage.High);     break;
            case 15: mUnityPlayer.onTrimMemory(UnityPlayerForActivityOrService.MemoryUsage.Critical); break;
        }
    }

    @Override
    public boolean dispatchKeyEvent(android.view.KeyEvent event) {
        if (event.getAction() == 2) return mUnityPlayer.injectEvent(event);
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyUp(int keyCode, android.view.KeyEvent event) {
        return mUnityPlayer.getFrameLayout().onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        return mUnityPlayer.getFrameLayout().onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(android.view.MotionEvent event) {
        return mUnityPlayer.getFrameLayout().onTouchEvent(event);
    }

    @Override
    public boolean onGenericMotionEvent(android.view.MotionEvent event) {
        return mUnityPlayer.getFrameLayout().onGenericMotionEvent(event);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        mUnityPlayer.windowFocusChanged(hasFocus);
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mUnityPlayer.configurationChanged(newConfig);
    }

    @Override
    public void onRequestPermissionsResult(int reqCode, String[] perms, int[] results) {
        super.onRequestPermissionsResult(reqCode, perms, results);
        mUnityPlayer.permissionResponse(this, reqCode, perms, results);
    }

    @Override
    public void requestPermissions(PermissionRequest req) {
        mUnityPlayer.addPermissionRequest(req);
    }

    @Override
    public UnityPlayerForActivityOrService getUnityPlayerConnection() {
        return mUnityPlayer;
    }

    @Override
    public void onUnityPlayerQuitted() {}

    @Override
    public void onUnityPlayerUnloaded() {
        moveTaskToBack(true);
    }

    protected String updateUnityCommandLineArguments(String cmdLine) {
        return cmdLine;
    }
}
