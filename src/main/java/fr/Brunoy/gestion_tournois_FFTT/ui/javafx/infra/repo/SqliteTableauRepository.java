package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.db.SqliteDb;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.TableauRow;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class SqliteTableauRepository {

    private final SqliteDb db;

    public SqliteTableauRepository(SqliteDb db) {
        this.db = db;
    }

    public List<TableauRow> listByTournamentId(String tournamentId) {
        String sql = """
                SELECT id, tournament_id, code, label, price_cents, capacity
                FROM tableau
                WHERE tournament_id = ?
                ORDER BY code
                """;
        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setString(1, tournamentId);
            try (var rs = ps.executeQuery()) {
                List<TableauRow> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(new TableauRow(
                            rs.getString("id"),
                            rs.getString("tournament_id"),
                            rs.getString("code"),
                            rs.getString("label"),
                            rs.getInt("price_cents"),
                            rs.getInt("capacity")));
                }
                return out;
            }

        } catch (Exception e) {
            throw new RuntimeException("DB error listByTournamentId(tableau)", e);
        }
    }
}
