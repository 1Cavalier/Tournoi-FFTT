package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.SqliteClubRepository;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.SqliteTableauRepository;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.SqliteTournamentRepository;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.service.OrganizerAuthService;
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
 * Navigator centralise :
 * - la navigation JavaFX (changement de scène, ouverture de dialogs)
 * - la session organisateur en cours
 * - l'accès aux dépendances applicatives utiles aux vues
 *
 * Il ne contient :
 * - aucune initialisation technique de base de données
 * - aucune logique métier
 */
public final class Navigator {

    private static final double DEFAULT_WIDTH = 900;
    private static final double DEFAULT_HEIGHT = 600;

    private static final double DASHBOARD_WIDTH = 1200;
    private static final double DASHBOARD_HEIGHT = 700;

    private final Stage stage;
    private final AppContext ctx;

    private OrganizerAccount currentOrganizer;

    public Navigator(Stage stage, AppContext ctx) {
        this.stage = Objects.requireNonNull(stage, "stage must not be null");
        this.ctx = Objects.requireNonNull(ctx, "app context must not be null");
    }

    // -------------------------------------------------------------------------
    // Accès aux services / repositories
    // -------------------------------------------------------------------------

    public OrganizerAuthService organizerAuth() {
        return ctx.organizerAuthService();
    }

    public SqliteClubRepository clubRepo() {
        return ctx.clubRepo();
    }

    public SqliteTournamentRepository tournamentRepo() {
        return ctx.tournamentRepo();
    }

    public SqliteTableauRepository tableauRepo() {
        return ctx.tableauRepo();
    }

    // -------------------------------------------------------------------------
    // Session organisateur
    // -------------------------------------------------------------------------

    public OrganizerAccount getCurrentOrganizer() {
        return currentOrganizer;
    }

    public boolean isOrganizerLoggedIn() {
        return currentOrganizer != null;
    }

    public void setCurrentOrganizer(OrganizerAccount organizer) {
        this.currentOrganizer = Objects.requireNonNull(organizer, "organizer must not be null");
    }

    public OrganizerAccount requireOrganizerSession() {
        if (currentOrganizer == null) {
            throw new IllegalStateException("Aucun organisateur connecté.");
        }
        return currentOrganizer;
    }

    public void logoutOrganizer() {
        currentOrganizer = null;
        showHome();
    }

    // -------------------------------------------------------------------------
    // Navigation principale
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
        requireOrganizerSession();
        setScene(
                new OrganizerDashboardView(this),
                DASHBOARD_WIDTH,
                DASHBOARD_HEIGHT,
                "PingManager — Tableau de bord organisateur");
    }

    public void showInfo(String title, String message) {

        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);

        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);

        alert.showAndWait();
    }
    // -------------------------------------------------------------------------
    // Dialogs organisateur
    // -------------------------------------------------------------------------

    public void showOrganizerProfileDialog() {
        requireOrganizerSession();

        OrganizerProfileDialog dialog = new OrganizerProfileDialog(this);
        dialog.showAndWait();

        showOrganizerDashboard();
    }

    public void showCreateTournamentDialog() {
        requireOrganizerSession();

        CreateTournamentDialog dialog = new CreateTournamentDialog(this);
        dialog.showAndWait();

        showOrganizerDashboard();
    }

    // -------------------------------------------------------------------------
    // Helpers UI
    // -------------------------------------------------------------------------

    private void setScene(Parent root, double width, double height, String title) {
        Scene scene = new Scene(root, width, height);
        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();
    }
}