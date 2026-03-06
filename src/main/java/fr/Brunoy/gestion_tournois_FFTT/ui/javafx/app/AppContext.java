package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.db.DbMigrations;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.db.SqliteDb;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.SqliteClubRepository;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.SqliteOrganizerAccountRepository;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.SqliteTableauRepository;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.SqliteTournamentRepository;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.mail.ConsoleEmailSender;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.mail.EmailSender;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.mail.EmailVerificationService;

import java.nio.file.Path;
import java.sql.Connection;

/**
 * AppContext est responsable de l'initialisation technique :
 * - Bases SQLite
 * - Migrations SQL
 * - Seed de données locales
 * - Repositories
 * - Services (auth, email)
 *
 * Objectif :
 * - Navigator ne fait plus l'initialisation
 * - Les vues accèdent aux dépendances via Navigator -> AppContext
 */
public final class AppContext {

    private final SqliteDb clubDb;
    private final SqliteDb competitionDb;

    private final SqliteOrganizerAccountRepository organizerAccountRepo;
    private final SqliteClubRepository clubRepo;

    private final SqliteTournamentRepository tournamentRepo;
    private final SqliteTableauRepository tableauRepo;

    private final EmailSender emailSender;
    private final EmailVerificationService emailVerificationService;

    private final OrganizerAuthService organizerAuthService;

    public AppContext() {
        // Localisation des fichiers DB
        Path clubDbFile = Path.of("data", "club.db");
        Path competitionDbFile = Path.of("data", "competition.db");

        // Initialisation des accès DB
        this.clubDb = new SqliteDb(clubDbFile);
        this.competitionDb = new SqliteDb(competitionDbFile);

        // Migrations / schémas
        applySql(clubDb, "/db/Club.sql", "Club");
        applySql(competitionDb, "/db/Competition.sql", "Competition");

        // Données locales de référence (seed)
        applySql(clubDb, "/db/SeedData.sql", "SeedData");

        // Repositories
        this.organizerAccountRepo = new SqliteOrganizerAccountRepository(clubDb);
        this.clubRepo = new SqliteClubRepository(clubDb);
        this.tournamentRepo = new SqliteTournamentRepository(competitionDb);
        this.tableauRepo = new SqliteTableauRepository(competitionDb);

        // Email
        this.emailSender = new ConsoleEmailSender();
        this.emailVerificationService = new EmailVerificationService(organizerAccountRepo, emailSender);

        // Services
        this.organizerAuthService = new OrganizerAuthService(
                organizerAccountRepo,
                clubRepo,
                emailVerificationService);
    }

    private void applySql(SqliteDb db, String resourceSqlPath, String label) {
        try (Connection c = db.openConnection()) {
            DbMigrations.applySchema(c, resourceSqlPath);
        } catch (Exception e) {
            throw new RuntimeException("DB init failed (" + label + ")", e);
        }
    }

    // --------- Getters ---------

    public OrganizerAuthService organizerAuthService() {
        return organizerAuthService;
    }

    public SqliteClubRepository clubRepo() {
        return clubRepo;
    }

    public SqliteTournamentRepository tournamentRepo() {
        return tournamentRepo;
    }

    public SqliteTableauRepository tableauRepo() {
        return tableauRepo;
    }

    public SqliteOrganizerAccountRepository organizerAccountRepo() {
        return organizerAccountRepo;
    }

    public EmailVerificationService emailVerificationService() {
        return emailVerificationService;
    }

    public EmailSender emailSender() {
        return emailSender;
    }
}