package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.theme;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Thème UI centralisé (tokens + helpers).
 * Objectif : rendu desktop pro cohérent avec la charte PingManager.
 */
public final class AppTheme {

        private AppTheme() {
        }

        // -------------------------------------------------------------------------
        // COLOR TOKENS
        // -------------------------------------------------------------------------

        public static final String COLOR_PRIMARY = "#1565C0";
        public static final String COLOR_PRIMARY_DARK = "#0B3C91";
        public static final String COLOR_PRIMARY_LIGHT = "#42A5F5";

        public static final String COLOR_BG = "#F4F7FB";
        public static final String COLOR_SURFACE = "#FFFFFF";
        public static final String COLOR_SURFACE_SOFT = "#F8FAFD";

        public static final String COLOR_TEXT = "#1E293B";
        public static final String COLOR_TEXT_MUTED = "rgba(30,41,59,0.72)";
        public static final String COLOR_BORDER = "rgba(30,41,59,0.14)";

        public static final String COLOR_SUCCESS = "#2E7D32";
        public static final String COLOR_DANGER = "#C62828";
        public static final String COLOR_WARNING = "#64748B";
        public static final String COLOR_CANCELLED = "#111827";

        public static final String COLOR_SIDEBAR_TEXT = "rgba(255,255,255,0.97)";
        public static final String COLOR_SIDEBAR_TEXT_MUTED = "rgba(255,255,255,0.74)";
        public static final String COLOR_SIDEBAR_BORDER = "rgba(255,255,255,0.10)";
        public static final String COLOR_SIDEBAR_PANEL = "rgba(255,255,255,0.08)";
        public static final String COLOR_SIDEBAR_PANEL_HOVER = "rgba(255,255,255,0.12)";
        public static final String COLOR_SIDEBAR_PANEL_PRESS = "rgba(255,255,255,0.18)";
        public static final String COLOR_SIDEBAR_ITEM = "rgba(255,255,255,0.06)";
        public static final String COLOR_SIDEBAR_ITEM_HOVER = "rgba(255,255,255,0.10)";

        /**
         * Dégradé diagonal premium pour la sidebar.
         */
        public static final String COLOR_SIDEBAR_BG = "linear-gradient(from 0% 0% to 100% 100%, #06204D 0%, #0A2F73 42%, #0B3C91 72%, #1565C0 100%)";

        public static final String LOGO_RESOURCE = "/fr/Brunoy/gestion_tournois_FFTT/ui/javafx/theme/PingManager.png";

        // -------------------------------------------------------------------------
        // LAYOUT
        // -------------------------------------------------------------------------

        public static final double PADDING_PAGE = 28;
        public static final double SPACE_XS = 6;
        public static final double SPACE_SM = 8;
        public static final double SPACE_MD = 14;
        public static final double SPACE_LG = 22;
        public static final double SPACE_XL = 28;

        public static final double BTN_HEIGHT = 42;
        public static final double BTN_HEIGHT_SM = 36;
        public static final double RADIUS = 12;
        public static final double RADIUS_LG = 18;

        public static final double TOPBAR_HEIGHT = 58;
        public static final double SIDEBAR_WIDTH = 310;

        // -------------------------------------------------------------------------
        // TYPOGRAPHY
        // -------------------------------------------------------------------------

        public static final String FONT_TITLE = "Montserrat";
        public static final String FONT_BODY = "Open Sans";

        public static final String TITLE_STYLE = "-fx-font-family: '" + FONT_TITLE + "';" +
                        "-fx-font-size: 28px;" +
                        "-fx-font-weight: 800;" +
                        "-fx-text-fill: " + COLOR_TEXT + ";";

        public static final String SUBTITLE_STYLE = "-fx-font-family: '" + FONT_BODY + "';" +
                        "-fx-font-size: 14px;" +
                        "-fx-text-fill: " + COLOR_TEXT_MUTED + ";";

        public static final String CARD_TITLE_STYLE = "-fx-font-family: '" + FONT_TITLE + "';" +
                        "-fx-font-size: 17px;" +
                        "-fx-font-weight: 800;" +
                        "-fx-text-fill: " + COLOR_TEXT + ";";

