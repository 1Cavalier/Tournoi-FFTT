package fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.entity;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.TournamentLevel;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value.TournamentRegistrationPolicy;
import fr.Brunoy.gestion_tournois_FFTT.domain.organization.model.Club;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.enums.RankingPhase;

import java.time.LocalDate;
import java.util.*;

public final class Tournament {

    private final String name;
    private final Club organizingClub;
    private final TournamentLevel level;

    private final RankingPhase rankingPhase;

    private final Set<LocalDate> days;
    private final List<Tableau> tableaux;

    private final TournamentRegistrationPolicy registrationPolicy;

    private final Map<String, List<Registration>> registrationsByTableauCode = new HashMap<>();

    public Tournament(
            String name,
            Club organizingClub,
            TournamentLevel level,
            RankingPhase rankingPhase,
            Collection<LocalDate> days,
            TournamentRegistrationPolicy registrationPolicy) {

        if (name == null || name.isBlank()) {
            throw new BusinessException(ErrorCode.TOURNAMENT_NAME_REQUIRED);
        }
        if (organizingClub == null) {
            throw new BusinessException(ErrorCode.TOURNAMENT_ORGANIZING_CLUB_REQUIRED);
        }
        if (level == null) {
            throw new BusinessException(ErrorCode.TOURNAMENT_LEVEL_REQUIRED);
        }
        if (rankingPhase == null) {
            throw new BusinessException(ErrorCode.TOURNAMENT_RANKING_PHASE_REQUIRED);
        }
        if (days == null || days.isEmpty()) {
            throw new BusinessException(ErrorCode.TOURNAMENT_DAYS_REQUIRED);
        }
        if (registrationPolicy == null) {
            throw new BusinessException(ErrorCode.TOURNAMENT_REGISTRATION_POLICY_REQUIRED);
        }

        this.name = name.trim();
        this.organizingClub = organizingClub;
        this.level = level;
        this.rankingPhase = rankingPhase;

        this.days = Collections.unmodifiableSet(new TreeSet<>(days));
        this.tableaux = new ArrayList<>();
        this.registrationPolicy = registrationPolicy;
    }

    public void addTableau(Tableau tableau) {
        if (tableau == null) {
            throw new BusinessException(ErrorCode.TOURNAMENT_TABLEAU_REQUIRED);
        }
        if (!days.contains(tableau.date())) {
            throw new BusinessException(ErrorCode.TOURNAMENT_TABLEAU_DATE_NOT_IN_TOURNAMENT_DAYS);
        }

        boolean duplicateCode = tableaux.stream()
                .anyMatch(t -> t.code().equalsIgnoreCase(tableau.code()));

        if (duplicateCode) {
            throw new BusinessException(ErrorCode.TOURNAMENT_TABLEAU_CODE_DUPLICATE);
        }

        tableaux.add(tableau);

        registrationsByTableauCode.put(normalizeCode(tableau.code()), new ArrayList<>());
    }

    public List<Registration> registrationsFor(String tableauCode) {
        String key = normalizeCode(tableauCode);
        return registrationsByTableauCode.computeIfAbsent(key, k -> new ArrayList<>());
    }

    private static String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase();
    }

    public String name() {
        return name;
    }

    public Club organizingClub() {
        return organizingClub;
    }

    public TournamentLevel level() {
        return level;
    }

    public RankingPhase rankingPhase() {
        return rankingPhase;
    }

    public Set<LocalDate> days() {
        return days;
    }

    public List<Tableau> tableaux() {
        return Collections.unmodifiableList(tableaux);
    }

    public TournamentRegistrationPolicy registrationPolicy() {
        return registrationPolicy;
    }
}
