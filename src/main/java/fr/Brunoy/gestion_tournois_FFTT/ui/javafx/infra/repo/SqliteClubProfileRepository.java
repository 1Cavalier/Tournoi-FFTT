package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.db.SqliteDb;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.ClubProfileRow;

import java.sql.Connection;
import java.time.Instant;
import java.util.Optional;

public class SqliteClubProfileRepository {

    private final SqliteDb db;

    public SqliteClubProfileRepository(SqliteDb db) {
        this.db = db;
    }

    public Optional<ClubProfileRow> findByOrganizerId(String organizerId) {
        String sql = """
                SELECT organizer_id, club_number, club_name, departement_code, city,
                       address1, address2, latitude, longitude,
                       contact_first_name, contact_last_name, logo_path
                FROM club_profile
                WHERE organizer_id = ?
                """;
        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql)) {
            ps.setString(1, organizerId);

            try (var rs = ps.executeQuery()) {
                if (!rs.next())
                    return Optional.empty();

                Double lat = rs.getObject("latitude") == null ? null : rs.getDouble("latitude");
                Double lon = rs.getObject("longitude") == null ? null : rs.getDouble("longitude");

                return Optional.of(new ClubProfileRow(
                        rs.getString("organizer_id"),
                        rs.getString("club_number"),
                        rs.getString("club_name"),
                        rs.getString("departement_code"),
                        rs.getString("city"),
                        rs.getString("address1"),
                        rs.getString("address2"),
                        lat,
                        lon,
                        rs.getString("contact_first_name"),
                        rs.getString("contact_last_name"),
                        rs.getString("logo_path")));
            }
        } catch (Exception e) {
            throw new RuntimeException("DB error findByOrganizerId(club_profile)", e);
        }
    }

    public void upsert(ClubProfileRow p) {
        String now = Instant.now().toString();

        String sql = """
                INSERT INTO club_profile(
                    organizer_id, club_number, club_name, departement_code, city,
                    address1, address2, latitude, longitude,
                    contact_first_name, contact_last_name, logo_path, updated_at
                ) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)
                ON CONFLICT(organizer_id) DO UPDATE SET
                    club_number=excluded.club_number,
                    club_name=excluded.club_name,
                    departement_code=excluded.departement_code,
                    city=excluded.city,
                    address1=excluded.address1,
                    address2=excluded.address2,
                    latitude=excluded.latitude,
                    longitude=excluded.longitude,
                    contact_first_name=excluded.contact_first_name,
                    contact_last_name=excluded.contact_last_name,
                    logo_path=excluded.logo_path,
                    updated_at=excluded.updated_at
                """;

        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setString(1, p.organizerId());
            ps.setString(2, emptyToNull(p.clubNumber()));
            ps.setString(3, emptyToNull(p.clubName()));
            ps.setString(4, emptyToNull(p.departementCode()));
            ps.setString(5, emptyToNull(p.city()));
            ps.setString(6, emptyToNull(p.address1()));
            ps.setString(7, emptyToNull(p.address2()));

            if (p.latitude() == null)
                ps.setObject(8, null);
            else
                ps.setDouble(8, p.latitude());

            if (p.longitude() == null)
                ps.setObject(9, null);
            else
                ps.setDouble(9, p.longitude());

            ps.setString(10, emptyToNull(p.contactFirstName()));
            ps.setString(11, emptyToNull(p.contactLastName()));
            ps.setString(12, emptyToNull(p.logoPath()));
            ps.setString(13, now);

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("DB error upsert(club_profile)", e);
        }
    }

    private String emptyToNull(String s) {
        if (s == null)
            return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
