package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.SqliteOrganizerAccountRepository;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.mail.EmailVerificationService;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.security.HashUtils;

public class OrganizerAuthService {

    private final SqliteOrganizerAccountRepository repo;
    private final EmailVerificationService emailService;

    public OrganizerAuthService(SqliteOrganizerAccountRepository repo,
            EmailVerificationService emailService) {
        this.repo = repo;
        this.emailService = emailService;
    }

    public OrganizerAccount register(String clubName, String email, String password) {
        String hash = HashUtils.hash(password);

        OrganizerAccount acc = repo.insert(clubName, email, hash);
        emailService.sendVerificationCode(acc.getId(), email);

        return acc;
    }

    public boolean verifyEmail(String email, String code) {
        return emailService.verify(email, code);
    }

    public OrganizerAccount login(String email, String password) {

        var opt = repo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable"));

        if (!opt.isEmailVerified())
            throw new IllegalArgumentException("Email non vérifié");

        return opt;
    }
}