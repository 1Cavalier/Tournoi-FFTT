package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.home;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Écran d'accueil.
 */
public class HomeView extends VBox {

    private static final String TITLE_STYLE = "-fx-font-size: 22px; -fx-font-weight: bold;";
    private static final String SUBTITLE_STYLE = "-fx-font-size: 13px; -fx-opacity: 0.85;";

    public HomeView(Navigator nav) {
        setPadding(new Insets(24));
        setSpacing(14);

        Label title = new Label("Tournoi FFTT — Accueil");
        title.setStyle(TITLE_STYLE);

        Label subtitle = new Label("Choisissez votre mode de connexion (V0 : sans base, sans serveur).");
        subtitle.setStyle(SUBTITLE_STYLE);

        Button organizerButton = new Button("Connexion Organisme");
        organizerButton.setPrefHeight(42);
        organizerButton.setMaxWidth(Double.MAX_VALUE);
        organizerButton.setOnAction(e -> nav.showOrganizerLogin());

        Button playerButton = new Button("Connexion Joueur");
        playerButton.setPrefHeight(42);
        playerButton.setMaxWidth(Double.MAX_VALUE);
        playerButton.setOnAction(e -> {
            // Plus tard
        });

        getChildren().addAll(title, subtitle, organizerButton, playerButton);
    }
}