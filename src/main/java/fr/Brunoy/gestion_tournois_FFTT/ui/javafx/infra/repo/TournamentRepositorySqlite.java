package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.TournamentDto;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.db.SqliteDb;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implémentation SQLite du TournamentRepository.
 */
public class TournamentRepositorySqlite implements TournamentRepository {

    private final SqliteDb db;

    public TournamentRepositorySqlite(SqliteDb db) {
        this.db = db;
    }

    @Override
    public Optional<String> findCurrentTournamentId() {

        String sql = "SELECT current_tournament_id FROM app_state WHERE id = 1";

        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql);
                var rs = ps.executeQuery()) {

            if (!rs.next())
                return Optional.empty();

            String id = rs.getString(1);
            return (id == null || id.isBlank())
                    ? Optional.empty()
                    : Optional.of(id);

        } catch (Exception e) {
            throw new RuntimeException("DB error findCurrentTournamentId", e);
        }
    }

    @Override
    public Optional<TournamentDto> findById(String id) {

        String sql = """
                SELECT id, organizer_id, name, level, phase,
                       start_date, end_date, status,
                       max_tableaux_per_day,
                       female_extra_rule,
                       female_extra_code
                FROM tournament
                WHERE id = ?
                """;

        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setString(1, id);

            try (var rs = ps.executeQuery()) {

                if (!rs.next())
                    return Optional.empty();

                return Optional.of(map(rs));
            }

        } catch (Exception e) {
            throw new RuntimeException("DB error findById(tournament)", e);
        }
    }

    @Override
    public List<TournamentDto> findActiveForOrganizer(String organizerId) {

        String sql = """
                SELECT id, organizer_id, name, level, phase,
                       start_date, end_date, status,
                       max_tableaux_per_day,
                       female_extra_rule,
                       female_extra_code
                FROM tournament
                WHERE organizer_id = ?
                  AND status IN ('RUNNING','OPEN')
                ORDER BY
                    CASE status
                        WHEN 'RUNNING' THEN 0
                        WHEN 'OPEN' THEN 1
                        ELSE 9
                    END,
                    start_date ASC
                """;

        return queryTournamentList(sql, organizerId);
    }

    @Override
    public List<TournamentDto> findDraftForOrganizer(String organizerId) {

        String sql = """
                SELECT id, organizer_id, name, level, phase,
                       start_date, end_date, status,
                       max_tableaux_per_day,
                       female_extra_rule,
                       female_extra_code
                FROM tournament
                WHERE organizer_id = ?
                  AND status = 'DRAFT'
                ORDER BY updated_at DESC
                """;

        return queryTournamentList(sql, organizerId);
    }

    private List<TournamentDto> queryTournamentList(String sql, String organizerId) {

        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setString(1, organizerId);

            try (var rs = ps.executeQuery()) {

                List<TournamentDto> out = new ArrayList<>();

                while (rs.next()) {
                    out.add(map(rs));
                }

                return out;
            }

        } catch (Exception e) {
            throw new RuntimeException("DB error list tournaments", e);
        }
    }

    @Override
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

        long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        int maxTotal = (int) Math.max(1, days * (long) maxPerDay);

        String insert = """
                INSERT INTO tournament(
                    id, organizer_id, name, level, phase,
                    start_date, end_date, status,
                    max_tableaux_per_day, max_total_tableaux,
                    female_extra_rule, female_extra_code,
                    created_at, updated_at
                ) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """;

        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(insert)) {

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
            ps.setString(12, femaleCode);
            ps.setString(13, now);
            ps.setString(14, now);

            ps.executeUpdate();

            return id;

        } catch (Exception e) {
            throw new RuntimeException("DB error createDraftTournament", e);
        }
    }

    private TournamentDto map(java.sql.ResultSet rs) throws java.sql.SQLException {

        return new TournamentDto(
                rs.getString("id"),
                rs.getString("organizer_id"),
                rs.getString("name"),
                rs.getString("level"),
                rs.getString("phase"),
                rs.getString("start_date"),
                rs.getString("end_date"),
                rs.getString("status"),
                (Integer) rs.getObject("max_tableaux_per_day"),
                rs.getString("female_extra_rule"),
                rs.getString("female_extra_code"));
    }
}