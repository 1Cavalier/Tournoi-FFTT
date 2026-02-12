package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model;

public class OrganizerAccount {

    private String name;
    private String email;
    private String password;

    public OrganizerAccount(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
