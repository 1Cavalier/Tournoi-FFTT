package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.mail;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.OrganizerRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class EmailVerificationService {

    private final OrganizerRepository repo;
    private final EmailSender sender;

    public EmailVerificationService(
            OrganizerRepository repo,
            EmailSender sender) {

        this.repo = Objects.requireNonNull(repo);
        this.sender = Objects.requireNonNull(sender);
    }

    // -------------------------------------------------------------------------
    // INSCRIPTION : vérification email
    // -------------------------------------------------------------------------

    public void sendVerificationCode(String organizerId, String email) {

        requireNotBlank(organizerId, "OrganizerId obligatoire.");

        String normalizedEmail = normalizeEmail(email);

        String code = VerificationCodeGenerator.code6();

        String expiresAt = Instant.now()
                .plus(EmailTemplates.EMAIL_VERIFICATION_TTL_MINUTES, ChronoUnit.MINUTES)
                .toString();

        repo.setEmailVerification(organizerId, code, expiresAt);

        sender.send(
                normalizedEmail,
                EmailTemplates.verificationSubject(),
                EmailTemplates.verificationBody(code));
    }

    public boolean verify(String email, String code) {

        if (email == null || email.isBlank()) {
            return false;
        }

        if (code == null || code.isBlank()) {
            return false;
        }

        return repo.verifyEmail(
                normalizeEmail(email),
                code.trim());
    }

    // -------------------------------------------------------------------------
    // CONNEXION : OTP
    // -------------------------------------------------------------------------

    public void sendLoginOtp(String email, String otp) {

        String normalizedEmail = normalizeEmail(email);

        requireNotBlank(otp, "OTP obligatoire.");

        sender.send(
                normalizedEmail,
                EmailTemplates.loginOtpSubject(),
                EmailTemplates.loginOtpBody(otp));
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private static String normalizeEmail(String email) {

        requireNotBlank(email, "Email obligatoire.");

        return email.trim().toLowerCase();
    }

    private static void requireNotBlank(String value, String message) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}