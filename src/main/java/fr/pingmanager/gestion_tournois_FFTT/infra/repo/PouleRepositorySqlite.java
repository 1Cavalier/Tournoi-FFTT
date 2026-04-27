package fr.pingmanager.gestion_tournois_FFTT.infra.repo;

import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.pool.*;
import fr.pingmanager.gestion_tournois_FFTT.domain.identity.Participant;
import fr.pingmanager.gestion_tournois_FFTT.infra.db.SqliteDb;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Implémentation SQLite du PouleRepository.
 *
 * Stratégie de reconstruction :
 * Les participants sont identifiés par leur participantId (clé stable).
 * Pour reconstruire un FfttParticipant complet, on a besoin du
 * PlayerRepository.
 * Dans un premier temps, on stocke et recharge via un ParticipantResolver
 * injecté.
 */
public class PouleRepositorySqlite implements PouleRepository {

    private final SqliteDb db;
    private final ParticipantResolver participantResolver;

    public PouleRepositorySqlite(SqliteDb db, ParticipantResolver participantResolver) {
        this.db = Objects.requireNonNull(db);
        this.participantResolver = Objects.requireNonNull(participantResolver);
    }

    // =========================================================================
    // SAVE — Insertion complète (poule + slots + matchs + sets)
    // =========================================================================

    @Override
    public void save(Poule poule, String tournamentId) {
        try (Connection c = db.openConnection()) {
            c.setAutoCommit(false);
            try {
                insertPoule(c, poule, tournamentId);
                for (PoolSlot slot : poule.slots()) {
                    insertSlot(c, poule.id(), slot);
                }
                for (PoolMatch match : poule.matches()) {
                    insertMatch(c, poule.id(), match);
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
            throw new RuntimeException("Save poule failed: " + poule.id(), e);
        }
    }

    // =========================================================================
    // UPDATE — Mise à jour des matchs uniquement (les slots ne changent pas)
    // =========================================================================

    @Override
    public void update(Poule poule) {
        try (Connection c = db.openConnection()) {
            c.setAutoCommit(false);
            try {
                for (PoolMatch match : poule.matches()) {
                    updateMatch(c, match);
                    // Supprimer et recréer les sets si le score a changé
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
            throw new RuntimeException("Update poule failed: " + poule.id(), e);
        }
    }

    // =========================================================================
    // FIND
    // =========================================================================

    @Override
    public List<Poule> findByTableau(String tournamentId, String tableauCode) {
        String sql = """
                SELECT id, pool_number
                FROM poule
                WHERE tournament_id = ? AND tableau_code = ?
                ORDER BY pool_number
                """;
        List<Poule> result = new ArrayList<>();
        try (Connection c = db.openConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, tournamentId);
            ps.setString(2, tableauCode.toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String pouleId = rs.getString("id");
                    int poolNumber = rs.getInt("pool_number");
                    result.add(reconstruct(c, pouleId, tableauCode, poolNumber));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("findByTableau poule failed", e);
        }
        return result;
    }

    @Override
    public Optional<Poule> findById(String pouleId) {
        String sql = "SELECT tableau_code, pool_number FROM poule WHERE id = ?";
        try (Connection c = db.openConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, pouleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String tableauCode = rs.getString("tableau_code");
                    int poolNumber = rs.getInt("pool_number");
                    return Optional.of(reconstruct(c, pouleId, tableauCode, poolNumber));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("findById poule failed: " + pouleId, e);
        }
        return Optional.empty();
    }

    @Override
    public void deleteByTableau(String tournamentId, String tableauCode) {
        String sql = "DELETE FROM poule WHERE tournament_id = ? AND tableau_code = ?";
        try (Connection c = db.openConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, tournamentId);
            ps.setString(2, tableauCode.toUpperCase());
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("deleteByTableau poule failed", e);
        }
    }

    // =========================================================================
    // RECONSTRUCTION
    // =========================================================================

    private Poule reconstruct(Connection c, String pouleId,
            String tableauCode, int poolNumber) throws SQLException {
        List<PoolSlot> slots = loadSlots(c, pouleId);
        List<PoolMatch> matches = loadMatches(c, pouleId, slots);
        return new Poule(pouleId, tableauCode, poolNumber, slots, matches);
    }

    private List<PoolSlot> loadSlots(Connection c, String pouleId) throws SQLException {
        String sql = """
                SELECT participant_id, seed_rank, position_in_pool
                FROM pool_slot
                WHERE poule_id = ?
                ORDER BY position_in_pool
                """;
        List<PoolSlot> slots = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, pouleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String pid = rs.getString("participant_id");
                    int seedRank = rs.getInt("seed_rank");
                    int posInPool = rs.getInt("position_in_pool");
                    Participant p = participantResolver.resolve(pid);
                    slots.add(new PoolSlot(seedRank, posInPool, p));
                }
            }
        }
        return slots;
    }

