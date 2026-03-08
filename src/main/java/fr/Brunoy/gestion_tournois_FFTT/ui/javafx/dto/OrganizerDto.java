package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto;

public class OrganizerDto {

    private final String id;
    private final String firstName;
    private final String lastName;
    private final String clubName;
    private final String email;
    private final boolean emailVerified;

    public OrganizerDto(
            String id,
            String firstName,
            String lastName,
            String clubName,
            String email,
            boolean emailVerified) {

        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.clubName = clubName;
        this.email = email;
        this.emailVerified = emailVerified;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
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