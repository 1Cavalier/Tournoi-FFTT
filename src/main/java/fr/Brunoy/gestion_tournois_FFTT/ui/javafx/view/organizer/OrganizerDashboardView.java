package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.components.OrganizerMainContent;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.components.OrganizerSidebar;
import javafx.scene.layout.BorderPane;

/**
 * Écran principal Organisateur.
 * Contient uniquement l'assemblage des blocs (sidebar + contenu central).
 */
public class OrganizerDashboardView extends BorderPane {

    public OrganizerDashboardView(Navigator nav) {
        OrganizerAccount organizer = nav.getCurrentOrganizer();

        setLeft(new OrganizerSidebar(nav, organizer));
        setCenter(new OrganizerMainContent(nav, organizer));
    }
}