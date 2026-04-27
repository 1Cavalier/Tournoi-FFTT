package fr.pingmanager.gestion_tournois_FFTT.ui.javafx.app;

import fr.pingmanager.gestion_tournois_FFTT.domain.identity.FfttParticipant;
import fr.pingmanager.gestion_tournois_FFTT.domain.identity.GuestParticipant;
import fr.pingmanager.gestion_tournois_FFTT.domain.identity.Participant;
import fr.pingmanager.gestion_tournois_FFTT.domain.identity.Player;
import fr.pingmanager.gestion_tournois_FFTT.domain.organization.Club;
import fr.pingmanager.gestion_tournois_FFTT.domain.organization.Departement;
import fr.pingmanager.gestion_tournois_FFTT.domain.organization.Region;
import fr.pingmanager.gestion_tournois_FFTT.domain.refdata.*;
import fr.pingmanager.gestion_tournois_FFTT.infra.db.SqliteDb;
import fr.pingmanager.gestion_tournois_FFTT.infra.repo.ParticipantResolver;

import java.sql.*;
import java.util.Objects;

/**
 * Implémentation de ParticipantResolver.
 *
 * Stratégie :
 * 1. Si l'ID commence par "GUEST-" → cherche dans la table guest_participant
 * (competition.db)
 * 2. Sinon → cherche dans la table player (club.db) → FfttParticipant
 *
 * La table guest_participant est une table légère créée dans competition.db
 * pour stocker les invités inscrits aux tournois.
 */
public class PlayerParticipantResolver implements ParticipantResolver {

    private final SqliteDb clubDb;
    private final SqliteDb competitionDb;

    public PlayerParticipantResolver(SqliteDb clubDb, SqliteDb competitionDb) {
        this.clubDb = Objects.requireNonNull(clubDb);
        this.competitionDb = Objects.requireNonNull(competitionDb);
    }

    @Override
    public Participant resolve(String participantId) {
        Objects.requireNonNull(participantId, "participantId");

        if (participantId.startsWith("GUEST-")) {
            return resolveGuest(participantId);
        }
        if (participantId.startsWith("FOREIGN-")) {
            // Pour l'instant : résoudre comme un guest simplifié
            return resolveGuest(participantId);
        }
        return resolveFftt(participantId);
    }

    // -------------------------------------------------------------------------
    // FFTT
    // -------------------------------------------------------------------------

    private FfttParticipant resolveFftt(String licenseNumber) {
        String sql = """
                SELECT p.license_number, p.first_name, p.last_name,
                       p.gender, p.nationality, p.age_category,
                       p.license_type, p.medical_cert_status,
                       p.phase1_start_points, p.phase2_official_points,
                       p.club_number,
                       c.name AS club_name, c.city AS club_city,
                       d.code AS dep_code, d.name AS dep_name,
                       r.code AS reg_code, r.name AS reg_name
                FROM player p
                JOIN club c ON c.number = p.club_number
                JOIN departement d ON d.code = c.department_code
                JOIN region r ON r.code = d.region_code
                WHERE p.license_number = ?
                """;
        try (Connection c = clubDb.openConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, licenseNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new FfttParticipant(mapPlayer(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("resolve FFTT participant failed: " + licenseNumber, e);
        }
        throw new RuntimeException("Participant FFTT introuvable : " + licenseNumber);
    }

    private Player mapPlayer(ResultSet rs) throws SQLException {
        Region region = new Region(rs.getString("reg_code"), rs.getString("reg_name"));
        Departement dep = new Departement(rs.getString("dep_code"), rs.getString("dep_name"), region);
        Club club = new Club(rs.getString("club_number"), rs.getString("club_name"),
                dep, rs.getString("club_city"), null, null, null, null);

        return new Player(
                rs.getString("license_number"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                Gender.valueOf(rs.getString("gender")),
                rs.getString("nationality"),
                club,
                AgeCategory.valueOf(rs.getString("age_category")),
                LicenseType.valueOf(rs.getString("license_type")),
                false,
                MedicalCertificateStatus.valueOf(rs.getString("medical_cert_status")),
                rs.getInt("phase1_start_points"),
                rs.getInt("phase2_official_points"));
    }

    // -------------------------------------------------------------------------
    // GUEST
    // -------------------------------------------------------------------------

    private GuestParticipant resolveGuest(String guestId) {
        String sql = """
                SELECT guest_id, full_name, gender, nationality, age_category, medical_cert_status
                FROM guest_participant
                WHERE guest_id = ?
                """;
        try (Connection c = competitionDb.openConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, guestId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new GuestParticipant(
                            rs.getString("guest_id"),
                            rs.getString("full_name"),
                            Gender.valueOf(rs.getString("gender")),
                            rs.getString("nationality"),
                            AgeCategory.valueOf(rs.getString("age_category")),
                            MedicalCertificateStatus.valueOf(rs.getString("medical_cert_status")));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("resolve guest participant failed: " + guestId, e);
        }
        throw new RuntimeException("Participant invité introuvable : " + guestId);
    }
}