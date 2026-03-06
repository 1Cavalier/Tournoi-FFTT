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
     * Inscription organisateur avec rattachement obligatoire à un club existant.
     */
    public OrganizerAccount register(String email, String password, String clubId) {
        requireNotBlank(email, "Email obligatoire.");
        requireNotBlank(password, "Mot de passe obligatoire.");
        requireNotBlank(clubId, "Club obligatoire.");

        PasswordPolicy.validateOrThrow(password);

        String cleanEmail = normalizeEmail(email);
        String cleanClubId = clubId.trim();

        clubRepo.findById(cleanClubId)
                .orElseThrow(() -> new IllegalArgumentException("Club introuvable."));

        String passwordHash = HashUtils.hash(password);
        OrganizerAccount account = organizerRepo.insert(cleanClubId, cleanEmail, passwordHash);

        emailVerification.sendVerificationCode(account.getId(), account.getEmail());
        return account;
    }

    public boolean verifyEmail(String email, String code) {
        if (email == null || email.isBlank()) {
            return false;
        }
        if (code == null || code.isBlank()) {
            return false;
        }
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

        return OrganizerAccount.fromDb(row.id(), "", row.email(), row.passwordHash(), true);
    }

    // ---------------- CONNEXION - étape 2 ----------------

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