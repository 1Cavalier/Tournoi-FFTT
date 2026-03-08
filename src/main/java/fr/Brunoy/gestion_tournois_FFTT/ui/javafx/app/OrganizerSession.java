package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.OrganizerDto;

/**
 * Gère la session de l'organisateur connecté.
 */
public final class OrganizerSession {

    private OrganizerDto currentOrganizer;

    public boolean isLoggedIn() {
        return currentOrganizer != null;
    }

    public OrganizerDto get() {
        if (currentOrganizer == null) {
            throw new IllegalStateException("Aucun organisateur connecté.");
        }
        return currentOrganizer;
    }

    public void login(OrganizerDto organizer) {
        if (organizer == null) {
            throw new IllegalArgumentException("organizer must not be null");
        }
        this.currentOrganizer = organizer;
    }

    public void logout() {
        this.currentOrganizer = null;
    }
}