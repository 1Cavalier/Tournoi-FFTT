package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.db.SqliteDb;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.TournamentRow;

import java.sql.Connection;
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
            return Optional.ofNullable(id);

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
                        rs.getInt("phase"),
                        rs.getString("start_date"),
                        rs.getString("end_date"),
                        rs.getString("status")));
            }

        } catch (Exception e) {
            throw new RuntimeException("DB error findById(tournament)", e);
        }
    }
}