        public static final String BODY_STYLE = "-fx-font-family: '" + FONT_BODY + "';" +
                        "-fx-font-size: 13px;" +
                        "-fx-text-fill: " + COLOR_TEXT_MUTED + ";";

        public static final String TOPBAR_APP_NAME_STYLE = "-fx-font-family: '" + FONT_TITLE + "';" +
                        "-fx-font-size: 17px;" +
                        "-fx-font-weight: 800;" +
                        "-fx-text-fill: " + COLOR_SIDEBAR_TEXT + ";";

        public static final String TOPBAR_PAGE_TITLE_STYLE = "-fx-font-family: '" + FONT_TITLE + "';" +
                        "-fx-font-size: 18px;" +
                        "-fx-font-weight: 700;" +
                        "-fx-text-fill: rgba(255,255,255,0.92);";

        public static final String TOPBAR_CONTEXT_STYLE = "-fx-font-family: '" + FONT_BODY + "';" +
                        "-fx-font-size: 13px;" +
                        "-fx-text-fill: rgba(255,255,255,0.76);";

        public static final String SIDEBAR_BRAND_TITLE_STYLE = "-fx-font-family: '" + FONT_TITLE + "';" +
                        "-fx-font-size: 20px;" +
                        "-fx-font-weight: 800;" +
                        "-fx-text-fill: " + COLOR_SIDEBAR_TEXT + ";";

        public static final String SIDEBAR_SECTION_TITLE_STYLE = "-fx-font-family: '" + FONT_TITLE + "';" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: 800;" +
                        "-fx-text-fill: " + COLOR_SIDEBAR_TEXT + ";";

        public static final String SIDEBAR_CLUB_NAME_STYLE = "-fx-font-family: '" + FONT_TITLE + "';" +
                        "-fx-font-size: 15px;" +
                        "-fx-font-weight: 800;" +
                        "-fx-text-fill: " + COLOR_SIDEBAR_TEXT + ";";

        public static final String SIDEBAR_PRIMARY_TEXT_STYLE = "-fx-font-family: '" + FONT_BODY + "';" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: 700;" +
                        "-fx-text-fill: " + COLOR_SIDEBAR_TEXT + ";";

        public static final String SIDEBAR_MUTED_TEXT_STYLE = "-fx-font-family: '" + FONT_BODY + "';" +
                        "-fx-font-size: 12px;" +
                        "-fx-text-fill: " + COLOR_SIDEBAR_TEXT_MUTED + ";";

        public static final String SIDEBAR_EMPTY_TEXT_STYLE = "-fx-font-family: '" + FONT_BODY + "';" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-style: italic;" +
                        "-fx-text-fill: " + COLOR_SIDEBAR_TEXT_MUTED + ";";

        // -------------------------------------------------------------------------
        // BACKGROUNDS / SHELL
        // -------------------------------------------------------------------------

        public static final String PAGE_STYLE = "-fx-background-color: " + COLOR_BG + ";";

        public static final String TOPBAR_STYLE = "-fx-background-color: linear-gradient(from 0% 0% to 100% 0%, #06204D 0%, #0A2F73 38%, #0B3C91 68%, #1565C0 100%);"
                        +
                        "-fx-border-color: rgba(255,255,255,0.10);" +
                        "-fx-border-width: 0 0 1 0;";

        public static final String SIDEBAR_STYLE = "-fx-background-color: " + COLOR_SIDEBAR_BG + ";";

        public static final String SIDEBAR_PANEL_STYLE = "-fx-background-color: " + COLOR_SIDEBAR_PANEL + ";" +
                        "-fx-background-radius: " + RADIUS_LG + ";" +
                        "-fx-border-color: " + COLOR_SIDEBAR_BORDER + ";" +
                        "-fx-border-radius: " + RADIUS_LG + ";";

        public static final String SIDEBAR_INFO_BOX_STYLE = "-fx-background-color: rgba(255,255,255,0.06);" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: rgba(255,255,255,0.08);" +
                        "-fx-border-radius: 12;";

