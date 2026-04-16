package fr.pingmanager.gestion_tournois_FFTT.ui.javafx.app;

import java.nio.file.Path;
import java.sql.Connection;

import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.infra.db.DbMigrations;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.infra.db.SqliteDb;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.infra.mail.ConsoleEmailSender;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.infra.mail.EmailSender;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.infra.mail.EmailVerificationService;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.infra.repo.ClubAccessRepository;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.infra.repo.ClubAccessRepositorySqlite;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.infra.repo.ClubRepository;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.infra.repo.ClubRepositorySqlite;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.infra.repo.OrganizerRepository;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.infra.repo.OrganizerRepositorySqlite;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.infra.repo.PlayerRepository;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.infra.repo.PlayerRepositorySqlite;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.infra.repo.TableauRepository;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.infra.repo.TableauRepositorySqlite;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.infra.repo.TournamentRegulationRepository;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.infra.repo.TournamentRegulationRepositorySqlite;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.infra.repo.TournamentRepository;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.infra.repo.TournamentRepositorySqlite;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.service.OrganizerAuthService;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.service.TournamentService;

public final class ApplicationContext {

    private final SqliteDb clubDb;
    private final SqliteDb competitionDb;

    private final ClubRepository clubRepository;
    private final ClubAccessRepository clubAccessRepository;
    private final OrganizerRepository organizerRepository;
    private final PlayerRepository playerRepository;

    private final TournamentRepository tournamentRepository;
    private final TournamentRegulationRepository tournamentRegulationRepository;
    private final TableauRepository tableauRepository;

    private final EmailSender emailSender;
    private final EmailVerificationService emailVerificationService;

    private final OrganizerAuthService organizerAuthService;
    private final TournamentService tournamentService;

    public ApplicationContext() {
        Path clubDbFile = Path.of("data", "club.db");
        Path competitionDbFile = Path.of("data", "competition.db");

        this.clubDb = new SqliteDb(clubDbFile);
        this.competitionDb = new SqliteDb(competitionDbFile);

        initClubDatabase();
        initCompetitionDatabase();

        this.clubRepository = new ClubRepositorySqlite(clubDb);
        this.clubAccessRepository = new ClubAccessRepositorySqlite(clubDb);
        this.organizerRepository = new OrganizerRepositorySqlite(clubDb);
        this.playerRepository = new PlayerRepositorySqlite(clubDb);

        this.tournamentRepository = new TournamentRepositorySqlite(competitionDb);
        this.tournamentRegulationRepository = new TournamentRegulationRepositorySqlite(competitionDb);
        this.tableauRepository = new TableauRepositorySqlite(competitionDb);

        this.emailSender = new ConsoleEmailSender();
        this.emailVerificationService = new EmailVerificationService(
                organizerRepository,
                emailSender);

        this.organizerAuthService = new OrganizerAuthService(
                organizerRepository,
                clubRepository,
                clubAccessRepository,
                emailVerificationService);

        this.tournamentService = new TournamentService(
                tournamentRepository,
                tournamentRegulationRepository,
                tableauRepository);
    }

    // -------------------------------------------------------------------------
    // INIT DB
    // -------------------------------------------------------------------------

    private void initClubDatabase() {
        applySql(clubDb, "/db/Club.sql");
        applySql(clubDb, "/db/Player.sql");
        applySql(clubDb, "/db/SeedData.sql");
        applySql(clubDb, "/db/SeedData_Officials.sql");
    }

    private void initCompetitionDatabase() {
        applySql(competitionDb, "/db/competition/tournament.sql");
        applySql(competitionDb, "/db/competition/tournament_regulation.sql");
        applySql(competitionDb, "/db/competition/tournament_policy.sql");
        applySql(competitionDb, "/db/competition/tableau.sql");
        applySql(competitionDb, "/db/competition/tableau_prize_tier.sql");
        applySql(competitionDb, "/db/competition/app_state.sql");
    }

    private void applySql(SqliteDb db, String resourceSqlPath) {
        try (Connection c = db.openConnection()) {
            DbMigrations.applySchema(c, resourceSqlPath);
        } catch (Exception e) {
            throw new RuntimeException("DB init failed: " + resourceSqlPath, e);
        }
    }

    // -------------------------------------------------------------------------
    // GETTERS
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

    public ClubRepository clubRepository() {
        return clubRepository;
    }

    public ClubAccessRepository clubAccessRepository() {
        return clubAccessRepository;
    }

    public OrganizerRepository organizerRepository() {
        return organizerRepository;
    }

    public PlayerRepository playerRepository() {
        return playerRepository;
    }

    public TournamentRepository tournamentRepository() {
        return tournamentRepository;
    }

    public TournamentRegulationRepository tournamentRegulationRepository() {
        return tournamentRegulationRepository;
    }

    public TableauRepository tableauRepository() {
        return tableauRepository;
    }

    public EmailSender emailSender() {
        return emailSender;
    }
}