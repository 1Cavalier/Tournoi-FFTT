package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;

import java.util.Optional;

public interface OrganizerAccountRepository {
    boolean isEmpty();

    Optional<OrganizerAccount> findByEmail(String email);

    void insert(OrganizerAccount acc);
}
