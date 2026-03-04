package fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.entity;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.TournamentLevel;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.RegistrationStatus;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value.TournamentRegistrationPolicy;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value.TournamentRegulationInfo;
import fr.Brunoy.gestion_tournois_FFTT.domain.identity.Player;
import fr.Brunoy.gestion_tournois_FFTT.domain.organization.Club;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.RankingPhase;

import java.time.Instant;
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

    /**
     * Bloc règlement FFTT (obligatoire en version pro).
     * Peut être incomplet tant que le tournoi est en "draft".
     */
    private TournamentRegulationInfo regulationInfo;

    private final Map<String, List<Registration>> registrationsByTableauCode = new HashMap<>();

    public Tournament(
            String name,
            Club organizingClub,
            TournamentLevel level,
            RankingPhase rankingPhase,
            Collection<LocalDate> days,
            TournamentRegistrationPolicy registrationPolicy,
            TournamentRegulationInfo regulationInfo) {
        if (name == null || name.isBlank())
            throw new BusinessException(ErrorCode.TOURNAMENT_NAME_REQUIRED);
        if (organizingClub == null)
            throw new BusinessException(ErrorCode.TOURNAMENT_ORGANIZING_CLUB_REQUIRED);
        if (level == null)
            throw new BusinessException(ErrorCode.TOURNAMENT_LEVEL_REQUIRED);
        if (rankingPhase == null)
            throw new BusinessException(ErrorCode.TOURNAMENT_RANKING_PHASE_REQUIRED);
        if (days == null || days.isEmpty())
            throw new BusinessException(ErrorCode.TOURNAMENT_DAYS_REQUIRED);
        if (days.stream().anyMatch(Objects::isNull))
            throw new BusinessException(ErrorCode.TOURNAMENT_DAYS_REQUIRED);
        if (registrationPolicy == null)
            throw new BusinessException(ErrorCode.TOURNAMENT_REGISTRATION_POLICY_REQUIRED);
        if (regulationInfo == null)
            throw new BusinessException(ErrorCode.TOURNAMENT_REGULATION_INFO_REQUIRED);

        this.name = name.trim();
        this.organizingClub = organizingClub;
        this.level = level;
        this.rankingPhase = rankingPhase;

        this.days = Collections.unmodifiableSet(new TreeSet<>(days));
        this.tableaux = new ArrayList<>();
        this.registrationPolicy = registrationPolicy;

        // IMPORTANT : pas de validation "complète" ici -> tournoi peut être en DRAFT.
        this.regulationInfo = regulationInfo;
    }

    // -------------------------------------------------------------------------
    // TABLEAUX
    // -------------------------------------------------------------------------

    public void addTableau(Tableau tableau) {
        if (tableau == null)
            throw new BusinessException(ErrorCode.TOURNAMENT_TABLEAU_REQUIRED);

        if (!days.contains(tableau.date()))
            throw new BusinessException(ErrorCode.TOURNAMENT_TABLEAU_DATE_NOT_IN_TOURNAMENT_DAYS);

        boolean duplicateCode = tableaux.stream()
                .anyMatch(t -> t.code().equalsIgnoreCase(tableau.code()));

        if (duplicateCode)
            throw new BusinessException(ErrorCode.TOURNAMENT_TABLEAU_CODE_DUPLICATE);

        tableaux.add(tableau);
        registrationsByTableauCode.put(normalizeCode(tableau.code()), new ArrayList<>());
    }

    // -------------------------------------------------------------------------
    // REGISTRATIONS
    // -------------------------------------------------------------------------

    public List<Registration> registrationsFor(String tableauCode) {
        String key = normalizeCode(tableauCode);
        List<Registration> regs = registrationsByTableauCode.get(key);
        if (regs == null)
            throw new BusinessException(ErrorCode.REGISTRATION_TABLEAU_NOT_FOUND);

        return Collections.unmodifiableList(regs);
    }

    public void addRegistration(Registration registration, Instant now) {

        if (registration == null)
            throw new BusinessException(ErrorCode.REGISTRATION_REQUIRED);

        final Instant at = (now != null) ? now : Instant.now();

        String key = normalizeCode(registration.tableauCode());
        Tableau tableau = findTableauByNormalizedCode(key);

        if (tableau == null)
            throw new BusinessException(ErrorCode.REGISTRATION_TABLEAU_NOT_FOUND);

        List<Registration> regs = registrationsByTableauCode.get(key);
        if (regs == null)
            throw new BusinessException(ErrorCode.REGISTRATION_TABLEAU_NOT_FOUND);

        Player player = registration.player();
        if (player == null)
            throw new BusinessException(ErrorCode.PLAYER_REQUIRED);

        // 1) Unicité : pas déjà inscrit (active = CONFIRMED/RESERVED/WAITLISTED)
        boolean alreadyActive = regs.stream()
                .anyMatch(r -> r.player().equals(player) && r.isActiveAt(at));

        if (alreadyActive)
            throw new BusinessException(ErrorCode.REGISTRATION_ALREADY_REGISTERED);

        // 2) Compter les places réellement prises (status qui bloquent une place)
        long spotCount = regs.stream()
                .filter(r -> r.isActiveAt(at))
                .filter(r -> r.status().blocksSpot())
                .count();

        // 3) Si une place est dispo => on force l'inscription en CONFIRMED
        if (spotCount < tableau.maxPlayers()) {
            if (registration.status() != RegistrationStatus.CONFIRMED) {
                registration.confirm();
            }
            regs.add(registration);
            return;
        }

        // 4) Tableau complet => file d'attente
        int waitCap = tableau.waitlistCapacity();
        if (waitCap <= 0)
            throw new BusinessException(ErrorCode.TABLEAU_FULL);

        long waitCount = regs.stream()
                .filter(r -> r.isActiveAt(at))
                .filter(r -> r.status() == RegistrationStatus.WAITLISTED)
                .count();

        if (waitCount >= waitCap)
            throw new BusinessException(ErrorCode.TABLEAU_WAITLIST_FULL);

        // 5) On place automatiquement en file d'attente
        if (registration.status() != RegistrationStatus.WAITLISTED) {
            registration.waitlist(); // nécessite la méthode waitlist() dans Registration
        }

        regs.add(registration);
    }

    public long activeRegistrationsCount(String tableauCode, Instant now) {
        final Instant at = (now != null) ? now : Instant.now();

        String key = normalizeCode(tableauCode);
        List<Registration> regs = registrationsByTableauCode.get(key);

        if (regs == null)
            throw new BusinessException(ErrorCode.REGISTRATION_TABLEAU_NOT_FOUND);

        return regs.stream()
                .filter(r -> r.isActiveAt(at))
                .count();
    }

    private Tableau findTableauByNormalizedCode(String normalizedCode) {
        return tableaux.stream()
                .filter(t -> normalizeCode(t.code()).equals(normalizedCode))
                .findFirst()
                .orElse(null);
    }

    private static String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase();
    }

    // -------------------------------------------------------------------------
    // REGLEMENT FFTT
    // -------------------------------------------------------------------------

    public void validateForOfficialPublication() {
        regulationInfo.validateCompleteForRegulation(level, true);
    }

    public void updateRegulationInfo(TournamentRegulationInfo newInfo) {
        if (newInfo == null)
            throw new BusinessException(ErrorCode.TOURNAMENT_REGULATION_INFO_REQUIRED);

        // Mise à jour autorisée même si incomplet (draft)
        this.regulationInfo = newInfo;
    }

    // -------------------------------------------------------------------------
    // GETTERS
    // -------------------------------------------------------------------------

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

    public TournamentRegulationInfo regulationInfo() {
        return regulationInfo;
    }
}