package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.db.SqliteDb;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;

import java.sql.Connection;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class SqliteOrganizerAccountRepository {

    private final SqliteDb db;

    public SqliteOrganizerAccountRepository(SqliteDb db) {
        this.db = Objects.requireNonNull(db, "db must not be null");
    }

    /**
     * Crée un compte organisateur rattaché à un club existant.
     * email_verified = 0 à la création.
     */
    public OrganizerAccount insert(String clubId, String email, String passwordHash) {
        requireNotBlank(clubId, "clubId obligatoire");
        requireNotBlank(email, "email obligatoire");
        requireNotBlank(passwordHash, "passwordHash obligatoire");

        String id = "org-" + UUID.randomUUID();
        String now = Instant.now().toString();

        String sql = """
                INSERT INTO organizer_account(
                  id, club_id, email, password_hash,
                  email_verified, email_verification_code, email_verification_expires_at,
                  login_otp_code, login_otp_expires_at,
                  created_at, updated_at
                ) VALUES(?,?,?,?,0,NULL,NULL,NULL,NULL,?,?)
                """;

        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setString(1, id);
            ps.setString(2, clubId);
            ps.setString(3, email);
            ps.setString(4, passwordHash);
            ps.setString(5, now);
            ps.setString(6, now);

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("DB error insert(organizer_account)", e);
        }

        return findById(id)
                .orElseThrow(() -> new RuntimeException("Compte créé mais introuvable après insertion."));
    }

    /**
     * Lecture technique minimale pour auth / OTP / vérification email.
     */
    public Optional<AccountDbRow> findDbRowByEmail(String email) {
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

                return Optional.of(new AccountDbRow(
                        rs.getString("id"),
                        rs.getString("club_id"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getInt("email_verified") == 1));
            }

        } catch (Exception e) {
            throw new RuntimeException("DB error findDbRowByEmail", e);
        }
    }

    /**
     * Retourne le compte UI enrichi avec le nom du club.
     */
    public Optional<OrganizerAccount> findByEmail(String email) {
        requireNotBlank(email, "email obligatoire");

        String sql = """
                SELECT oa.id,
                       oa.email,
                       oa.password_hash,
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

                return Optional.of(OrganizerAccount.fromDb(
                        rs.getString("id"),
                        rs.getString("club_name"),
                        rs.getString("email"),
                        rs.getInt("email_verified") == 1));
            }

        } catch (Exception e) {
            throw new RuntimeException("DB error findByEmail", e);
        }
    }

    /**
     * Retourne le compte UI enrichi avec le nom du club.
     */
    public Optional<OrganizerAccount> findById(String organizerId) {
        requireNotBlank(organizerId, "organizerId obligatoire");

        String sql = """
                SELECT oa.id,
                       oa.email,
                       oa.password_hash,
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

                return Optional.of(OrganizerAccount.fromDb(
                        rs.getString("id"),
                        rs.getString("club_name"),
                        rs.getString("email"),
                        rs.getInt("email_verified") == 1));
            }

        } catch (Exception e) {
            throw new RuntimeException("DB error findById", e);
        }
    }

    // -------------------------------------------------------------------------
    // EMAIL VERIFICATION
    // -------------------------------------------------------------------------

    public void setEmailVerification(String organizerId, String code, String expiresAt) {
        requireNotBlank(organizerId, "organizerId obligatoire");
        requireNotBlank(code, "code obligatoire");
        requireNotBlank(expiresAt, "expiresAt obligatoire");

        String sql = """
                UPDATE organizer_account
                SET email_verification_code = ?,
                    email_verification_expires_at = ?,
                    updated_at = ?
                WHERE id = ?
                """;

        String now = Instant.now().toString();

        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setString(1, code);
            ps.setString(2, expiresAt);
            ps.setString(3, now);
            ps.setString(4, organizerId);

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("DB error setEmailVerification", e);
        }
    }

    public boolean verifyEmail(String email, String code) {
        requireNotBlank(email, "email obligatoire");
        requireNotBlank(code, "code obligatoire");

        String sql = """
                UPDATE organizer_account
                SET email_verified = 1,
                    email_verification_code = NULL,
                    email_verification_expires_at = NULL,
                    updated_at = ?
                WHERE email = ?
                  AND email_verification_code = ?
                  AND email_verification_expires_at IS NOT NULL
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

    // -------------------------------------------------------------------------
    // LOGIN OTP
    // -------------------------------------------------------------------------

    public void setLoginOtp(String organizerId, String otpCode, String expiresAt) {
        requireNotBlank(organizerId, "organizerId obligatoire");
        requireNotBlank(otpCode, "otpCode obligatoire");
        requireNotBlank(expiresAt, "expiresAt obligatoire");

        String sql = """
                UPDATE organizer_account
                SET login_otp_code = ?,
                    login_otp_expires_at = ?,
                    updated_at = ?
                WHERE id = ?
                """;

        String now = Instant.now().toString();

        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setString(1, otpCode);
            ps.setString(2, expiresAt);
            ps.setString(3, now);
            ps.setString(4, organizerId);

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("DB error setLoginOtp", e);
        }
    }

    public boolean verifyLoginOtp(String email, String otpCode) {
        requireNotBlank(email, "email obligatoire");
        requireNotBlank(otpCode, "otpCode obligatoire");

        String sql = """
                UPDATE organizer_account
                SET login_otp_code = NULL,
                    login_otp_expires_at = NULL,
                    updated_at = ?
                WHERE email = ?
                  AND login_otp_code = ?
                  AND login_otp_expires_at IS NOT NULL
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

    private void requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    public record AccountDbRow(
            String id,
            String clubId,
            String email,
            String passwordHash,
            boolean emailVerified) {
    }
}