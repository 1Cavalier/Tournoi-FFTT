package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.mail;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.SqliteOrganizerAccountRepository;

import java.time.Instant;

public class EmailVerificationService {

    private final SqliteOrganizerAccountRepository repo;
    private final EmailSender sender;

    public EmailVerificationService(SqliteOrganizerAccountRepository repo, EmailSender sender) {
        this.repo = repo;
        this.sender = sender;
    }

    // ---------------- INSCRIPTION : verification email ----------------

    public void sendVerificationCode(String organizerId, String email) {
        String code = VerificationCodeGenerator.code6();
        String expiresAt = Instant.now().plusSeconds(15 * 60).toString(); // 15 min

        repo.setEmailVerification(organizerId, code, expiresAt);

        sender.send(email,
                EmailTemplates.verificationSubject(),
                EmailTemplates.verificationBody(code));
    }

    public boolean verify(String email, String code) {
        return repo.verifyEmail(email, code);
    }

    // ---------------- CONNEXION : OTP à chaque login ----------------

    public void sendLoginOtp(String email, String otp) {
        sender.send(email,
                EmailTemplates.loginOtpSubject(),
                EmailTemplates.loginOtpBody(otp));
    }
}