package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.db.DbMigrations;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.db.SqliteDb;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.migration.OrganizerJsonToSqliteMigration;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.SqliteOrganizerAccountRepository;
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

    private final SqliteDb db;
    private final OrganizerAuthService organizerAuth;

    private OrganizerAccount currentOrganizer;

    

    public Navigator(Stage stage) {
        this.stage = stage;

        // DB file local
        Path dbFile = Path.of("data", "app.db");
        this.db = new SqliteDb(dbFile);

        // migrations
        try (Connection c = db.openConnection()) {
            DbMigrations.applySchema(c);
        } catch (Exception e) {
            throw new RuntimeException("DB init failed", e);
        }

        // repos + migration JSON -> SQLite (une seule fois)
        var organizerRepo = new SqliteOrganizerAccountRepository(db);
        OrganizerJsonToSqliteMigration.importIfNeeded(organizerRepo, Path.of("data", "organizers.json"));

        // services
        this.organizerAuth = new OrganizerAuthService(organizerRepo);
    }

    public OrganizerAuthService organizerAuth() {
        return organizerAuth;
    }

    public void setCurrentOrganizer(OrganizerAccount acc) {
        this.currentOrganizer = acc;
    }

    public OrganizerAccount getCurrentOrganizer() {
        return currentOrganizer;
    }

    public void logoutOrganizer() {
        this.currentOrganizer = null;
        showHome();
    }

    public void showHome() {
        stage.setScene(new Scene(new HomeView(this), 900, 600));
    }

    public void showOrganizerLogin() {
        stage.setScene(new Scene(new OrganizerLoginView(this), 900, 600));
    }

    public void showOrganizerRegister() {
        stage.setScene(new Scene(new OrganizerRegisterView(this), 900, 600));
    }

    public void showOrganizerDashboard() {
        stage.setScene(new Scene(new OrganizerDashboardView(this), 1200, 700));
    }
}
