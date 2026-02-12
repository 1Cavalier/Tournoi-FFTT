package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.vm.AppState;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class OrganizerDashboardView extends VBox {

    public OrganizerDashboardView(AppState state, Navigator nav) {

        setPadding(new Insets(20));
        setSpacing(12);

        String organizerName = (state.getCurrentOrganizer() != null)
                ? state.getCurrentOrganizer().getName()
                : "(non connecté)";

        Label welcome = new Label("Bienvenue " + organizerName);
        welcome.setStyle("-fx-font-size:18px; -fx-font-weight:bold;");

        Button createTournamentBtn = new Button("Créer un tournoi");
        createTournamentBtn.setOnAction(e -> {
            // À créer à l’étape suivante :
            // nav.showCreateTournament();
            // Pour l’instant, on peut juste revenir sur l’écran tableaux si tu veux tester
            // :
            nav.showTableauSelection();
        });

        Button logoutBtn = new Button("Déconnexion");
        logoutBtn.setOnAction(e -> {
            state.setCurrentOrganizer(null);
            nav.showHome();
        });

        getChildren().addAll(welcome, createTournamentBtn, logoutBtn);
    }
}
