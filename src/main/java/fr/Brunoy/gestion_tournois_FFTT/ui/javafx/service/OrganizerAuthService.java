package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.service;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.OrganizerDto;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.mail.EmailTemplates;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.mail.EmailVerificationService;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.mail.VerificationCodeGenerator;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.ClubRepository;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.OrganizerRepository;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.security.HashUtils;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.security.PasswordPolicy;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class OrganizerAuthService {

    private final OrganizerRepository organizerRepo;
    private final ClubRepository clubRepo;
    private final EmailVerificationService emailVerification;

    public OrganizerAuthService(
            OrganizerRepository organizerRepo,
            ClubRepository clubRepo,
            EmailVerificationService emailVerification) {

        this.organizerRepo = Objects.requireNonNull(organizerRepo, "organizerRepo must not be null");
        this.clubRepo = Objects.requireNonNull(clubRepo, "clubRepo must not be null");
        this.emailVerification = Objects.requireNonNull(emailVerification, "emailVerification must not be null");
    }

    // -------------------------------------------------------------------------
    // INSCRIPTION
    // -------------------------------------------------------------------------

    public OrganizerDto register(String email, String password, String clubId) {

        String cleanEmail = normalizeRequiredEmail(email);
        String cleanPassword = normalizeRequiredText(password, "Mot de passe obligatoire.");
        String cleanClubId = normalizeRequiredText(clubId, "Club obligatoire.");

        PasswordPolicy.validateOrThrow(cleanPassword);

        clubRepo.findById(cleanClubId)
                .orElseThrow(() -> new IllegalArgumentException("Club introuvable."));

        organizerRepo.findByEmail(cleanEmail).ifPresent(existing -> {
            throw new IllegalArgumentException("Un compte existe déjà avec cet email.");
        });

        String passwordHash = HashUtils.hash(cleanPassword);

        OrganizerDto account = organizerRepo.insert(cleanClubId, cleanEmail, passwordHash);

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

        OrganizerDto organizer = organizerRepo.findByEmail(cleanEmail)
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable."));

        if (organizer.isEmailVerified()) {
            throw new IllegalArgumentException("Email déjà vérifié.");
        }

        emailVerification.sendVerificationCode(organizer.getId(), organizer.getEmail());
    }

    // -------------------------------------------------------------------------
    // CONNEXION - ÉTAPE 1
    // -------------------------------------------------------------------------

    public void loginStart(String email, String password) {

        String cleanEmail = normalizeRequiredEmail(email);
        String cleanPassword = normalizeRequiredText(password, "Mot de passe obligatoire.");

        OrganizerDto organizer = organizerRepo.findByEmail(cleanEmail)
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable."));

        var dbRow = organizerRepo.findAuthByEmail(cleanEmail)
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable."));

        if (!HashUtils.verify(cleanPassword, dbRow.passwordHash())) {
            throw new IllegalArgumentException("Mot de passe incorrect.");
        }

        if (!organizer.isEmailVerified()) {
            throw new IllegalArgumentException("Email non vérifié. Vérifie ton email avec le code reçu.");
        }

        String otp = VerificationCodeGenerator.code6();
        String expiresAt = Instant.now()
                .plus(EmailTemplates.LOGIN_OTP_TTL_MINUTES, ChronoUnit.MINUTES)
                .toString();

        organizerRepo.setLoginOtp(organizer.getId(), otp, expiresAt);
        emailVerification.sendLoginOtp(organizer.getEmail(), otp);
    }

    // -------------------------------------------------------------------------
    // CONNEXION - ÉTAPE 2
    // -------------------------------------------------------------------------

    public OrganizerDto verifyLoginOtpAndFinish(String email, String otpCode) {

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