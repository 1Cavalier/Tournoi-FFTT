package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.components;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.TournamentRow;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Objects;

public class OrganizerMainContent extends VBox {

    private final Navigator nav;
    private final OrganizerAccount organizer;

    public OrganizerMainContent(Navigator nav, OrganizerAccount organizer) {
        this.nav = Objects.requireNonNull(nav, "nav must not be null");
        this.organizer = Objects.requireNonNull(organizer, "organizer must not be null");
        build();
    }

    private void build() {
        AppTheme.applyPage(this);

        VBox content = new VBox(AppTheme.SPACE_LG);
        content.setPadding(new Insets(18));
        content.setMaxWidth(Double.MAX_VALUE);

        content.getChildren().add(buildHeader());
        content.getChildren().addAll(buildTournamentSections());
        content.getChildren().add(buildCreateButtonRow());

        setChildrenAsScroll(content);
    }

    private VBox buildHeader() {
        VBox header = new VBox(AppTheme.SPACE_SM);

        Label title = new Label("Tableau de bord");
        AppTheme.applyTitle(title);

        Label subtitle = new Label(
                "Gérez vos tournois, vos inscriptions et les principales actions d’organisation.");
        AppTheme.applySubtitle(subtitle);

        header.getChildren().addAll(title, subtitle);
        return header;
    }

    private List<VBox> buildTournamentSections() {
        List<TournamentRow> activeTournaments = loadActiveTournaments();
        List<TournamentRow> draftTournaments = loadDraftTournaments();

        VBox activeSection = buildSection(
                "Tournois actifs",
                "OPEN / RUNNING",
                activeTournaments.isEmpty()
                        ? infoBanner("Aucun tournoi publié ou en cours.")
                        : UiUtils.tournamentList(nav, activeTournaments, TournamentCard.Mode.ACTIVE));

        VBox draftSection = buildSection(
                "Tournois en préparation",
                "DRAFT",
                draftTournaments.isEmpty()
                        ? infoBanner("Aucun tournoi en brouillon.")
                        : UiUtils.tournamentList(nav, draftTournaments, TournamentCard.Mode.DRAFT));

        return List.of(activeSection, draftSection);
    }

    private List<TournamentRow> loadActiveTournaments() {
        return nav.tournamentRepo().findActiveForOrganizer(organizer.getId());
    }

    private List<TournamentRow> loadDraftTournaments() {
        return nav.tournamentRepo().findDraftForOrganizer(organizer.getId());
    }

    private VBox buildSection(String title, String badgeText, Region content) {
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(title);
        AppTheme.applyCardTitle(titleLabel);

        Label badge = new Label(badgeText);
        badge.setStyle(AppTheme.badgeStyle(AppTheme.COLOR_PRIMARY));

        header.getChildren().addAll(titleLabel, badge);

        VBox inner = new VBox(AppTheme.SPACE_MD, header, content);
        VBox card = AppTheme.card(inner);
        card.setMaxWidth(Double.MAX_VALUE);

        return card;
    }

    private HBox buildCreateButtonRow() {
        Button createButton = new Button("Créer un tournoi");
        AppTheme.stylePrimary(createButton);
        createButton.setOnAction(e -> nav.showCreateTournamentDialog());
        createButton.setMaxWidth(260);

        HBox actions = new HBox(createButton);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(4, 0, 0, 0));

        return actions;
    }

    private Region infoBanner(String text) {
        Label label = new Label(text);
        AppTheme.applyBody(label);

        VBox box = new VBox(label);
        box.setPadding(new Insets(12));
        box.setStyle(
                "-fx-background-color: " + AppTheme.COLOR_SURFACE + ";" +
                        "-fx-background-radius: " + AppTheme.RADIUS + ";" +
                        "-fx-border-color: " + AppTheme.COLOR_BORDER + ";" +
                        "-fx-border-radius: " + AppTheme.RADIUS + ";");

        return box;
    }

    private void setChildrenAsScroll(VBox content) {
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPadding(Insets.EMPTY);

        getChildren().setAll(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }
}