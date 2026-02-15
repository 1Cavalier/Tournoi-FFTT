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

    @JsonCreator
    public OrganizerAccount(
            @JsonProperty("id") String id,
            @JsonProperty("clubName") String clubName,
            @JsonProperty("email") String email,
            @JsonProperty("passwordHash") String passwordHash) {
        this.id = id;
        this.clubName = clubName;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public static OrganizerAccount createNew(String clubName, String email, String passwordHash) {
        return new OrganizerAccount(UUID.randomUUID().toString(), clubName, email, passwordHash);
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
}
