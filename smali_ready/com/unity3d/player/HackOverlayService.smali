# ============================================================
#  HackOverlayService.smali
#  Goose Goose Duck üzerinde yüzen hack menüsü.
#  SYSTEM_ALERT_WINDOW izni gerektirir.
# ============================================================

.class public Lcom/unity3d/player/HackOverlayService;
.super Landroid/app/Service;
.source "HackOverlayService.java"

# ── Alanlar (Fields)
.field private wm:Landroid/view/WindowManager;
.field private rootView:Landroid/view/View;
.field menuPanel:Landroid/widget/LinearLayout;
.field menuOpen:Z


# ── Yapıcı
.method public constructor <init>()V
    .registers 1
    invoke-direct {p0}, Landroid/app/Service;-><init>()V
    return-void
.end method


# ── onBind — null döndür (bound service değil)
.method public onBind(Landroid/content/Intent;)Landroid/os/IBinder;
    .registers 2
    const/4 v0, 0x0
    return-object v0
.end method


# ── onStartCommand — START_STICKY (servis ölürse yeniden başlat)
.method public onStartCommand(Landroid/content/Intent;II)I
    .registers 5
    const/4 v0, 0x1
    return v0
.end method


# ── onCreate — overlay penceresini oluştur
.method public onCreate()V
    # Yerel değişkenler:
    # v0  = geçici / WindowManager
    # v1  = rootLayout (FrameLayout)
    # v2  = button (Button)
    # v3  = geçici int / string / object
    # v4  = menuPanel (LinearLayout)
    # v5  = listener $1 veya $2
    # v6  = WindowManager.LayoutParams
    # v7  = geçici int
    # v8  = başlık TextView (geçici)
    # p0  = this
    .registers 10

    invoke-super {p0}, Landroid/app/Service;->onCreate()V

    # ── WindowManager al
    const-string v0, "window"
    invoke-virtual {p0, v0}, Landroid/app/Service;->getSystemService(Ljava/lang/String;)Ljava/lang/Object;
    move-result-object v0
    check-cast v0, Landroid/view/WindowManager;
    iput-object v0, p0, Lcom/unity3d/player/HackOverlayService;->wm:Landroid/view/WindowManager;

    # ── Kök FrameLayout
    new-instance v1, Landroid/widget/FrameLayout;
    invoke-direct {v1, p0}, Landroid/widget/FrameLayout;-><init>(Landroid/content/Context;)V

    # ── GGD Butonu
    new-instance v2, Landroid/widget/Button;
    invoke-direct {v2, p0}, Landroid/widget/Button;-><init>(Landroid/content/Context;)V

    const-string v3, "GGD"
    invoke-virtual {v2, v3}, Landroid/widget/Button;->setText(Ljava/lang/CharSequence;)V

    # Arka plan rengi: koyu mavi (#1565C0)
    const v3, 0xFF1565C0
    invoke-virtual {v2, v3}, Landroid/widget/Button;->setBackgroundColor(I)V

    # Yazı rengi: beyaz
    const v3, 0xFFFFFFFF
    invoke-virtual {v2, v3}, Landroid/widget/Button;->setTextColor(I)V

    # Padding: sol=20, üst=8, sağ=20, alt=8
    const/16 v3, 0x14
    const/16 v7, 0x8
    invoke-virtual {v2, v3, v7, v3, v7}, Landroid/widget/Button;->setPadding(IIII)V

    # OnClickListener bağla ($1 sınıfı)
    new-instance v5, Lcom/unity3d/player/HackOverlayService$1;
    invoke-direct {v5, p0}, Lcom/unity3d/player/HackOverlayService$1;-><init>(Lcom/unity3d/player/HackOverlayService;)V
    invoke-virtual {v2, v5}, Landroid/widget/Button;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    # Butonu root layout'a ekle
    invoke-virtual {v1, v2}, Landroid/widget/FrameLayout;->addView(Landroid/view/View;)V


    # ── Menü Paneli (LinearLayout, dikey)
    new-instance v4, Landroid/widget/LinearLayout;
    invoke-direct {v4, p0}, Landroid/widget/LinearLayout;-><init>(Landroid/content/Context;)V

    # Yön: VERTICAL = 1
    const/4 v3, 0x1
    invoke-virtual {v4, v3}, Landroid/widget/LinearLayout;->setOrientation(I)V

    # Arka plan: koyu lacivert, %93 opak (#EE0A0A1E)
    const v3, 0xEE0A0A1E
    invoke-virtual {v4, v3}, Landroid/widget/LinearLayout;->setBackgroundColor(I)V

    # Padding: 20px her yandan
    const/16 v3, 0x14
    invoke-virtual {v4, v3, v3, v3, v3}, Landroid/widget/LinearLayout;->setPadding(IIII)V

    # Başlangıçta gizli: GONE = 8
    const/4 v3, 0x8
    invoke-virtual {v4, v3}, Landroid/widget/LinearLayout;->setVisibility(I)V

    # Field'a kaydet
    iput-object v4, p0, Lcom/unity3d/player/HackOverlayService;->menuPanel:Landroid/widget/LinearLayout;


    # ── Başlık TextView
    new-instance v8, Landroid/widget/TextView;
    invoke-direct {v8, p0}, Landroid/widget/TextView;-><init>(Landroid/content/Context;)V

    const-string v3, "=== GGD MOD MENU v2 ==="
    invoke-virtual {v8, v3}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    # Renk: açık mavi (#64B5F6)
    const v3, 0xFF64B5F6
    invoke-virtual {v8, v3}, Landroid/widget/TextView;->setTextColor(I)V

    # TextSize: SP=2, 15.0f=0x41700000
    const/4 v3, 0x2
    const v7, 0x41700000
    invoke-virtual {v8, v3, v7}, Landroid/widget/TextView;->setTextSize(IF)V

    invoke-virtual {v4, v8}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;)V


    # ── Toggle Listener (tüm switch'ler için tek listener)
    new-instance v5, Lcom/unity3d/player/HackOverlayService$2;
    invoke-direct {v5, p0}, Lcom/unity3d/player/HackOverlayService$2;-><init>(Lcom/unity3d/player/HackOverlayService;)V


    # ── 6 Toggle Satırı Ekle
    # addToggle(container, label, initialState, featureId, listener)

    const-string v3, "ESP (Duvar Gorusu)"
    const/4 v7, 0x1
    const/4 v8, 0x0
    invoke-direct {p0, v4, v3, v7, v8, v5}, Lcom/unity3d/player/HackOverlayService;->addToggle(Landroid/widget/LinearLayout;Ljava/lang/String;ZILandroid/widget/CompoundButton$OnCheckedChangeListener;)V

    const-string v3, "HIZ x5"
    const/4 v7, 0x1
    const/4 v8, 0x1
    invoke-direct {p0, v4, v3, v7, v8, v5}, Lcom/unity3d/player/HackOverlayService;->addToggle(Landroid/widget/LinearLayout;Ljava/lang/String;ZILandroid/widget/CompoundButton$OnCheckedChangeListener;)V

    const-string v3, "Cooldown Yok"
    const/4 v7, 0x1
    const/4 v8, 0x2
    invoke-direct {p0, v4, v3, v7, v8, v5}, Lcom/unity3d/player/HackOverlayService;->addToggle(Landroid/widget/LinearLayout;Ljava/lang/String;ZILandroid/widget/CompoundButton$OnCheckedChangeListener;)V

    const-string v3, "Sonsuz Gorüs"
    const/4 v7, 0x1
    const/4 v8, 0x3
    invoke-direct {p0, v4, v3, v7, v8, v5}, Lcom/unity3d/player/HackOverlayService;->addToggle(Landroid/widget/LinearLayout;Ljava/lang/String;ZILandroid/widget/CompoundButton$OnCheckedChangeListener;)V

    const-string v3, "Oto Kill"
    const/4 v7, 0x0
    const/4 v8, 0x4
    invoke-direct {p0, v4, v3, v7, v8, v5}, Lcom/unity3d/player/HackOverlayService;->addToggle(Landroid/widget/LinearLayout;Ljava/lang/String;ZILandroid/widget/CompoundButton$OnCheckedChangeListener;)V

    const-string v3, "Her Zaman Katil"
    const/4 v7, 0x0
    const/4 v8, 0x5
    invoke-direct {p0, v4, v3, v7, v8, v5}, Lcom/unity3d/player/HackOverlayService;->addToggle(Landroid/widget/LinearLayout;Ljava/lang/String;ZILandroid/widget/CompoundButton$OnCheckedChangeListener;)V


    # ── Kapat Butonu
    new-instance v8, Landroid/widget/Button;
    invoke-direct {v8, p0}, Landroid/widget/Button;-><init>(Landroid/content/Context;)V

    const-string v3, "X  Kapat"
    invoke-virtual {v8, v3}, Landroid/widget/Button;->setText(Ljava/lang/CharSequence;)V

    const v3, 0xFF880E4F
    invoke-virtual {v8, v3}, Landroid/widget/Button;->setBackgroundColor(I)V

    const v3, 0xFFFFFFFF
    invoke-virtual {v8, v3}, Landroid/widget/Button;->setTextColor(I)V

    # Kapat butonuna özel listener
    new-instance v5, Lcom/unity3d/player/HackOverlayService$1;
    invoke-direct {v5, p0}, Lcom/unity3d/player/HackOverlayService$1;-><init>(Lcom/unity3d/player/HackOverlayService;)V
    invoke-virtual {v8, v5}, Landroid/widget/Button;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    invoke-virtual {v4, v8}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;)V


    # ── Menü Panelini Root'a Ekle
    # FrameLayout.LayoutParams(WRAP_CONTENT=-2, WRAP_CONTENT=-2)
    new-instance v3, Landroid/widget/FrameLayout$LayoutParams;
    const/4 v7, -0x2
    invoke-direct {v3, v7, v7}, Landroid/widget/FrameLayout$LayoutParams;-><init>(II)V
    # leftMargin = 10, topMargin = 120 (butonun altına)
    const/16 v7, 0x78
    iput v7, v3, Landroid/widget/FrameLayout$LayoutParams;->topMargin:I
    const/16 v7, 0xa
    iput v7, v3, Landroid/widget/FrameLayout$LayoutParams;->leftMargin:I
    invoke-virtual {v1, v4, v3}, Landroid/widget/FrameLayout;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V


    # ── WindowManager.LayoutParams Oluştur
    new-instance v6, Landroid/view/WindowManager$LayoutParams;
    # width=WRAP_CONTENT=-2, height=WRAP_CONTENT=-2
    # type=TYPE_APPLICATION_OVERLAY=2038
    # flags=FLAG_NOT_FOCUSABLE=8
    # format=PixelFormat.TRANSLUCENT=-2
    const/4 v3, -0x2
    const v7, 0x7F6
    const/4 v8, 0x8
    const/4 v0, -0x2
    invoke-direct {v6, v3, v3, v7, v8, v0}, Landroid/view/WindowManager$LayoutParams;-><init>(IIIII)V

    # gravity = TOP|LEFT = 51 (0x33)
    const/16 v3, 0x33
    iput v3, v6, Landroid/view/WindowManager$LayoutParams;->gravity:I

    # x = 30, y = 200
    const/16 v3, 0x1e
    iput v3, v6, Landroid/view/WindowManager$LayoutParams;->x:I
    const/16 v3, 0xc8
    iput v3, v6, Landroid/view/WindowManager$LayoutParams;->y:I


    # ── Overlay'i Ekran'a Ekle
    iget-object v0, p0, Lcom/unity3d/player/HackOverlayService;->wm:Landroid/view/WindowManager;
    invoke-interface {v0, v1, v6}, Landroid/view/WindowManager;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    # rootView'ı kaydet (onDestroy'da kaldırmak için)
    iput-object v1, p0, Lcom/unity3d/player/HackOverlayService;->rootView:Landroid/view/View;

    return-void
