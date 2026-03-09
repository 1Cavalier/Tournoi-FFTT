package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.ClubAccessDto;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.db.SqliteDb;

import java.sql.Connection;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClubAccessRepositorySqlite implements ClubAccessRepository {

    private final SqliteDb db;

    public ClubAccessRepositorySqlite(SqliteDb db) {
        this.db = db;
    }

    @Override
    public List<ClubAccessDto> findByClubId(String clubId) {
        requireNotBlank(clubId, "clubId obligatoire.");

        String sql = """
                SELECT id, club_id, email, first_name, last_name, updated_at
                FROM club_access
                WHERE club_id = ?
                ORDER BY lower(coalesce(email, '')) ASC,
                         lower(coalesce(last_name, '')) ASC,
                         lower(coalesce(first_name, '')) ASC
                """;

        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setString(1, clubId);

            List<ClubAccessDto> out = new ArrayList<>();

            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(map(rs));
                }
            }

            return out;

        } catch (Exception e) {
            throw new RuntimeException("DB error findByClubId(club_access)", e);
        }
    }

    @Override
    public boolean existsByClubIdAndEmail(String clubId, String email) {
        requireNotBlank(clubId, "clubId obligatoire.");
        requireNotBlank(email, "email obligatoire.");

        String sql = """
                SELECT 1
                FROM club_access
                WHERE club_id = ?
                  AND lower(email) = ?
                LIMIT 1
                """;

        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setString(1, clubId);
            ps.setString(2, normalizeEmail(email));

            try (var rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {
            throw new RuntimeException("DB error existsByClubIdAndEmail(club_access)", e);
        }
    }

    @Override
    public void insert(ClubAccessDto access) {
        if (access == null) {
            throw new IllegalArgumentException("access obligatoire.");
        }

        requireNotBlank(access.clubId(), "clubId obligatoire.");
        requireNotBlank(access.email(), "email obligatoire.");

        String id = access.id() == null || access.id().isBlank()
                ? "club-access-" + UUID.randomUUID()
                : access.id();

        String now = Instant.now().toString();

        String sql = """
                INSERT INTO club_access(
                  id, club_id, email, first_name, last_name, created_at, updated_at
                ) VALUES(?,?,?,?,?,?,?)
                """;

        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setString(1, id);
            ps.setString(2, access.clubId());
            ps.setString(3, normalizeEmail(access.email()));
            ps.setString(4, blankToNull(access.firstName()));
            ps.setString(5, blankToNull(access.lastName()));
            ps.setString(6, now);
            ps.setString(7, now);

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("DB error insert(club_access)", e);
        }
    }

    @Override
    public void deleteById(String id) {
        requireNotBlank(id, "id obligatoire.");

        String sql = """
                DELETE FROM club_access
                WHERE id = ?
                """;

        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setString(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("DB error deleteById(club_access)", e);
        }
    }

    private ClubAccessDto map(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new ClubAccessDto(
                rs.getString("id"),
                rs.getString("club_id"),
                rs.getString("email"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("updated_at"));
    }

    private static String normalizeEmail(String email) {
        requireNotBlank(email, "email obligatoire.");
        return email.trim().toLowerCase();
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String s = value.trim();
        return s.isEmpty() ? null : s;
    }

    private static void requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}