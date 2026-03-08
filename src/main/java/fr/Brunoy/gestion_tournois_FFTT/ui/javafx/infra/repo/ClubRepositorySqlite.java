package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.ClubDto;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.db.SqliteDb;

import java.sql.Connection;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implémentation SQLite du ClubRepository.
 */
public class ClubRepositorySqlite implements ClubRepository {

    private final SqliteDb db;

    public ClubRepositorySqlite(SqliteDb db) {
        this.db = db;
    }

    // ---------------------------------------------------------
    // CREATE
    // ---------------------------------------------------------

    @Override
    public String createClub(String clubNumberOrNull, String clubNameOrNull) {

        String id = "club-" + java.util.UUID.randomUUID();
        String now = Instant.now().toString();

        String sql = """
                INSERT INTO club(
                  id, club_number, club_name,
                  created_at, updated_at
                ) VALUES(?,?,?,?,?)
                """;

        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setString(1, id);
            ps.setString(2, blankToNull(clubNumberOrNull));
            ps.setString(3, blankToNull(clubNameOrNull));
            ps.setString(4, now);
            ps.setString(5, now);

            ps.executeUpdate();

            return id;

        } catch (Exception e) {
            throw new RuntimeException("DB error createClub", e);
        }
    }

    // ---------------------------------------------------------
    // READ
    // ---------------------------------------------------------

    @Override
    public Optional<ClubDto> findById(String clubId) {

        String sql = """
                SELECT id, club_number, club_name,
                       departement_code, city, address1, address2,
                       latitude, longitude,
                       contact_first_name, contact_last_name,
                       logo_path, updated_at
                FROM club
                WHERE id = ?
                """;

        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setString(1, clubId);

            try (var rs = ps.executeQuery()) {

                if (!rs.next())
                    return Optional.empty();

                return Optional.of(map(rs));
            }

        } catch (Exception e) {
            throw new RuntimeException("DB error findById(club)", e);
        }
    }

    @Override
    public Optional<ClubDto> findByOrganizerId(String organizerId) {

        String sql = """
                SELECT c.id, c.club_number, c.club_name,
                       c.departement_code, c.city, c.address1, c.address2,
                       c.latitude, c.longitude,
                       c.contact_first_name, c.contact_last_name,
                       c.logo_path, c.updated_at
                FROM organizer_account oa
                JOIN club c ON c.id = oa.club_id
                WHERE oa.id = ?
                """;

        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setString(1, organizerId);

            try (var rs = ps.executeQuery()) {

                if (!rs.next())
                    return Optional.empty();

                return Optional.of(map(rs));
            }

        } catch (Exception e) {
            throw new RuntimeException("DB error findByOrganizerId(club)", e);
        }
    }

    @Override
    public List<ClubDto> search(String query, int limit) {

        String q = query == null ? "" : query.trim();
        if (q.isEmpty())
            return List.of();

        int lim = limit <= 0 ? 20 : Math.min(limit, 100);

        String sql = """
                SELECT id, club_number, club_name,
                       departement_code, city, address1, address2,
                       latitude, longitude,
                       contact_first_name, contact_last_name,
                       logo_path, updated_at
                FROM club
                WHERE upper(coalesce(club_name,'')) LIKE upper(?)
                   OR upper(coalesce(club_number,'')) LIKE upper(?)
                ORDER BY club_name
                LIMIT ?
                """;

        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql)) {

            String like = "%" + q + "%";

            ps.setString(1, like);
            ps.setString(2, like);
            ps.setInt(3, lim);

            List<ClubDto> out = new ArrayList<>();

            try (var rs = ps.executeQuery()) {

                while (rs.next()) {
                    out.add(map(rs));
                }
            }

            return out;

        } catch (Exception e) {
            throw new RuntimeException("DB error search(club)", e);
        }
    }

    // ---------------------------------------------------------
    // UPDATE
    // ---------------------------------------------------------

    @Override
    public void updateClubProfile(ClubDto club) {

        if (club == null || club.id() == null) {
            throw new IllegalArgumentException("club.id obligatoire");
        }

        String now = Instant.now().toString();

        String sql = """
                UPDATE club
                SET club_number = ?,
                    club_name = ?,
                    departement_code = ?,
                    city = ?,
                    address1 = ?,
                    address2 = ?,
                    latitude = ?,
                    longitude = ?,
                    contact_first_name = ?,
                    contact_last_name = ?,
                    logo_path = ?,
                    updated_at = ?
                WHERE id = ?
                """;

        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setString(1, blankToNull(club.clubNumber()));
            ps.setString(2, blankToNull(club.clubName()));
            ps.setString(3, blankToNull(club.departementCode()));
            ps.setString(4, blankToNull(club.city()));
            ps.setString(5, blankToNull(club.address1()));
            ps.setString(6, blankToNull(club.address2()));

            ps.setObject(7, club.latitude());
            ps.setObject(8, club.longitude());

            ps.setString(9, blankToNull(club.contactFirstName()));
            ps.setString(10, blankToNull(club.contactLastName()));

            ps.setString(11, blankToNull(club.logoPath()));
            ps.setString(12, now);
            ps.setString(13, club.id());

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("DB error updateClubProfile", e);
        }
    }

    // ---------------------------------------------------------
    // MAPPING
    // ---------------------------------------------------------

    private ClubDto map(java.sql.ResultSet rs) throws java.sql.SQLException {

        return new ClubDto(
                rs.getString("id"),
                rs.getString("club_number"),
                rs.getString("club_name"),
                rs.getString("departement_code"),
                rs.getString("city"),
                rs.getString("address1"),
                rs.getString("address2"),
                (Double) rs.getObject("latitude"),
                (Double) rs.getObject("longitude"),
                rs.getString("contact_first_name"),
                rs.getString("contact_last_name"),
                rs.getString("logo_path"),
                rs.getString("updated_at"));
    }

    // ---------------------------------------------------------
    // UTILS
    // ---------------------------------------------------------

    private static String blankToNull(String s) {

        if (s == null)
            return null;

        String t = s.trim();

        return t.isEmpty() ? null : t;
    }
}