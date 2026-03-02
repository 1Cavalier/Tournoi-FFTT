package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.components;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;

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

    public static Label kv(String k, String v) {
        // Ici pas de style : le parent peut styler s'il veut
        return new Label(k + " : " + (v == null || v.isBlank() ? "—" : v));
    }

    public static VBox tournamentList(Navigator nav,
            List<fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.TournamentRow> tournaments,
            TournamentCard.Mode mode) {
        VBox list = new VBox(12);
        for (var t : tournaments) {
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
}