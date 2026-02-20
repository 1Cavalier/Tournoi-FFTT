package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.db.SqliteDb;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.TournamentRow;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteTournamentRepository {

    private final SqliteDb db;

    public SqliteTournamentRepository(SqliteDb db) {
        this.db = db;
    }

    public Optional<String> findCurrentTournamentId() {
        String sql = "SELECT current_tournament_id FROM app_state WHERE id = 1";
        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql);
                var rs = ps.executeQuery()) {
            if (!rs.next())
                return Optional.empty();
            String id = rs.getString(1);
            return (id == null || id.isBlank()) ? Optional.empty() : Optional.of(id);
        } catch (Exception e) {
            throw new RuntimeException("DB error findCurrentTournamentId", e);
        }
    }

    public Optional<TournamentRow> findById(String id) {
        String sql = """
                SELECT id, organizer_id, name, level, phase, start_date, end_date, status
                FROM tournament
                WHERE id = ?
                """;
        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            try (var rs = ps.executeQuery()) {
                if (!rs.next())
                    return Optional.empty();
                return Optional.of(new TournamentRow(
                        rs.getString("id"),
                        rs.getString("organizer_id"),
                        rs.getString("name"),
                        rs.getString("level"),
                        rs.getString("phase"),
                        rs.getString("start_date"),
                        rs.getString("end_date"),
                        rs.getString("status")));
            }
        } catch (Exception e) {
            throw new RuntimeException("DB error findById(tournament)", e);
        }
    }

    // ----------------- LISTES POUR DASHBOARD -----------------

    public List<TournamentRow> findActiveForOrganizer(String organizerId) {
        // RUNNING d'abord, puis OPEN, ensuite par start_date ASC
        String sql = """
                SELECT id, organizer_id, name, level, phase, start_date, end_date, status
                FROM tournament
                WHERE organizer_id = ?
                  AND status IN ('RUNNING','OPEN')
                ORDER BY
                  CASE status
                    WHEN 'RUNNING' THEN 0
                    WHEN 'OPEN' THEN 1
                    ELSE 9
                  END,
                  start_date ASC,
                  updated_at DESC
                """;

        return queryTournamentList(sql, organizerId);
    }

    public List<TournamentRow> findDraftForOrganizer(String organizerId) {
        // DRAFT trié du plus récemment modifié au plus ancien
        String sql = """
                SELECT id, organizer_id, name, level, phase, start_date, end_date, status
                FROM tournament
                WHERE organizer_id = ?
                  AND status = 'DRAFT'
                ORDER BY updated_at DESC, start_date ASC
                """;

        return queryTournamentList(sql, organizerId);
    }

    private List<TournamentRow> queryTournamentList(String sql, String organizerId) {
        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql)) {
            ps.setString(1, organizerId);
            try (var rs = ps.executeQuery()) {
                List<TournamentRow> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(new TournamentRow(
                            rs.getString("id"),
                            rs.getString("organizer_id"),
                            rs.getString("name"),
                            rs.getString("level"),
                            rs.getString("phase"),
                            rs.getString("start_date"),
                            rs.getString("end_date"),
                            rs.getString("status")));
                }
                return out;
            }
        } catch (Exception e) {
            throw new RuntimeException("DB error list tournaments", e);
        }
    }

    // ----------------- CREATION -----------------

    public String createDraftTournament(
            String organizerId,
            String name,
            String level,
            String rankingPhase,
            LocalDate startDate,
            LocalDate endDate,
            int maxPerDay,
            String femaleRule,
            String femaleCode) {

        String id = "tourn-" + java.util.UUID.randomUUID();
        String now = java.time.Instant.now().toString();

        long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1; // inclusif
        int maxTotal = (int) Math.max(1, days * (long) maxPerDay);

        String insert = """
                INSERT INTO tournament(
                  id, organizer_id, name, level, phase, start_date, end_date, status,
                  max_tableaux_per_day, max_total_tableaux, female_extra_rule, female_extra_code,
                  created_at, updated_at
                ) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """;

        String setCurrent = """
                UPDATE app_state
                SET current_tournament_id = ?
                WHERE id = 1
                """;

        try (Connection c = db.openConnection()) {
            c.setAutoCommit(false);

            try (var ps = c.prepareStatement(insert)) {
                ps.setString(1, id);
                ps.setString(2, organizerId);
                ps.setString(3, name);
                ps.setString(4, level);
                ps.setString(5, rankingPhase);
                ps.setString(6, startDate.toString());
                ps.setString(7, endDate.toString());
                ps.setString(8, "DRAFT");
                ps.setInt(9, maxPerDay);
                ps.setInt(10, maxTotal);
                ps.setString(11, femaleRule);
                ps.setString(12, (femaleCode == null || femaleCode.isBlank())
                        ? null
                        : femaleCode.trim().toUpperCase());
                ps.setString(13, now);
                ps.setString(14, now);
                ps.executeUpdate();
            }

            try (var ps2 = c.prepareStatement(setCurrent)) {
                ps2.setString(1, id);
                ps2.executeUpdate();
            }

            c.commit();
            return id;

        } catch (Exception e) {
            throw new RuntimeException("DB error createDraftTournament", e);
        }
    }
}