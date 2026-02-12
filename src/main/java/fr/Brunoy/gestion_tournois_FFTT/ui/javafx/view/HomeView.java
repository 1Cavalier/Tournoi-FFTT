package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.vm.AppState;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class HomeView extends VBox {

    public HomeView(AppState state, Navigator nav) {
        setPadding(new Insets(24));
        setSpacing(14);

        var title = new Label("Tournoi FFTT — Accueil");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        var subtitle = new Label("Choisissez votre mode d’utilisation (V1 : sans connexion, sans base).");
        subtitle.setStyle("-fx-font-size: 13px; -fx-opacity: 0.85;");

        var orgaBtn = new Button("👤 Mode Organisme (club / orga)");
        orgaBtn.setPrefHeight(42);
        orgaBtn.setMaxWidth(Double.MAX_VALUE);
        orgaBtn.setOnAction(e -> nav.showOrganizerLogin());


        var playerBtn = new Button("🧑‍🏓 Mode Joueur");
        playerBtn.setPrefHeight(42);
        playerBtn.setMaxWidth(Double.MAX_VALUE);
        playerBtn.setOnAction(e -> {
            state.setUserMode(AppState.UserMode.JOUEUR);
            state.resetSelection();
            nav.showTableauSelection(); // idem : même écran pour l’instant
        });

        var hint = new Label("""
                Ensuite, on pourra faire diverger :
                - Organisme : gestion inscriptions / listes / résultats
                - Joueur : inscription / paiement / récap
                """);
        hint.setStyle("-fx-font-size: 12px; -fx-opacity: 0.8;");

        getChildren().addAll(title, subtitle, orgaBtn, playerBtn, hint);
    }
}