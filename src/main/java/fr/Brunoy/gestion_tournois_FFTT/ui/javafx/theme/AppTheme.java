package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.theme;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Thème UI centralisé (tokens + helpers).
 * Objectif : rendu "desktop pro" (cards, titres, actions).
 */
public final class AppTheme {

    private AppTheme() {
    }

    // ---------- Color tokens ----------
    public static final String COLOR_PRIMARY = "#1565C0";
    public static final String COLOR_PRIMARY_DARK = "#0B3C91";
    public static final String COLOR_BG = "#F4F7FB";
    public static final String COLOR_SURFACE = "#FFFFFF";
    public static final String COLOR_TEXT = "#1E293B";
    public static final String COLOR_TEXT_MUTED = "rgba(30,41,59,0.72)";
    public static final String COLOR_BORDER = "rgba(30,41,59,0.15)";

    // Dans AppTheme
    public static final String LOGO_RESOURCE = "/fr/Brunoy/gestion_tournois_FFTT/ui/javafx/theme/PingMaster.png";

    // ---------- Layout ----------
    public static final double PADDING_PAGE = 28;
    public static final double SPACE_SM = 8;
    public static final double SPACE_MD = 14;
    public static final double SPACE_LG = 22;

    public static final double BTN_HEIGHT = 42;
    public static final double RADIUS = 12;

    // ---------- Typography ----------
    public static final String FONT_FAMILY = "System";

    public static final String TITLE_STYLE = "-fx-font-family: " + FONT_FAMILY + ";" +
            "-fx-font-size: 26px;" +
            "-fx-font-weight: 800;" +
            "-fx-text-fill: " + COLOR_TEXT + ";";

    public static final String SUBTITLE_STYLE = "-fx-font-family: " + FONT_FAMILY + ";" +
            "-fx-font-size: 14px;" +
            "-fx-text-fill: " + COLOR_TEXT_MUTED + ";";

    public static final String CARD_TITLE_STYLE = "-fx-font-family: " + FONT_FAMILY + ";" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: 800;" +
            "-fx-text-fill: " + COLOR_TEXT + ";";

    public static final String BODY_STYLE = "-fx-font-family: " + FONT_FAMILY + ";" +
            "-fx-font-size: 13px;" +
            "-fx-text-fill: " + COLOR_TEXT_MUTED + ";";

    // ---------- Background ----------
    public static final String PAGE_STYLE = "-fx-background-color: " + COLOR_BG + ";";

    // ---------- Buttons ----------
    public static final String PRIMARY_BUTTON_STYLE = "-fx-background-color: " + COLOR_PRIMARY + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: 800;" +
            "-fx-background-radius: " + RADIUS + ";" +
            "-fx-padding: 10 14;" +
            "-fx-cursor: hand;";

    public static final String SECONDARY_BUTTON_STYLE = "-fx-background-color: " + COLOR_SURFACE + ";" +
            "-fx-text-fill: " + COLOR_TEXT + ";" +
            "-fx-font-weight: 800;" +
            "-fx-background-radius: " + RADIUS + ";" +
            "-fx-border-color: " + COLOR_BORDER + ";" +
            "-fx-border-radius: " + RADIUS + ";" +
            "-fx-padding: 10 14;" +
            "-fx-cursor: hand;";

    // ---------- Cards ----------
    public static final String CARD_STYLE = "-fx-background-color: " + COLOR_SURFACE + ";" +
            "-fx-background-radius: " + RADIUS + ";" +
            "-fx-border-color: " + COLOR_BORDER + ";" +
            "-fx-border-radius: " + RADIUS + ";";

    // --- Links / small buttons ---
    public static final String LINK_BUTTON_STYLE = "-fx-background-color: transparent;" +
            "-fx-text-fill: " + COLOR_PRIMARY + ";" +
            "-fx-font-weight: 700;" +
            "-fx-padding: 0;" +
            "-fx-cursor: hand;";

    public static void styleLinkButton(Button btn) {
        btn.setStyle(LINK_BUTTON_STYLE);
        btn.setOnMouseEntered(e -> btn.setStyle(LINK_BUTTON_STYLE + "-fx-underline: true;"));
        btn.setOnMouseExited(e -> btn.setStyle(LINK_BUTTON_STYLE + "-fx-underline: false;"));
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
    }

    public static void applyBody(Label label) {
        label.setStyle(BODY_STYLE);
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

        btn.setOnMouseEntered(
                e -> btn.setStyle(SECONDARY_BUTTON_STYLE + "-fx-background-color: rgba(255,255,255,0.92);"));
        btn.setOnMouseExited(e -> btn.setStyle(SECONDARY_BUTTON_STYLE));
        btn.setOnMousePressed(e -> btn.setStyle(SECONDARY_BUTTON_STYLE + "-fx-background-color: rgba(245,247,251,1);"));
        btn.setOnMouseReleased(
                e -> btn.setStyle(SECONDARY_BUTTON_STYLE + "-fx-background-color: rgba(255,255,255,0.92);"));
    }

    /** Crée une "card" avec padding + ombre légère */
    public static VBox card(Node... children) {
        VBox box = new VBox(SPACE_MD);
        box.setStyle(CARD_STYLE);
        box.setPadding(new Insets(18));
        box.getChildren().addAll(children);

        DropShadow ds = new DropShadow();
        ds.setRadius(14);
        ds.setOffsetY(6);
        ds.setColor(Color.rgb(15, 23, 42, 0.10)); // ombre douce
        box.setEffect(ds);

        return box;
    }
}