package fr.pingmanager.gestion_tournois_FFTT.infra.repo;

import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.classification.*;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.pool.PoolMatchScore;
import fr.pingmanager.gestion_tournois_FFTT.domain.identity.Participant;
import fr.pingmanager.gestion_tournois_FFTT.infra.db.SqliteDb;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class ClassificationBracketRepositorySqlite implements ClassificationBracketRepository {

    private final SqliteDb db;
    private final ParticipantResolver participantResolver;

    public ClassificationBracketRepositorySqlite(SqliteDb db,
            ParticipantResolver participantResolver) {
        this.db = Objects.requireNonNull(db);
        this.participantResolver = Objects.requireNonNull(participantResolver);
    }

    // =========================================================================
    // SAVE
    // =========================================================================

    @Override
    public void save(ClassificationBracket bracket, String tournamentId) {
        try (Connection c = db.openConnection()) {
            c.setAutoCommit(false);
            try {
                insertBracket(c, bracket, tournamentId);
                for (ClassificationMatch match : bracket.matches()) {
                    insertMatch(c, bracket.id(), match);
                    if (match.score() != null) {
                        insertSets(c, match.id(), match.score());
                    }
                }
                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        } catch (Exception e) {
            throw new RuntimeException("Save ClassificationBracket failed", e);
        }
    }

    // =========================================================================
    // UPDATE
    // =========================================================================

    @Override
    public void update(ClassificationBracket bracket) {
        try (Connection c = db.openConnection()) {
            c.setAutoCommit(false);
            try {
                for (ClassificationMatch match : bracket.matches()) {
                    updateMatch(c, match);
                    deleteSets(c, match.id());
                    if (match.score() != null) {
                        insertSets(c, match.id(), match.score());
                    }
                }
                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        } catch (Exception e) {
            throw new RuntimeException("Update ClassificationBracket failed", e);
        }
    }

    // =========================================================================
    // FIND
    // =========================================================================

    @Override
    public Optional<ClassificationBracket> findByTableau(String tournamentId, String tableauCode) {
        String sql = """
                SELECT id, mode, bracket_size
                FROM classification_bracket
                WHERE tournament_id = ? AND tableau_code = ?
                """;
        try (Connection c = db.openConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, tournamentId);
            ps.setString(2, tableauCode.toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String bracketId = rs.getString("id");
                    ClassificationMode mode = ClassificationMode.valueOf(rs.getString("mode"));
                    int bracketSize = rs.getInt("bracket_size");
                    List<ClassificationMatch> matches = loadMatches(c, bracketId);
                    return Optional.of(new ClassificationBracket(
                            bracketId, tableauCode, mode, bracketSize, matches));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("findByTableau ClassificationBracket failed", e);
        }
        return Optional.empty();
    }

    @Override
    public void deleteByTableau(String tournamentId, String tableauCode) {
        String sql = """
                DELETE FROM classification_bracket
                WHERE tournament_id = ? AND tableau_code = ?
                """;
        try (Connection c = db.openConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, tournamentId);
            ps.setString(2, tableauCode.toUpperCase());
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("deleteByTableau ClassificationBracket failed", e);
        }
    }

    // =========================================================================
    // RECONSTRUCTION
    // =========================================================================

    private List<ClassificationMatch> loadMatches(Connection c,
            String bracketId) throws SQLException {
        String sql = """
                SELECT id, winner_rank, loser_rank,
                       player1_id, player2_id, status, walkover_id
                FROM classification_match
                WHERE classification_bracket_id = ?
                ORDER BY winner_rank
                """;
        List<ClassificationMatch> matches = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, bracketId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String matchId = rs.getString("id");
                    int winnerRank = rs.getInt("winner_rank");
                    int loserRank = rs.getInt("loser_rank");
                    String p1Id = rs.getString("player1_id");
                    String p2Id = rs.getString("player2_id");
                    String statusStr = rs.getString("status");
                    String walkover = rs.getString("walkover_id");

                    Participant p1 = participantResolver.resolve(p1Id);
                    Participant p2 = participantResolver.resolve(p2Id);
                    ClassificationMatch.Status status = ClassificationMatch.Status.valueOf(statusStr);
                    PoolMatchScore score = loadScore(c, matchId);

                    matches.add(new ClassificationMatch(matchId, winnerRank, loserRank,
                            p1, p2, status, score, walkover));
                }
            }
        }
        return matches;
    }

    private PoolMatchScore loadScore(Connection c, String matchId) throws SQLException {
        String sql = """
                SELECT points_p1, points_p2
                FROM classification_match_set
                WHERE classification_match_id = ?
                ORDER BY set_order
                """;
        List<int[]> sets = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, matchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sets.add(new int[] { rs.getInt("points_p1"), rs.getInt("points_p2") });
                }
            }
        }
        return sets.isEmpty() ? null : new PoolMatchScore(sets);
    }

    // =========================================================================
    // INSERTIONS / UPDATES
    // =========================================================================

    private void insertBracket(Connection c, ClassificationBracket bracket,
            String tournamentId) throws SQLException {
        String sql = """
                INSERT OR IGNORE INTO classification_bracket
                  (id, tableau_code, tournament_id, mode, bracket_size, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, bracket.id());
            ps.setString(2, bracket.tableauCode());
            ps.setString(3, tournamentId);
            ps.setString(4, bracket.mode().name());
            ps.setInt(5, bracket.bracketSize());
            ps.setString(6, LocalDateTime.now().toString());
            ps.executeUpdate();
        }
    }

    private void insertMatch(Connection c, String bracketId,
            ClassificationMatch match) throws SQLException {
        String sql = """
                INSERT OR IGNORE INTO classification_match
                  (id, classification_bracket_id, winner_rank, loser_rank,
                   player1_id, player2_id, status, walkover_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, match.id());
            ps.setString(2, bracketId);
            ps.setInt(3, match.winnerRank());
            ps.setInt(4, match.loserRank());
            ps.setString(5, match.player1().participantId());
            ps.setString(6, match.player2().participantId());
            ps.setString(7, match.status().name());
            ps.setString(8, match.walkoverId());
            ps.executeUpdate();
        }
    }

    private void updateMatch(Connection c, ClassificationMatch match) throws SQLException {
        String sql = """
                UPDATE classification_match
                SET status = ?, walkover_id = ?
                WHERE id = ?
                """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, match.status().name());
            ps.setString(2, match.walkoverId());
            ps.setString(3, match.id());
            ps.executeUpdate();
        }
    }

    private void insertSets(Connection c, String matchId,
            PoolMatchScore score) throws SQLException {
        String sql = """
                INSERT INTO classification_match_set
                  (id, classification_match_id, set_order, points_p1, points_p2)
                VALUES (?, ?, ?, ?, ?)
                """;
        List<int[]> sets = score.sets();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < sets.size(); i++) {
                ps.setString(1, UUID.randomUUID().toString());
                ps.setString(2, matchId);
                ps.setInt(3, i + 1);
                ps.setInt(4, sets.get(i)[0]);
                ps.setInt(5, sets.get(i)[1]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void deleteSets(Connection c, String matchId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "DELETE FROM classification_match_set WHERE classification_match_id = ?")) {
            ps.setString(1, matchId);
            ps.executeUpdate();
        }
    }
}