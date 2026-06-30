.class public Lcom/unity3d/player/NativeGGD;
.super Ljava/lang/Object;
.source "NativeGGD.java"

# libggdmod.so'yu yükle (zaten UnityPlayerActivity'de yükleniyor
# ama güvenlik için burada da tekrar yüklüyoruz)
.method static constructor <clinit>()V
    .registers 1
    const-string v0, "ggdmod"
    invoke-static {v0}, Ljava/lang/System;->loadLibrary(Ljava/lang/String;)V
    return-void
.end method

.method public constructor <init>()V
    .registers 1
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method

# libggdmod.so içindeki native fonksiyonlar
# C tarafında: Java_com_unity3d_player_NativeGGD_setEsp(JNIEnv*, jclass, jboolean)
.method public static native setEsp(Z)V
.end method

.method public static native setSpeed(Z)V
.end method

.method public static native setCooldown(Z)V
.end method

.method public static native setVision(Z)V
.end method

.method public static native setAutoKill(Z)V
.end method

.method public static native setAlwaysKill(Z)V
.end method
