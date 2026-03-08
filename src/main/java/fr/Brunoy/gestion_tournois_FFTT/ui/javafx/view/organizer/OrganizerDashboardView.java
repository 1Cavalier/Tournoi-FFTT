package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.components.OrganizerMainContent;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.components.OrganizerSidebar;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.components.OrganizerTopBar;
import javafx.scene.layout.BorderPane;

import java.util.Objects;

public class OrganizerDashboardView extends BorderPane {

    private final Navigator nav;
    private final OrganizerAccount organizer;

    public OrganizerDashboardView(Navigator nav) {
        this.nav = Objects.requireNonNull(nav, "nav must not be null");
        this.organizer = nav.requireOrganizerSession();

        AppTheme.applyPage(this);

        setTop(buildTopBar());
        setLeft(buildSidebar());
        setCenter(buildMainContent());
    }

    private OrganizerTopBar buildTopBar() {
        return new OrganizerTopBar(nav, organizer);
    }

    private OrganizerSidebar buildSidebar() {
        return new OrganizerSidebar(nav, organizer);
    }

    private OrganizerMainContent buildMainContent() {
        return new OrganizerMainContent(nav, organizer);
    }
}