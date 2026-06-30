# HackOverlayService$1 — Button OnClickListener
# Menü butonuna basıldığında menü panelini aç/kapat

.class Lcom/unity3d/player/HackOverlayService$1;
.super Ljava/lang/Object;
.source "HackOverlayService.java"

.implements Landroid/view/View$OnClickListener;

# Dış sınıfa referans
.field final synthetic this$0:Lcom/unity3d/player/HackOverlayService;

.method constructor <init>(Lcom/unity3d/player/HackOverlayService;)V
    .registers 2
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    iput-object p1, p0, Lcom/unity3d/player/HackOverlayService$1;->this$0:Lcom/unity3d/player/HackOverlayService;
    return-void
.end method

.method public onClick(Landroid/view/View;)V
    .registers 4
    # v0 = dış sınıf (HackOverlayService)
    iget-object v0, p0, Lcom/unity3d/player/HackOverlayService$1;->this$0:Lcom/unity3d/player/HackOverlayService;

    # v1 = menuOpen alanını oku
    iget-boolean v1, v0, Lcom/unity3d/player/HackOverlayService;->menuOpen:Z

    if-nez v1, :close_menu

    :open_menu
    # menuOpen = true
    const/4 v1, 0x1
    iput-boolean v1, v0, Lcom/unity3d/player/HackOverlayService;->menuOpen:Z
    # menuPanel.setVisibility(VISIBLE=0)
    iget-object v2, v0, Lcom/unity3d/player/HackOverlayService;->menuPanel:Landroid/widget/LinearLayout;
    const/4 v3, 0x0
    invoke-virtual {v2, v3}, Landroid/widget/LinearLayout;->setVisibility(I)V
    return-void

    :close_menu
    # menuOpen = false
    const/4 v1, 0x0
    iput-boolean v1, v0, Lcom/unity3d/player/HackOverlayService;->menuOpen:Z
    # menuPanel.setVisibility(GONE=8)
    iget-object v2, v0, Lcom/unity3d/player/HackOverlayService;->menuPanel:Landroid/widget/LinearLayout;
    const/4 v3, 0x8
    invoke-virtual {v2, v3}, Landroid/widget/LinearLayout;->setVisibility(I)V
    return-void
.end method
