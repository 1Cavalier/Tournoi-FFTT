package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.db.DbMigrations;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.db.SqliteDb;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.migration.OrganizerJsonToSqliteMigration;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.SqliteOrganizerAccountRepository;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.SqliteTableauRepository;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.SqliteTournamentRepository;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.HomeView;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.OrganizerLoginView;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.OrganizerRegisterView;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.dashboard.OrganizerDashboardView;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.sql.Connection;

public class Navigator {

    private final Stage stage;

    // --- DB + repos ---
    private final SqliteDb db;
    private final SqliteTournamentRepository tournamentRepo;
    private final SqliteTableauRepository tableauRepo;

    // --- services ---
    private final OrganizerAuthService organizerAuth;

    // --- session ---
    private OrganizerAccount currentOrganizer;

    public Navigator(Stage stage) {
        this.stage = stage;

        // 1) DB local file
        Path dbFile = Path.of("data", "app.db");
        this.db = new SqliteDb(dbFile);

        // 2) Apply schema (idempotent)
        try (Connection c = db.openConnection()) {
            DbMigrations.applySchema(c);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("DB init failed", e);
        }

        // 3) Repositories
        var organizerRepo = new SqliteOrganizerAccountRepository(db);
        this.tournamentRepo = new SqliteTournamentRepository(db);
        this.tableauRepo = new SqliteTableauRepository(db);

        // 4) One-time migration JSON -> SQLite (only if organizer table empty)
        try {
            OrganizerJsonToSqliteMigration.importIfNeeded(organizerRepo, Path.of("data", "organizers.json"));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        // 5) Services
        this.organizerAuth = new OrganizerAuthService(organizerRepo);
    }

    // ---------------- Services / Repos accessors ----------------

    public OrganizerAuthService organizerAuth() {
        return organizerAuth;
    }

    public SqliteTournamentRepository tournamentRepo() {
        return tournamentRepo;
    }

    public SqliteTableauRepository tableauRepo() {
        return tableauRepo;
    }

    // ---------------- Session ----------------

    public OrganizerAccount getCurrentOrganizer() {
        return currentOrganizer;
    }

    public void setCurrentOrganizer(OrganizerAccount currentOrganizer) {
        this.currentOrganizer = currentOrganizer;
    }

    public void logoutOrganizer() {
        this.currentOrganizer = null;
        showHome();
    }

    // ---------------- Navigation ----------------

    public void showHome() {
        stage.setScene(new Scene(new HomeView(this), 900, 600));
        stage.setTitle("Tournoi FFTT — Accueil");
    }

    public void showOrganizerLogin() {
        stage.setScene(new Scene(new OrganizerLoginView(this), 900, 600));
        stage.setTitle("Tournoi FFTT — Connexion Organisme");
    }

    public void showOrganizerRegister() {
        stage.setScene(new Scene(new OrganizerRegisterView(this), 900, 600));
        stage.setTitle("Tournoi FFTT — Inscription Organisme");
    }

    public void showOrganizerDashboard() {
        stage.setScene(new Scene(new OrganizerDashboardView(this), 1200, 700));
        stage.setTitle("Tournoi FFTT — Dashboard Organisme");
    }
}