.end method


# ── Yardımcı: Tek toggle satırı ekle
# p0=this, p1=container, p2=label, p3=initialState(Z), p4=featureId(I), p5=listener
.method private addToggle(Landroid/widget/LinearLayout;Ljava/lang/String;ZILandroid/widget/CompoundButton$OnCheckedChangeListener;)V
    # Yerel değişkenler:
    # v0 = row LinearLayout
    # v1 = TextView
    # v2 = Switch
    # v3 = LayoutParams / temp
    # v4 = geçici int/float
    # p0=this, p1=container, p2=label, p3=initialState, p4=featureId, p5=listener
    .registers 11

    # Satır: yatay LinearLayout
    new-instance v0, Landroid/widget/LinearLayout;
    invoke-direct {v0, p0}, Landroid/widget/LinearLayout;-><init>(Landroid/content/Context;)V
    # HORIZONTAL = 0
    const/4 v3, 0x0
    invoke-virtual {v0, v3}, Landroid/widget/LinearLayout;->setOrientation(I)V
    # Padding: 4px
    const/4 v3, 0x4
    invoke-virtual {v0, v3, v3, v3, v3}, Landroid/widget/LinearLayout;->setPadding(IIII)V

    # Label TextView
    new-instance v1, Landroid/widget/TextView;
    invoke-direct {v1, p0}, Landroid/widget/TextView;-><init>(Landroid/content/Context;)V
    invoke-virtual {v1, p2}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V
    # Renk: beyaz
    const v3, 0xFFFFFFFF
    invoke-virtual {v1, v3}, Landroid/widget/TextView;->setTextColor(I)V
    # TextSize: 13sp
    const/4 v3, 0x2
    const v4, 0x41500000
    invoke-virtual {v1, v3, v4}, Landroid/widget/TextView;->setTextSize(IF)V

    # LayoutParams(width=0, height=WRAP_CONTENT, weight=1.0f)
    new-instance v3, Landroid/widget/LinearLayout$LayoutParams;
    const/4 v4, 0x0
    const/4 v5, -0x2
    const v6, 0x3F800000
    invoke-direct {v3, v4, v5, v6}, Landroid/widget/LinearLayout$LayoutParams;-><init>(IIF)V
    invoke-virtual {v0, v1, v3}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    # Switch
    new-instance v2, Landroid/widget/Switch;
    invoke-direct {v2, p0}, Landroid/widget/Switch;-><init>(Landroid/content/Context;)V

    # setChecked(initialState)
    invoke-virtual {v2, p3}, Landroid/widget/Switch;->setChecked(Z)V

    # setTag(Integer.valueOf(featureId))
    invoke-static {p4}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
    move-result-object v3
    invoke-virtual {v2, v3}, Landroid/widget/Switch;->setTag(Ljava/lang/Object;)V

    # setOnCheckedChangeListener(listener)
    invoke-virtual {v2, p5}, Landroid/widget/Switch;->setOnCheckedChangeListener(Landroid/widget/CompoundButton$OnCheckedChangeListener;)V

    # Switch'i satıra ekle
    invoke-virtual {v0, v2}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;)V

    # Satırı container'a ekle
    invoke-virtual {p1, v0}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;)V

    return-void
.end method


# ── onDestroy — overlay'i kaldır
.method public onDestroy()V
    .registers 3
    invoke-super {p0}, Landroid/app/Service;->onDestroy()V

    iget-object v0, p0, Lcom/unity3d/player/HackOverlayService;->rootView:Landroid/view/View;
    if-eqz v0, :skip

    iget-object v1, p0, Lcom/unity3d/player/HackOverlayService;->wm:Landroid/view/WindowManager;
    invoke-interface {v1, v0}, Landroid/view/WindowManager;->removeView(Landroid/view/View;)V

    const/4 v0, 0x0
    iput-object v0, p0, Lcom/unity3d/player/HackOverlayService;->rootView:Landroid/view/View;

    :skip
    return-void
.end method
