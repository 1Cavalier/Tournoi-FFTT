package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.mail;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.SqliteOrganizerAccountRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Service d'envoi et validation des codes liés à l'email :
 * - code de vérification d'email à l'inscription
 * - envoi du code OTP de connexion (le stockage OTP est géré ailleurs)
 */
public class EmailVerificationService {

    private final SqliteOrganizerAccountRepository repo;
    private final EmailSender sender;

    public EmailVerificationService(SqliteOrganizerAccountRepository repo, EmailSender sender) {
        this.repo = repo;
        this.sender = sender;
    }

    // ---------------- INSCRIPTION : vérification email ----------------

    public void sendVerificationCode(String organizerId, String email) {
        requireNotBlank(organizerId, "OrganizerId obligatoire.");
        requireNotBlank(email, "Email obligatoire.");

        String code = VerificationCodeGenerator.code6();
        String expiresAt = Instant.now()
                .plus(EmailTemplates.EMAIL_VERIFICATION_TTL_MINUTES, ChronoUnit.MINUTES)
                .toString();

        repo.setEmailVerification(organizerId, code, expiresAt);

        sender.send(
                email,
                EmailTemplates.verificationSubject(),
                EmailTemplates.verificationBody(code));
    }

    public boolean verify(String email, String code) {
        if (email == null || email.isBlank())
            return false;
        if (code == null || code.isBlank())
            return false;
        return repo.verifyEmail(email.trim().toLowerCase(), code.trim());
    }

    // ---------------- CONNEXION : OTP ----------------

    public void sendLoginOtp(String email, String otp) {
        requireNotBlank(email, "Email obligatoire.");
        requireNotBlank(otp, "OTP obligatoire.");

        sender.send(
                email,
                EmailTemplates.loginOtpSubject(),
                EmailTemplates.loginOtpBody(otp));
    }

    private static void requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}