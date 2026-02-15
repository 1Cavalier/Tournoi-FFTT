package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.db.SqliteDb;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;

import java.sql.Connection;
import java.time.Instant;
import java.util.Optional;

public class SqliteOrganizerAccountRepository implements OrganizerAccountRepository {

    private final SqliteDb db;

    public SqliteOrganizerAccountRepository(SqliteDb db) {
        this.db = db;
    }

    @Override
    public boolean isEmpty() {
        String sql = "SELECT COUNT(*) FROM organizer_account";
        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql);
                var rs = ps.executeQuery()) {
            return rs.next() && rs.getLong(1) == 0;
        } catch (Exception e) {
            throw new RuntimeException("DB error isEmpty", e);
        }
    }

    @Override
    public Optional<OrganizerAccount> findByEmail(String email) {
        String sql = "SELECT id, club_name, email, password_hash FROM organizer_account WHERE lower(email)=lower(?)";
        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (var rs = ps.executeQuery()) {
                if (!rs.next())
                    return Optional.empty();
                return Optional.of(new OrganizerAccount(
                        rs.getString("id"),
                        rs.getString("club_name"),
                        rs.getString("email"),
                        rs.getString("password_hash")));
            }
        } catch (Exception e) {
            throw new RuntimeException("DB error findByEmail", e);
        }
    }

    @Override
    public void insert(OrganizerAccount acc) {
        String sql = """
                INSERT INTO organizer_account(id, club_name, email, password_hash, created_at, updated_at)
                VALUES(?,?,?,?,?,?)
                """;
        String now = Instant.now().toString();
        try (Connection c = db.openConnection();
                var ps = c.prepareStatement(sql)) {
            ps.setString(1, acc.getId());
            ps.setString(2, acc.getClubName());
            ps.setString(3, acc.getEmail());
            ps.setString(4, acc.getPasswordHash());
            ps.setString(5, now);
            ps.setString(6, now);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("DB error insert organizer", e);
        }
    }
}
