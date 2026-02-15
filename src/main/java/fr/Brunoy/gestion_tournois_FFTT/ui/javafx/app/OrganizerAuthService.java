package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.HashUtils;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.PasswordPolicy;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.OrganizerAccountRepository;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;

public class OrganizerAuthService {

    private final OrganizerAccountRepository repo;

    public OrganizerAuthService(OrganizerAccountRepository repo) {
        this.repo = repo;
    }

    public OrganizerAccount register(String clubName, String email, String password) {
        if (clubName == null || clubName.isBlank())
            throw new IllegalArgumentException("Nom du club obligatoire.");

        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail.isBlank() || !normalizedEmail.contains("@"))
            throw new IllegalArgumentException("Email invalide.");

        if (!PasswordPolicy.isValid(password))
            throw new IllegalArgumentException(PasswordPolicy.rulesText());

        if (repo.findByEmail(normalizedEmail).isPresent())
            throw new IllegalArgumentException("Un compte existe déjà avec cet email.");

        String hash = HashUtils.sha256(password);
        OrganizerAccount created = OrganizerAccount.createNew(clubName.trim(), normalizedEmail, hash);
        repo.insert(created);
        return created;
    }

    public OrganizerAccount login(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail.isBlank())
            throw new IllegalArgumentException("Email obligatoire.");

        String hash = HashUtils.sha256(password == null ? "" : password);

        OrganizerAccount acc = repo.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Email ou mot de passe incorrect."));

        if (!acc.getPasswordHash().equals(hash))
            throw new IllegalArgumentException("Email ou mot de passe incorrect.");

        return acc;
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
