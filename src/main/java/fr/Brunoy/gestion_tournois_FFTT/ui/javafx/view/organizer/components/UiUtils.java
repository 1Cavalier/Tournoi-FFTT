package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.components;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.TournamentRow;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Utilitaires UI (styles simples + helpers).
 * Centralise les petits composants pour éviter la duplication.
 */
public final class UiUtils {

    private UiUtils() {
    }

    public static String nvl(String s) {
        return (s == null || s.isBlank()) ? "—" : s.trim();
    }

    public static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    public static String fullNameOrDash(String firstName, String lastName) {
        String fn = firstName == null ? "" : firstName.trim();
        String ln = lastName == null ? "" : lastName.trim();
        String full = (fn + " " + ln).trim();
        return full.isEmpty() ? "—" : full;
    }

    public static Label sectionTitle(String txt) {
        Label l = new Label(txt);
        l.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        return l;
    }

    public static VBox infoBanner(String txt) {
        VBox box = new VBox();
        box.setPadding(new Insets(12));
        box.setStyle("-fx-border-color:black; -fx-border-width:2; -fx-background-color:white;");
        Label l = new Label(txt);
        l.setStyle("-fx-font-weight:bold;");
        box.getChildren().add(l);
        return box;
    }

    public static Label kv(String k, String v) {
        Label l = new Label(k + " : " + (v == null || v.isBlank() ? "—" : v));
        l.setStyle("-fx-opacity:0.9;");
        return l;
    }

    public static VBox tournamentList(Navigator nav, List<TournamentRow> tournaments, TournamentCard.Mode mode) {
        VBox list = new VBox(12);
        for (TournamentRow t : tournaments) {
            list.getChildren().add(new TournamentCard(nav, t, mode));
        }
        return list;
    }

    public static void info(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    public static VBox centeredBox(javafx.scene.Node node, int paddingTop) {
        VBox box = new VBox(node);
        box.setPadding(new Insets(paddingTop, 0, 0, 0));
        box.setStyle("-fx-alignment:center;");
        return box;
    }

    public static Label centeredLabel(String text, String style) {
        Label l = new Label(text);
        l.setMaxWidth(Double.MAX_VALUE);
        l.setStyle(style + " -fx-alignment:center;");
        return l;
    }
}