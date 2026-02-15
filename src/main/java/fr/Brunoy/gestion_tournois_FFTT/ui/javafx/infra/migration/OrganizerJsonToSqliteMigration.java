package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.migration;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.OrganizerAccountStore;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.OrganizerAccountRepository;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class OrganizerJsonToSqliteMigration {

    private OrganizerJsonToSqliteMigration() {}

    public static void importIfNeeded(OrganizerAccountRepository repo, Path jsonFile) {
        try {
            if (!repo.isEmpty()) return;
            if (!Files.exists(jsonFile)) return;

            OrganizerAccountStore store = new OrganizerAccountStore(jsonFile);
            List<OrganizerAccount> accounts = store.loadAll();
            if (accounts.isEmpty()) return;

            for (OrganizerAccount acc : accounts) {
                // ignore doublons potentiels
                repo.findByEmail(acc.getEmail()).ifPresentOrElse(
                        existing -> {},
                        () -> repo.insert(acc)
                );
            }

            // Option “pro” : on renomme le fichier après import
            Path backup = jsonFile.resolveSibling("organizers.backup.json");
            Files.move(jsonFile, backup);
        } catch (Exception e) {
            throw new RuntimeException("Migration JSON -> SQLite impossible", e);
        }
    }
}
