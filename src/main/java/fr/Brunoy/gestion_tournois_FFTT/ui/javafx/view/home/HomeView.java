package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.home;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.AppRouter;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Objects;

public class HomeView extends BorderPane {

    private static final double CARD_WIDTH = 420;

    private final AppRouter nav;

    public HomeView(AppRouter nav) {
        this.nav = Objects.requireNonNull(nav, "nav must not be null");

        AppTheme.applyPage(this);
        setPadding(new Insets(AppTheme.PADDING_PAGE));

        setTop(buildTopBar());
        setCenter(buildCenterContent());
    }

    private HBox buildTopBar() {
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        ImageView logo = AppTheme.logoView(44);
        if (logo != null) {
            topBar.getChildren().add(logo);
        } else {
            Label fallback = new Label("PM");
            fallback.setStyle(
                    "-fx-background-color: " + AppTheme.COLOR_PRIMARY + ";" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: 900;" +
                            "-fx-padding: 10 12;" +
                            "-fx-background-radius: 10;");
            topBar.getChildren().add(fallback);
        }

        Label appName = new Label("PingManager");
        AppTheme.applyCardTitle(appName);

        topBar.getChildren().add(appName);
        return topBar;
    }

    private VBox buildCenterContent() {
        VBox center = new VBox(AppTheme.SPACE_LG);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(10, 0, 10, 0));

        VBox hero = buildHeroSection();
        HBox cards = buildCardsSection();

        center.getChildren().addAll(hero, cards);
        return center;
    }

    private VBox buildHeroSection() {
        VBox hero = new VBox(AppTheme.SPACE_SM);
        hero.setAlignment(Pos.CENTER);
        hero.setMaxWidth(760);

        Label title = new Label("Bienvenue sur PingManager");
        AppTheme.applyTitle(title);

        Label subtitle = new Label(
                "La solution desktop pour préparer, organiser et suivre vos tournois de tennis de table.");
        AppTheme.applySubtitle(subtitle);

        Label hint = new Label(
                "Choisissez votre espace pour accéder aux fonctionnalités adaptées à votre profil.");
        AppTheme.applyBody(hint);

        hero.getChildren().addAll(title, subtitle, hint);
        return hero;
    }

    private HBox buildCardsSection() {
        HBox cards = new HBox(18);
        cards.setAlignment(Pos.CENTER);
        cards.setMaxWidth(920);

        VBox organizerCard = buildOrganizerCard();
        VBox playerCard = buildPlayerCard();

        organizerCard.setPrefWidth(CARD_WIDTH);
        organizerCard.setMaxWidth(CARD_WIDTH);

        playerCard.setPrefWidth(CARD_WIDTH);
        playerCard.setMaxWidth(CARD_WIDTH);

        cards.getChildren().addAll(organizerCard, playerCard);
        return cards;
    }

    private VBox buildOrganizerCard() {
        Label title = new Label("Club / Organisateur");
        AppTheme.applyCardTitle(title);

        Label description = new Label(
                "Créez vos tournois, configurez les tableaux, gérez les inscriptions "
                        + "et pilotez l’organisation depuis un espace centralisé.");
        AppTheme.applyBody(description);

        Button button = new Button("Accéder à l’espace organisateur");
        AppTheme.stylePrimary(button);
        button.setOnAction(e -> nav.showOrganizerLogin());

        VBox card = AppTheme.card(title, description, spacer(), button);
        VBox.setVgrow(card, Priority.ALWAYS);
        return card;
    }

    private VBox buildPlayerCard() {
        Label title = new Label("Joueur");
        AppTheme.applyCardTitle(title);

        Label description = new Label(
                "Consultez les tournois ouverts et suivez vos inscriptions. "
                        + "Cet espace sera disponible dans une prochaine version.");
        AppTheme.applyBody(description);

        Button button = new Button("Espace joueur bientôt disponible");
        AppTheme.styleSecondary(button);
        button.setDisable(true);

        VBox card = AppTheme.card(title, description, spacer(), button);
        VBox.setVgrow(card, Priority.ALWAYS);
        return card;
    }

    private Region spacer() {
        Region region = new Region();
        VBox.setVgrow(region, Priority.ALWAYS);
        return region;
    }
}