    private List<PoolMatch> loadMatches(Connection c, String pouleId,
            List<PoolSlot> slots) throws SQLException {
        String sql = """
                SELECT id, match_order, slot1_participant_id, slot2_participant_id,
                       status, walkover_participant_id
                FROM pool_match
                WHERE poule_id = ?
                ORDER BY match_order
                """;
        List<PoolMatch> matches = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, pouleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String matchId = rs.getString("id");
                    int matchOrder = rs.getInt("match_order");
                    String pid1 = rs.getString("slot1_participant_id");
                    String pid2 = rs.getString("slot2_participant_id");
                    String statusStr = rs.getString("status");
                    String walkover = rs.getString("walkover_participant_id");

                    PoolSlot s1 = slotByParticipant(slots, pid1);
                    PoolSlot s2 = slotByParticipant(slots, pid2);

                    PoolMatchScore score = loadScore(c, matchId);
                    PoolMatch.Status status = PoolMatch.Status.valueOf(statusStr);

                    matches.add(new PoolMatch(matchId, s1, s2, matchOrder,
                            status, score, walkover));
                }
            }
        }
        return matches;
    }

    private PoolMatchScore loadScore(Connection c, String matchId) throws SQLException {
        String sql = """
                SELECT points_p1, points_p2
                FROM pool_match_set
                WHERE pool_match_id = ?
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

    private PoolSlot slotByParticipant(List<PoolSlot> slots, String participantId) {
        return slots.stream()
                .filter(s -> s.participant().participantId().equals(participantId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Slot not found for: " + participantId));
    }

    // =========================================================================
    // INSERTIONS
    // =========================================================================

    private void insertPoule(Connection c, Poule poule, String tournamentId) throws SQLException {
        String sql = """
                INSERT OR IGNORE INTO poule (id, tableau_code, tournament_id, pool_number, created_at)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, poule.id());
            ps.setString(2, poule.tableauCode());
            ps.setString(3, tournamentId);
            ps.setInt(4, poule.poolNumber());
            ps.setString(5, LocalDateTime.now().toString());
            ps.executeUpdate();
        }
    }

    private void insertSlot(Connection c, String pouleId, PoolSlot slot) throws SQLException {
        String sql = """
                INSERT OR IGNORE INTO pool_slot
                  (id, poule_id, participant_id, seed_rank, position_in_pool)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, pouleId);
            ps.setString(3, slot.participant().participantId());
            ps.setInt(4, slot.seedRank());
            ps.setInt(5, slot.positionInPool());
            ps.executeUpdate();
        }
    }

    private void insertMatch(Connection c, String pouleId, PoolMatch match) throws SQLException {
        String sql = """
                INSERT OR IGNORE INTO pool_match
                  (id, poule_id, match_order,
                   slot1_participant_id, slot2_participant_id,
                   status, walkover_participant_id)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, match.id());
            ps.setString(2, pouleId);
            ps.setInt(3, match.matchOrderInPool());
            ps.setString(4, match.slot1().participant().participantId());
            ps.setString(5, match.slot2().participant().participantId());
            ps.setString(6, match.status().name());
            ps.setString(7, match.walkoverId());
            ps.executeUpdate();
        }
    }

    private void updateMatch(Connection c, PoolMatch match) throws SQLException {
        String sql = """
                UPDATE pool_match
                SET status = ?, walkover_participant_id = ?
                WHERE id = ?
                """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, match.status().name());
            ps.setString(2, match.walkoverId());
            ps.setString(3, match.id());
            ps.executeUpdate();
        }
    }

    private void insertSets(Connection c, String matchId, PoolMatchScore score) throws SQLException {
        String sql = """
                INSERT INTO pool_match_set (id, pool_match_id, set_order, points_p1, points_p2)
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
                "DELETE FROM pool_match_set WHERE pool_match_id = ?")) {
            ps.setString(1, matchId);
            ps.executeUpdate();
        }
    }
}