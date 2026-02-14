package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.HashUtils;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.OrganizerAccountStore;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.PasswordPolicy;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;

import java.util.List;

public class OrganizerAuthService {

    private final OrganizerAccountStore store;

    public OrganizerAuthService(OrganizerAccountStore store) {
        this.store = store;
    }

    public OrganizerAccount register(String clubName, String email, String password) {
        if (clubName == null || clubName.isBlank()) {
            throw new IllegalArgumentException("Nom du club obligatoire.");
        }

        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail.isBlank() || !normalizedEmail.contains("@")) {
            throw new IllegalArgumentException("Email invalide.");
        }

        if (!PasswordPolicy.isValid(password)) {
            throw new IllegalArgumentException(PasswordPolicy.rulesText());
        }

        List<OrganizerAccount> all = store.loadAll();
        boolean exists = all.stream().anyMatch(a -> a.getEmail().equalsIgnoreCase(normalizedEmail));
        if (exists) {
            throw new IllegalArgumentException("Un compte existe déjà avec cet email.");
        }

        String hash = HashUtils.sha256(password);
        OrganizerAccount created = OrganizerAccount.createNew(clubName.trim(), normalizedEmail, hash);

        all.add(created);
        store.saveAll(all);

        return created;
    }

    public OrganizerAccount login(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail.isBlank()) {
            throw new IllegalArgumentException("Email obligatoire.");
        }

        String hash = HashUtils.sha256(password == null ? "" : password);

        List<OrganizerAccount> all = store.loadAll();

        OrganizerAccount acc = all.stream()
                .filter(a -> a.getEmail().equalsIgnoreCase(normalizedEmail))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Email ou mot de passe incorrect."));

        if (!acc.getPasswordHash().equals(hash)) {
            throw new IllegalArgumentException("Email ou mot de passe incorrect.");
        }

        return acc;
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
