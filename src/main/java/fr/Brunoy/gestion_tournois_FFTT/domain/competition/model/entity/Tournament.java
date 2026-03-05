package fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.entity;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.RegistrationStatus;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.TournamentLevel;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value.JudgeRefereeAssignment;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value.RegulationDocumentData;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value.RegulationDocumentData.TableauLine;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value.TournamentRegistrationPolicy;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value.TournamentRegulationInfo;
import fr.Brunoy.gestion_tournois_FFTT.domain.identity.Participant;
import fr.Brunoy.gestion_tournois_FFTT.domain.identity.Player;
import fr.Brunoy.gestion_tournois_FFTT.domain.organization.Club;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.RankingPhase;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

/**
 * Aggregate root : Tournament.
 *
 * V1 FFTT (inscriptions + règlement) :
 * - Tableaux ajoutés au tournoi (unicité code + date valide)
 * - Inscriptions centralisées dans l'aggregate
 * - Application des règles d'inscription (TournamentRegistrationPolicy)
 * - Éligibilité tableau (points + genre + âge)
 * - Capacité tableau + file d'attente
 * - Promotion FIFO de la file d'attente quand une place se libère
 * - Validation règlement "officielle" via TournamentRegulationInfo
 */
public final class Tournament {

    // -------------------------------------------------------------------------
    // FIELDS (core)
    // -------------------------------------------------------------------------

    private final String name;
    private final Club organizingClub;
    private final TournamentLevel level;
    private final RankingPhase rankingPhase;

    private final Set<LocalDate> days;

    private final TournamentRegistrationPolicy registrationPolicy;

    /**
     * Bloc règlement FFTT (obligatoire en version pro).
     * Peut être incomplet tant que le tournoi est en "draft".
     */
    private TournamentRegulationInfo regulationInfo;

    /**
     * Liste de JA affectés (draft autorisé).
     * (On garde Player ici car un JA FFTT est forcément licencié FFTT.)
     */
    private final List<JudgeRefereeAssignment> judgeReferees = new ArrayList<>();

    // -------------------------------------------------------------------------
    // FIELDS (tableaux + inscriptions)
    // -------------------------------------------------------------------------

    /** Liste conservée pour l'ordre d'affichage. */
    private final List<Tableau> tableaux = new ArrayList<>();

    /** Index pro : lookup O(1). */
    private final Map<String, Tableau> tableauxByCode = new HashMap<>();

    /** Inscriptions indexées par code tableau normalisé. */
    private final Map<String, List<Registration>> registrationsByTableauCode = new HashMap<>();

    /**
     * BONUS FEMININ "ONCE" : mémorise les participantes ayant déjà consommé le bonus.
     * Choix pro : le bonus reste consommé même si la joueuse annule ensuite.
     */
    private final Set<String> femaleExtraOnceUsedParticipantIds = new HashSet<>();

    // -------------------------------------------------------------------------
    // CONSTRUCTOR
    // -------------------------------------------------------------------------

    public Tournament(
            String name,
            Club organizingClub,
            TournamentLevel level,
            RankingPhase rankingPhase,
            Collection<LocalDate> days,
            TournamentRegistrationPolicy registrationPolicy,
            TournamentRegulationInfo regulationInfo) {

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
        if (days == null || days.isEmpty() || days.stream().anyMatch(Objects::isNull)) {
            throw new BusinessException(ErrorCode.TOURNAMENT_DAYS_REQUIRED);
        }
        if (registrationPolicy == null) {
            throw new BusinessException(ErrorCode.TOURNAMENT_REGISTRATION_POLICY_REQUIRED);
        }
        if (regulationInfo == null) {
            throw new BusinessException(ErrorCode.TOURNAMENT_REGULATION_INFO_REQUIRED);
        }

        this.name = name.trim();
        this.organizingClub = organizingClub;
        this.level = level;
        this.rankingPhase = rankingPhase;

        this.days = Collections.unmodifiableSet(new TreeSet<>(days));
        this.registrationPolicy = registrationPolicy;

        // IMPORTANT : pas de validation "complète" ici -> tournoi peut être en DRAFT.
        this.regulationInfo = regulationInfo;
    }

    // -------------------------------------------------------------------------
    // TABLEAUX
    // -------------------------------------------------------------------------

