package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.TournamentRow;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.db.SqliteDb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TournamentRepositorySqlite implements TournamentRepository {

    private final SqliteDb db;

    public TournamentRepositorySqlite(SqliteDb db) {
        this.db = db;
    }

    @Override
    public TournamentRow insert(TournamentRow t) {
        String sql = """
                INSERT INTO tournament (
                    id, club_id, organizer_id,
                    name, address1, address2, city, department,
                    level, phase,
                    start_date, end_date,
                    homologation_number,
                    status,
                    created_at, updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection c = db.openConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, t.id());
            ps.setString(2, t.clubId());
            ps.setString(3, t.organizerId());
            ps.setString(4, t.name());
            ps.setString(5, t.address1());
            ps.setString(6, t.address2());
            ps.setString(7, t.city());
            ps.setString(8, t.department());
            ps.setString(9, t.level());
            ps.setString(10, t.phase());
            ps.setString(11, t.startDate());
            ps.setString(12, t.endDate());
            ps.setString(13, t.homologationNumber());
            ps.setString(14, t.status());
            ps.setString(15, t.createdAt());
            ps.setString(16, t.updatedAt());

            ps.executeUpdate();
            return t;

        } catch (Exception e) {
            throw new RuntimeException("Insert tournament failed", e);
        }
    }

    @Override
    public Optional<TournamentRow> findById(String id) {
        String sql = "SELECT * FROM tournament WHERE id = ?";

        try (Connection c = db.openConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }

            return Optional.empty();

        } catch (Exception e) {
            throw new RuntimeException("Find tournament by id failed", e);
        }
    }

    @Override
    public List<TournamentRow> findByClubId(String clubId) {
        String sql = "SELECT * FROM tournament WHERE club_id = ? ORDER BY start_date DESC";

        List<TournamentRow> list = new ArrayList<>();

        try (Connection c = db.openConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, clubId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }

            return list;

        } catch (Exception e) {
            throw new RuntimeException("Find tournaments by club failed", e);
        }
    }

    @Override
    public List<TournamentRow> findDraftForClub(String clubId) {
        String sql = """
                SELECT * FROM tournament
                WHERE club_id = ? AND status = 'DRAFT'
                ORDER BY start_date DESC
                """;

        return findByQuery(clubId, sql);
    }

    @Override
    public List<TournamentRow> findActiveForClub(String clubId) {
        String sql = """
                SELECT * FROM tournament
                WHERE club_id = ? AND status != 'DRAFT'
                ORDER BY start_date DESC
                """;

        return findByQuery(clubId, sql);
    }

    private List<TournamentRow> findByQuery(String clubId, String sql) {
        List<TournamentRow> list = new ArrayList<>();

        try (Connection c = db.openConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, clubId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }

            return list;

        } catch (Exception e) {
            throw new RuntimeException("Query tournament failed", e);
        }
    }

    @Override
    public void update(TournamentRow t) {
        String sql = """
                UPDATE tournament SET
                    name = ?,
                    address1 = ?,
                    address2 = ?,
                    city = ?,
                    department = ?,
                    level = ?,
                    phase = ?,
                    start_date = ?,
                    end_date = ?,
                    homologation_number = ?,
                    status = ?,
                    updated_at = ?
                WHERE id = ?
                """;

        try (Connection c = db.openConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, t.name());
            ps.setString(2, t.address1());
            ps.setString(3, t.address2());
            ps.setString(4, t.city());
            ps.setString(5, t.department());
            ps.setString(6, t.level());
            ps.setString(7, t.phase());
            ps.setString(8, t.startDate());
            ps.setString(9, t.endDate());
            ps.setString(10, t.homologationNumber());
            ps.setString(11, t.status());
            ps.setString(12, t.updatedAt());
            ps.setString(13, t.id());

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Update tournament failed", e);
        }
    }

    @Override
    public void delete(String id) {
        String sql = "DELETE FROM tournament WHERE id = ?";

        try (Connection c = db.openConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Delete tournament failed", e);
        }
    }

    private TournamentRow map(ResultSet rs) throws Exception {
        return new TournamentRow(
                rs.getString("id"),
                rs.getString("club_id"),
                rs.getString("organizer_id"),
                rs.getString("name"),
                rs.getString("address1"),
                rs.getString("address2"),
                rs.getString("city"),
                rs.getString("department"),
                rs.getString("level"),
                rs.getString("phase"),
                rs.getString("start_date"),
                rs.getString("end_date"),
                rs.getString("homologation_number"),
                rs.getString("status"),
                rs.getString("created_at"),
                rs.getString("updated_at"));
    }
}