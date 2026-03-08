package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.pages;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.TournamentRow;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.components.TournamentCard;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.components.UiUtils;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.components.TournamentCard.Mode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Objects;

public class OrganizerHomeView extends VBox {

    private final Navigator nav;
    private final OrganizerAccount organizer;

    public OrganizerHomeView(Navigator nav, OrganizerAccount organizer) {
        this.nav = Objects.requireNonNull(nav);
        this.organizer = Objects.requireNonNull(organizer);

        build();
    }

    // ---------------------------------------------------------
    // BUILD
    // ---------------------------------------------------------

    private void build() {

        AppTheme.applyPage(this);

        VBox root = new VBox(AppTheme.SPACE_LG);
        root.setPadding(new Insets(20));
        root.setMaxWidth(Double.MAX_VALUE);

        root.getChildren().add(buildHeader());
        root.getChildren().addAll(buildTournamentSections());
        root.getChildren().add(buildCreateTournamentButton());

        setChildrenAsScroll(root);
    }

    // ---------------------------------------------------------
    // HEADER
    // ---------------------------------------------------------

    private VBox buildHeader() {

        VBox box = new VBox(AppTheme.SPACE_SM);

        Label title = new Label("Tableau de bord");
        AppTheme.applyTitle(title);

        Label subtitle = new Label(
                "Gérez vos tournois, inscriptions et les principales actions d'organisation.");
        AppTheme.applySubtitle(subtitle);

        box.getChildren().addAll(title, subtitle);

        return box;
    }

    // ---------------------------------------------------------
    // SECTIONS TOURNOIS
    // ---------------------------------------------------------

    private List<VBox> buildTournamentSections() {

        List<TournamentRow> active = loadActiveTournaments();
        List<TournamentRow> draft = loadDraftTournaments();

        VBox activeSection = buildSection(
                "Tournois actifs",
                "OPEN / RUNNING",
                active.isEmpty()
                        ? infoBanner("Aucun tournoi publié ou en cours.")
                        : UiUtils.tournamentList(nav, active, TournamentCard.Mode.ACTIVE));

        VBox draftSection = buildSection(
                "Tournois en préparation",
                "DRAFT",
                draft.isEmpty()
                        ? infoBanner("Aucun tournoi en brouillon.")
                        : UiUtils.tournamentList(nav, draft, TournamentCard.Mode.DRAFT));

        return List.of(activeSection, draftSection);
    }

    private List<TournamentRow> loadActiveTournaments() {
        return nav.tournamentRepo().findActiveForOrganizer(organizer.getId());
    }

    private List<TournamentRow> loadDraftTournaments() {
        return nav.tournamentRepo().findDraftForOrganizer(organizer.getId());
    }

    // ---------------------------------------------------------
    // SECTION GENERIQUE
    // ---------------------------------------------------------

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

    // ---------------------------------------------------------
    // CREATE TOURNAMENT BUTTON
    // ---------------------------------------------------------

    private HBox buildCreateTournamentButton() {

        Button button = new Button("Créer un tournoi");
        AppTheme.stylePrimary(button);

        button.setOnAction(e -> nav.showCreateTournamentDialog());

        button.setMaxWidth(260);

        HBox row = new HBox(button);
        row.setAlignment(Pos.CENTER);
        row.setPadding(new Insets(6, 0, 0, 0));

        return row;
    }

    // ---------------------------------------------------------
    // INFO BANNER
    // ---------------------------------------------------------

    private Region infoBanner(String text) {

        Label label = new Label(text);
        AppTheme.applyBody(label);

        VBox box = new VBox(label);

        box.setPadding(new Insets(12));
        box.setStyle(
                "-fx-background-color:" + AppTheme.COLOR_SURFACE + ";" +
                        "-fx-background-radius:" + AppTheme.RADIUS + ";" +
                        "-fx-border-color:" + AppTheme.COLOR_BORDER + ";" +
                        "-fx-border-radius:" + AppTheme.RADIUS + ";");

        return box;
    }

    // ---------------------------------------------------------
    // SCROLL
    // ---------------------------------------------------------

    private void setChildrenAsScroll(VBox content) {

        ScrollPane scroll = new ScrollPane(content);

        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setPadding(Insets.EMPTY);

        getChildren().setAll(scroll);

        VBox.setVgrow(scroll, Priority.ALWAYS);
    }
}