    public void addTableau(Tableau tableau) {
        if (tableau == null) {
            throw new BusinessException(ErrorCode.TOURNAMENT_TABLEAU_REQUIRED);
        }

        if (!days.contains(tableau.date())) {
            throw new BusinessException(ErrorCode.TOURNAMENT_TABLEAU_DATE_NOT_IN_TOURNAMENT_DAYS);
        }

        String key = normalizeRequiredCode(tableau.code(), ErrorCode.TABLEAU_CODE_REQUIRED);

        if (tableauxByCode.containsKey(key)) {
            throw new BusinessException(ErrorCode.TOURNAMENT_TABLEAU_CODE_DUPLICATE);
        }

        tableaux.add(tableau);
        tableauxByCode.put(key, tableau);
        registrationsByTableauCode.put(key, new ArrayList<>());
    }

    // -------------------------------------------------------------------------
    // REGISTRATIONS (READ)
    // -------------------------------------------------------------------------

    /**
     * Lecture seule. Ne crée jamais une liste “dans le vide”.
     */
    public List<Registration> registrationsFor(String tableauCode) {
        String key = normalizeForLookup(tableauCode);
        List<Registration> regs = registrationsByTableauCode.get(key);
        if (regs == null) {
            throw new BusinessException(ErrorCode.REGISTRATION_TABLEAU_NOT_FOUND);
        }
        return Collections.unmodifiableList(regs);
    }

    // -------------------------------------------------------------------------
    // REGISTRATIONS (WRITE)
    // -------------------------------------------------------------------------

    public void addRegistration(Registration registration, Instant now) {

        if (registration == null) {
            throw new BusinessException(ErrorCode.REGISTRATION_REQUIRED);
        }

        final Instant at = nowOrNow(now);

        String key = normalizeForLookup(registration.tableauCode());
        Tableau tableau = getTableauOrThrow(key);
        List<Registration> regs = getRegsOrThrow(key);

        Participant participant = registration.participant();
        if (participant == null) {
            throw new BusinessException(ErrorCode.PARTICIPANT_REQUIRED);
        }

        // Policy "who is allowed to play" (guest/foreign/etc.)
        registrationPolicy.participantEligibilityPolicy().assertEligible(participant);

        // Pro : purge inactifs (CANCELLED + RESERVED expirées) pour éviter que les listes gonflent.
        purgeInactive(regs, at);

        // 0) Éligibilité tableau (points + genre + âge)
        int points = participant.pointsFor(rankingPhase);
        if (!tableau.accepts(points, participant.isFemale(), participant.ageCategory())) {
            throw new BusinessException(ErrorCode.REGISTRATION_NOT_ELIGIBLE);
        }

        // 1) Unicité : pas déjà inscrit actif (CONFIRMED/RESERVED/WAITLISTED)
        boolean alreadyActive = regs.stream()
                .anyMatch(r -> sameParticipant(r, participant) && r.isActiveAt(at));

        if (alreadyActive) {
            throw new BusinessException(ErrorCode.REGISTRATION_ALREADY_REGISTERED);
        }

        // 2) Policy tournoi : max total / max par jour (+ règles féminines)
        boolean consumesFemaleExtraOnce = enforceRegistrationPolicy(participant, tableau, at);

        // 3) Capacité tableau : spots (RESERVED/CONFIRMED) + file d'attente
        long spotCount = countSpots(regs, at);

        // Place dispo -> CONFIRMED
        if (spotCount < tableau.maxPlayers()) {
            if (registration.status() != RegistrationStatus.CONFIRMED) {
                registration.confirm();
            }
            regs.add(registration);

            if (consumesFemaleExtraOnce) {
                markFemaleExtraOnceUsed(participant);
            }
            return;
        }

        // Tableau complet -> waitlist si possible
        int waitCap = tableau.waitlistCapacity();
        if (waitCap <= 0) {
            throw new BusinessException(ErrorCode.TABLEAU_FULL);
        }

        long waitCount = countWaitlisted(regs, at);
        if (waitCount >= waitCap) {
            throw new BusinessException(ErrorCode.TABLEAU_WAITLIST_FULL);
        }

        if (registration.status() != RegistrationStatus.WAITLISTED) {
            registration.waitlist();
        }

        regs.add(registration);

        if (consumesFemaleExtraOnce) {
            markFemaleExtraOnceUsed(participant);
        }
    }

