package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.db.DbMigrations;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.db.SqliteDb;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.mail.ConsoleEmailSender;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.mail.EmailSender;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.mail.EmailVerificationService;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.*;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.service.OrganizerAuthService;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.service.TournamentService;

import java.nio.file.Path;
import java.sql.Connection;

/**
 * Contexte applicatif.
 * Initialise les bases SQLite, repositories et services.
 */
public final class ApplicationContext {

    private final SqliteDb clubDb;
    private final SqliteDb competitionDb;

    private final ClubRepository clubRepository;
    private final ClubAccessRepository clubAccessRepository;
    private final OrganizerRepository organizerRepository;

    private final TournamentRepository tournamentRepository;

    private final EmailSender emailSender;
    private final EmailVerificationService emailVerificationService;

    private final OrganizerAuthService organizerAuthService;
    private final TournamentService tournamentService;

    public ApplicationContext() {

        Path clubDbFile = Path.of("data", "club.db");
        Path competitionDbFile = Path.of("data", "competition.db");

        this.clubDb = new SqliteDb(clubDbFile);
        this.competitionDb = new SqliteDb(competitionDbFile);

        applySql(clubDb, "/db/Club.sql");
        applySql(competitionDb, "/db/Competition.sql");
        applySql(clubDb, "/db/SeedData.sql");

        this.clubRepository = new ClubRepositorySqlite(clubDb);
        this.clubAccessRepository = new ClubAccessRepositorySqlite(clubDb);
        this.organizerRepository = new OrganizerRepositorySqlite(clubDb);

        this.tournamentRepository = new TournamentRepositorySqlite(competitionDb);

        this.emailSender = new ConsoleEmailSender();
        this.emailVerificationService = new EmailVerificationService(
                organizerRepository,
                emailSender);

        this.organizerAuthService = new OrganizerAuthService(
                organizerRepository,
                clubRepository,
                clubAccessRepository,
                emailVerificationService);

        this.tournamentService = new TournamentService(tournamentRepository);
    }

    private void applySql(SqliteDb db, String resourceSqlPath) {
        try (Connection c = db.openConnection()) {
            DbMigrations.applySchema(c, resourceSqlPath);
        } catch (Exception e) {
            throw new RuntimeException("DB init failed: " + resourceSqlPath, e);
        }
    }

    // -------------------------------------------------------------------------
    // SERVICES
    // -------------------------------------------------------------------------

    public OrganizerAuthService organizerAuthService() {
        return organizerAuthService;
    }

    public TournamentService tournamentService() {
        return tournamentService;
    }

    public EmailVerificationService emailVerificationService() {
        return emailVerificationService;
    }

    // -------------------------------------------------------------------------
    // REPOSITORIES
    // -------------------------------------------------------------------------

    public ClubRepository clubRepository() {
        return clubRepository;
    }

    public ClubAccessRepository clubAccessRepository() {
        return clubAccessRepository;
    }

    public OrganizerRepository organizerRepository() {
        return organizerRepository;
    }

    public TournamentRepository tournamentRepository() {
        return tournamentRepository;
    }

    // -------------------------------------------------------------------------
    // MAIL
    // -------------------------------------------------------------------------

    public EmailSender emailSender() {
        return emailSender;
    }
}