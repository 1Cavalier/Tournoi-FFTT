package fr.pingmanager.gestion_tournois_FFTT.infra.repo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import fr.pingmanager.gestion_tournois_FFTT.infra.db.SqliteDb;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.PrizeRewardTypeDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.PrizeTierDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TableauDto;

public class TableauRepositorySqlite implements TableauRepository {

    private final SqliteDb db;

    public TableauRepositorySqlite(SqliteDb db) {
        this.db = db;
    }

    @Override
    public TableauDto insert(TableauDto t) {
        String sql = """
                INSERT INTO tableau (
                    id, tournament_id,
                    code, designation, date,
                    gender_policy,
                    age_policy_type, age_min_category, age_max_category, allowed_age_categories,
                    points_rule_type, min_points, max_points,
                    max_players, waitlist_capacity,
                    check_in_end, start_time,
                    prepaid_fee, on_site_fee,
                    pool_size, qualified_per_pool, classification_mode,
                    created_at, updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection c = db.openConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, t.id());
            ps.setString(2, t.tournamentId());
            ps.setString(3, t.code());
            ps.setString(4, t.designation());
            ps.setString(5, t.date());
            ps.setString(6, t.genderPolicy());
            ps.setString(7, t.agePolicyType());
            ps.setString(8, t.ageMinCategory());
            ps.setString(9, t.ageMaxCategory());
            ps.setString(10, joinList(t.allowedAgeCategories()));
            ps.setString(11, t.pointsRuleType());
            setInteger(ps, 12, t.minPoints());
            setInteger(ps, 13, t.maxPoints());
            setInteger(ps, 14, t.maxPlayers());
            setInteger(ps, 15, t.waitlistCapacity());
            ps.setString(16, t.checkInEnd());
            ps.setString(17, t.startTime());
            setInteger(ps, 18, t.prepaidFee());
            setInteger(ps, 19, t.onSiteFee());
            setInteger(ps, 20, t.poolSize() != null ? t.poolSize() : 3);
            setInteger(ps, 21, t.qualifiedPerPool() != null ? t.qualifiedPerPool() : 2);
            ps.setString(22, t.classificationMode() != null ? t.classificationMode() : "NONE");
            ps.setString(23, t.createdAt());
            ps.setString(24, t.updatedAt());

            ps.executeUpdate();

            replacePrizeTiers(c, t.id(), t.prizeTiers());
            return t;

        } catch (Exception e) {
            throw new RuntimeException("Insert tableau failed", e);
        }
    }

    @Override
    public Optional<TableauDto> findById(String id) {
        String sql = "SELECT * FROM tableau WHERE id = ?";

        try (Connection c = db.openConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(c, rs));
                }
            }

            return Optional.empty();

        } catch (Exception e) {
            throw new RuntimeException("Find tableau by id failed", e);
        }
    }

    @Override
    public List<TableauDto> findByTournamentId(String tournamentId) {
        String sql = """
                SELECT * FROM tableau
                WHERE tournament_id = ?
                ORDER BY date ASC, start_time ASC, code ASC
                """;

        List<TableauDto> list = new ArrayList<>();

        try (Connection c = db.openConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, tournamentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(c, rs));
                }
            }

            return list;

        } catch (Exception e) {
            throw new RuntimeException("Find tableaux by tournament failed", e);
        }
    }

    @Override
    public void update(TableauDto t) {
        String sql = """
                UPDATE tableau SET
                    code = ?,
                    designation = ?,
                    date = ?,
                    gender_policy = ?,
                    age_policy_type = ?,
                    age_min_category = ?,
                    age_max_category = ?,
                    allowed_age_categories = ?,
                    points_rule_type = ?,
                    min_points = ?,
                    max_points = ?,
                    max_players = ?,
                    waitlist_capacity = ?,
                    check_in_end = ?,
                    start_time = ?,
                    prepaid_fee = ?,
                    on_site_fee = ?,
                    pool_size = ?,
                    qualified_per_pool = ?,
                    classification_mode = ?,
                    updated_at = ?
                WHERE id = ?
                """;

        try (Connection c = db.openConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, t.code());
            ps.setString(2, t.designation());
            ps.setString(3, t.date());
            ps.setString(4, t.genderPolicy());
            ps.setString(5, t.agePolicyType());
            ps.setString(6, t.ageMinCategory());
            ps.setString(7, t.ageMaxCategory());
            ps.setString(8, joinList(t.allowedAgeCategories()));
            ps.setString(9, t.pointsRuleType());
            setInteger(ps, 10, t.minPoints());
            setInteger(ps, 11, t.maxPoints());
            setInteger(ps, 12, t.maxPlayers());
            setInteger(ps, 13, t.waitlistCapacity());
            ps.setString(14, t.checkInEnd());
            ps.setString(15, t.startTime());
            setInteger(ps, 16, t.prepaidFee());
            setInteger(ps, 17, t.onSiteFee());
            setInteger(ps, 18, t.poolSize() != null ? t.poolSize() : 3);
            setInteger(ps, 19, t.qualifiedPerPool() != null ? t.qualifiedPerPool() : 2);
            ps.setString(20, t.classificationMode() != null ? t.classificationMode() : "NONE");
            ps.setString(21, t.updatedAt());
            ps.setString(22, t.id());

            ps.executeUpdate();

            replacePrizeTiers(c, t.id(), t.prizeTiers());

        } catch (Exception e) {
            throw new RuntimeException("Update tableau failed", e);
        }
    }

    @Override
    public void delete(String id) {
        try (Connection c = db.openConnection()) {
            deletePrizeTiers(c, id);

            try (PreparedStatement ps = c.prepareStatement("DELETE FROM tableau WHERE id = ?")) {
                ps.setString(1, id);
                ps.executeUpdate();
            }

        } catch (Exception e) {
            throw new RuntimeException("Delete tableau failed", e);
        }
    }

    private TableauDto map(Connection c, ResultSet rs) throws Exception {
        String tableauId = rs.getString("id");

        return new TableauDto(
                tableauId,
                rs.getString("tournament_id"),
                rs.getString("code"),
                rs.getString("designation"),
                rs.getString("date"),
                rs.getString("gender_policy"),
                rs.getString("age_policy_type"),
                rs.getString("age_min_category"),
                rs.getString("age_max_category"),
                splitList(rs.getString("allowed_age_categories")),
                rs.getString("points_rule_type"),
                getInteger(rs, "min_points"),
                getInteger(rs, "max_points"),
                getInteger(rs, "max_players"),
                getInteger(rs, "waitlist_capacity"),
                rs.getString("check_in_end"),
                rs.getString("start_time"),
                getInteger(rs, "prepaid_fee"),
                getInteger(rs, "on_site_fee"),
                findPrizeTiers(c, tableauId),
                getInteger(rs, "pool_size") != null ? getInteger(rs, "pool_size") : 3,
                getInteger(rs, "qualified_per_pool") != null ? getInteger(rs, "qualified_per_pool") : 2,
                rs.getString("classification_mode") != null
                        ? rs.getString("classification_mode")
                        : "NONE",
                rs.getString("created_at"),
                rs.getString("updated_at"));
    }

    private List<PrizeTierDto> findPrizeTiers(Connection c, String tableauId) throws Exception {
        String sql = """
                SELECT * FROM tableau_prize_tier
                WHERE tableau_id = ?
                ORDER BY from_rank ASC, to_rank ASC
                """;

        List<PrizeTierDto> list = new ArrayList<>();

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, tableauId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new PrizeTierDto(
                            getInteger(rs, "from_rank"),
                            getInteger(rs, "to_rank"),
                            parseRewardType(rs.getString("reward_type")),
                            getInteger(rs, "cash_amount"),
                            getInteger(rs, "registration_discount_percent")));
                }
            }
        }

        return list;
    }

    private void replacePrizeTiers(Connection c, String tableauId, List<PrizeTierDto> tiers) throws Exception {
        deletePrizeTiers(c, tableauId);

        if (tiers == null || tiers.isEmpty()) {
            return;
        }

        String sql = """
                INSERT INTO tableau_prize_tier (
                    tableau_id, from_rank, to_rank, reward_type, cash_amount, registration_discount_percent
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (PrizeTierDto tier : tiers) {
                ps.setString(1, tableauId);
                setInteger(ps, 2, tier.fromRank());
                setInteger(ps, 3, tier.toRank());
                ps.setString(4, tier.rewardType() == null ? null : tier.rewardType().name());
                setInteger(ps, 5, tier.cashAmount());
                setInteger(ps, 6, tier.registrationDiscountPercent());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void deletePrizeTiers(Connection c, String tableauId) throws Exception {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM tableau_prize_tier WHERE tableau_id = ?")) {
            ps.setString(1, tableauId);
            ps.executeUpdate();
        }
    }

    private static void setInteger(PreparedStatement ps, int index, Integer value) throws Exception {
        if (value == null) {
            ps.setObject(index, null);
        } else {
            ps.setInt(index, value);
        }
    }

    private static Integer getInteger(ResultSet rs, String column) throws Exception {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private static String joinList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return String.join("|", values);
    }

    private static List<String> splitList(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return List.of(raw.split("\\|"));
    }

    private static PrizeRewardTypeDto parseRewardType(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return PrizeRewardTypeDto.valueOf(raw);
    }
}