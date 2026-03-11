package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.OrganizerDto;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.ClubRepository;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.ClubAccessRepository;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.TournamentRepository;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.service.OrganizerAuthService;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.service.TournamentService;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.auth.OrganizerLoginView;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.auth.OrganizerRegisterView;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.home.HomeView;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.dialogs.CreateTournamentDialog;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.dialogs.OrganizerProfileDialog;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.pages.OrganizerDashboardView;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * Router central de navigation JavaFX.
 */
public final class AppRouter {

    private static final double DEFAULT_WIDTH = 900;
    private static final double DEFAULT_HEIGHT = 600;

    private static final double DASHBOARD_WIDTH = 1200;
    private static final double DASHBOARD_HEIGHT = 700;

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
        setScene(
                new HomeView(this),
                DEFAULT_WIDTH,
                DEFAULT_HEIGHT,
                "PingManager — Accueil");
    }

    public void showOrganizerLogin() {
        setScene(
                new OrganizerLoginView(this),
                DEFAULT_WIDTH,
                DEFAULT_HEIGHT,
                "PingManager — Connexion organisateur");
    }

    public void showOrganizerRegister() {
        setScene(
                new OrganizerRegisterView(this),
                DEFAULT_WIDTH,
                DEFAULT_HEIGHT,
                "PingManager — Inscription organisateur");
    }

    public void showOrganizerDashboard() {

        requireOrganizer();

        setScene(
                new OrganizerDashboardView(this),
                DASHBOARD_WIDTH,
                DASHBOARD_HEIGHT,
                "PingManager — Tableau de bord organisateur");
    }

    // -------------------------------------------------------------------------
    // DIALOGS
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

    // -------------------------------------------------------------------------
    // HELPERS UI
    // -------------------------------------------------------------------------

    public void showInfo(String title, String message) {

        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);

        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);

        alert.showAndWait();
    }

    private void setScene(Parent root, double width, double height, String title) {

        Scene scene = new Scene(root, width, height);

        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();
    }
}