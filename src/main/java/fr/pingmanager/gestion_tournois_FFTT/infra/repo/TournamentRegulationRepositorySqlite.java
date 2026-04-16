package fr.pingmanager.gestion_tournois_FFTT.infra.repo;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import fr.pingmanager.gestion_tournois_FFTT.infra.db.SqliteDb;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TournamentOfficialAssignmentDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TournamentRegulationDto;

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
                    required_judge_grade, recommended_judge_count,
                    recommended_referee_grade, recommended_referee_count,
                    assigned_officials_json,
                    created_at, updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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

            ps.setString(20, r.requiredJudgeGrade());

            if (r.recommendedJudgeCount() != null) {
                ps.setInt(21, r.recommendedJudgeCount());
            } else {
                ps.setObject(21, null);
            }

            ps.setString(22, r.recommendedRefereeGrade());

            if (r.recommendedRefereeCount() != null) {
                ps.setInt(23, r.recommendedRefereeCount());
            } else {
                ps.setObject(23, null);
            }

            ps.setString(24, serializeAssignedOfficials(r.assignedOfficials()));

            ps.setString(25, r.createdAt());
            ps.setString(26, r.updatedAt());

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
                    required_judge_grade = ?,
                    recommended_judge_count = ?,
                    recommended_referee_grade = ?,
                    recommended_referee_count = ?,
                    assigned_officials_json = ?,
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

            ps.setString(19, r.requiredJudgeGrade());

            if (r.recommendedJudgeCount() != null) {
                ps.setInt(20, r.recommendedJudgeCount());
            } else {
                ps.setObject(20, null);
            }

            ps.setString(21, r.recommendedRefereeGrade());

            if (r.recommendedRefereeCount() != null) {
                ps.setInt(22, r.recommendedRefereeCount());
            } else {
                ps.setObject(22, null);
            }

            ps.setString(23, serializeAssignedOfficials(r.assignedOfficials()));

            ps.setString(24, r.updatedAt());
            ps.setString(25, r.tournamentId());

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

        Integer recommendedJudgeCount = rs.getObject("recommended_judge_count") == null
                ? null
                : rs.getInt("recommended_judge_count");

        Integer recommendedRefereeCount = rs.getObject("recommended_referee_count") == null
                ? null
                : rs.getInt("recommended_referee_count");

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

                rs.getString("required_judge_grade"),
                recommendedJudgeCount,
                rs.getString("recommended_referee_grade"),
                recommendedRefereeCount,
                deserializeAssignedOfficials(rs.getString("assigned_officials_json")),

                rs.getString("created_at"),
                rs.getString("updated_at"));
    }

    private String serializeAssignedOfficials(List<TournamentOfficialAssignmentDto> officials) {
        if (officials == null || officials.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (TournamentOfficialAssignmentDto o : officials) {
            if (sb.length() > 0) {
                sb.append("\n");
            }

            sb.append(enc(o.playerLicenseNumber())).append("|")
                    .append(enc(o.firstName())).append("|")
                    .append(enc(o.lastName())).append("|")
                    .append(enc(o.clubName())).append("|")
                    .append(enc(o.officialRoleType())).append("|")
                    .append(enc(o.judgeGrade())).append("|")
                    .append(enc(o.refereeGrade())).append("|")
                    .append(enc(boolToString(o.designatedMainJudge()))).append("|")
                    .append(enc(boolToString(o.assistantJudge()))).append("|")
                    .append(enc(boolToString(o.activeForFinalsOnly())));
        }
        return sb.toString();
    }

    private List<TournamentOfficialAssignmentDto> deserializeAssignedOfficials(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }

        List<TournamentOfficialAssignmentDto> result = new ArrayList<>();
        String[] lines = raw.split("\\R");

        for (String line : lines) {
            if (line == null || line.isBlank()) {
                continue;
            }

            String[] parts = line.split("\\|", -1);
            if (parts.length < 10) {
                continue;
            }

            result.add(new TournamentOfficialAssignmentDto(
                    dec(parts[0]),
                    dec(parts[1]),
                    dec(parts[2]),
                    dec(parts[3]),
                    dec(parts[4]),
                    dec(parts[5]),
                    dec(parts[6]),
                    parseBoolean(dec(parts[7])),
                    parseBoolean(dec(parts[8])),
                    parseBoolean(dec(parts[9]))));
        }

        return List.copyOf(result);
    }

    private String enc(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private String dec(String value) {
        String decoded = URLDecoder.decode(value == null ? "" : value, StandardCharsets.UTF_8);
        return decoded.isEmpty() ? null : decoded;
    }

    private String boolToString(Boolean value) {
        return value == null ? "" : value.toString();
    }

    private Boolean parseBoolean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Boolean.parseBoolean(value);
    }
}