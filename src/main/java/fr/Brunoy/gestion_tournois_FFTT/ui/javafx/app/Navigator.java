package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.db.DbMigrations;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.db.SqliteDb;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.SqliteClubProfileRepository;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.SqliteOrganizerAccountRepository;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.SqliteTableauRepository;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.SqliteTournamentRepository;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.mail.EmailSender;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.mail.EmailVerificationService;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.HomeView;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.OrganizerLoginView;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.OrganizerRegisterView;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.CreateTournamentDialog;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.OrganizerDashboardView;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.OrganizerProfileDialog;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.sql.Connection;

public class Navigator {

    private final Stage stage;

    // --- DB + repos ---
    private final SqliteDb clubDb;
    private final SqliteDb competitionDb;

    private final SqliteOrganizerAccountRepository organizerRepo;
    private final SqliteClubProfileRepository clubProfileRepo;

    private final SqliteTournamentRepository tournamentRepo;
    private final SqliteTableauRepository tableauRepo;

    // --- mail ---
    private final EmailSender emailSender;
    private final EmailVerificationService emailVerification;

    // --- services ---
    private final OrganizerAuthService organizerAuth;

    // --- session ---
    private OrganizerAccount currentOrganizer;

    public Navigator(Stage stage) {
        this.stage = stage;

        // 2 DB files (séparation club/competition)
        Path clubDbFile = Path.of("data", "club.db");
        Path competitionDbFile = Path.of("data", "competition.db");

        this.clubDb = new SqliteDb(clubDbFile);
        this.competitionDb = new SqliteDb(competitionDbFile);

        // Apply schemas (idempotent)
        try (Connection c1 = clubDb.openConnection()) {
            DbMigrations.applySqlResource(c1, "/db/Club.sql");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("DB init failed (Club)", e);
        }

        try (Connection c2 = competitionDb.openConnection()) {
            DbMigrations.applySqlResource(c2, "/db/Competition.sql");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("DB init failed (Competition)", e);
        }

        // Repos
        this.organizerRepo = new SqliteOrganizerAccountRepository(clubDb);
        this.clubProfileRepo = new SqliteClubProfileRepository(clubDb);

        this.tournamentRepo = new SqliteTournamentRepository(competitionDb);
        this.tableauRepo = new SqliteTableauRepository(competitionDb);

        // Mail (dev console)
        this.emailSender = new EmailSender();
        this.emailVerification = new EmailVerificationService(organizerRepo, emailSender);

        // Services
        this.organizerAuth = new OrganizerAuthService(organizerRepo, emailVerification);
    }

    // ---------------- Accessors ----------------

    public OrganizerAuthService organizerAuth() {
        return organizerAuth;
    }

    public SqliteTournamentRepository tournamentRepo() {
        return tournamentRepo;
    }

    public SqliteTableauRepository tableauRepo() {
        return tableauRepo;
    }

    public SqliteClubProfileRepository clubProfileRepo() {
        return clubProfileRepo;
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
        stage.show();
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
}