package fr.pingmanager.gestion_tournois_FFTT.ui.javafx.view.home;

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

import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.app.AppRouter;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;

public class HomeView extends BorderPane {

    private static final double CARD_WIDTH = 420;
    private static final double CARDS_CONTAINER_MAX_WIDTH = 920;
    private static final double HERO_MAX_WIDTH = 760;

    private final AppRouter nav;

    public HomeView(AppRouter nav) {
        this.nav = Objects.requireNonNull(nav, "nav must not be null");

        AppTheme.applyPage(this);
        setPadding(new Insets(AppTheme.PADDING_PAGE));

        setTop(buildTopBar());
        setCenter(buildCenterWrapper());
    }

    private HBox buildTopBar() {
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 0, AppTheme.SPACE_LG, 0));

        ImageView logo = AppTheme.logoView(44);
        if (logo != null) {
            topBar.getChildren().add(logo);
        } else {
            topBar.getChildren().add(buildLogoFallback());
        }

        Label appName = new Label("PingManager");
        AppTheme.applyCardTitle(appName);

        topBar.getChildren().add(appName);
        return topBar;
    }

    /**
     * Wrapper central pour garantir un vrai centrage vertical et horizontal.
     */
    private BorderPane buildCenterWrapper() {
        BorderPane wrapper = new BorderPane();
        wrapper.setCenter(buildCenterContent());
        return wrapper;
    }

    private VBox buildCenterContent() {
        VBox center = new VBox(AppTheme.SPACE_LG);
        center.setAlignment(Pos.CENTER);
        center.setFillWidth(false);
        center.setPadding(new Insets(10, 0, 10, 0));

        center.getChildren().addAll(
                buildHeroSection(),
                buildCardsSection());

        return center;
    }

    private VBox buildHeroSection() {
        VBox hero = new VBox(AppTheme.SPACE_SM);
        hero.setAlignment(Pos.CENTER);
        hero.setMaxWidth(HERO_MAX_WIDTH);

        Label title = new Label("Bienvenue sur PingManager");
        AppTheme.applyTitle(title);
        title.setAlignment(Pos.CENTER);

        Label subtitle = new Label(
                "La solution desktop pour préparer, organiser et suivre vos tournois de tennis de table.");
        AppTheme.applySubtitle(subtitle);
        subtitle.setAlignment(Pos.CENTER);

        Label hint = new Label(
                "Choisissez votre espace pour accéder aux fonctionnalités adaptées à votre profil.");
        AppTheme.applyBody(hint);
        hint.setAlignment(Pos.CENTER);

        hero.getChildren().addAll(title, subtitle, hint);
        return hero;
    }

    private HBox buildCardsSection() {
        HBox cards = new HBox(18);
        cards.setAlignment(Pos.CENTER);
        cards.setMaxWidth(CARDS_CONTAINER_MAX_WIDTH);

        VBox organizerCard = buildOrganizerCard();
        VBox playerCard = buildPlayerCard();

        configureCardWidth(organizerCard);
        configureCardWidth(playerCard);

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

        VBox card = AppTheme.card(
                title,
                description,
                verticalSpacer(),
                button);
        card.setFillWidth(true);

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

        VBox card = AppTheme.card(
                title,
                description,
                verticalSpacer(),
                button);
        card.setFillWidth(true);

        return card;
    }

    private Label buildLogoFallback() {
        Label fallback = new Label("PM");
        fallback.setStyle(
                "-fx-background-color: " + AppTheme.COLOR_PRIMARY + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: 900;" +
                        "-fx-padding: 10 12;" +
                        "-fx-background-radius: 10;");
        return fallback;
    }

    private void configureCardWidth(VBox card) {
        card.setPrefWidth(CARD_WIDTH);
        card.setMinWidth(CARD_WIDTH);
        card.setMaxWidth(CARD_WIDTH);
        VBox.setVgrow(card, Priority.ALWAYS);
    }

    private Region verticalSpacer() {
        Region region = new Region();
        VBox.setVgrow(region, Priority.ALWAYS);
        return region;
    }
}