package fr.pingmanager.gestion_tournois_FFTT.ui.javafx.app;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.util.Objects;

import fr.pingmanager.gestion_tournois_FFTT.infra.repo.ClubAccessRepository;
import fr.pingmanager.gestion_tournois_FFTT.infra.repo.ClubRepository;
import fr.pingmanager.gestion_tournois_FFTT.infra.repo.PlayerRepository;
import fr.pingmanager.gestion_tournois_FFTT.infra.repo.TournamentRepository;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.OrganizerDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TournamentDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TournamentRegulationDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.service.OrganizerAuthService;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.service.TournamentService;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.view.auth.OrganizerLoginView;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.view.auth.OrganizerRegisterView;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.view.home.HomeView;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.view.organizer.dialogs.CreateTournamentDialog;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.view.organizer.dialogs.EditTournamentRegulationDialog;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.view.organizer.dialogs.OrganizerProfileDialog;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.view.organizer.layout.TournamentSection;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.view.organizer.pages.OrganizerDashboardView;

/**
 * Router central de navigation JavaFX.
 *
 * Le Stage principal est conservé tout au long de l'application.
 * Les tailles de fenêtre sont gérées par AppTheme.
 */
public final class AppRouter {

    private final Stage stage;
    private final ApplicationContext ctx;
    private final OrganizerSession session;

    public AppRouter(Stage stage, ApplicationContext ctx) {
        this.stage = Objects.requireNonNull(stage);
        this.ctx = Objects.requireNonNull(ctx);
        this.session = new OrganizerSession();
    }

    // -------------------------------------------------------------------------
    // SERVICES
    // -------------------------------------------------------------------------

    public OrganizerAuthService organizerAuth() {
        return ctx.organizerAuthService();
    }

    public TournamentService tournamentService() {
        return ctx.tournamentService();
    }

    // -------------------------------------------------------------------------
    // REPOSITORIES
    // -------------------------------------------------------------------------

    public ClubRepository clubRepo() {
        return ctx.clubRepository();
    }

    public ClubAccessRepository clubAccessRepo() {
        return ctx.clubAccessRepository();
    }

    public TournamentRepository tournamentRepo() {
        return ctx.tournamentRepository();
    }

    public PlayerRepository playerRepo() {
        return ctx.playerRepository();
    }

    // -------------------------------------------------------------------------
    // SESSION
    // -------------------------------------------------------------------------

    public OrganizerSession session() {
        return session;
    }

    public OrganizerDto requireOrganizer() {
        return session.get();
    }

    public void loginOrganizer(OrganizerDto organizer) {
        session.login(organizer);
    }

    public void logoutOrganizer() {
        session.logout();
        showHome();
    }

    // -------------------------------------------------------------------------
    // NAVIGATION
    // -------------------------------------------------------------------------

    public void showHome() {
        setMainScene(
                new HomeView(this),
                "PingManager — Accueil");
    }

    public void showOrganizerLogin() {
        setMainScene(
                new OrganizerLoginView(this),
                "PingManager — Connexion organisateur");
    }

    public void showOrganizerRegister() {
        setMainScene(
                new OrganizerRegisterView(this),
                "PingManager — Inscription organisateur");
    }

    public void showOrganizerDashboard() {
        requireOrganizer();
        setMainScene(
                new OrganizerDashboardView(this),
                "PingManager — Tableau de bord organisateur");
    }

    /**
     * Navigue directement vers une section d'un tournoi.
     * Le contenu principal change sans popup.
     */
    public void showTournamentSection(TournamentDto tournament, TournamentSection section) {
        requireOrganizer();
        setMainScene(
                new OrganizerDashboardView(this, tournament, section),
                "PingManager — " + (section.label() != null ? section.label() : "Tableau de bord"));
    }

    // -------------------------------------------------------------------------
    // DIALOGS (conservés pour compatibilité — appellent la navigation inline)
    // -------------------------------------------------------------------------

    public void showOrganizerProfileDialog() {
        requireOrganizer();
        OrganizerProfileDialog dialog = new OrganizerProfileDialog(this);
        dialog.showAndWait();
        showOrganizerDashboard();
    }

    public void showCreateTournamentDialog() {
        requireOrganizer();
        CreateTournamentDialog dialog = new CreateTournamentDialog(this);
        dialog.showAndWait();
        showOrganizerDashboard();
    }

    /**
     * Ouvre le dialog de modification Général puis revient
     * sur la section Général du tournoi (pas le dashboard global).
     */
    public void showEditTournamentGeneralDialog(TournamentDto tournament) {
        requireOrganizer();
        CreateTournamentDialog dialog = new CreateTournamentDialog(this, tournament);
        dialog.showAndWait();
        // Recharger le tournoi depuis la base pour avoir les données fraîches
        TournamentDto fresh = tournamentService()
                .findById(tournament.id())
                .orElse(tournament);
        showTournamentSection(fresh, TournamentSection.GENERAL);
    }

    /**
     * Ouvre le dialog de modification Règlement puis revient
     * sur la section Règlement du tournoi.
     */
    public void showEditTournamentRegulationDialog(TournamentDto tournament) {
        requireOrganizer();
        TournamentRegulationDto regulation = tournamentService().getRegulation(tournament.id());
        EditTournamentRegulationDialog dialog = new EditTournamentRegulationDialog(this, tournament, regulation);
        dialog.showAndWait();
        showTournamentSection(tournament, TournamentSection.REGLEMENT);
    }

    /**
     * @deprecated Remplacé par showTournamentSection(tournament, TABLEAUX).
     *             Conservé pour compatibilité avec TournamentDashboardCard.
     */
    @Deprecated
    public void showTableauxManagementDialog(TournamentDto tournament) {
        showTournamentSection(tournament, TournamentSection.TABLEAUX);
    }

    // -------------------------------------------------------------------------
    // HELPERS UI
    // -------------------------------------------------------------------------

    public Stage primaryStage() {
        return stage;
    }

    public void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(stage);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void setMainScene(Parent root, String title) {
        Objects.requireNonNull(root, "root");
        Objects.requireNonNull(title, "title");

        Scene currentScene = stage.getScene();
        Scene newScene;

        if (currentScene == null) {
            newScene = new Scene(root);
        } else {
            newScene = new Scene(root, currentScene.getWidth(), currentScene.getHeight());
        }

        stage.setScene(newScene);
        stage.setTitle(title);

        AppTheme.applyMainWindow(stage);
        stage.show();
    }
}