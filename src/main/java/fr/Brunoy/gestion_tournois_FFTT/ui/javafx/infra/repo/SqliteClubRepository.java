package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.db.SqliteDb;

import java.sql.Connection;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteClubRepository {

    private final SqliteDb db;

    public SqliteClubRepository(SqliteDb db) {
        this.db = db;
    }

    /**
     * Crée un club minimal. Tu pourras enrichir plus tard via un écran "profil club".
     */
    public String createClub(String clubNumber, String clubName) {
        String id = "club-" + java.util.UUID.randomUUID();
        String now = Instant.now().toString();

        String sql = """
                INSERT INTO club(
                  id, club_number, club_name, updated_at
                ) VALUES(?,?,?,?)
                """;

        try (Connection c = db.openConnection();
             var ps = c.prepareStatement(sql)) {

            ps.setString(1, id);
            ps.setString(2, blankToNull(clubNumber));
            ps.setString(3, blankToNull(clubName));
            ps.setString(4, now);

            ps.executeUpdate();
            return id;

        } catch (Exception e) {
            throw new RuntimeException("DB error createClub", e);
        }
    }

    public Optional<ClubRow> findById(String clubId) {
        String sql = """
                SELECT id, club_number, club_name, departement_code, city, address1, address2,
                       latitude, longitude, contact_first_name, contact_last_name, logo_path, updated_at
                FROM club
                WHERE id = ?
                """;

        try (Connection c = db.openConnection();
             var ps = c.prepareStatement(sql)) {

            ps.setString(1, clubId);

            try (var rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();

                return Optional.of(new ClubRow(
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
                        rs.getString("updated_at")
                ));
            }

        } catch (Exception e) {
            throw new RuntimeException("DB error findClubById", e);
        }
    }

    /**
     * Recherche simple par nom/numéro (LIKE). Tu peux améliorer plus tard.
     */
    public List<ClubRow> search(String query, int limit) {
        String q = (query == null) ? "" : query.trim();
        if (q.isEmpty()) return List.of();

        int lim = (limit <= 0) ? 20 : Math.min(limit, 100);

        String sql = """
                SELECT id, club_number, club_name, departement_code, city, address1, address2,
                       latitude, longitude, contact_first_name, contact_last_name, logo_path, updated_at
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

            try (var rs = ps.executeQuery()) {
                List<ClubRow> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(new ClubRow(
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
                            rs.getString("updated_at")
                    ));
                }
                return out;
            }

        } catch (Exception e) {
            throw new RuntimeException("DB error searchClub", e);
        }
    }

    private static String blankToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /**
     * Petit DTO repo (tu pourras le déplacer dans ui.javafx.model si tu préfères).
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
            String updatedAt
    ) {}
}