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

    public OrganizerAccount insert(String clubName, String email, String passwordHash) {
        String id = "org-" + java.util.UUID.randomUUID();
        String now = Instant.now().toString();

        String sql = """
                INSERT INTO organizer_account(
                  id, club_name, email, password_hash,
                  email_verified, email_verification_code, email_verification_expires_at,
                  created_at, updated_at
                ) VALUES(?,?,?,?,0,NULL,NULL,?,?)
                """;

        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setString(1, id);
            ps.setString(2, clubName);
            ps.setString(3, email);
            ps.setString(4, passwordHash);
            ps.setString(5, now);
            ps.setString(6, now);

            ps.executeUpdate();

            return OrganizerAccount.fromDb(id, clubName, email, passwordHash, false);

        } catch (Exception e) {
            throw new RuntimeException("DB error insert(organizer_account)", e);
        }
    }

    public Optional<OrganizerAccount> findByEmail(String email) {
        String sql = """
                SELECT id, club_name, email, password_hash, email_verified
                FROM organizer_account
                WHERE email = ?
                """;

        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setString(1, email);

            try (var rs = ps.executeQuery()) {
                if (!rs.next())
                    return Optional.empty();

                return Optional.of(
                        OrganizerAccount.fromDb(
                                rs.getString("id"),
                                rs.getString("club_name"),
                                rs.getString("email"),
                                rs.getString("password_hash"),
                                rs.getInt("email_verified") == 1));
            }

        } catch (Exception e) {
            throw new RuntimeException("DB error findByEmail(organizer_account)", e);
        }
    }

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

        // ✅ Vérification + expiration côté SQL
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

            int updated = ps.executeUpdate();
            return updated > 0;

        } catch (Exception e) {
            throw new RuntimeException("DB error verifyEmail", e);
        }
    }

    // (optionnel) utile pour afficher un message "vérifié ou non"
    public boolean isEmailVerified(String email) {
        String sql = "SELECT email_verified FROM organizer_account WHERE email = ?";
        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (var rs = ps.executeQuery()) {
                if (!rs.next())
                    return false;
                return rs.getInt(1) == 1;
            }
        } catch (Exception e) {
            throw new RuntimeException("DB error isEmailVerified", e);
        }
    }
}