package fr.Brunoy.gestion_tournois_FFTT.testutil;

import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value.ParticipantEligibilityPolicy;
import fr.Brunoy.gestion_tournois_FFTT.domain.identity.*;
import fr.Brunoy.gestion_tournois_FFTT.domain.organization.*;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.*;

import java.util.Locale;
import java.util.Set;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    // -------------------------------------------------------------------------
    // ORGANIZATION FIXTURES (stables)
    // -------------------------------------------------------------------------

    public static Region regionIdf() {
        return new Region("IDF", "Île-de-France");
    }

    public static Departement dep91Essonne() {
        return new Departement("91", "Essonne", regionIdf());
    }

    public static Departement dep78Yvelines() {
        return new Departement("78", "Yvelines", regionIdf());
    }

    public static Region regionNormandie() {
        return new Region("NOR", "Normandie");
    }

    public static Departement dep14Calvados() {
        return new Departement("14", "Calvados", regionNormandie());
    }

    public static Club clubBrunoy() {
        return new Club(
                "08911132",
                "Brunoy CTT",
                dep91Essonne(),
                "Brunoy",
                "Gymnase de Brunoy",
                null,
                null,
                null);
    }

    public static Club clubVersailles() {
        return new Club(
                "08780329",
                "Versailles TT",
                dep78Yvelines(),
                "Versailles",
                "Gymnase de Versailles",
                null,
                null,
                null);
    }

    public static Club clubCaen() {
        return new Club(
                "09140156",
                "Caen TT",
                dep14Calvados(),
                "Caen",
                "Gymnase de Caen",
                null,
                null,
                null);
    }

    // -------------------------------------------------------------------------
    // PLAYER FIXTURES
    // -------------------------------------------------------------------------

    private static Player player(
            String license,
            String firstName,
            String lastName,
            Gender gender,
            String nationalityIso2,
            Club club,
            AgeCategory ageCategory,
            int p1,
            int p2) {

        return new Player(
                normalizeUpperRequired(license),
                normalizeKeepCaseRequired(firstName),
                normalizeKeepCaseRequired(lastName),
                gender,
                normalizeIso2Required(nationalityIso2),
                club,
                ageCategory,
                LicenseType.COMPETITION,
                false,
                MedicalCertificateStatus.VALIDE,
                p1,
                p2);
    }

    // 1 homme par club (senior)
    public static Player maleSeniorBrunoy() {
        return player("08911132A", "Alex", "Brunoy", Gender.MALE, "FR", clubBrunoy(), AgeCategory.SENIOR, 950, 980);
    }

    public static Player maleSeniorVersailles() {
        return player("08780329A", "Victor", "Versailles", Gender.MALE, "FR", clubVersailles(), AgeCategory.SENIOR,
                1200, 1230);
    }

    public static Player maleSeniorCaen() {
        return player("09140156A", "Charles", "Caen", Gender.MALE, "FR", clubCaen(), AgeCategory.SENIOR, 800, 820);
    }

    // 1 jeune (benjamin)
    public static Player maleBenjaminBrunoy() {
        return player("08911132B", "Benoit", "Jeune", Gender.MALE, "FR", clubBrunoy(), AgeCategory.BENJAMIN_1, 400,
                420);
    }

    // 1 vétéran 80
    public static Player maleVeteran80Caen() {
        return player("09140156V80", "Michel", "Veteran", Gender.MALE, "FR", clubCaen(), AgeCategory.VETERAN_80, 650,
                660);
    }

    // 3 féminines (catégories au choix)
    public static Player femaleSeniorVersailles() {
        return player("08780329F1", "Sophie", "Versailles", Gender.FEMALE, "FR", clubVersailles(), AgeCategory.SENIOR,
                1100, 1120);
    }

    public static Player femaleJuniorBrunoy() {
        return player("08911132F2", "Julie", "Brunoy", Gender.FEMALE, "FR", clubBrunoy(), AgeCategory.JUNIOR_2, 700,
                710);
    }

    public static Player femaleVeteran45Caen() {
        return player("09140156F3", "Claire", "Caen", Gender.FEMALE, "FR", clubCaen(), AgeCategory.VETERAN_45, 900,
                920);
    }

    // -------------------------------------------------------------------------
    // PARTICIPANT FIXTURES
    // -------------------------------------------------------------------------

    public static FfttParticipant participantFrom(Player player) {
        return new FfttParticipant(player);
    }

    /** Alias pratique, évite “method undefined” dans les tests. */
    public static FfttParticipant ffttParticipantAlexBrunoy() {
        return participantFrom(maleSeniorBrunoy());
    }

    /**
     * Guest factory "pro" :
     * - accepte "G1" ou "guest-0001" ou "GUEST-0001"
     * - normalise vers "GUEST-..."
     */
    public static GuestParticipant guest(String id, String fullName, Gender gender, String natIso2, AgeCategory age,
            MedicalCertificateStatus cert) {
        return new GuestParticipant(
                normalizePrefixedId(id, "GUEST-"),
                normalizeKeepCaseRequired(fullName),
                gender,
                normalizeIso2Required(natIso2),
                age,
                cert);
    }

    /**
     * ForeignFederationInfo factory "pro" :
     * - normalise pays ISO2 (upper)
     * - federationName trim
     */
    public static ForeignFederationInfo foreignFederationInfo(String countryIso2, String federationName,
            String licenseId) {
        return ForeignFederationInfo.of(
                normalizeIso2Required(countryIso2),
                normalizeKeepCaseRequired(federationName),
                normalizeOptionalKeepCase(licenseId));
    }

    /**
     * ForeignParticipant factory "pro" :
     * - accepte "F1" ou "foreign-0001" ou "FOREIGN-0001"
     * - normalise vers "FOREIGN-..."
     */
    public static ForeignParticipant foreignParticipant(
            String foreignId,
            String fullName,
            Gender gender,
            AgeCategory age,
            MedicalCertificateStatus cert,
            ForeignFederationInfo info,
            int convertedPoints) {

        return new ForeignParticipant(
                normalizePrefixedId(foreignId, "FOREIGN-"),
                normalizeKeepCaseRequired(fullName),
                gender,
                age,
                cert,
                info,
                convertedPoints);
    }

    // -------------------------------------------------------------------------
    // ELIGIBILITY POLICY FIXTURES
    // -------------------------------------------------------------------------

    public static ParticipantEligibilityPolicy policyAllAllowed() {
        return new ParticipantEligibilityPolicy(
                true,
                true,
                Set.of() // vide => tous pays acceptés
        );
    }

    public static ParticipantEligibilityPolicy policyNoGuestNoForeign() {
        return new ParticipantEligibilityPolicy(
                false,
                false,
                Set.of());
    }

    public static ParticipantEligibilityPolicy policyForeignOnlyWhitelist(Set<String> allowedIso2) {
        return new ParticipantEligibilityPolicy(
                false,
                true,
                normalizeIso2Set(allowedIso2));
    }

    // -------------------------------------------------------------------------
    // NORMALIZATION HELPERS (test-only)
    // -------------------------------------------------------------------------

    private static Set<String> normalizeIso2Set(Set<String> iso2) {
        if (iso2 == null || iso2.isEmpty())
            return Set.of();
        return iso2.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(TestDataFactory::normalizeIso2Required)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    private static String normalizePrefixedId(String raw, String prefix) {
        if (raw == null)
            return null; // laisse les constructeurs lever les erreurs
        String t = raw.trim().toUpperCase(Locale.ROOT);
        if (t.isEmpty())
            return raw; // idem
        return t.startsWith(prefix) ? t : (prefix + t);
    }

    private static String normalizeIso2Required(String raw) {
        if (raw == null)
            return null; // laisse les constructeurs lever les erreurs
        String t = raw.trim().toUpperCase(Locale.ROOT);
        return t;
    }

    private static String normalizeUpperRequired(String raw) {
        if (raw == null)
            return null;
        return raw.trim().toUpperCase(Locale.ROOT);
    }

    private static String normalizeKeepCaseRequired(String raw) {
        if (raw == null)
            return null;
        return raw.trim();
    }

    private static String normalizeOptionalKeepCase(String raw) {
        if (raw == null)
            return null;
        String t = raw.trim();
        return t.isEmpty() ? null : t;
    }
}