package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo;

import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.entity.Tableau;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.db.SqliteDb;

import java.sql.Connection;
import java.util.List;

public class SqliteTableauRepository {

    private final SqliteDb db;

    public SqliteTableauRepository(SqliteDb db) {
        this.db = db;
    }

    public void insertMany(String tournamentId, List<Tableau> tableaux) {
        if (tableaux == null || tableaux.isEmpty())
            return;

        String sql = """
                INSERT INTO tableau(
                  id, tournament_id, code, label, date,
                  prepaid_cents, onsite_cents, capacity,
                  created_at, updated_at
                ) VALUES(?,?,?,?,?,?,?,?,?,?)
                """;

        String now = java.time.Instant.now().toString();

        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql)) {

            c.setAutoCommit(false);

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
            c.commit();

        } catch (Exception e) {
            throw new RuntimeException("DB error insertMany(tableau)", e);
        }
    }
}