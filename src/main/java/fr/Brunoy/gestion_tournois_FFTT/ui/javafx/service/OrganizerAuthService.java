package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.service;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.mail.EmailVerificationService;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.SqliteClubRepository;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.SqliteOrganizerAccountRepository;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.security.HashUtils;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.security.PasswordPolicy;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

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
        this.organizerRepo = Objects.requireNonNull(organizerRepo, "organizerRepo must not be null");
        this.clubRepo = Objects.requireNonNull(clubRepo, "clubRepo must not be null");
        this.emailVerification = Objects.requireNonNull(emailVerification, "emailVerification must not be null");
    }

    // -------------------------------------------------------------------------
    // INSCRIPTION
    // -------------------------------------------------------------------------

    /**
     * Inscription organisateur avec rattachement obligatoire à un club existant.
     * Le compte est créé, puis un code de vérification email est envoyé.
     */
    public OrganizerAccount register(String email, String password, String clubId) {
        String cleanEmail = normalizeRequiredEmail(email);
        String cleanPassword = normalizeRequiredText(password, "Mot de passe obligatoire.");
        String cleanClubId = normalizeRequiredText(clubId, "Club obligatoire.");

        PasswordPolicy.validateOrThrow(cleanPassword);

        clubRepo.findById(cleanClubId)
                .orElseThrow(() -> new IllegalArgumentException("Club introuvable."));

        organizerRepo.findDbRowByEmail(cleanEmail).ifPresent(existing -> {
            throw new IllegalArgumentException("Un compte existe déjà avec cet email.");
        });

        String passwordHash = HashUtils.hash(cleanPassword);
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
        String cleanEmail = normalizeRequiredEmail(email);

        var row = organizerRepo.findDbRowByEmail(cleanEmail)
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable."));

        if (row.emailVerified()) {
            throw new IllegalArgumentException("Email déjà vérifié.");
        }

        emailVerification.sendVerificationCode(row.id(), row.email());
    }

    // -------------------------------------------------------------------------
    // CONNEXION - ÉTAPE 1
    // -------------------------------------------------------------------------

    /**
     * Vérifie email + mot de passe, puis génère et envoie un OTP de connexion.
     * Cette méthode n'ouvre pas la session.
     */
    public void loginStart(String email, String password) {
        String cleanEmail = normalizeRequiredEmail(email);
        String cleanPassword = normalizeRequiredText(password, "Mot de passe obligatoire.");

        var row = organizerRepo.findDbRowByEmail(cleanEmail)
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable."));

        String providedHash = HashUtils.hash(cleanPassword);
        if (!providedHash.equals(row.passwordHash())) {
            throw new IllegalArgumentException("Mot de passe incorrect.");
        }

        if (!row.emailVerified()) {
            throw new IllegalArgumentException("Email non vérifié. Vérifie ton email avec le code reçu.");
        }

        String otp = generateOtp();
        String expiresAt = Instant.now()
                .plus(OTP_EXPIRY_MINUTES, ChronoUnit.MINUTES)
                .toString();

        organizerRepo.setLoginOtp(row.id(), otp, expiresAt);
        emailVerification.sendLoginOtp(row.email(), otp);
    }

    // -------------------------------------------------------------------------
    // CONNEXION - ÉTAPE 2
    // -------------------------------------------------------------------------

    /**
     * Vérifie l'OTP et retourne le compte final utilisable en session.
     */
    public OrganizerAccount verifyLoginOtpAndFinish(String email, String otpCode) {
        String cleanEmail = normalizeRequiredEmail(email);
        String cleanOtp = normalizeRequiredText(otpCode, "Code OTP obligatoire.");

        boolean ok = organizerRepo.verifyLoginOtp(cleanEmail, cleanOtp);
        if (!ok) {
            throw new IllegalArgumentException("OTP invalide ou expiré.");
        }

        return organizerRepo.findByEmail(cleanEmail)
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable."));
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private String generateOtp() {
        int value = secureRandom.nextInt(OTP_BOUND);
        return String.format("%0" + OTP_DIGITS + "d", value);
    }

    private String normalizeRequiredEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email obligatoire.");
        }
        return normalizeEmail(email);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String normalizeRequiredText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}