        public static final String CARD_STYLE = "-fx-background-color: " + COLOR_SURFACE + ";" +
                        "-fx-background-radius: " + RADIUS + ";" +
                        "-fx-border-color: " + COLOR_BORDER + ";" +
                        "-fx-border-radius: " + RADIUS + ";";

        // -------------------------------------------------------------------------
        // BUTTONS
        // -------------------------------------------------------------------------

        public static final String PRIMARY_BUTTON_STYLE = "-fx-background-color: " + COLOR_PRIMARY + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-family: '" + FONT_BODY + "';" +
                        "-fx-font-weight: 800;" +
                        "-fx-background-radius: " + RADIUS + ";" +
                        "-fx-padding: 10 14;" +
                        "-fx-cursor: hand;";

        public static final String SECONDARY_BUTTON_STYLE = "-fx-background-color: " + COLOR_SURFACE + ";" +
                        "-fx-text-fill: " + COLOR_TEXT + ";" +
                        "-fx-font-family: '" + FONT_BODY + "';" +
                        "-fx-font-weight: 800;" +
                        "-fx-background-radius: " + RADIUS + ";" +
                        "-fx-border-color: " + COLOR_BORDER + ";" +
                        "-fx-border-radius: " + RADIUS + ";" +
                        "-fx-padding: 10 14;" +
                        "-fx-cursor: hand;";

        public static final String LINK_BUTTON_STYLE = "-fx-background-color: transparent;" +
                        "-fx-text-fill: " + COLOR_PRIMARY + ";" +
                        "-fx-font-family: '" + FONT_BODY + "';" +
                        "-fx-font-weight: 700;" +
                        "-fx-padding: 0;" +
                        "-fx-cursor: hand;";

        public static final String SIDEBAR_SECONDARY_BUTTON_STYLE = "-fx-background-color: " + COLOR_SIDEBAR_PANEL + ";"
                        +
                        "-fx-text-fill: " + COLOR_SIDEBAR_TEXT + ";" +
                        "-fx-font-family: '" + FONT_BODY + "';" +
                        "-fx-font-weight: 800;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: " + COLOR_SIDEBAR_BORDER + ";" +
                        "-fx-border-radius: 12;" +
                        "-fx-padding: 10 14;" +
                        "-fx-cursor: hand;";

        public static final String SIDEBAR_LINK_BUTTON_STYLE = "-fx-background-color: transparent;" +
                        "-fx-text-fill: #8CC8FF;" +
                        "-fx-font-family: '" + FONT_BODY + "';" +
                        "-fx-font-weight: 700;" +
                        "-fx-padding: 0;" +
                        "-fx-cursor: hand;";

        public static final String SIDEBAR_TOURNAMENT_ITEM_STYLE = "-fx-background-color: " + COLOR_SIDEBAR_ITEM + ";" +
                        "-fx-text-fill: " + COLOR_SIDEBAR_TEXT + ";" +
                        "-fx-font-family: '" + FONT_BODY + "';" +
                        "-fx-font-weight: 700;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 8 12;" +
                        "-fx-cursor: hand;" +
                        "-fx-alignment: CENTER-LEFT;";

        public static final String SIDEBAR_TOURNAMENT_ITEM_ACTIVE_STYLE = "-fx-background-color: " + COLOR_PRIMARY + ";"
                        +
                        "-fx-text-fill: white;" +
                        "-fx-font-family: '" + FONT_BODY + "';" +
                        "-fx-font-weight: 800;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 8 12;" +
                        "-fx-cursor: hand;" +
                        "-fx-alignment: CENTER-LEFT;";

        // -------------------------------------------------------------------------
        // HELPERS
        // -------------------------------------------------------------------------

        public static String badgeStyle(String bg) {
                return "-fx-background-color: " + bg + ";" +
                                "-fx-text-fill: white;" +
                                "-fx-font-family: '" + FONT_BODY + "';" +
                                "-fx-font-weight: 800;" +
                                "-fx-padding: 4 10;" +
                                "-fx-background-radius: 999;";
        }

