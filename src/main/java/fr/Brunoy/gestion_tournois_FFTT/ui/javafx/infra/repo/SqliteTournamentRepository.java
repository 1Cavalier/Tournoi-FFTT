package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo;

import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.entity.Tableau;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.db.SqliteDb;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.TournamentRow;

import java.sql.Connection;
import java.time.LocalDate;
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

    /**
     * Crée le tournoi DRAFT + persiste les tableaux + set current_tournament_id.
     * Transaction: tout ou rien.
     */
    public String createDraftTournamentAndTableaux(
            String organizerId,
            String name,
            String level,
            String rankingPhase,
            LocalDate startDate,
            LocalDate endDate,
            int maxPerDay,
            String femaleRule,
            String femaleCode,
            List<Tableau> tableaux) {

        String tournamentId = "tourn-" + java.util.UUID.randomUUID();
        String now = java.time.Instant.now().toString();

        long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        int maxTotal = (int) Math.max(1, days * (long) maxPerDay);

        String insertTournament = """
                INSERT INTO tournament(
                  id, organizer_id, name, level, phase, start_date, end_date, status,
                  max_tableaux_per_day, max_total_tableaux, female_extra_rule, female_extra_code,
                  created_at, updated_at
                ) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """;

        String insertTableau = """
                INSERT INTO tableau(
                  id, tournament_id, code, label, date,
                  prepaid_cents, onsite_cents, capacity,
                  created_at, updated_at
                ) VALUES(?,?,?,?,?,?,?,?,?,?)
                """;

        String setCurrent = """
                UPDATE app_state
                SET current_tournament_id = ?
                WHERE id = 1
                """;

        try (Connection c = db.openConnection()) {
            c.setAutoCommit(false);

            // 1) insert tournament
            try (var ps = c.prepareStatement(insertTournament)) {
                ps.setString(1, tournamentId);
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
                ps.setString(12, (femaleCode == null || femaleCode.isBlank()) ? null : femaleCode.trim().toUpperCase());
                ps.setString(13, now);
                ps.setString(14, now);
                ps.executeUpdate();
            }

            // 2) insert tableaux (batch)
            if (tableaux != null && !tableaux.isEmpty()) {
                try (var ps = c.prepareStatement(insertTableau)) {
                    for (Tableau tb : tableaux) {
                        String id = "tab-" + java.util.UUID.randomUUID();

                        ps.setString(1, id);
                        ps.setString(2, tournamentId);
                        ps.setString(3, tb.code());
                        ps.setString(4, tb.designation());
                        ps.setString(5, tb.date().toString());
                        ps.setInt(6, tb.fee().prepaid());
                        ps.setInt(7, tb.fee().onSite());
                        ps.setInt(8, tb.maxPlayers());
                        ps.setString(9, now);
                        ps.setString(10, now);

                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }

            // 3) update app_state
            try (var ps = c.prepareStatement(setCurrent)) {
                ps.setString(1, tournamentId);
                ps.executeUpdate();
            }

            c.commit();
            return tournamentId;

        } catch (Exception e) {
            throw new RuntimeException("DB error createDraftTournamentAndTableaux", e);
        }
    }
}