package fr.pingmanager.gestion_tournois_FFTT.ui.javafx.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.ClubAccessDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.ClubDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.OrganizerDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.infra.mail.EmailTemplates;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.infra.mail.EmailVerificationService;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.infra.mail.VerificationCodeGenerator;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.infra.repo.ClubAccessRepository;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.infra.repo.ClubRepository;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.infra.repo.OrganizerRepository;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.infra.security.HashUtils;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.infra.security.PasswordPolicy;

public class OrganizerAuthService {

    private final OrganizerRepository organizerRepo;
    private final ClubRepository clubRepo;
    private final ClubAccessRepository clubAccessRepo;
    private final EmailVerificationService emailVerification;

    public OrganizerAuthService(
            OrganizerRepository organizerRepo,
            ClubRepository clubRepo,
            ClubAccessRepository clubAccessRepo,
            EmailVerificationService emailVerification) {

        this.organizerRepo = Objects.requireNonNull(organizerRepo, "organizerRepo must not be null");
        this.clubRepo = Objects.requireNonNull(clubRepo, "clubRepo must not be null");
        this.clubAccessRepo = Objects.requireNonNull(clubAccessRepo, "clubAccessRepo must not be null");
        this.emailVerification = Objects.requireNonNull(emailVerification, "emailVerification must not be null");
    }

    // -------------------------------------------------------------------------
    // INSCRIPTION
    // -------------------------------------------------------------------------

    public OrganizerDto register(String firstName, String lastName, String email, String password, String clubId) {

        String cleanFirstName = normalizeRequiredText(firstName, "Prénom obligatoire.");
        String cleanLastName = normalizeRequiredText(lastName, "Nom obligatoire.");
        String cleanEmail = normalizeRequiredEmail(email);
        String cleanPassword = normalizeRequiredText(password, "Mot de passe obligatoire.");
        String cleanClubId = normalizeRequiredText(clubId, "Club obligatoire.");

        PasswordPolicy.validateOrThrow(cleanPassword);

        ClubDto club = clubRepo.findById(cleanClubId)
                .orElseThrow(() -> new IllegalArgumentException("Club introuvable."));

        organizerRepo.findByEmail(cleanEmail).ifPresent(existing -> {
            throw new IllegalArgumentException("Un compte existe déjà avec cet email.");
        });

        String clubVerificationEmail = normalizeRequiredEmail(
                club.officialContactEmail(),
                "Aucune adresse email officielle n'est renseignée pour ce club.");

        String passwordHash = HashUtils.hash(cleanPassword);

        OrganizerDto account = organizerRepo.insert(
                cleanClubId,
                cleanFirstName,
                cleanLastName,
                cleanEmail,
                passwordHash);

        // Le code est stocké sur le compte organisateur,
        // mais envoyé à l'adresse officielle du club.
        emailVerification.sendVerificationCode(account.getId(), clubVerificationEmail);

        return account;
    }

    public boolean verifyEmail(String email, String code) {

        if (email == null || email.isBlank()) {
            return false;
        }

        if (code == null || code.isBlank()) {
            return false;
        }

        String cleanEmail = normalizeEmail(email);
        boolean ok = emailVerification.verify(cleanEmail, code.trim());

        if (!ok) {
            return false;
        }

        OrganizerDto organizer = organizerRepo.findByEmail(cleanEmail)
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable."));

        ClubDto club = clubRepo.findByOrganizerId(organizer.getId())
                .orElseThrow(() -> new IllegalArgumentException("Club introuvable pour cet organisateur."));

        if (!clubAccessRepo.existsByClubIdAndEmail(club.id(), organizer.getEmail())) {
            clubAccessRepo.insert(new ClubAccessDto(
                    null,
                    club.id(),
                    organizer.getEmail(),
                    blankToNull(organizer.getFirstName()),
                    blankToNull(organizer.getLastName()),
                    null));
        }

        return true;
    }

    public void resendVerificationCode(String email) {

        String cleanEmail = normalizeRequiredEmail(email);

        OrganizerDto organizer = organizerRepo.findByEmail(cleanEmail)
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable."));

        if (organizer.isEmailVerified()) {
            throw new IllegalArgumentException("Email déjà vérifié.");
        }

        ClubDto club = clubRepo.findByOrganizerId(organizer.getId())
                .orElseThrow(() -> new IllegalArgumentException("Club introuvable pour cet organisateur."));

        String clubVerificationEmail = normalizeRequiredEmail(
                club.officialContactEmail(),
                "Aucune adresse email officielle n'est renseignée pour ce club.");

        emailVerification.sendVerificationCode(organizer.getId(), clubVerificationEmail);
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
        return normalizeRequiredEmail(email, "Email obligatoire.");
    }

    private String normalizeRequiredEmail(String email, String message) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(message);
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

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}