    /**
     * Annulation métier (V1) : déclenche la promotion FIFO de la file d'attente.
     */
    public void cancelRegistration(String tableauCode, String participantId, Instant now) {

        if (participantId == null || participantId.isBlank()) {
            throw new BusinessException(ErrorCode.PARTICIPANT_ID_REQUIRED);
        }

        final Instant at = nowOrNow(now);
        String key = normalizeForLookup(tableauCode);

        List<Registration> regs = getRegsOrThrow(key);

        // Pro : purge avant recherche (évite de matcher sur des inactifs).
        purgeInactive(regs, at);

        Registration reg = regs.stream()
                .filter(r -> r.participant() != null)
                .filter(r -> participantId.equalsIgnoreCase(r.participant().participantId()))
                .filter(r -> r.isActiveAt(at))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.REGISTRATION_INVALID));

        reg.cancel();

        // Pro : purge après annulation (CANCELLED -> inactif).
        purgeInactive(regs, at);

        // Une place peut se libérer => promotion FIFO (peut promouvoir plusieurs)
        promoteWaitlistIfPossible(key, at);

        // Pro : purge après promotion (optionnel)
        purgeInactive(regs, at);
    }

    /**
     * Backward helper (si tu as encore du code UI qui n'a que Player).
     */
    public void cancelRegistration(String tableauCode, Player player, Instant now) {
        if (player == null) {
            throw new BusinessException(ErrorCode.PLAYER_REQUIRED);
        }
        cancelRegistration(tableauCode, player.getLicenseNumber(), now);
    }

    /**
     * À appeler périodiquement (ou à l'ouverture d'écran) pour :
     * - purger les réservations expirées / annulations
     * - promouvoir la waitlist si possible
     */
    public void refreshTableauQueue(String tableauCode, Instant now) {
        final Instant at = nowOrNow(now);
        String key = normalizeForLookup(tableauCode);

        List<Registration> regs = getRegsOrThrow(key);
        purgeInactive(regs, at);
        promoteWaitlistIfPossible(key, at);
        purgeInactive(regs, at);
    }

    /**
     * Compte “actifs” (CONFIRMED/WAITLISTED/RESERVED non expirée).
     */
    public long activeRegistrationsCount(String tableauCode, Instant now) {
        final Instant at = nowOrNow(now);

        String key = normalizeForLookup(tableauCode);
        List<Registration> regs = getRegsOrThrow(key);

        purgeInactive(regs, at);

        return regs.stream()
                .filter(r -> r.isActiveAt(at))
                .count();
    }

    // -------------------------------------------------------------------------
    // POLICY
    // -------------------------------------------------------------------------

    /**
     * @return true si l'inscription consomme le bonus féminin "ONCE" (si règle ONCE active).
     */
    private boolean enforceRegistrationPolicy(Participant participant, Tableau targetTableau, Instant at) {

        // A) toutes les inscriptions actives du participant (tous tableaux)
        List<Registration> activeRegs = allActiveRegistrationsOf(participant, at);

        // état ONCE déjà consommé ?
        String pidKey = normalizeParticipantId(participant.participantId());
        boolean onceAlreadyUsed = femaleExtraOnceUsedParticipantIds.contains(pidKey);

        int baseTotal = registrationPolicy.maxTotalTableaux();
        int basePerDay = registrationPolicy.maxTableauxPerDay();

        // B) limite totale (max total + bonus féminin)
        int allowedTotal = registrationPolicy.allowedTotalTableaux(
                participant,
                targetTableau,
                days.size(),
                onceAlreadyUsed);

        if (activeRegs.size() + 1 > allowedTotal) {
            throw new BusinessException(ErrorCode.REGISTRATION_MAX_TOTAL_TABLEAUX_EXCEEDED);
        }

        // C) inscriptions du jour
        LocalDate day = targetTableau.date();
        int selectedThatDayCount = 0;

        for (Registration r : activeRegs) {
            // Important : on normalise quand même (safe) : tu ne veux pas dépendre d’un invariant "ailleurs".
            String tKey = normalizeForLookup(r.tableauCode());
            Tableau t = tableauxByCode.get(tKey);
            if (t != null && day.equals(t.date())) {
                selectedThatDayCount++;
            }
        }

        // D) limite par jour (max/jour + bonus féminin)
        int allowedThatDay = registrationPolicy.allowedTableauxPerDay(
                participant,
                targetTableau,
                onceAlreadyUsed);

        if (selectedThatDayCount + 1 > allowedThatDay) {
            throw new BusinessException(ErrorCode.REGISTRATION_MAX_TABLEAUX_PER_DAY_EXCEEDED);
        }

        // E) Consommation du bonus "ONCE" :
        // On le marque "utilisé" uniquement si cette inscription dépasse une limite de base,
        // et si le tableau cible peut déclencher la règle.
        if (!registrationPolicy.isFemaleExtraOnceRule()) {
            return false;
        }
        if (onceAlreadyUsed) {
            return false;
        }
        if (!registrationPolicy.targetQualifiesForFemaleExtra(participant, targetTableau)) {
            return false;
        }

        boolean exceedsBaseTotal = (activeRegs.size() + 1 > baseTotal);
        boolean exceedsBaseDay = (selectedThatDayCount + 1 > basePerDay);

        return exceedsBaseTotal || exceedsBaseDay;
    }

    private List<Registration> allActiveRegistrationsOf(Participant participant, Instant at) {
        List<Registration> out = new ArrayList<>();
        String pid = participant.participantId();

        for (List<Registration> regs : registrationsByTableauCode.values()) {
            // Pro : purge local pour garder des listes petites
            purgeInactive(regs, at);

            for (Registration r : regs) {
                if (r != null
                        && r.participant() != null
                        && pid.equalsIgnoreCase(r.participant().participantId())
                        && r.isActiveAt(at)) {
                    out.add(r);
                }
            }
        }
        return out;
    }

    private void markFemaleExtraOnceUsed(Participant participant) {
        String pidKey = normalizeParticipantId(participant.participantId());
        femaleExtraOnceUsedParticipantIds.add(pidKey);
    }

    private static String normalizeParticipantId(String participantId) {
        return participantId == null ? "" : participantId.trim().toUpperCase(Locale.ROOT);
    }

    // -------------------------------------------------------------------------
    // WAITLIST PROMOTION (FIFO)
    // -------------------------------------------------------------------------

    /**
     * Pro : peut promouvoir plusieurs personnes si plusieurs places sont libres.
     */
    private void promoteWaitlistIfPossible(String normalizedTableauCode, Instant at) {

        Tableau tableau = tableauxByCode.get(normalizedTableauCode);
        if (tableau == null) {
            return;
        }

        List<Registration> regs = registrationsByTableauCode.get(normalizedTableauCode);
        if (regs == null) {
            return;
        }

        purgeInactive(regs, at);

        while (true) {
            long spotCount = countSpots(regs, at);
            if (spotCount >= tableau.maxPlayers()) {
                return;
            }

            Registration next = regs.stream()
                    .filter(r -> r != null && r.isActiveAt(at))
                    .filter(r -> r.status() == RegistrationStatus.WAITLISTED)
                    .min(Comparator.comparing(Registration::registeredAt)) // FIFO
                    .orElse(null);

            if (next == null) {
                return;
            }

            next.confirm();
            // boucle : si plusieurs spots, on continue
        }
    }

    /**
     * Option : rafraîchit toutes les files (purge + promotion FIFO).
     */
    public void refreshAllQueues(Instant now) {
        final Instant at = nowOrNow(now);

        for (Tableau t : tableaux) {
            if (t == null) {
                continue;
            }
            String key = normalizeForLookup(t.code());
            List<Registration> regs = registrationsByTableauCode.get(key);
            if (regs == null) {
                continue;
            }
            purgeInactive(regs, at);
            promoteWaitlistIfPossible(key, at);
            purgeInactive(regs, at);
        }
    }

    // -------------------------------------------------------------------------
    // REGULATION / PUBLICATION
    // -------------------------------------------------------------------------

    public void validateForOfficialPublication() {
        regulationInfo.validateCompleteForRegulation(level, true);
        validateJudgeRefereesForOfficialPublication();
    }

    private void validateJudgeRefereesForOfficialPublication() {

        if (judgeReferees.isEmpty()) {
            throw new BusinessException(ErrorCode.TOURNAMENT_JA_REQUIRED);
        }

        var required = level.requiredJudgeRefereeGrade();

        boolean ok = judgeReferees.stream()
                .map(JudgeRefereeAssignment::grade)
                .anyMatch(g -> g != null && g.qualifiesFor(required));

        if (!ok) {
            throw new BusinessException(ErrorCode.TOURNAMENT_JA_GRADE_TOO_LOW_FOR_LEVEL);
        }
    }

    public void addJudgeReferee(JudgeRefereeAssignment assignment) {
        if (assignment == null) {
            throw new BusinessException(ErrorCode.TOURNAMENT_JA_REQUIRED);
        }

        boolean duplicate = judgeReferees.stream()
                .anyMatch(a -> a.judgeReferee().getLicenseNumber()
                        .equalsIgnoreCase(assignment.judgeReferee().getLicenseNumber()));
        if (duplicate) {
            throw new BusinessException(ErrorCode.TOURNAMENT_JA_DUPLICATE);
        }

        judgeReferees.add(assignment);
    }

    public List<JudgeRefereeAssignment> judgeReferees() {
        return List.copyOf(judgeReferees);
    }

    public void updateRegulationInfo(TournamentRegulationInfo newInfo) {
        if (newInfo == null) {
            throw new BusinessException(ErrorCode.TOURNAMENT_REGULATION_INFO_REQUIRED);
        }
        this.regulationInfo = newInfo;
    }

    public RegulationDocumentData regulationDocumentDataForPublication() {

        validateForOfficialPublication();

        TournamentRegulationInfo info = this.regulationInfo;

        String playingAreaSummary = (info.playingArea() == null)
                ? null
                : info.playingArea().toString();

        List<TableauLine> lines = this.tableaux.stream()
                .map(t -> new TableauLine(
                        t.code(),
                        t.designation(),
                        t.date(),
                        t.checkInEnd(),
                        t.startTime(),
                        String.valueOf(t.genderPolicy()),
                        String.valueOf(t.pointsRuleType()),
                        t.minPoints(),
                        t.maxPoints(),
                        t.maxPlayers(),
                        t.waitlistCapacity(),
                        String.valueOf(t.fee()),
                        String.valueOf(t.prizes())))
                .toList();

        return new RegulationDocumentData(
                this.name,
                this.level,
                this.rankingPhase,
                this.days,

                this.organizingClub.getName(),
                this.organizingClub.getNumber(),
                this.organizingClub.getCity(),

                info.homologationNumber(),

                info.organizerContactName(),
                info.organizerEmail(),
                info.organizerPhone(),

                info.venueName(),
                info.venueStreet(),
                info.venueZip(),
                info.venueCity(),

                info.numberOfTables(),
                playingAreaSummary,
                info.ballBrandAndType(),
                String.valueOf(info.ballProvisionPolicy()),

                info.registrationDeadline(),
                info.checkInDeadline(),
                info.firstMatchesStart(),

                info.expectedEndTime(),

                lines);
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

    // -------------------------------------------------------------------------
    // INTERNAL HELPERS
    // -------------------------------------------------------------------------

    private static Instant nowOrNow(Instant now) {
        return (now != null) ? now : Instant.now();
    }

    private Tableau getTableauOrThrow(String normalizedTableauCode) {
        Tableau t = tableauxByCode.get(normalizedTableauCode);
        if (t == null) {
            throw new BusinessException(ErrorCode.REGISTRATION_TABLEAU_NOT_FOUND);
        }
        return t;
    }

    private List<Registration> getRegsOrThrow(String normalizedTableauCode) {
        List<Registration> regs = registrationsByTableauCode.get(normalizedTableauCode);
        if (regs == null) {
            throw new BusinessException(ErrorCode.REGISTRATION_TABLEAU_NOT_FOUND);
        }
        return regs;
    }

    private static long countSpots(List<Registration> regs, Instant at) {
        return regs.stream()
                .filter(r -> r != null && r.isActiveAt(at))
                .filter(r -> r.status().blocksSpot())
                .count();
    }

    private static long countWaitlisted(List<Registration> regs, Instant at) {
        return regs.stream()
                .filter(r -> r != null && r.isActiveAt(at))
                .filter(r -> r.status() == RegistrationStatus.WAITLISTED)
                .count();
    }

    /**
     * Pro : supprime les inscriptions inactives.
     * - CANCELLED => inactif
     * - RESERVED expirée => inactif
     */
    private static void purgeInactive(List<Registration> regs, Instant at) {
        regs.removeIf(r -> r == null || !r.isActiveAt(at));
    }

    private static boolean sameParticipant(Registration r, Participant p) {
        return r != null
                && r.participant() != null
                && p != null
                && r.participant().participantId().equalsIgnoreCase(p.participantId());
    }

    /**
     * Normalisation stricte pour lookup (inputs externes : UI/API).
     */
    private static String normalizeForLookup(String code) {
        if (code == null || code.isBlank()) {
            throw new BusinessException(ErrorCode.REGISTRATION_TABLEAU_NOT_FOUND);
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private static String normalizeRequiredCode(String code, ErrorCode errorIfBlank) {
        if (code == null || code.isBlank()) {
            throw new BusinessException(errorIfBlank);
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }
}