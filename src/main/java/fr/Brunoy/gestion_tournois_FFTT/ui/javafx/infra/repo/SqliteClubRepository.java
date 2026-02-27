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
 * - CRUD de base sur la table club
 * - Recherche par nom/numéro
 * - Récupération du club associé à un organisateur via
 * organizer_account.club_id (option 2 "pro")
 */
public class SqliteClubRepository {

    private final SqliteDb db;

    public SqliteClubRepository(SqliteDb db) {
        this.db = db;
    }

    /**
     * Crée un club minimal.
     * Utilisé lors de l'inscription si l'organisme crée un nouveau club.
     *
     * @return l'id du club créé
     */
    public String createClub(String clubNumberOrNull, String clubNameOrNull) {
        String id = "club-" + java.util.UUID.randomUUID();
        String now = Instant.now().toString();

        String sql = """
                INSERT INTO club(
                  id,
                  club_number, club_name,
                  departement_code, city, address1, address2,
                  latitude, longitude,
                  contact_first_name, contact_last_name,
                  logo_path,
                  updated_at
                ) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)
                """;

        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setString(1, id);

            ps.setString(2, blankToNull(clubNumberOrNull));
            ps.setString(3, blankToNull(clubNameOrNull));

            // profil (vide au départ)
            ps.setString(4, null);
            ps.setString(5, null);
            ps.setString(6, null);
            ps.setString(7, null);

            ps.setObject(8, null);
            ps.setObject(9, null);

            ps.setString(10, null);
            ps.setString(11, null);

            ps.setString(12, null);

            ps.setString(13, now);

            ps.executeUpdate();
            return id;

        } catch (Exception e) {
            throw new RuntimeException("DB error createClub(club)", e);
        }
    }

    /**
     * Charge un club par son id.
     */
    public Optional<ClubRow> findById(String clubId) {
        String sql = """
                SELECT id,
                       club_number, club_name,
                       departement_code, city, address1, address2,
                       latitude, longitude,
                       contact_first_name, contact_last_name,
                       logo_path,
                       updated_at
                FROM club
                WHERE id = ?
                """;

        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setString(1, clubId);

            try (var rs = ps.executeQuery()) {
                if (!rs.next())
                    return Optional.empty();
                return Optional.of(mapClubRow(rs));
            }

        } catch (Exception e) {
            throw new RuntimeException("DB error findById(club)", e);
        }
    }

    /**
     * Option 2 "pro" : récupérer le club associé à un organisateur via
     * organizer_account.club_id.
     */
    public Optional<ClubRow> findByOrganizerId(String organizerId) {
        String sql = """
                SELECT c.id,
                       c.club_number, c.club_name,
                       c.departement_code, c.city, c.address1, c.address2,
                       c.latitude, c.longitude,
                       c.contact_first_name, c.contact_last_name,
                       c.logo_path,
                       c.updated_at
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
                return Optional.of(mapClubRow(rs));
            }

        } catch (Exception e) {
            throw new RuntimeException("DB error findByOrganizerId(club)", e);
        }
    }

    /**
     * Recherche un club par nom ou numéro FFTT (LIKE).
     * Limite max = 100 pour éviter de surcharger la UI.
     */
    public List<ClubRow> search(String query, int limit) {
        String q = (query == null) ? "" : query.trim();
        if (q.isEmpty())
            return List.of();

        int lim = (limit <= 0) ? 20 : Math.min(limit, 100);

        String sql = """
                SELECT id,
                       club_number, club_name,
                       departement_code, city, address1, address2,
                       latitude, longitude,
                       contact_first_name, contact_last_name,
                       logo_path,
                       updated_at
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
                while (rs.next())
                    out.add(mapClubRow(rs));
            }
            return out;

        } catch (Exception e) {
            throw new RuntimeException("DB error search(club)", e);
        }
    }

    /**
     * Met à jour le "profil" du club (ville, département, adresse, contact, logo,
     * coordonnées...).
     * À utiliser depuis ton futur OrganizerProfileDialog (option 2).
     *
     * Remarque : ici on met à jour toutes les colonnes de profil, en laissant null
     * si vide.
     */
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

            if (club.latitude() == null)
                ps.setObject(7, null);
            else
                ps.setDouble(7, club.latitude());

            if (club.longitude() == null)
                ps.setObject(8, null);
            else
                ps.setDouble(8, club.longitude());

            ps.setString(9, blankToNull(club.contactFirstName()));
            ps.setString(10, blankToNull(club.contactLastName()));
            ps.setString(11, blankToNull(club.logoPath()));

            ps.setString(12, now);
            ps.setString(13, club.id());

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("DB error updateClubProfile(club)", e);
        }
    }

    // ------------------ Helpers ------------------

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
                rs.getString("logo_path"),
                rs.getString("updated_at"));
    }

    private static String blankToNull(String s) {
        if (s == null)
            return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /**
     * DTO transport pour la UI.
     * Si tu préfères, tu peux le déplacer dans ui.javafx.model.
     */
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
            String logoPath,
            String updatedAt) {
    }
}