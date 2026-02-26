package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.db.SqliteDb;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;

import java.sql.Connection;
import java.time.Instant;
import java.util.Optional;

public class SqliteOrganizerAccountRepository {

    private final SqliteDb db;

    public SqliteOrganizerAccountRepository(SqliteDb db) {
        this.db = db;
    }

    /**
     * Crée un compte ORGANISME rattaché à un club existant.
     * email_verified=0 à la création.
     */
    public OrganizerAccount insert(String clubId, String email, String passwordHash) {
        String id = "org-" + java.util.UUID.randomUUID();
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

            // OrganizerAccount contient clubName dans ton modèle actuel.
            // Ici on n’a pas le nom -> on met une valeur vide (tu peux charger via club
            // repo plus tard)
            return OrganizerAccount.fromDb(id, "", email, passwordHash, false);

        } catch (Exception e) {
            throw new RuntimeException("DB error insert(organizer_account)", e);
        }
    }

    /**
     * Récupère le compte par email (sert au login, resend code, etc.)
     */
    public Optional<AccountDbRow> findDbRowByEmail(String email) {
        String sql = """
                SELECT id, club_id, email, password_hash, email_verified
                FROM organizer_account
                WHERE email = ?
                """;

        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setString(1, email);

            try (var rs = ps.executeQuery()) {
                if (!rs.next())
                    return Optional.empty();

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
     * Compat : retourne OrganizerAccount (ton modèle UI).
     * clubName est vide ici (à enrichir ensuite via join club si tu veux).
     */
    public Optional<OrganizerAccount> findByEmail(String email) {
        return findDbRowByEmail(email)
                .map(r -> OrganizerAccount.fromDb(r.id(), "", r.email(), r.passwordHash(), r.emailVerified()));
    }

    // ---------------- EMAIL VERIFICATION (inscription) ----------------

    public void setEmailVerification(String organizerId, String code, String expiresAt) {
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

    // ---------------- LOGIN OTP (à chaque connexion) ----------------

    public void setLoginOtp(String organizerId, String otpCode, String expiresAt) {
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

    // DTO interne repo
    public record AccountDbRow(
            String id,
            String clubId,
            String email,
            String passwordHash,
            boolean emailVerified) {
    }
}