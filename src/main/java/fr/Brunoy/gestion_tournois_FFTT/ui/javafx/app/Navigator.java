package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.SqliteClubRepository;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.SqliteTableauRepository;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.SqliteTournamentRepository;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.auth.OrganizerLoginView;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.auth.OrganizerRegisterView;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.home.HomeView;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.OrganizerDashboardView;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.OrganizerProfileDialog;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.tournament.CreateTournamentDialog;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Navigator :
 * - Gère la navigation JavaFX (changement de Scene, ouverture des dialogs)
 * - Gère une session simple (organizer connecté)
 * - Expose AppContext aux vues via des getters
 *
 * Important :
 * - Aucun code d'initialisation DB ici (c'est dans AppContext)
 */
public final class Navigator {

    private static final double DEFAULT_WIDTH = 900;
    private static final double DEFAULT_HEIGHT = 600;

    private final Stage stage;
    private final AppContext ctx;

    // Session (simple)
    private OrganizerAccount currentOrganizer;

    public Navigator(Stage stage, AppContext ctx) {
        this.stage = stage;
        this.ctx = ctx;
    }

    // --------- Accès aux services/repos (via AppContext) ---------

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

    // --------- Session ---------

    public OrganizerAccount getCurrentOrganizer() {
        return currentOrganizer;
    }

    public void setCurrentOrganizer(OrganizerAccount organizer) {
        this.currentOrganizer = organizer;
    }

    public void logoutOrganizer() {
        this.currentOrganizer = null;
        showHome();
    }

    // --------- Navigation ---------

    public void showHome() {
        setScene(new HomeView(this), DEFAULT_WIDTH, DEFAULT_HEIGHT, "Tournoi FFTT — Accueil");
    }

    public void showOrganizerLogin() {
        setScene(new OrganizerLoginView(this), DEFAULT_WIDTH, DEFAULT_HEIGHT, "Tournoi FFTT — Connexion Organisme");
    }

    public void showOrganizerRegister() {
        setScene(new OrganizerRegisterView(this), DEFAULT_WIDTH, DEFAULT_HEIGHT,
                "Tournoi FFTT — Inscription Organisme");
    }

    public void showOrganizerDashboard() {
        setScene(new OrganizerDashboardView(this), 1200, 700, "Tournoi FFTT — Dashboard Organisme");
    }

    public void showOrganizerProfileDialog() {
        OrganizerProfileDialog dialog = new OrganizerProfileDialog(this);
        dialog.showAndWait();
        showOrganizerDashboard();
    }

    public void showCreateTournamentDialog() {
        CreateTournamentDialog dialog = new CreateTournamentDialog(this);
        dialog.showAndWait();
        showOrganizerDashboard();
    }

    private void setScene(Parent root, double w, double h, String title) {
        stage.setScene(new Scene(root, w, h));
        stage.setTitle(title);
        stage.show();
    }
}