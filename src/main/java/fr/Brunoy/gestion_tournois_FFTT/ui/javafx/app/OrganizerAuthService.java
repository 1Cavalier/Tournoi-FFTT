package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.SqliteOrganizerAccountRepository;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.security.HashUtils;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.security.PasswordPolicy;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.mail.EmailVerificationService;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;

public class OrganizerAuthService {

    private final SqliteOrganizerAccountRepository repo;
    private final EmailVerificationService emailVerification;

    public OrganizerAuthService(SqliteOrganizerAccountRepository repo,
            EmailVerificationService emailVerification) {
        this.repo = repo;
        this.emailVerification = emailVerification;
    }

    public OrganizerAccount register(String clubName, String email, String password) {

        if (clubName == null || clubName.isBlank()) {
            throw new IllegalArgumentException("Nom du club obligatoire.");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email obligatoire.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Mot de passe obligatoire.");
        }

        PasswordPolicy.isValid(password);

        String cleanEmail = email.trim().toLowerCase();
        String hash = HashUtils.hash(password);

        OrganizerAccount acc = repo.insert(clubName.trim(), cleanEmail, hash);

        // envoyer code email vérification
        emailVerification.sendVerificationCode(acc.getId(), acc.getEmail());

        return acc;
    }

    public OrganizerAccount login(String email, String password) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email obligatoire.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Mot de passe obligatoire.");
        }

        String cleanEmail = email.trim().toLowerCase();

        OrganizerAccount acc = repo.findByEmail(cleanEmail)
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable."));

        // vérifier mot de passe
        if (!HashUtils.verify(password, acc.getPasswordHash())) {
            throw new IllegalArgumentException("Mot de passe incorrect.");
        }

        // bloquer login si email non vérifié
        if (!acc.isEmailVerified()) {
            throw new IllegalArgumentException("Email non vérifié. Vérifie ton email avec le code reçu.");
        }

        return acc;
    }

    public boolean verifyEmail(String email, String code) {
        if (email == null || email.isBlank())
            return false;
        if (code == null || code.isBlank())
            return false;

        return emailVerification.verify(email.trim().toLowerCase(), code.trim());
    }

    public void resendVerificationCode(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email obligatoire.");
        }

        String cleanEmail = email.trim().toLowerCase();

        OrganizerAccount acc = repo.findByEmail(cleanEmail)
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable."));

        if (acc.isEmailVerified()) {
            throw new IllegalArgumentException("Email déjà vérifié.");
        }

        emailVerification.sendVerificationCode(acc.getId(), acc.getEmail());
    }
}