package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class HomeView extends VBox {

    public HomeView(Navigator nav) {
        setPadding(new Insets(24));
        setSpacing(14);

        var title = new Label("Tournoi FFTT — Accueil");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        var subtitle = new Label("Choisissez votre mode de connexion (V0 : sans base, sans serveur).");
        subtitle.setStyle("-fx-font-size: 13px; -fx-opacity: 0.85;");

        var orgaBtn = new Button("👤 Connexion Organisme");
        orgaBtn.setPrefHeight(42);
        orgaBtn.setMaxWidth(Double.MAX_VALUE);
        orgaBtn.setOnAction(e -> nav.showOrganizerLogin());

        var playerBtn = new Button("🧑‍🏓 Connexion Joueur");
        playerBtn.setPrefHeight(42);
        playerBtn.setMaxWidth(Double.MAX_VALUE);
        playerBtn.setOnAction(e -> {
            /* plus tard */});

        getChildren().addAll(title, subtitle, orgaBtn, playerBtn);
    }
}