        public static String tournamentStatusBadgeStyle(String status) {
                if (status == null) {
                        return badgeStyle(COLOR_WARNING);
                }

                return switch (status.trim().toUpperCase()) {
                        case "RUNNING" -> badgeStyle(COLOR_SUCCESS);
                        case "OPEN" -> badgeStyle(COLOR_PRIMARY);
                        case "DRAFT" -> badgeStyle(COLOR_WARNING);
                        case "FINISHED" -> badgeStyle(COLOR_DANGER);
                        case "CANCELLED", "CANCELED" -> badgeStyle(COLOR_CANCELLED);
                        default -> badgeStyle(COLOR_WARNING);
                };
        }

        public static String topBarConnectionBadgeStyle(boolean connected) {
                String bg = connected ? "#7DD3FC" : "#64748B";
                String text = connected ? "#082F49" : "white";

                return "-fx-background-color: " + bg + ";" +
                                "-fx-text-fill: " + text + ";" +
                                "-fx-font-family: '" + FONT_BODY + "';" +
                                "-fx-font-weight: 800;" +
                                "-fx-padding: 5 10;" +
                                "-fx-background-radius: 999;";
        }

        public static ImageView logoView(double height) {
                try {
                        var url = AppTheme.class.getResource(LOGO_RESOURCE);
                        if (url == null) {
                                return null;
                        }
                        ImageView iv = new ImageView(new Image(url.toExternalForm(), true));
                        iv.setFitHeight(height);
                        iv.setPreserveRatio(true);
                        iv.setSmooth(true);
                        iv.setCache(true);
                        return iv;
                } catch (Exception e) {
                        return null;
                }
        }

        public static void applyPage(Region root) {
                root.setStyle(PAGE_STYLE);
        }

        public static void applyTitle(Label label) {
                label.setStyle(TITLE_STYLE);
                label.setWrapText(true);
        }

        public static void applySubtitle(Label label) {
                label.setStyle(SUBTITLE_STYLE);
                label.setWrapText(true);
        }

        public static void applyCardTitle(Label label) {
                label.setStyle(CARD_TITLE_STYLE);
                label.setWrapText(true);
        }

        public static void applyBody(Label label) {
                label.setStyle(BODY_STYLE);
                label.setWrapText(true);
        }

        public static void applyTopBarAppName(Label label) {
                label.setStyle(TOPBAR_APP_NAME_STYLE);
                label.setWrapText(true);
        }

        public static void applyTopBarPageTitle(Label label) {
                label.setStyle(TOPBAR_PAGE_TITLE_STYLE);
                label.setWrapText(true);
        }

        public static void applyTopBarContext(Label label) {
                label.setStyle(TOPBAR_CONTEXT_STYLE);
                label.setWrapText(true);
        }

        public static void applySidebarSectionTitle(Label label) {
                label.setStyle(SIDEBAR_SECTION_TITLE_STYLE);
                label.setWrapText(true);
        }

        public static void applySidebarClubName(Label label) {
                label.setStyle(SIDEBAR_CLUB_NAME_STYLE);
                label.setWrapText(true);
        }

        public static void applySidebarPrimaryText(Label label) {
                label.setStyle(SIDEBAR_PRIMARY_TEXT_STYLE);
                label.setWrapText(true);
        }

        public static void applySidebarMutedText(Label label) {
                label.setStyle(SIDEBAR_MUTED_TEXT_STYLE);
                label.setWrapText(true);
        }

        public static void applySidebarEmptyText(Label label) {
                label.setStyle(SIDEBAR_EMPTY_TEXT_STYLE);
                label.setWrapText(true);
        }

        public static void stylePrimary(Button btn) {
                btn.setStyle(PRIMARY_BUTTON_STYLE);
                btn.setPrefHeight(BTN_HEIGHT);
                btn.setMaxWidth(Double.MAX_VALUE);

                btn.setOnMouseEntered(e -> btn.setStyle(PRIMARY_BUTTON_STYLE + "-fx-opacity: 0.92;"));
                btn.setOnMouseExited(e -> btn.setStyle(PRIMARY_BUTTON_STYLE));
                btn.setOnMousePressed(e -> btn.setStyle(PRIMARY_BUTTON_STYLE + "-fx-opacity: 0.85;"));
                btn.setOnMouseReleased(e -> btn.setStyle(PRIMARY_BUTTON_STYLE + "-fx-opacity: 0.92;"));
        }

