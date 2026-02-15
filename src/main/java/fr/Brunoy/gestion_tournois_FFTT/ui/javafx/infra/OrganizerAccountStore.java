package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class OrganizerAccountStore {

    private final Path filePath;
    private final ObjectMapper mapper = new ObjectMapper();

    public OrganizerAccountStore(Path filePath) {
        this.filePath = filePath;
    }

    public List<OrganizerAccount> loadAll() {
        try {
            ensureParentDir();
            if (!Files.exists(filePath))
                return new ArrayList<>();

            byte[] json = Files.readAllBytes(filePath);
            if (json.length == 0)
                return new ArrayList<>();

            JsonNode root = mapper.readTree(json);

            // Ancien format: [ {...}, {...} ]
            if (root.isArray()) {
                return mapper.convertValue(root, new TypeReference<List<OrganizerAccount>>() {
                });
            }

            // Nouveau format "pro": { schemaVersion, hashAlgorithm, accounts: [ ... ] }
            if (root.isObject() && root.has("accounts") && root.get("accounts").isArray()) {
                return mapper.convertValue(root.get("accounts"), new TypeReference<List<OrganizerAccount>>() {
                });
            }

            throw new IllegalStateException("Format organizers.json inconnu. Attendu: [..] ou {\"accounts\":[..]}");
        } catch (IOException e) {
            throw new RuntimeException("Cannot read " + filePath, e);
        }
    }

    private void ensureParentDir() throws IOException {
        Path parent = filePath.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
    }
}
