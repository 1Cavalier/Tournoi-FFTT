package fr.pingmanager.gestion_tournois_FFTT.ui.javafx.view.organizer.layout;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.app.AppRouter;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.ClubDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.OrganizerDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TournamentDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.view.organizer.components.OrganizerViewUtils;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.view.organizer.pages.OrganizerDashboardView;

public class OrganizerSidebar extends VBox {

    private final AppRouter nav;
    private final OrganizerDto organizer;

    /** Tournoi actuellement actif (peut être null = accueil). */
    private final TournamentDto activeTournament;

    /** Section actuellement active. */
    private final TournamentSection activeSection;

    /** Référence vers la vue parente pour déclencher la navigation inline. */
    private final OrganizerDashboardView dashboard;

    // -------------------------------------------------------------------------
    // CONSTRUCTEUR
    // -------------------------------------------------------------------------

    public OrganizerSidebar(AppRouter nav,
            OrganizerDto organizer,
            TournamentDto activeTournament,
            TournamentSection activeSection,
            OrganizerDashboardView dashboard) {
        this.nav = Objects.requireNonNull(nav);
        this.organizer = Objects.requireNonNull(organizer);
        this.activeTournament = activeTournament;
        this.activeSection = activeSection != null ? activeSection : TournamentSection.HOME;
        this.dashboard = Objects.requireNonNull(dashboard);

        setPrefWidth(AppTheme.SIDEBAR_WIDTH);
        setMinWidth(AppTheme.SIDEBAR_WIDTH);
        setStyle(AppTheme.SIDEBAR_STYLE);

        build();
    }

    // -------------------------------------------------------------------------
    // CONSTRUCTION
    // -------------------------------------------------------------------------

