package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganizerAccount {

    private final String id;
    private final String clubName;
    private final String email;
    private final boolean emailVerified;

    @JsonCreator
    public OrganizerAccount(
            @JsonProperty("id") String id,
            @JsonProperty("clubName") String clubName,
            @JsonProperty("email") String email,
            @JsonProperty("emailVerified") Boolean emailVerified) {
        this.id = id;
        this.clubName = clubName;
        this.email = email;
        this.emailVerified = emailVerified != null && emailVerified;
    }

    public static OrganizerAccount fromDb(
            String id,
            String clubName,
            String email,
            boolean emailVerified) {
        return new OrganizerAccount(id, clubName, email, emailVerified);
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

    public boolean isEmailVerified() {
        return emailVerified;
    }
}