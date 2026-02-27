package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.SqliteClubRepository;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.SqliteOrganizerAccountRepository;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.security.HashUtils;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.security.PasswordPolicy;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.mail.EmailVerificationService;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Service d'authentification "organisateur".
 *
 * Inscription :
 * - Crée (ou rattache) un club
 * - Crée un compte
 * - Envoie un code de vérification email
 *
 * Connexion :
 * - Étape 1 : vérifie mot de passe + email vérifié, génère un OTP
 * - Étape 2 : vérifie OTP, retourne le compte final utilisable en session
 */
public class OrganizerAuthService {

    private static final int OTP_DIGITS = 6;
    private static final int OTP_BOUND = 1_000_000;
    private static final int OTP_EXPIRY_MINUTES = 10;

    private final SqliteOrganizerAccountRepository organizerRepo;
    private final SqliteClubRepository clubRepo;
    private final EmailVerificationService emailVerification;

    private final SecureRandom secureRandom = new SecureRandom();

    public OrganizerAuthService(
            SqliteOrganizerAccountRepository organizerRepo,
            SqliteClubRepository clubRepo,
            EmailVerificationService emailVerification) {
        this.organizerRepo = organizerRepo;
        this.clubRepo = clubRepo;
        this.emailVerification = emailVerification;
    }

    // ---------------- INSCRIPTION ----------------

    /**
     * Inscription :
     * - Si existingClubIdOrNull est fourni => rattache à un club existant
     * - Sinon => crée un club minimal (nom optionnel mais recommandé)
     * - Crée le compte (password hash)
     * - Envoie le code de vérification
     */
    public OrganizerAccount register(
            String email,
            String password,
            String existingClubIdOrNull,
            String newClubNameOrNull) {

        requireNotBlank(email, "Email obligatoire.");
        requireNotBlank(password, "Mot de passe obligatoire.");
        PasswordPolicy.validateOrThrow(password);

        String cleanEmail = normalizeEmail(email);
        String clubId = resolveClubId(existingClubIdOrNull, newClubNameOrNull);

        String passwordHash = HashUtils.hash(password);
        OrganizerAccount account = organizerRepo.insert(clubId, cleanEmail, passwordHash);

        emailVerification.sendVerificationCode(account.getId(), account.getEmail());
        return account;
    }

    private String resolveClubId(String existingClubIdOrNull, String newClubNameOrNull) {
        if (existingClubIdOrNull != null && !existingClubIdOrNull.isBlank()) {
            return existingClubIdOrNull.trim();
        }
        String clubName = (newClubNameOrNull == null) ? null : newClubNameOrNull.trim();
        return clubRepo.createClub(null, clubName);
    }

    public boolean verifyEmail(String email, String code) {
        if (email == null || email.isBlank())
            return false;
        if (code == null || code.isBlank())
            return false;
        return emailVerification.verify(normalizeEmail(email), code.trim());
    }

    public void resendVerificationCode(String email) {
        requireNotBlank(email, "Email obligatoire.");

        var row = organizerRepo.findDbRowByEmail(normalizeEmail(email))
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable."));

        if (row.emailVerified()) {
            throw new IllegalArgumentException("Email déjà vérifié.");
        }

        emailVerification.sendVerificationCode(row.id(), row.email());
    }

    // ---------------- CONNEXION - étape 1 ----------------

    /**
     * Étape 1 :
     * - Vérifie le mot de passe
     * - Refuse si email non vérifié
     * - Génère OTP et l'enregistre (durée limitée)
     * - Envoie OTP par email
     *
     * Important : ne pas ouvrir la session ici.
     */
    public OrganizerAccount loginStart(String email, String password) {
        requireNotBlank(email, "Email obligatoire.");
        requireNotBlank(password, "Mot de passe obligatoire.");

        String cleanEmail = normalizeEmail(email);

        var row = organizerRepo.findDbRowByEmail(cleanEmail)
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable."));

        String providedHash = HashUtils.hash(password);
        if (!providedHash.equals(row.passwordHash())) {
            throw new IllegalArgumentException("Mot de passe incorrect.");
        }

        if (!row.emailVerified()) {
            throw new IllegalArgumentException("Email non vérifié. Vérifie ton email avec le code reçu.");
        }

        String otp = generateOtp();
        String expiresAt = Instant.now().plus(OTP_EXPIRY_MINUTES, ChronoUnit.MINUTES).toString();

        organizerRepo.setLoginOtp(row.id(), otp, expiresAt);
        emailVerification.sendLoginOtp(row.email(), otp);

        // Retour minimal : la session ne doit être ouverte qu'après
        // verifyLoginOtpAndFinish
        return OrganizerAccount.fromDb(row.id(), "", row.email(), row.passwordHash(), true);
    }

    // ---------------- CONNEXION - étape 2 ----------------

    /**
     * Étape 2 :
     * - Vérifie OTP en base (valide + non expiré)
     * - Retourne le compte complet (pour session)
     */
    public OrganizerAccount verifyLoginOtpAndFinish(String email, String otpCode) {
        requireNotBlank(email, "Email obligatoire.");
        requireNotBlank(otpCode, "Code OTP obligatoire.");

        String cleanEmail = normalizeEmail(email);

        boolean ok = organizerRepo.verifyLoginOtp(cleanEmail, otpCode.trim());
        if (!ok) {
            throw new IllegalArgumentException("OTP invalide ou expiré.");
        }

        return organizerRepo.findByEmail(cleanEmail)
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable."));
    }

    // ---------------- Helpers ----------------

    private String generateOtp() {
        int value = secureRandom.nextInt(OTP_BOUND);
        return String.format("%0" + OTP_DIGITS + "d", value);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private void requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}