        public static void styleSecondary(Button btn) {
                btn.setStyle(SECONDARY_BUTTON_STYLE);
                btn.setPrefHeight(BTN_HEIGHT);
                btn.setMaxWidth(Double.MAX_VALUE);

                btn.setOnMouseEntered(e -> btn
                                .setStyle(SECONDARY_BUTTON_STYLE + "-fx-background-color: rgba(255,255,255,0.92);"));
                btn.setOnMouseExited(e -> btn.setStyle(SECONDARY_BUTTON_STYLE));
                btn.setOnMousePressed(e -> btn
                                .setStyle(SECONDARY_BUTTON_STYLE + "-fx-background-color: rgba(245,247,251,1);"));
                btn.setOnMouseReleased(e -> btn
                                .setStyle(SECONDARY_BUTTON_STYLE + "-fx-background-color: rgba(255,255,255,0.92);"));
        }

        public static void styleLinkButton(Button btn) {
                btn.setStyle(LINK_BUTTON_STYLE);
                btn.setOnMouseEntered(e -> btn.setStyle(LINK_BUTTON_STYLE + "-fx-underline: true;"));
                btn.setOnMouseExited(e -> btn.setStyle(LINK_BUTTON_STYLE + "-fx-underline: false;"));
        }

        public static void styleSidebarSecondaryButton(Button btn) {
                btn.setStyle(SIDEBAR_SECONDARY_BUTTON_STYLE);
                btn.setPrefHeight(BTN_HEIGHT);
                btn.setMaxWidth(Double.MAX_VALUE);

                btn.setOnMouseEntered(e -> btn.setStyle(SIDEBAR_SECONDARY_BUTTON_STYLE + "-fx-background-color: "
                                + COLOR_SIDEBAR_PANEL_HOVER + ";"));
                btn.setOnMouseExited(e -> btn.setStyle(SIDEBAR_SECONDARY_BUTTON_STYLE));
                btn.setOnMousePressed(e -> btn.setStyle(SIDEBAR_SECONDARY_BUTTON_STYLE + "-fx-background-color: "
                                + COLOR_SIDEBAR_PANEL_PRESS + ";"));
                btn.setOnMouseReleased(e -> btn.setStyle(SIDEBAR_SECONDARY_BUTTON_STYLE + "-fx-background-color: "
                                + COLOR_SIDEBAR_PANEL_HOVER + ";"));
        }

        public static void styleSidebarLinkButton(Button btn) {
                btn.setStyle(SIDEBAR_LINK_BUTTON_STYLE);
                btn.setOnMouseEntered(e -> btn.setStyle(SIDEBAR_LINK_BUTTON_STYLE + "-fx-underline: true;"));
                btn.setOnMouseExited(e -> btn.setStyle(SIDEBAR_LINK_BUTTON_STYLE + "-fx-underline: false;"));
        }

        public static void styleSidebarTournamentItem(Button btn, boolean active) {
                btn.setStyle(active ? SIDEBAR_TOURNAMENT_ITEM_ACTIVE_STYLE : SIDEBAR_TOURNAMENT_ITEM_STYLE);
                btn.setMaxWidth(Double.MAX_VALUE);

                if (active) {
                        return;
                }

                btn.setOnMouseEntered(e -> btn.setStyle(SIDEBAR_TOURNAMENT_ITEM_STYLE + "-fx-background-color: "
                                + COLOR_SIDEBAR_ITEM_HOVER + ";"));
                btn.setOnMouseExited(e -> btn.setStyle(SIDEBAR_TOURNAMENT_ITEM_STYLE));
        }

        /**
         * Card standard avec padding + ombre légère.
         */
        public static VBox card(Node... children) {
                VBox box = new VBox(SPACE_MD);
                box.setStyle(CARD_STYLE);
                box.setPadding(new Insets(18));
                box.getChildren().addAll(children);

                DropShadow ds = new DropShadow();
                ds.setRadius(14);
                ds.setOffsetY(6);
                ds.setColor(Color.rgb(15, 23, 42, 0.10));
                box.setEffect(ds);

                return box;
        }
}