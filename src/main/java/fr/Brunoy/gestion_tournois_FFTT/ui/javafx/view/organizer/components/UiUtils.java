package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.components;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerTournamentCardModel;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;

public final class UiUtils {

    private UiUtils() {
    }

    public static VBox tournamentCardList(Navigator nav, List<OrganizerTournamentCardModel> tournaments) {
        VBox box = new VBox(12);
        box.setPadding(new Insets(0));

        for (OrganizerTournamentCardModel t : tournaments) {
            box.getChildren().add(new TournamentCard(nav, t));
        }
        return box;
    }

    public static Label kv(String key, String value) {
        return new Label(key + " : " + (value == null || value.isBlank() ? "—" : value));
    }

    public static String nvl(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    public static void info(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static String safe(String value) {
        if (value == null) {
            return "";
        }
        String v = value.trim();
        return v.isEmpty() ? "" : v;
    }

    public static String fullNameOrDash(String firstName, String lastName) {
        String first = safe(firstName);
        String last = safe(lastName);

        if (first.isEmpty() && last.isEmpty()) {
            return "—";
        }
        if (first.isEmpty()) {
            return last;
        }
        if (last.isEmpty()) {
            return first;
        }
        return first + " " + last;
    }
}