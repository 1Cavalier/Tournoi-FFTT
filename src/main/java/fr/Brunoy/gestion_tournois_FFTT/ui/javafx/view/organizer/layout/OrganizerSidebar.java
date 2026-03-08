package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.layout;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.SqliteClubRepository;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.TournamentRow;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.components.UiUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class OrganizerSidebar extends VBox {

    private final Navigator nav;
    private final OrganizerAccount organizer;

    public OrganizerSidebar(Navigator nav, OrganizerAccount organizer) {
        this.nav = Objects.requireNonNull(nav, "nav must not be null");
        this.organizer = Objects.requireNonNull(organizer, "organizer must not be null");

        setPrefWidth(AppTheme.SIDEBAR_WIDTH);
        setMinWidth(AppTheme.SIDEBAR_WIDTH);
        setStyle(AppTheme.SIDEBAR_STYLE);

        build();
    }

    private void build() {
        Optional<SqliteClubRepository.ClubRow> clubOpt = nav.clubRepo().findByOrganizerId(organizer.getId());

        VBox content = new VBox(AppTheme.SPACE_LG);
        content.setPadding(new Insets(18, 16, 18, 16));

        VBox profileSection = buildProfileSection(clubOpt);
        Button homeButton = buildHomeButton();
        VBox tournamentsSection = buildTournamentsSection();
        VBox bottomSection = buildBottomSection();

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        content.getChildren().addAll(
                profileSection,
                homeButton,
                tournamentsSection,
                spacer,
                bottomSection);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setPadding(Insets.EMPTY);

        getChildren().setAll(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }

    // -------------------------------------------------------------------------
    // PROFILE SECTION
    // -------------------------------------------------------------------------

    private VBox buildProfileSection(Optional<SqliteClubRepository.ClubRow> clubOpt) {
        VBox section = new VBox(AppTheme.SPACE_MD);

        StackPane clubLogo = buildLogo(clubOpt.map(SqliteClubRepository.ClubRow::logoPath).orElse(null));

        String clubName = clubOpt.map(SqliteClubRepository.ClubRow::clubName).orElse("(club non renseigné)");
        Label clubNameLabel = new Label(UiUtils.safe(clubName));
        AppTheme.applySidebarPrimaryText(clubNameLabel);
        clubNameLabel.setAlignment(Pos.CENTER);
        clubNameLabel.setMaxWidth(Double.MAX_VALUE);

        Label emailLabel = new Label(UiUtils.safe(organizer.getEmail()));
        AppTheme.applySidebarMutedText(emailLabel);
        emailLabel.setAlignment(Pos.CENTER);
        emailLabel.setMaxWidth(Double.MAX_VALUE);

        Button infosClubButton = new Button("Infos club");
        AppTheme.styleSidebarLinkButton(infosClubButton);
        infosClubButton.setOnAction(e -> UiUtils.info("Infos club", buildClubInfoText(clubOpt)));

        Button editProfileButton = new Button("Modifier le profil");
        AppTheme.styleSidebarSecondaryButton(editProfileButton);
        editProfileButton.setOnAction(e -> nav.showOrganizerProfileDialog());

        VBox profileCard = new VBox(12, clubLogo, clubNameLabel, emailLabel, infosClubButton, editProfileButton);
        profileCard.setAlignment(Pos.CENTER);
        profileCard.setPadding(new Insets(16));
        profileCard.setStyle(AppTheme.SIDEBAR_PANEL_STYLE);

        section.getChildren().add(profileCard);
        return section;
    }

    // -------------------------------------------------------------------------
    // Accueil SECTION
    // -------------------------------------------------------------------------

    private Button buildHomeButton() {
        Button homeButton = new Button("Accueil");
        AppTheme.styleSidebarSecondaryButton(homeButton);
        homeButton.setOnAction(e -> nav.showOrganizerDashboard());
        return homeButton;
    }

    // -------------------------------------------------------------------------
    // TOURNAMENTS SECTION
    // -------------------------------------------------------------------------

    private VBox buildTournamentsSection() {
        VBox section = new VBox(10);

        Label title = new Label("Tournois");
        AppTheme.applySidebarSectionTitle(title);

        VBox tournamentsBox = new VBox(8);

        List<TournamentRow> draft = nav.tournamentRepo().findDraftForOrganizer(organizer.getId());
        List<TournamentRow> active = nav.tournamentRepo().findActiveForOrganizer(organizer.getId());

        List<TournamentRow> visibleTournaments = new java.util.ArrayList<>();
        visibleTournaments.addAll(draft);
        visibleTournaments.addAll(active);

        if (visibleTournaments.isEmpty()) {
            Label empty = new Label("Aucun tournoi actif n'est disponible.");
            AppTheme.applySidebarMutedText(empty);

            VBox emptyBox = new VBox(empty);
            emptyBox.setPadding(new Insets(10, 12, 10, 12));
            emptyBox.setStyle(AppTheme.SIDEBAR_INFO_BOX_STYLE);

            tournamentsBox.getChildren().add(emptyBox);
        } else {
            for (TournamentRow row : visibleTournaments) {
                tournamentsBox.getChildren().add(buildTournamentBlock(row));
            }
        }

        section.getChildren().addAll(title, tournamentsBox);
        return section;
    }

    private VBox buildTournamentBlock(TournamentRow tournament) {
        VBox block = new VBox(6);
        block.setStyle(AppTheme.SIDEBAR_PANEL_STYLE);
        block.setPadding(new Insets(10));

        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label tournamentName = new Label(UiUtils.safe(tournament.name()));
        tournamentName.setStyle(AppTheme.SIDEBAR_PRIMARY_TEXT_STYLE);
        tournamentName.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(tournamentName, Priority.ALWAYS);

        Label statusBadge = new Label(resolveTournamentStatusText(tournament));
        statusBadge.setStyle(AppTheme.tournamentStatusBadgeStyle(resolveTournamentStatusText(tournament)));

        titleRow.getChildren().addAll(tournamentName, statusBadge);

        VBox submenu = new VBox(6);
        submenu.getChildren().addAll(
                buildTournamentMenuItem("Général"),
                buildTournamentMenuItem("Règlement"),
                buildTournamentMenuItem("Inscriptions"),
                buildTournamentMenuItem("Tableaux"),
                buildTournamentMenuItem("Résultats"));

        block.getChildren().addAll(titleRow, submenu);
        return block;
    }

    private Button buildTournamentMenuItem(String label) {
        Button button = new Button(label);
        AppTheme.styleSidebarTournamentItem(button, false);

        button.setOnAction(e -> UiUtils.info(
                "Fonction à venir",
                "La section \"" + label + "\" sera reliée à sa vue dédiée dans une prochaine étape."));

        return button;
    }

    private String resolveTournamentStatusText(TournamentRow tournament) {
        String status = UiUtils.safe(tournament.status());
        if (!status.isBlank()) {
            return status.toUpperCase();
        }

        return "DRAFT";
    }

    // -------------------------------------------------------------------------
    // BOTTOM SECTION
    // -------------------------------------------------------------------------

    private VBox buildBottomSection() {
        VBox box = new VBox(10);

        Button networkButton = new Button("Connexion réseau");
        AppTheme.styleSidebarSecondaryButton(networkButton);
        networkButton.setOnAction(e -> UiUtils.info(
                "Connexion réseau",
                "La connexion réseau sera ajoutée dans une prochaine étape."));

        Button logoutButton = new Button("Déconnexion");
        AppTheme.styleSidebarSecondaryButton(logoutButton);
        logoutButton.setOnAction(e -> nav.logoutOrganizer());

        box.getChildren().addAll(networkButton, logoutButton);
        return box;
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private String buildClubInfoText(Optional<SqliteClubRepository.ClubRow> clubOpt) {
        if (clubOpt.isEmpty()) {
            return "Club : introuvable";
        }

        SqliteClubRepository.ClubRow c = clubOpt.get();

        return ""
                + "N° club : " + UiUtils.nvl(c.clubNumber()) + "\n"
                + "Nom club : " + UiUtils.nvl(c.clubName()) + "\n"
                + "Département : " + UiUtils.nvl(c.departementCode()) + "\n"
                + "Ville : " + UiUtils.nvl(c.city()) + "\n"
                + "Adresse 1 : " + UiUtils.nvl(c.address1()) + "\n"
                + "Adresse 2 : " + UiUtils.nvl(c.address2()) + "\n"
                + "Latitude : " + (c.latitude() == null ? "—" : c.latitude()) + "\n"
                + "Longitude : " + (c.longitude() == null ? "—" : c.longitude()) + "\n"
                + "Responsable : " + UiUtils.fullNameOrDash(c.contactFirstName(), c.contactLastName()) + "\n"
                + "Email officiel : " + UiUtils.nvl(c.officialContactEmail());
    }

    private StackPane buildLogo(String logoPath) {
        double size = 86;

        StackPane container = new StackPane();
        container.setPrefSize(size, size);
        container.setMaxSize(size, size);
        container.setStyle(
                "-fx-background-color: rgba(255,255,255,0.12);" +
                        "-fx-background-radius: 999;" +
                        "-fx-border-color: rgba(255,255,255,0.10);" +
                        "-fx-border-radius: 999;");

        if (logoPath != null && !logoPath.isBlank()) {
            try {
                ImageView iv = new ImageView(new Image("file:" + logoPath, size, size, true, true));
                iv.setFitWidth(size);
                iv.setFitHeight(size);
                iv.setClip(new Circle(size / 2, size / 2, size / 2));
                container.getChildren().add(iv);
                return container;
            } catch (Exception ignored) {
            }
        }

        ImageView fallbackLogo = AppTheme.logoView(56);
        if (fallbackLogo != null) {
            container.getChildren().add(fallbackLogo);
            return container;
        }

        Label placeholder = new Label("LOGO");
        placeholder.setStyle("-fx-font-weight: 900; -fx-text-fill: rgba(255,255,255,0.72);");
        container.getChildren().add(placeholder);
        return container;
    }
}