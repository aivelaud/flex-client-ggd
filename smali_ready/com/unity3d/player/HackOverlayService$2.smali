# HackOverlayService$2 — Switch OnCheckedChangeListener
# Tüm toggle'lar bu tek listener'ı kullanır.
# Her Switch'in tag'i (Integer) hangi özellik olduğunu söyler:
#   0=ESP, 1=Hız, 2=Cooldown, 3=Görüş, 4=OtoKill, 5=HerZamanKatil

.class Lcom/unity3d/player/HackOverlayService$2;
.super Ljava/lang/Object;
.source "HackOverlayService.java"

.implements Landroid/widget/CompoundButton$OnCheckedChangeListener;

.field final synthetic this$0:Lcom/unity3d/player/HackOverlayService;

.method constructor <init>(Lcom/unity3d/player/HackOverlayService;)V
    .registers 2
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    iput-object p1, p0, Lcom/unity3d/player/HackOverlayService$2;->this$0:Lcom/unity3d/player/HackOverlayService;
    return-void
.end method

.method public onCheckedChanged(Landroid/widget/CompoundButton;Z)V
    # p0=this, p1=CompoundButton, p2=isChecked (boolean)
    .registers 4

    # Tag'i oku (Integer object)
    invoke-virtual {p1}, Landroid/view/View;->getTag()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, Ljava/lang/Integer;

    # Integer.intValue() ile int'e çevir
    invoke-virtual {v0}, Ljava/lang/Integer;->intValue()I
    move-result v0

    # 0-5 arası packed-switch
    packed-switch v0, :pswitch_data

    # Varsayılan: bilinmeyen tag, hiçbir şey yapma
    return-void

    :pswitch_0
    # ESP
    invoke-static {p2}, Lcom/unity3d/player/NativeGGD;->setEsp(Z)V
    return-void

    :pswitch_1
    # Hız ×5
    invoke-static {p2}, Lcom/unity3d/player/NativeGGD;->setSpeed(Z)V
    return-void

    :pswitch_2
    # Cooldown Yok
    invoke-static {p2}, Lcom/unity3d/player/NativeGGD;->setCooldown(Z)V
    return-void

    :pswitch_3
    # Sonsuz Görüş
    invoke-static {p2}, Lcom/unity3d/player/NativeGGD;->setVision(Z)V
    return-void

    :pswitch_4
    # Oto Kill
    invoke-static {p2}, Lcom/unity3d/player/NativeGGD;->setAutoKill(Z)V
    return-void

    :pswitch_5
    # Her Zaman Katil
    invoke-static {p2}, Lcom/unity3d/player/NativeGGD;->setAlwaysKill(Z)V
    return-void

    :pswitch_data
    .packed-switch 0x0, {:pswitch_0, :pswitch_1, :pswitch_2, :pswitch_3, :pswitch_4, :pswitch_5}
.end method
