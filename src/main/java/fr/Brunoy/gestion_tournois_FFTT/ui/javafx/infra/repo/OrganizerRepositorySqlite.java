package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.OrganizerDto;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.db.SqliteDb;

import java.sql.Connection;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Implémentation SQLite du OrganizerRepository.
 */
public class OrganizerRepositorySqlite implements OrganizerRepository {

    private final SqliteDb db;

    public OrganizerRepositorySqlite(SqliteDb db) {
        this.db = Objects.requireNonNull(db, "db must not be null");
    }

    @Override
    public OrganizerDto insert(String clubId, String firstName, String lastName, String email, String passwordHash) {

        requireNotBlank(clubId, "clubId obligatoire");
        requireNotBlank(firstName, "firstName obligatoire");
        requireNotBlank(lastName, "lastName obligatoire");
        requireNotBlank(email, "email obligatoire");
        requireNotBlank(passwordHash, "passwordHash obligatoire");

        String id = "org-" + UUID.randomUUID();
        String now = Instant.now().toString();

        String sql = """
                INSERT INTO organizer_account(
                  id, club_id, first_name, last_name, email, password_hash,
                  email_verified,
                  created_at, updated_at
                ) VALUES(?,?,?,?,?, ?,0,?,?)
                """;

        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setString(1, id);
            ps.setString(2, clubId);
            ps.setString(3, blankToNull(firstName));
            ps.setString(4, blankToNull(lastName));
            ps.setString(5, email);
            ps.setString(6, passwordHash);
            ps.setString(7, now);
            ps.setString(8, now);

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("DB error insert organizer", e);
        }

        return findById(id)
                .orElseThrow(() -> new RuntimeException("Organizer created but not found"));
    }

    @Override
    public Optional<OrganizerDto> findByEmail(String email) {

        requireNotBlank(email, "email obligatoire");

        String sql = """
                SELECT oa.id,
                       oa.first_name,
                       oa.last_name,
                       oa.email,
                       oa.email_verified,
                       c.club_name
                FROM organizer_account oa
                JOIN club c ON c.id = oa.club_id
                WHERE oa.email = ?
                """;

        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setString(1, email);

            try (var rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return Optional.empty();
                }

                return Optional.of(new OrganizerDto(
                        rs.getString("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("club_name"),
                        rs.getString("email"),
                        rs.getInt("email_verified") == 1));
            }

        } catch (Exception e) {
            throw new RuntimeException("DB error findByEmail", e);
        }
    }

    @Override
    public Optional<OrganizerDto> findById(String organizerId) {

        requireNotBlank(organizerId, "organizerId obligatoire");

        String sql = """
                SELECT oa.id,
                       oa.first_name,
                       oa.last_name,
                       oa.email,
                       oa.email_verified,
                       c.club_name
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

                return Optional.of(new OrganizerDto(
                        rs.getString("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("club_name"),
                        rs.getString("email"),
                        rs.getInt("email_verified") == 1));
            }

        } catch (Exception e) {
            throw new RuntimeException("DB error findById", e);
        }
    }

    @Override
    public void setEmailVerification(String organizerId, String code, String expiresAt) {

        String sql = """
                UPDATE organizer_account
                SET email_verification_code = ?,
                    email_verification_expires_at = ?,
                    updated_at = ?
                WHERE id = ?
                """;

        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setString(1, code);
            ps.setString(2, expiresAt);
            ps.setString(3, Instant.now().toString());
            ps.setString(4, organizerId);

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("DB error setEmailVerification", e);
        }
    }

    @Override
    public boolean verifyEmail(String email, String code) {

        String sql = """
                UPDATE organizer_account
                SET email_verified = 1,
                    email_verification_code = NULL,
                    email_verification_expires_at = NULL,
                    updated_at = ?
                WHERE email = ?
                  AND email_verification_code = ?
                  AND email_verification_expires_at > ?
                """;

        String now = Instant.now().toString();

        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setString(1, now);
            ps.setString(2, email);
            ps.setString(3, code);
            ps.setString(4, now);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            throw new RuntimeException("DB error verifyEmail", e);
        }
    }

    @Override
    public void setLoginOtp(String organizerId, String otpCode, String expiresAt) {

        String sql = """
                UPDATE organizer_account
                SET login_otp_code = ?,
                    login_otp_expires_at = ?,
                    updated_at = ?
                WHERE id = ?
                """;

        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setString(1, otpCode);
            ps.setString(2, expiresAt);
            ps.setString(3, Instant.now().toString());
            ps.setString(4, organizerId);

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("DB error setLoginOtp", e);
        }
    }

    @Override
    public boolean verifyLoginOtp(String email, String otpCode) {

        String sql = """
                UPDATE organizer_account
                SET login_otp_code = NULL,
                    login_otp_expires_at = NULL,
                    updated_at = ?
                WHERE email = ?
                  AND login_otp_code = ?
                  AND login_otp_expires_at > ?
                """;

        String now = Instant.now().toString();

        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setString(1, now);
            ps.setString(2, email);
            ps.setString(3, otpCode);
            ps.setString(4, now);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            throw new RuntimeException("DB error verifyLoginOtp", e);
        }
    }

    @Override
    public Optional<AuthOrganizerRow> findAuthByEmail(String email) {

        requireNotBlank(email, "email obligatoire");

        String sql = """
                SELECT id, club_id, email, password_hash, email_verified
                FROM organizer_account
                WHERE email = ?
                """;

        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setString(1, email);

            try (var rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return Optional.empty();
                }

                return Optional.of(new AuthOrganizerRow(
                        rs.getString("id"),
                        rs.getString("club_id"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getInt("email_verified") == 1));
            }

        } catch (Exception e) {
            throw new RuntimeException("DB error findAuthByEmail", e);
        }
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}