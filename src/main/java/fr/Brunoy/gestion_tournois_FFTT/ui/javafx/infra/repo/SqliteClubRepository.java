package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.db.SqliteDb;

import java.sql.Connection;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository SQLite pour la table club.
 *
 * Responsabilités :
 * - lecture / recherche sur les clubs de référence
 * - récupération du club associé à un organisateur
 * - mise à jour du profil club côté UI
 */
public class SqliteClubRepository {

    private final SqliteDb db;

    public SqliteClubRepository(SqliteDb db) {
        this.db = db;
    }

    public Optional<ClubRow> findById(String clubId) {
        String sql = """
                SELECT id,
                       club_number, club_name,
                       departement_code, city, address1, address2,
                       latitude, longitude,
                       contact_first_name, contact_last_name,
                       official_contact_email,
                       logo_path,
                       created_at, updated_at
                FROM club
                WHERE id = ?
                """;

        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setString(1, clubId);

            try (var rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapClubRow(rs));
            }

        } catch (Exception e) {
            throw new RuntimeException("DB error findById(club)", e);
        }
    }

    public Optional<ClubRow> findByOrganizerId(String organizerId) {
        String sql = """
                SELECT c.id,
                       c.club_number, c.club_name,
                       c.departement_code, c.city, c.address1, c.address2,
                       c.latitude, c.longitude,
                       c.contact_first_name, c.contact_last_name,
                       c.official_contact_email,
                       c.logo_path,
                       c.created_at, c.updated_at
                FROM organizer_account oa
                JOIN club c ON c.id = oa.club_id
                WHERE oa.id = ?
                """;

        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setString(1, organizerId);

            try (var rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapClubRow(rs));
            }

        } catch (Exception e) {
            throw new RuntimeException("DB error findByOrganizerId(club)", e);
        }
    }

    public List<ClubRow> search(String query, int limit) {
        String q = (query == null) ? "" : query.trim();
        if (q.isEmpty()) {
            return List.of();
        }

        int lim = (limit <= 0) ? 20 : Math.min(limit, 100);

        String sql = """
                SELECT id,
                       club_number, club_name,
                       departement_code, city, address1, address2,
                       latitude, longitude,
                       contact_first_name, contact_last_name,
                       official_contact_email,
                       logo_path,
                       created_at, updated_at
                FROM club
                WHERE upper(coalesce(club_name,'')) LIKE upper(?)
                   OR upper(coalesce(club_number,'')) LIKE upper(?)
                ORDER BY
                   CASE WHEN upper(coalesce(club_name,'')) LIKE upper(?) THEN 0 ELSE 1 END,
                   coalesce(club_name,'')
                LIMIT ?
                """;

        String like = "%" + q + "%";
        String likePrefix = q + "%";

        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, likePrefix);
            ps.setInt(4, lim);

            List<ClubRow> out = new ArrayList<>();
            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(mapClubRow(rs));
                }
            }
            return out;

        } catch (Exception e) {
            throw new RuntimeException("DB error search(club)", e);
        }
    }

    public void updateClubProfile(ClubRow club) {
        if (club == null || club.id() == null || club.id().isBlank()) {
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
                    official_contact_email = ?,
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

            if (club.latitude() == null) {
                ps.setObject(7, null);
            } else {
                ps.setDouble(7, club.latitude());
            }

            if (club.longitude() == null) {
                ps.setObject(8, null);
            } else {
                ps.setDouble(8, club.longitude());
            }

            ps.setString(9, blankToNull(club.contactFirstName()));
            ps.setString(10, blankToNull(club.contactLastName()));
            ps.setString(11, blankToNull(club.officialContactEmail()));
            ps.setString(12, blankToNull(club.logoPath()));
            ps.setString(13, now);
            ps.setString(14, club.id());

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("DB error updateClubProfile(club)", e);
        }
    }

    private ClubRow mapClubRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new ClubRow(
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
                rs.getString("official_contact_email"),
                rs.getString("logo_path"),
                rs.getString("created_at"),
                rs.getString("updated_at"));
    }

    private static String blankToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    public record ClubRow(
            String id,
            String clubNumber,
            String clubName,
            String departementCode,
            String city,
            String address1,
            String address2,
            Double latitude,
            Double longitude,
            String contactFirstName,
            String contactLastName,
            String officialContactEmail,
            String logoPath,
            String createdAt,
            String updatedAt) {
    }
}