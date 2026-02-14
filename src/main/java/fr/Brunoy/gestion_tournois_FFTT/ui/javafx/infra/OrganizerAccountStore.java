package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class OrganizerAccountStore {

    private final Path file;
    private final ObjectMapper mapper = new ObjectMapper();

    public OrganizerAccountStore(Path file) {
        this.file = file;
    }

    public List<OrganizerAccount> loadAll() {
        try {
            ensureParentDir();
            if (!Files.exists(file))
                return new ArrayList<>();
            byte[] json = Files.readAllBytes(file);
            if (json.length == 0)
                return new ArrayList<>();
            return mapper.readValue(json, new TypeReference<List<OrganizerAccount>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException("Cannot read " + file, e);
        }
    }

    public void saveAll(List<OrganizerAccount> accounts) {
        try {
            ensureParentDir();
            byte[] json = mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(accounts);
            Files.write(file, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Cannot write " + file, e);
        }
    }

    private void ensureParentDir() throws IOException {
        Path parent = file.getParent();
        if (parent != null && !Files.exists(parent))
            Files.createDirectories(parent);
    }
}
