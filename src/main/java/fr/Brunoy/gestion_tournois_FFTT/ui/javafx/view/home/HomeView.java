package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.home;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

public class HomeView extends BorderPane {

    public HomeView(Navigator nav) {
        AppTheme.applyPage(this);
        setPadding(new Insets(AppTheme.PADDING_PAGE));

        // --- Top: logo (gauche) ---
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.TOP_LEFT);

        ImageView logo = tryLoadLogo(AppTheme.LOGO_RESOURCE);
        if (logo != null) {
            logo.setFitHeight(44);
            logo.setPreserveRatio(true);
            topBar.getChildren().add(logo);
        } else {
            // fallback propre si pas de fichier logo
            Label logoFallback = new Label("PM");
            logoFallback.setStyle(
                    "-fx-background-color: " + AppTheme.COLOR_PRIMARY + ";" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: 900;" +
                            "-fx-padding: 10 12;" +
                            "-fx-background-radius: 10;");
            topBar.getChildren().add(logoFallback);
        }

        setTop(topBar);

        // --- Centre: texte + cartes ---
        VBox center = new VBox(AppTheme.SPACE_LG);
        center.setAlignment(Pos.TOP_CENTER);
        center.setPadding(new Insets(10, 0, 0, 0));

        // Bloc texte central
        VBox hero = new VBox(AppTheme.SPACE_SM);
        hero.setAlignment(Pos.TOP_CENTER);
        hero.setMaxWidth(720);

        Label title = new Label("PingManager");
        AppTheme.applyTitle(title);

        Label subtitle = new Label(
                "Gérez vos tournois de tennis de table, de l'inscription à la remontée des résultats.");
        AppTheme.applySubtitle(subtitle);

        Label hint = new Label("Choisissez votre espace : Organisateur (club) ou Joueur (inscription).");
        AppTheme.applyBody(hint);

        hero.getChildren().addAll(title, subtitle, hint);

        // Cartes
        HBox cards = new HBox(18);
        cards.setAlignment(Pos.TOP_CENTER);
        cards.setMaxWidth(900);

        VBox organizerCard = buildOrganizerCard(nav);
        VBox playerCard = buildPlayerCard(nav);

        // largeur harmonisée
        organizerCard.setPrefWidth(420);
        playerCard.setPrefWidth(420);

        cards.getChildren().addAll(organizerCard, playerCard);

        center.getChildren().addAll(hero, cards);
        setCenter(center);
    }

    private VBox buildOrganizerCard(Navigator nav) {
        Label t = new Label("Club / Organisateur");
        AppTheme.applyCardTitle(t);

        Label d = new Label(
                "Créez un tournoi, gérez les inscriptions, générez les tableaux, " +
                        "lancez les tours et exportez les résultats.");
        AppTheme.applyBody(d);

        Button b = new Button("Connexion Organisateur");
        AppTheme.stylePrimary(b);
        b.setOnAction(e -> nav.showOrganizerLogin());

        VBox card = AppTheme.card(t, d, spacer(), b);
        return card;
    }

    private VBox buildPlayerCard(Navigator nav) {
        Label t = new Label("Joueur");
        AppTheme.applyCardTitle(t);

        Label d = new Label(
                "Consultez les tournois disponibles et gérez vos inscriptions " +
                        "selon les règles du tournoi.");
        AppTheme.applyBody(d);

        Button b = new Button("Connexion Joueur");
        AppTheme.styleSecondary(b);
        b.setOnAction(e -> {
            // nav.showPlayerLogin(); // quand tu l'auras
        });

        VBox card = AppTheme.card(t, d, spacer(), b);
        return card;
    }

    private Region spacer() {
        Region r = new Region();
        VBox.setVgrow(r, Priority.ALWAYS);
        return r;
    }

    private ImageView tryLoadLogo(String resourcePath) {
        try {
            Image img = new Image(getClass().getResourceAsStream(resourcePath));
            return new ImageView(img);
        } catch (Exception ignore) {
            return null;
        }
    }
}