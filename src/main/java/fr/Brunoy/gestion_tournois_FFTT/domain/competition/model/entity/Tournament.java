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
 *
 * (Le format sportif / rencontres / sets viendra plus tard.)
 */
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

    /**
     * Liste de JA affectés (draft autorisé).
     * (On garde Player ici car un JA FFTT est forcément licencié FFTT.)
     */
    private final List<JudgeRefereeAssignment> judgeReferees = new ArrayList<>();

    /**
     * Inscriptions indexées par code tableau (normalisé en upper case).
     */
    private final Map<String, List<Registration>> registrationsByTableauCode = new HashMap<>();

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
    // REGISTRATIONS (READ)
    // -------------------------------------------------------------------------

    /**
     * Lecture seule. Ne crée jamais une liste “dans le vide”.
     */
    public List<Registration> registrationsFor(String tableauCode) {
        String key = normalizeCode(tableauCode);
        List<Registration> regs = registrationsByTableauCode.get(key);
        if (regs == null)
            throw new BusinessException(ErrorCode.REGISTRATION_TABLEAU_NOT_FOUND);

        return Collections.unmodifiableList(regs);
    }

    // -------------------------------------------------------------------------
    // REGISTRATIONS (WRITE) - V1 COMPLETE
    // -------------------------------------------------------------------------

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

        Participant participant = registration.participant();
        if (participant == null)
            throw new BusinessException(ErrorCode.PARTICIPANT_REQUIRED);

        // 0) Éligibilité tableau (points + genre + âge)
        int points = participant.pointsFor(rankingPhase);
        if (!tableau.accepts(points, participant.isFemale(), participant.ageCategory())) {
            throw new BusinessException(ErrorCode.REGISTRATION_NOT_ELIGIBLE);
        }

        // 1) Unicité : pas déjà inscrit actif (CONFIRMED/RESERVED/WAITLISTED)
        boolean alreadyActive = regs.stream()
                .anyMatch(r -> sameParticipant(r, participant) && r.isActiveAt(at));

        if (alreadyActive)
            throw new BusinessException(ErrorCode.REGISTRATION_ALREADY_REGISTERED);

        // 2) Policy tournoi : max total / max par jour (+ bonus féminin)
        enforceRegistrationPolicy(participant, tableau, at);

        // 3) Capacité tableau : spots (RESERVED/CONFIRMED) + file d'attente
        long spotCount = regs.stream()
                .filter(r -> r.isActiveAt(at))
                .filter(r -> r.status().blocksSpot())
                .count();

        // Place dispo -> CONFIRMED
        if (spotCount < tableau.maxPlayers()) {
            if (registration.status() != RegistrationStatus.CONFIRMED) {
                registration.confirm();
            }
            regs.add(registration);
            return;
        }

        // Tableau complet -> waitlist si possible
        int waitCap = tableau.waitlistCapacity();
        if (waitCap <= 0)
            throw new BusinessException(ErrorCode.TABLEAU_FULL);

        long waitCount = regs.stream()
                .filter(r -> r.isActiveAt(at))
                .filter(r -> r.status() == RegistrationStatus.WAITLISTED)
                .count();

        if (waitCount >= waitCap)
            throw new BusinessException(ErrorCode.TABLEAU_WAITLIST_FULL);

        if (registration.status() != RegistrationStatus.WAITLISTED) {
            registration.waitlist();
        }

        regs.add(registration);
    }

    /**
     * Annulation métier (V1) : permet de déclencher la promotion FIFO de la file
     * d'attente.
     * (Participant-compatible : works for FFTT/guest/foreign)
     */
    public void cancelRegistration(String tableauCode, String participantId, Instant now) {

        if (participantId == null || participantId.isBlank())
            throw new BusinessException(ErrorCode.PARTICIPANT_ID_REQUIRED);

        final Instant at = (now != null) ? now : Instant.now();

        String key = normalizeCode(tableauCode);
        List<Registration> regs = registrationsByTableauCode.get(key);
        if (regs == null)
            throw new BusinessException(ErrorCode.REGISTRATION_TABLEAU_NOT_FOUND);

        Registration reg = regs.stream()
                .filter(r -> r.participant() != null)
                .filter(r -> participantId.equalsIgnoreCase(r.participant().participantId()))
                .filter(r -> r.isActiveAt(at))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.REGISTRATION_INVALID));

        reg.cancel();

        // Une place peut se libérer => promotion FIFO
        promoteWaitlistIfPossible(key, at);
    }

    /**
     * Backward helper (si tu as encore du code UI qui n'a que Player).
     */
    public void cancelRegistration(String tableauCode, Player player, Instant now) {
        if (player == null)
            throw new BusinessException(ErrorCode.PLAYER_REQUIRED);
        cancelRegistration(tableauCode, player.getLicenseNumber(), now);
    }

    /**
     * Option utile : à appeler périodiquement (ou à l'ouverture d'écran) pour
     * nettoyer les réservations expirées et promouvoir la waitlist si possible.
     */
    public void refreshTableauQueue(String tableauCode, Instant now) {
        final Instant at = (now != null) ? now : Instant.now();
        String key = normalizeCode(tableauCode);

        if (!registrationsByTableauCode.containsKey(key))
            throw new BusinessException(ErrorCode.REGISTRATION_TABLEAU_NOT_FOUND);

        promoteWaitlistIfPossible(key, at);
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

    // -------------------------------------------------------------------------
    // POLICY
    // -------------------------------------------------------------------------

    private void enforceRegistrationPolicy(Participant participant, Tableau targetTableau, Instant at) {

        // A) toutes les inscriptions actives du participant (tous tableaux)
        List<Registration> activeRegs = allActiveRegistrationsOf(participant, at);

        // B) Max total
        if (activeRegs.size() + 1 > registrationPolicy.maxTotalTableaux()) {
            throw new BusinessException(ErrorCode.REGISTRATION_MAX_TOTAL_TABLEAUX_EXCEEDED);
        }

        // C) Max / jour (+ bonus féminin)
        LocalDate day = targetTableau.date();

        List<Tableau> selectedThatDay = new ArrayList<>();
        for (Registration r : activeRegs) {
            Tableau t = findTableauByCode(r.tableauCode());
            if (t != null && day.equals(t.date())) {
                selectedThatDay.add(t);
            }
        }

        int allowedThatDay = registrationPolicy.allowedTableauxPerDay(participant, selectedThatDay);
        if (selectedThatDay.size() + 1 > allowedThatDay) {
            throw new BusinessException(ErrorCode.REGISTRATION_MAX_TABLEAUX_PER_DAY_EXCEEDED);
        }

        // D) Garde-fou actuel : 1 seul tableau féminin-only / jour (si tu conserves
        // cette règle)
        if (participant.isFemale()) {
            int femaleOnlyCount = countFemaleOnlyTableaux(selectedThatDay);
            if (targetTableau.genderPolicy().isFemaleOnly()) {
                femaleOnlyCount++;
            }
            if (femaleOnlyCount > 1) {
                throw new BusinessException(ErrorCode.REGISTRATION_TOO_MANY_FEMALE_ONLY_TABLEAUX_PER_DAY);
            }
        }
    }

    private int countFemaleOnlyTableaux(List<Tableau> selectedThatDay) {
        int c = 0;
        for (Tableau t : selectedThatDay) {
            if (t != null && t.genderPolicy().isFemaleOnly())
                c++;
        }
        return c;
    }

    private List<Registration> allActiveRegistrationsOf(Participant participant, Instant at) {
        List<Registration> out = new ArrayList<>();
        for (List<Registration> regs : registrationsByTableauCode.values()) {
            for (Registration r : regs) {
                if (r.participant() != null
                        && participant.participantId().equalsIgnoreCase(r.participant().participantId())
                        && r.isActiveAt(at)) {
                    out.add(r);
                }
            }
        }
        return out;
    }

    // -------------------------------------------------------------------------
    // WAITLIST PROMOTION (FIFO)
    // -------------------------------------------------------------------------

    private void promoteWaitlistIfPossible(String normalizedTableauCode, Instant at) {

        Tableau tableau = findTableauByNormalizedCode(normalizedTableauCode);
        if (tableau == null)
            return;

        List<Registration> regs = registrationsByTableauCode.get(normalizedTableauCode);
        if (regs == null)
            return;

        long spotCount = regs.stream()
                .filter(r -> r.isActiveAt(at))
                .filter(r -> r.status().blocksSpot())
                .count();

        if (spotCount >= tableau.maxPlayers())
            return;

        Registration next = regs.stream()
                .filter(r -> r.isActiveAt(at))
                .filter(r -> r.status() == RegistrationStatus.WAITLISTED)
                .min(Comparator.comparing(Registration::registeredAt)) // FIFO
                .orElse(null);

        if (next == null)
            return;

        next.confirm();
    }

    /**
     * Option B : rafraîchit toutes les files (expiration RESERVED + promotion
     * FIFO).
     */
    public void refreshAllQueues(Instant now) {
        final Instant at = (now != null) ? now : Instant.now();

        for (Tableau t : tableaux) {
            if (t == null)
                continue;
            String key = normalizeCode(t.code());
            if (!registrationsByTableauCode.containsKey(key))
                continue;

            promoteWaitlistIfPossible(key, at);
        }
    }

    // -------------------------------------------------------------------------
    // LOOKUPS / HELPERS
    // -------------------------------------------------------------------------

    private static boolean sameParticipant(Registration r, Participant p) {
        return r != null
                && r.participant() != null
                && p != null
                && r.participant().participantId().equalsIgnoreCase(p.participantId());
    }

    private Tableau findTableauByNormalizedCode(String normalizedCode) {
        return tableaux.stream()
                .filter(t -> normalizeCode(t.code()).equals(normalizedCode))
                .findFirst()
                .orElse(null);
    }

    private Tableau findTableauByCode(String tableauCode) {
        String key = normalizeCode(tableauCode);
        for (Tableau t : tableaux) {
            if (normalizeCode(t.code()).equals(key))
                return t;
        }
        return null;
    }

    private static String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase();
    }

    // -------------------------------------------------------------------------
    // REGLEMENT FFTT
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
        if (assignment == null)
            throw new BusinessException(ErrorCode.TOURNAMENT_JA_REQUIRED);

        boolean duplicate = judgeReferees.stream()
                .anyMatch(a -> a.judgeReferee().getLicenseNumber()
                        .equalsIgnoreCase(assignment.judgeReferee().getLicenseNumber()));
        if (duplicate)
            throw new BusinessException(ErrorCode.TOURNAMENT_JA_DUPLICATE);

        judgeReferees.add(assignment);
    }

    public List<JudgeRefereeAssignment> judgeReferees() {
        return List.copyOf(judgeReferees);
    }

    public void updateRegulationInfo(TournamentRegulationInfo newInfo) {
        if (newInfo == null)
            throw new BusinessException(ErrorCode.TOURNAMENT_REGULATION_INFO_REQUIRED);
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
}