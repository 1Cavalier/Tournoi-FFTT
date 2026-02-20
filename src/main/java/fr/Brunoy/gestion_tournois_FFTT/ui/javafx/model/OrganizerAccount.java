package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganizerAccount {

    private final String id;
    private final String clubName;
    private final String email;
    private final String passwordHash;
    private final boolean emailVerified;

    @JsonCreator
    public OrganizerAccount(
            @JsonProperty("id") String id,
            @JsonProperty("clubName") String clubName,
            @JsonProperty("email") String email,
            @JsonProperty("passwordHash") String passwordHash,
            @JsonProperty("emailVerified") Boolean emailVerified) {
        this.id = id;
        this.clubName = clubName;
        this.email = email;
        this.passwordHash = passwordHash;

        // par défaut = false si absent (ancien JSON)
        this.emailVerified = (emailVerified != null) && emailVerified;
    }

    // conserve ton helper, mais emailVerified = false à la création
    public static OrganizerAccount createNew(String clubName, String email, String passwordHash) {
        return new OrganizerAccount(UUID.randomUUID().toString(), clubName, email, passwordHash, false);
    }

    // utile pour construire depuis DB facilement
    public static OrganizerAccount fromDb(String id, String clubName, String email, String passwordHash,
            boolean emailVerified) {
        return new OrganizerAccount(id, clubName, email, passwordHash, emailVerified);
    }

    public String getId() {
        return id;
    }

    public String getClubName() {
        return clubName;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }
}