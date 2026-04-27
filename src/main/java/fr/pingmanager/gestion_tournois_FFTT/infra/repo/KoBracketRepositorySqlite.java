package fr.pingmanager.gestion_tournois_FFTT.infra.repo;

import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.bracket.KoBracket;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.bracket.KoMatch;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.pool.PoolMatchScore;
import fr.pingmanager.gestion_tournois_FFTT.domain.identity.Participant;
import fr.pingmanager.gestion_tournois_FFTT.infra.db.SqliteDb;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class KoBracketRepositorySqlite implements KoBracketRepository {

    private final SqliteDb db;
    private final ParticipantResolver participantResolver;

    public KoBracketRepositorySqlite(SqliteDb db, ParticipantResolver participantResolver) {
        this.db = Objects.requireNonNull(db);
        this.participantResolver = Objects.requireNonNull(participantResolver);
    }

    // =========================================================================
    // SAVE
    // =========================================================================

    @Override
    public void save(KoBracket bracket, String tournamentId) {
        try (Connection c = db.openConnection()) {
            c.setAutoCommit(false);
            try {
                insertBracket(c, bracket, tournamentId);
                for (KoMatch match : bracket.allMatches()) {
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
            throw new RuntimeException("Save KoBracket failed: " + bracket.id(), e);
        }
    }

    // =========================================================================
    // UPDATE — Met à jour tous les matchs (joueurs propagés + scores)
    // =========================================================================

    @Override
    public void update(KoBracket bracket) {
        try (Connection c = db.openConnection()) {
            c.setAutoCommit(false);
            try {
                for (KoMatch match : bracket.allMatches()) {
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
            throw new RuntimeException("Update KoBracket failed", e);
        }
    }

    // =========================================================================
    // FIND
    // =========================================================================

    @Override
    public Optional<KoBracket> findByTableau(String tournamentId, String tableauCode) {
        String sql = """
                SELECT id, bracket_size
                FROM ko_bracket
                WHERE tournament_id = ? AND tableau_code = ?
                """;
        try (Connection c = db.openConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, tournamentId);
            ps.setString(2, tableauCode.toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String bracketId = rs.getString("id");
                    int bracketSize = rs.getInt("bracket_size");
                    List<KoMatch> matches = loadMatches(c, bracketId);
                    return Optional.of(new KoBracket(bracketId, tableauCode,
                            bracketSize, matches));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("findByTableau KoBracket failed", e);
        }
        return Optional.empty();
    }

    @Override
    public void deleteByTableau(String tournamentId, String tableauCode) {
        String sql = "DELETE FROM ko_bracket WHERE tournament_id = ? AND tableau_code = ?";
        try (Connection c = db.openConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, tournamentId);
            ps.setString(2, tableauCode.toUpperCase());
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("deleteByTableau KoBracket failed", e);
        }
    }

    // =========================================================================
    // RECONSTRUCTION
    // =========================================================================

    private List<KoMatch> loadMatches(Connection c, String bracketId) throws SQLException {
        String sql = """
                SELECT id, round, position, player1_id, player2_id,
                       status, walkover_id
                FROM ko_match
                WHERE ko_bracket_id = ?
                ORDER BY round, position
                """;
        List<KoMatch> matches = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, bracketId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String matchId = rs.getString("id");
                    int round = rs.getInt("round");
                    int position = rs.getInt("position");
                    String p1Id = rs.getString("player1_id");
                    String p2Id = rs.getString("player2_id");
                    String statusStr = rs.getString("status");
                    String walkover = rs.getString("walkover_id");

                    Participant p1 = p1Id != null ? participantResolver.resolve(p1Id) : null;
                    Participant p2 = p2Id != null ? participantResolver.resolve(p2Id) : null;
                    KoMatch.Status status = KoMatch.Status.valueOf(statusStr);
                    PoolMatchScore score = loadScore(c, matchId);

                    matches.add(new KoMatch(matchId, round, position,
                            p1, p2, status, score, walkover));
                }
            }
        }
        return matches;
    }

    private PoolMatchScore loadScore(Connection c, String matchId) throws SQLException {
        String sql = """
                SELECT points_p1, points_p2
                FROM ko_match_set
                WHERE ko_match_id = ?
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

    private void insertBracket(Connection c, KoBracket bracket,
            String tournamentId) throws SQLException {
        String sql = """
                INSERT OR IGNORE INTO ko_bracket
                  (id, tableau_code, tournament_id, bracket_size, total_rounds, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, bracket.id());
            ps.setString(2, bracket.tableauCode());
            ps.setString(3, tournamentId);
            ps.setInt(4, bracket.bracketSize());
            ps.setInt(5, bracket.totalRounds());
            ps.setString(6, LocalDateTime.now().toString());
            ps.executeUpdate();
        }
    }

    private void insertMatch(Connection c, String bracketId, KoMatch match) throws SQLException {
        String sql = """
                INSERT OR IGNORE INTO ko_match
                  (id, ko_bracket_id, round, position,
                   player1_id, player2_id, status, walkover_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, match.id());
            ps.setString(2, bracketId);
            ps.setInt(3, match.round());
            ps.setInt(4, match.position());
            ps.setString(5, match.player1() != null ? match.player1().participantId() : null);
            ps.setString(6, match.player2() != null ? match.player2().participantId() : null);
            ps.setString(7, match.status().name());
            ps.setString(8, match.walkoverId());
            ps.executeUpdate();
        }
    }

    private void updateMatch(Connection c, KoMatch match) throws SQLException {
        String sql = """
                UPDATE ko_match
                SET player1_id = ?, player2_id = ?, status = ?, walkover_id = ?
                WHERE id = ?
                """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, match.player1() != null ? match.player1().participantId() : null);
            ps.setString(2, match.player2() != null ? match.player2().participantId() : null);
            ps.setString(3, match.status().name());
            ps.setString(4, match.walkoverId());
            ps.setString(5, match.id());
            ps.executeUpdate();
        }
    }

    private void insertSets(Connection c, String matchId, PoolMatchScore score) throws SQLException {
        String sql = """
                INSERT INTO ko_match_set (id, ko_match_id, set_order, points_p1, points_p2)
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
                "DELETE FROM ko_match_set WHERE ko_match_id = ?")) {
            ps.setString(1, matchId);
            ps.executeUpdate();
        }
    }
}