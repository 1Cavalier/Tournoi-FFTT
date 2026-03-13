package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.TournamentRegulationDto;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.db.SqliteDb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

public class TournamentRegulationRepositorySqlite implements TournamentRegulationRepository {

    private final SqliteDb db;

    public TournamentRegulationRepositorySqlite(SqliteDb db) {
        this.db = db;
    }

    @Override
    public TournamentRegulationDto insert(TournamentRegulationDto r) {
        String sql = """
                INSERT INTO tournament_regulation (
                    tournament_id,
                    organizer_contact_name, organizer_email, organizer_phone,
                    venue_name, venue_street, venue_zip, venue_city,
                    number_of_tables,
                    playing_area_preset, playing_area_info_text,
                    playing_area_length_meters, playing_area_width_meters, playing_area_compliant,
                    ball_brand_and_type, ball_provision_policy,
                    registration_open_time, registration_deadline, gym_open_time,
                    created_at, updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection c = db.openConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, r.tournamentId());

            ps.setString(2, r.organizerContactName());
            ps.setString(3, r.organizerEmail());
            ps.setString(4, r.organizerPhone());

            ps.setString(5, r.venueName());
            ps.setString(6, r.venueStreet());
            ps.setString(7, r.venueZip());
            ps.setString(8, r.venueCity());

            if (r.numberOfTables() != null) {
                ps.setInt(9, r.numberOfTables());
            } else {
                ps.setObject(9, null);
            }

            ps.setString(10, r.playingAreaPreset());
            ps.setString(11, r.playingAreaInfoText());

            if (r.playingAreaLengthMeters() != null) {
                ps.setInt(12, r.playingAreaLengthMeters());
            } else {
                ps.setObject(12, null);
            }

            if (r.playingAreaWidthMeters() != null) {
                ps.setInt(13, r.playingAreaWidthMeters());
            } else {
                ps.setObject(13, null);
            }

            if (r.playingAreaCompliant() != null) {
                ps.setInt(14, r.playingAreaCompliant() ? 1 : 0);
            } else {
                ps.setObject(14, null);
            }

            ps.setString(15, r.ballBrandAndType());
            ps.setString(16, r.ballProvisionPolicy());

            ps.setString(17, r.registrationOpenTime());
            ps.setString(18, r.registrationDeadline());
            ps.setString(19, r.gymOpenTime());

            ps.setString(20, r.createdAt());
            ps.setString(21, r.updatedAt());

            ps.executeUpdate();
            return r;

        } catch (Exception e) {
            throw new RuntimeException("Insert tournament regulation failed", e);
        }
    }

    @Override
    public Optional<TournamentRegulationDto> findByTournamentId(String tournamentId) {
        String sql = "SELECT * FROM tournament_regulation WHERE tournament_id = ?";

        try (Connection c = db.openConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, tournamentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }

            return Optional.empty();

        } catch (Exception e) {
            throw new RuntimeException("Find tournament regulation by tournament id failed", e);
        }
    }

    @Override
    public void update(TournamentRegulationDto r) {
        String sql = """
                UPDATE tournament_regulation SET
                    organizer_contact_name = ?,
                    organizer_email = ?,
                    organizer_phone = ?,
                    venue_name = ?,
                    venue_street = ?,
                    venue_zip = ?,
                    venue_city = ?,
                    number_of_tables = ?,
                    playing_area_preset = ?,
                    playing_area_info_text = ?,
                    playing_area_length_meters = ?,
                    playing_area_width_meters = ?,
                    playing_area_compliant = ?,
                    ball_brand_and_type = ?,
                    ball_provision_policy = ?,
                    registration_open_time = ?,
                    registration_deadline = ?,
                    gym_open_time = ?,
                    updated_at = ?
                WHERE tournament_id = ?
                """;

        try (Connection c = db.openConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, r.organizerContactName());
            ps.setString(2, r.organizerEmail());
            ps.setString(3, r.organizerPhone());

            ps.setString(4, r.venueName());
            ps.setString(5, r.venueStreet());
            ps.setString(6, r.venueZip());
            ps.setString(7, r.venueCity());

            if (r.numberOfTables() != null) {
                ps.setInt(8, r.numberOfTables());
            } else {
                ps.setObject(8, null);
            }

            ps.setString(9, r.playingAreaPreset());
            ps.setString(10, r.playingAreaInfoText());

            if (r.playingAreaLengthMeters() != null) {
                ps.setInt(11, r.playingAreaLengthMeters());
            } else {
                ps.setObject(11, null);
            }

            if (r.playingAreaWidthMeters() != null) {
                ps.setInt(12, r.playingAreaWidthMeters());
            } else {
                ps.setObject(12, null);
            }

            if (r.playingAreaCompliant() != null) {
                ps.setInt(13, r.playingAreaCompliant() ? 1 : 0);
            } else {
                ps.setObject(13, null);
            }

            ps.setString(14, r.ballBrandAndType());
            ps.setString(15, r.ballProvisionPolicy());

            ps.setString(16, r.registrationOpenTime());
            ps.setString(17, r.registrationDeadline());
            ps.setString(18, r.gymOpenTime());

            ps.setString(19, r.updatedAt());
            ps.setString(20, r.tournamentId());

            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new RuntimeException("Tournament regulation not found for update: " + r.tournamentId());
            }

        } catch (Exception e) {
            throw new RuntimeException("Update tournament regulation failed", e);
        }
    }

    @Override
    public void deleteByTournamentId(String tournamentId) {
        String sql = "DELETE FROM tournament_regulation WHERE tournament_id = ?";

        try (Connection c = db.openConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, tournamentId);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Delete tournament regulation failed", e);
        }
    }

    private TournamentRegulationDto map(ResultSet rs) throws Exception {
        Integer numberOfTables = rs.getObject("number_of_tables") == null
                ? null
                : rs.getInt("number_of_tables");

        Integer playingAreaLengthMeters = rs.getObject("playing_area_length_meters") == null
                ? null
                : rs.getInt("playing_area_length_meters");

        Integer playingAreaWidthMeters = rs.getObject("playing_area_width_meters") == null
                ? null
                : rs.getInt("playing_area_width_meters");

        Boolean playingAreaCompliant = null;
        Object compliantRaw = rs.getObject("playing_area_compliant");
        if (compliantRaw != null) {
            playingAreaCompliant = rs.getInt("playing_area_compliant") == 1;
        }

        return new TournamentRegulationDto(
                rs.getString("tournament_id"),

                rs.getString("organizer_contact_name"),
                rs.getString("organizer_email"),
                rs.getString("organizer_phone"),

                rs.getString("venue_name"),
                rs.getString("venue_street"),
                rs.getString("venue_zip"),
                rs.getString("venue_city"),

                numberOfTables,

                rs.getString("playing_area_preset"),
                rs.getString("playing_area_info_text"),
                playingAreaLengthMeters,
                playingAreaWidthMeters,
                playingAreaCompliant,

                rs.getString("ball_brand_and_type"),
                rs.getString("ball_provision_policy"),

                rs.getString("registration_open_time"),
                rs.getString("registration_deadline"),
                rs.getString("gym_open_time"),

                rs.getString("created_at"),
                rs.getString("updated_at"));
    }
}