    private void build() {
        Optional<ClubDto> clubOpt = nav.clubRepo().findByOrganizerId(organizer.getId());

        VBox content = new VBox(AppTheme.SPACE_LG);
        content.setPadding(new Insets(18, 16, 18, 16));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        content.getChildren().addAll(
                buildProfileSection(clubOpt),
                buildHomeButton(),
                buildTournamentsSection(),
                spacer,
                buildBottomSection());

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setPadding(Insets.EMPTY);

        getChildren().setAll(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }

    // ---- Profil ----

    private VBox buildProfileSection(Optional<ClubDto> clubOpt) {
        VBox section = new VBox(AppTheme.SPACE_MD);

        StackPane clubLogo = buildLogo(clubOpt.map(ClubDto::logoPath).orElse(null));

        String clubName = clubOpt.map(ClubDto::clubName).orElse("(club non renseigné)");
        Label clubNameLabel = new Label(OrganizerViewUtils.safe(clubName));
        AppTheme.applySidebarPrimaryText(clubNameLabel);
        clubNameLabel.setAlignment(Pos.CENTER);
        clubNameLabel.setMaxWidth(Double.MAX_VALUE);

        Label emailLabel = new Label(OrganizerViewUtils.safe(organizer.getEmail()));
        AppTheme.applySidebarMutedText(emailLabel);
        emailLabel.setAlignment(Pos.CENTER);
        emailLabel.setMaxWidth(Double.MAX_VALUE);

        Button infosClubButton = new Button("Infos club");
        AppTheme.styleSidebarLinkButton(infosClubButton);
        infosClubButton.setOnAction(e -> nav.showInfo("Infos club", buildClubInfoText(clubOpt)));

        Button editProfileButton = new Button("Modifier le profil");
        AppTheme.styleSidebarSecondaryButton(editProfileButton);
        editProfileButton.setOnAction(e -> nav.showOrganizerProfileDialog());

        VBox profileCard = new VBox(12, clubLogo, clubNameLabel, emailLabel,
                infosClubButton, editProfileButton);
        profileCard.setAlignment(Pos.CENTER);
        profileCard.setPadding(new Insets(16));
        profileCard.setStyle(AppTheme.SIDEBAR_PANEL_STYLE);

        section.getChildren().add(profileCard);
        return section;
    }

    // ---- Bouton Accueil ----

    private Button buildHomeButton() {
        boolean isHome = activeSection == TournamentSection.HOME;
        Button homeButton = new Button("Accueil");
        AppTheme.styleSidebarTournamentItem(homeButton, isHome);
        homeButton.setOnAction(e -> dashboard.navigateTo(null, TournamentSection.HOME));
        return homeButton;
    }

    // ---- Section Tournois ----

    private VBox buildTournamentsSection() {
        VBox section = new VBox(10);

        Label title = new Label("Tournois");
        AppTheme.applySidebarSectionTitle(title);

        VBox tournamentsBox = new VBox(8);

        String clubId = nav.clubRepo()
                .findByOrganizerId(organizer.getId())
                .orElseThrow(() -> new IllegalStateException("Club introuvable"))
                .id();

        List<TournamentDto> visibleTournaments = new ArrayList<>();
        visibleTournaments.addAll(nav.tournamentService().findDraftForClub(clubId));
        visibleTournaments.addAll(nav.tournamentService().findActiveForClub(clubId));

        if (visibleTournaments.isEmpty()) {
            Label empty = new Label("Aucun tournoi disponible.");
            AppTheme.applySidebarMutedText(empty);
            VBox emptyBox = new VBox(empty);
            emptyBox.setPadding(new Insets(10, 12, 10, 12));
            emptyBox.setStyle(AppTheme.SIDEBAR_INFO_BOX_STYLE);
            tournamentsBox.getChildren().add(emptyBox);
        } else {
            for (TournamentDto t : visibleTournaments) {
                tournamentsBox.getChildren().add(buildTournamentBlock(t));
            }
        }

        section.getChildren().addAll(title, tournamentsBox);
        return section;
    }

    /**
     * Bloc d'un tournoi dans la sidebar avec ses 6 sections.
     * Le tournoi actif est mis en évidence. La section active est surlignée.
     */
    private VBox buildTournamentBlock(TournamentDto tournament) {
        boolean isActiveTournament = activeTournament != null
                && tournament.id().equals(activeTournament.id());

        VBox block = new VBox(8);
        // Bloc légèrement plus lumineux si c'est le tournoi actif
        block.setStyle(isActiveTournament
                ? AppTheme.SIDEBAR_PANEL_STYLE + "-fx-border-color: rgba(255,255,255,0.25); -fx-border-radius: 8;"
                : AppTheme.SIDEBAR_PANEL_STYLE);
        block.setPadding(new Insets(10));

        Label tournamentName = new Label(OrganizerViewUtils.safe(tournament.name()));
        tournamentName.setStyle(AppTheme.SIDEBAR_PRIMARY_TEXT_STYLE + "-fx-font-weight: 700;");
        tournamentName.setWrapText(true);
        tournamentName.setMaxWidth(Double.MAX_VALUE);

        VBox submenu = new VBox(4);
        submenu.getChildren().addAll(
                buildSectionItem(tournament, TournamentSection.GENERAL),
                buildSectionItem(tournament, TournamentSection.REGLEMENT),
                buildSectionItem(tournament, TournamentSection.TABLEAUX),
                buildSectionItem(tournament, TournamentSection.DOCUMENTS),
                buildSectionItemDisabled(TournamentSection.INSCRIPTIONS),
                buildSectionItemDisabled(TournamentSection.DIRECT));

        block.getChildren().addAll(tournamentName, submenu);
        return block;
    }

    /**
     * Bouton de section disponible et cliquable.
     * Mis en surbrillance si c'est la section active du tournoi actif.
     */
    private Button buildSectionItem(TournamentDto tournament, TournamentSection section) {
        boolean isActive = activeTournament != null
                && tournament.id().equals(activeTournament.id())
                && activeSection == section;

        String icon = switch (section) {
            case GENERAL -> "⚙  ";
            case REGLEMENT -> "📜  ";
            case TABLEAUX -> "🏓  ";
            case DOCUMENTS -> "📄  ";
            default -> "   ";
        };

        Button btn = new Button(icon + section.label());
        AppTheme.styleSidebarTournamentItem(btn, isActive);
        btn.setOnAction(e -> dashboard.navigateTo(tournament, section));
        return btn;
    }

    /**
     * Bouton de section non encore disponible — grisé, non cliquable.
     */
    private Button buildSectionItemDisabled(TournamentSection section) {
        String icon = switch (section) {
            case INSCRIPTIONS -> "👥  ";
            case DIRECT -> "📡  ";
            default -> "   ";
        };

        Button btn = new Button(icon + section.label() + " (à venir)");
        btn.setDisable(true);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(
                "-fx-background-color: transparent;"
                        + "-fx-text-fill: rgba(255,255,255,0.35);"
                        + "-fx-font-size: 12px;"
                        + "-fx-padding: 5 10 5 10;"
                        + "-fx-cursor: default;");
        Tooltip.install(btn, new Tooltip("Cette section sera disponible dans une prochaine version."));
        return btn;
    }

    // ---- Section bas de page ----

    private VBox buildBottomSection() {
        VBox box = new VBox(10);

        Button networkButton = new Button("Connexion réseau");
        AppTheme.styleSidebarSecondaryButton(networkButton);
        networkButton.setOnAction(e -> nav.showInfo(
                "Connexion réseau", "Disponible dans une prochaine version."));

        Button logoutButton = new Button("Déconnexion");
        AppTheme.styleSidebarSecondaryButton(logoutButton);
        logoutButton.setOnAction(e -> nav.logoutOrganizer());

        box.getChildren().addAll(networkButton, logoutButton);
        return box;
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private String buildClubInfoText(Optional<ClubDto> clubOpt) {
        if (clubOpt.isEmpty())
            return "Club : introuvable";
        ClubDto c = clubOpt.get();
        return "N° club : " + OrganizerViewUtils.nvl(c.clubNumber()) + "\n"
                + "Nom club : " + OrganizerViewUtils.nvl(c.clubName()) + "\n"
                + "Département : " + OrganizerViewUtils.nvl(c.departementCode()) + "\n"
                + "Ville : " + OrganizerViewUtils.nvl(c.city()) + "\n"
                + "Adresse 1 : " + OrganizerViewUtils.nvl(c.address1()) + "\n"
                + "Adresse 2 : " + OrganizerViewUtils.nvl(c.address2()) + "\n"
                + "Responsable : " + OrganizerViewUtils.fullNameOrDash(c.contactFirstName(), c.contactLastName());
    }

    private StackPane buildLogo(String logoPath) {
        double size = 86;
        StackPane container = new StackPane();
        container.setPrefSize(size, size);
        container.setMaxSize(size, size);
        container.setStyle(
                "-fx-background-color: rgba(255,255,255,0.12);"
                        + "-fx-background-radius: 999;"
                        + "-fx-border-color: rgba(255,255,255,0.10);"
                        + "-fx-border-radius: 999;");

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