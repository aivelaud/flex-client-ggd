package com.unity3d.player;

/**
 * libggdmod.so içindeki native C fonksiyonlarına Java köprüsü.
 * Fonksiyon isimleri C tarafında:
 *   Java_com_unity3d_player_NativeGGD_setEsp   vb. olmalı.
 */
public class NativeGGD {
    static {
        System.loadLibrary("ggdmod");
    }

    public static native void setEsp(boolean enabled);
    public static native void setSpeed(boolean enabled);
    public static native void setCooldown(boolean enabled);
    public static native void setVision(boolean enabled);
    public static native void setAutoKill(boolean enabled);
    public static native void setAlwaysKill(boolean enabled);
}
