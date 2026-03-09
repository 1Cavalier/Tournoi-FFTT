package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.pages;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.AppRouter;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.OrganizerDto;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.TournamentCardDto;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.TournamentDto;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.mapper.TournamentCardMapper;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.components.TournamentCard;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OrganizerDashboardContent extends VBox {

    private final AppRouter nav;
    private final OrganizerDto organizer;

    public OrganizerDashboardContent(AppRouter nav, OrganizerDto organizer) {
        this.nav = Objects.requireNonNull(nav, "nav must not be null");
        this.organizer = Objects.requireNonNull(organizer, "organizer must not be null");
        build();
    }

    private void build() {
        AppTheme.applyPage(this);

        VBox root = new VBox(AppTheme.SPACE_LG);
        root.setPadding(new Insets(20));
        root.setMaxWidth(Double.MAX_VALUE);

        root.getChildren().add(buildWelcomeSection());
        root.getChildren().add(buildClubTournamentsSection());
        root.getChildren().add(buildHelpSection());

        setChildrenAsScroll(root);
    }

    private VBox buildWelcomeSection() {
        VBox content = new VBox(AppTheme.SPACE_SM);

        Label title = new Label(buildWelcomeTitle());
        AppTheme.applyTitle(title);

        Label intro = new Label(
                "Vous vous trouvez actuellement sur la page d'accueil de PingManager, "
                        + "le logiciel de gestion de tournois de tennis de table.");
        AppTheme.applyBody(intro);
        intro.setWrapText(true);

        Label details = new Label(
                "Depuis cet espace, vous pourrez créer et gérer vos tournois, préparer les tableaux, "
                        + "suivre l'organisation générale du club et accéder progressivement aux principales fonctions de gestion.");
        AppTheme.applyBody(details);
        details.setWrapText(true);

        content.getChildren().addAll(title, intro, details);

        VBox card = AppTheme.card(content);
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private String buildWelcomeTitle() {
        String clubName = organizer.getClubName();
        if (clubName == null || clubName.isBlank()) {
            return "Bienvenue sur l'espace organisateur";
        }
        return "Bienvenue sur le compte du club : " + clubName;
    }

    private VBox buildClubTournamentsSection() {
        List<TournamentDto> active = loadActiveTournaments();
        List<TournamentDto> draft = loadDraftTournaments();

        List<TournamentDto> allTournaments = new ArrayList<>();
        allTournaments.addAll(draft);
        allTournaments.addAll(active);

        List<TournamentCardDto> cards = new ArrayList<>();
        for (TournamentDto tournament : allTournaments) {
            cards.add(toCardModel(tournament));
        }

        VBox content = new VBox(AppTheme.SPACE_MD);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Tournois du club");
        AppTheme.applyCardTitle(title);

        Label badge = new Label(String.valueOf(cards.size()));
        badge.setStyle(AppTheme.badgeStyle(AppTheme.COLOR_PRIMARY));

        header.getChildren().addAll(title, badge);
        content.getChildren().add(header);

        if (cards.isEmpty()) {
            content.getChildren().add(buildEmptyTournamentState());
        } else {
            content.getChildren().add(buildTournamentCardList(cards));
        }

        content.getChildren().add(buildCreateTournamentRow());

        VBox card = AppTheme.card(content);
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private TournamentCardDto toCardModel(TournamentDto tournament) {
        var clubOpt = nav.clubRepo().findByOrganizerId(tournament.organizerId());
        var tableaux = nav.tableauRepo().findByTournamentId(tournament.id());

        return TournamentCardMapper.map(tournament, clubOpt, tableaux);
    }

    private VBox buildEmptyTournamentState() {
        VBox box = new VBox(AppTheme.SPACE_SM);
        box.setPadding(new Insets(14));
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle(
                "-fx-background-color:" + AppTheme.COLOR_SURFACE + ";" +
                        "-fx-background-radius:" + AppTheme.RADIUS + ";" +
                        "-fx-border-color:" + AppTheme.COLOR_BORDER + ";" +
                        "-fx-border-radius:" + AppTheme.RADIUS + ";");

        Label title = new Label("Aucun tournoi n'a encore été créé.");
        AppTheme.applyBody(title);

        Label subtitle = new Label(
                "Commencez par créer votre premier tournoi pour démarrer l'organisation du club.");
        AppTheme.applyBody(subtitle);
        subtitle.setWrapText(true);

        box.getChildren().addAll(title, subtitle);
        return box;
    }

    private HBox buildCreateTournamentRow() {
        Button button = new Button("Créer un tournoi");
        AppTheme.stylePrimary(button);
        button.setOnAction(e -> nav.showCreateTournamentDialog());
        button.setMaxWidth(260);

        HBox row = new HBox(button);
        row.setAlignment(Pos.CENTER);
        row.setPadding(new Insets(8, 0, 0, 0));
        return row;
    }

    private List<TournamentDto> loadActiveTournaments() {
        String clubId = nav.clubRepo()
                .findByOrganizerId(organizer.getId())
                .orElseThrow(() -> new IllegalStateException("Club introuvable pour cet organisateur"))
                .id();

        return nav.tournamentRepo().findActiveForClub(clubId);
    }

    private VBox buildTournamentCardList(List<TournamentCardDto> cards) {
        VBox box = new VBox(12);

        for (TournamentCardDto card : cards) {
            box.getChildren().add(new TournamentCard(nav, card));
        }

        return box;
    }

    private List<TournamentDto> loadDraftTournaments() {
        String clubId = nav.clubRepo()
                .findByOrganizerId(organizer.getId())
                .orElseThrow(() -> new IllegalStateException("Club introuvable pour cet organisateur"))
                .id();

        return nav.tournamentRepo().findDraftForClub(clubId);
    }

    private VBox buildHelpSection() {
        VBox content = new VBox(AppTheme.SPACE_MD);

        Label title = new Label("Besoin d'aide ?");
        AppTheme.applyCardTitle(title);

        Label text = new Label(
                "En cas de question, de retour ou de problème, utilisez le bouton ci-dessous pour accéder à l'aide.");
        AppTheme.applyBody(text);
        text.setWrapText(true);

        Button helpButton = new Button("Obtenir de l'aide");
        AppTheme.styleSecondary(helpButton);
        helpButton.setOnAction(e -> onHelp());

        HBox buttonRow = new HBox(helpButton);
        buttonRow.setAlignment(Pos.CENTER_LEFT);

        content.getChildren().addAll(title, text, buttonRow);

        VBox card = AppTheme.card(content);
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private void onHelp() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Aide");
        alert.setHeaderText("Section d'aide");
        alert.setContentText("La section d'aide sera bientôt disponible.");
        alert.showAndWait();
    }

    private void setChildrenAsScroll(VBox content) {
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setPadding(Insets.EMPTY);

        getChildren().setAll(scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
    }
}