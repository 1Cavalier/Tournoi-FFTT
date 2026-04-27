package fr.pingmanager.gestion_tournois_FFTT.ui.javafx.app;

import java.nio.file.Path;
import java.sql.Connection;

import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.bracket.BracketBuilder;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.service.PoolPhaseService;
import fr.pingmanager.gestion_tournois_FFTT.infra.db.DbMigrations;
import fr.pingmanager.gestion_tournois_FFTT.infra.db.SqliteDb;
import fr.pingmanager.gestion_tournois_FFTT.infra.mail.ConsoleEmailSender;
import fr.pingmanager.gestion_tournois_FFTT.infra.mail.EmailSender;
import fr.pingmanager.gestion_tournois_FFTT.infra.mail.EmailVerificationService;
import fr.pingmanager.gestion_tournois_FFTT.infra.repo.ClassificationBracketRepository;
import fr.pingmanager.gestion_tournois_FFTT.infra.repo.ClassificationBracketRepositorySqlite;
import fr.pingmanager.gestion_tournois_FFTT.infra.repo.ClubAccessRepository;
import fr.pingmanager.gestion_tournois_FFTT.infra.repo.ClubAccessRepositorySqlite;
import fr.pingmanager.gestion_tournois_FFTT.infra.repo.ClubRepository;
import fr.pingmanager.gestion_tournois_FFTT.infra.repo.ClubRepositorySqlite;
import fr.pingmanager.gestion_tournois_FFTT.infra.repo.KoBracketRepository;
import fr.pingmanager.gestion_tournois_FFTT.infra.repo.KoBracketRepositorySqlite;
import fr.pingmanager.gestion_tournois_FFTT.infra.repo.OrganizerRepository;
import fr.pingmanager.gestion_tournois_FFTT.infra.repo.OrganizerRepositorySqlite;
import fr.pingmanager.gestion_tournois_FFTT.infra.repo.ParticipantResolver;
import fr.pingmanager.gestion_tournois_FFTT.infra.repo.PlayerRepository;
import fr.pingmanager.gestion_tournois_FFTT.infra.repo.PlayerRepositorySqlite;
import fr.pingmanager.gestion_tournois_FFTT.infra.repo.PouleRepository;
import fr.pingmanager.gestion_tournois_FFTT.infra.repo.PouleRepositorySqlite;
import fr.pingmanager.gestion_tournois_FFTT.infra.repo.TableauRepository;
import fr.pingmanager.gestion_tournois_FFTT.infra.repo.TableauRepositorySqlite;
import fr.pingmanager.gestion_tournois_FFTT.infra.repo.TournamentRegulationRepository;
import fr.pingmanager.gestion_tournois_FFTT.infra.repo.TournamentRegulationRepositorySqlite;
import fr.pingmanager.gestion_tournois_FFTT.infra.repo.TournamentRepository;
import fr.pingmanager.gestion_tournois_FFTT.infra.repo.TournamentRepositorySqlite;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.service.OrganizerAuthService;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.service.TournamentService;

public final class ApplicationContext {

    private final SqliteDb clubDb;
    private final SqliteDb competitionDb;

    // Repos club
    private final ClubRepository clubRepository;
    private final ClubAccessRepository clubAccessRepository;
    private final OrganizerRepository organizerRepository;
    private final PlayerRepository playerRepository;

    // Repos compétition — existants
    private final TournamentRepository tournamentRepository;
    private final TournamentRegulationRepository tournamentRegulationRepository;
    private final TableauRepository tableauRepository;

    // Repos compétition — nouveaux (phase de poules)
    private final PouleRepository pouleRepository;
    private final KoBracketRepository koBracketRepository;
    private final ClassificationBracketRepository classificationBracketRepository;

    // Services
    private final EmailSender emailSender;
    private final EmailVerificationService emailVerificationService;
    private final OrganizerAuthService organizerAuthService;
    private final TournamentService tournamentService;
    private final PoolPhaseService poolPhaseService;

    public ApplicationContext() {
        Path clubDbFile = Path.of("data", "club.db");
        Path competitionDbFile = Path.of("data", "competition.db");

        this.clubDb = new SqliteDb(clubDbFile);
        this.competitionDb = new SqliteDb(competitionDbFile);

        initClubDatabase();
        initCompetitionDatabase();

        // ---- Repos club ----
        this.clubRepository = new ClubRepositorySqlite(clubDb);
        this.clubAccessRepository = new ClubAccessRepositorySqlite(clubDb);
        this.organizerRepository = new OrganizerRepositorySqlite(clubDb);
        this.playerRepository = new PlayerRepositorySqlite(clubDb);

        // ---- Repos compétition existants ----
        this.tournamentRepository = new TournamentRepositorySqlite(competitionDb);
        this.tournamentRegulationRepository = new TournamentRegulationRepositorySqlite(competitionDb);
        this.tableauRepository = new TableauRepositorySqlite(competitionDb);

        // ---- ParticipantResolver — résout un ID en Participant ----
        // Cherche d'abord dans club.db (FFTT), puis invités stockés en compétition.db
        ParticipantResolver resolver = new PlayerParticipantResolver(clubDb, competitionDb);

        // ---- Repos phase de poules ----
        this.pouleRepository = new PouleRepositorySqlite(competitionDb, resolver);
        this.koBracketRepository = new KoBracketRepositorySqlite(competitionDb, resolver);
        this.classificationBracketRepository = new ClassificationBracketRepositorySqlite(competitionDb, resolver);

        // ---- Services ----
        this.emailSender = new ConsoleEmailSender();
        this.emailVerificationService = new EmailVerificationService(
                organizerRepository, emailSender);

        this.organizerAuthService = new OrganizerAuthService(
                organizerRepository, clubRepository,
                clubAccessRepository, emailVerificationService);

        this.tournamentService = new TournamentService(
                tournamentRepository, tournamentRegulationRepository, tableauRepository);

        this.poolPhaseService = new PoolPhaseService(new BracketBuilder());
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
        applySql(competitionDb, "/db/competition/pool_phase.sql"); // ← nouveau
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

    public PoolPhaseService poolPhaseService() {
        return poolPhaseService;
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

    public PouleRepository pouleRepository() {
        return pouleRepository;
    }

    public KoBracketRepository koBracketRepository() {
        return koBracketRepository;
    }

    public ClassificationBracketRepository classificationBracketRepository() {
        return classificationBracketRepository;
    }

    public EmailSender emailSender() {
        return emailSender;
    }
}