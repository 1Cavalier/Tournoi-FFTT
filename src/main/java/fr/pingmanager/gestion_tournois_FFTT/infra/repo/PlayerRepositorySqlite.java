package fr.pingmanager.gestion_tournois_FFTT.infra.repo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import fr.pingmanager.gestion_tournois_FFTT.infra.db.SqliteDb;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.OfficialSelectablePlayerDto;

/**
 * Implémentation SQLite du PlayerRepository.
 * Interroge club.db — tables player + official_qualification.
 */
public class PlayerRepositorySqlite implements PlayerRepository {

    private final SqliteDb db;

    public PlayerRepositorySqlite(SqliteDb db) {
        this.db = db;
    }

    // -------------------------------------------------------------------------
    // RECHERCHE JUGES-ARBITRES
    // -------------------------------------------------------------------------

    @Override
    public List<OfficialSelectablePlayerDto> searchJudgeReferees(String query, int limit) {
        return searchByRole(query, "JUGE_ARBITRE", limit);
    }

    // -------------------------------------------------------------------------
    // RECHERCHE ARBITRES
    // -------------------------------------------------------------------------

    @Override
    public List<OfficialSelectablePlayerDto> searchReferees(String query, int limit) {
        return searchByRole(query, "ARBITRE", limit);
    }

    // -------------------------------------------------------------------------
    // INTERNE
    // -------------------------------------------------------------------------

    private List<OfficialSelectablePlayerDto> searchByRole(String query, String roleType, int limit) {
        String q = query == null ? "" : query.trim();
        if (q.isBlank()) {
            return List.of();
        }

        int lim = limit <= 0 ? 20 : Math.min(limit, 100);
        String like = "%" + q.toUpperCase() + "%";

        // Jointure player + official_qualification filtrée sur le rôle.
        // On remonte le grade correspondant au rôle demandé.
        String sql = """
                SELECT
                    p.license_number,
                    p.first_name,
                    p.last_name,
                    p.club_name,
                    oq.judge_referee_grade,
                    oq.referee_grade
                FROM player p
                JOIN official_qualification oq ON oq.player_id = p.id
                WHERE oq.role_type = ?
                  AND (
                      upper(p.last_name)      LIKE ?
                   OR upper(p.first_name)     LIKE ?
                   OR upper(p.license_number) LIKE ?
                  )
                ORDER BY p.last_name, p.first_name
                LIMIT ?
                """;

        List<OfficialSelectablePlayerDto> results = new ArrayList<>();

        try (Connection c = db.openConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, roleType);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            ps.setInt(5, lim);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(map(rs));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("DB error searchByRole(" + roleType + ")", e);
        }

        return results;
    }

    // -------------------------------------------------------------------------
    // MAPPING
    // -------------------------------------------------------------------------

    private OfficialSelectablePlayerDto map(ResultSet rs) throws Exception {
        String licenseNumber = rs.getString("license_number");
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        String clubName = rs.getString("club_name");

        String jaGrade = rs.getString("judge_referee_grade");
        String refGrade = rs.getString("referee_grade");

        List<String> judgeGrades = jaGrade != null ? List.of(jaGrade) : List.of();
        List<String> refereeGrades = refGrade != null ? List.of(refGrade) : List.of();

        return new OfficialSelectablePlayerDto(
                licenseNumber,
                firstName,
                lastName,
                clubName,
                judgeGrades,
                refereeGrades);
    }
}