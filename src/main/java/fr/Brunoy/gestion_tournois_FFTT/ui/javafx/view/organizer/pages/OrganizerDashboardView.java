package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.pages;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.AppRouter;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.OrganizerDto;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.layout.OrganizerSidebar;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.layout.OrganizerTopBar;
import javafx.scene.layout.BorderPane;

import java.util.Objects;

public class OrganizerDashboardView extends BorderPane {

    private final AppRouter nav;
    private final OrganizerDto organizer;

    public OrganizerDashboardView(AppRouter nav) {
        this.nav = Objects.requireNonNull(nav, "nav must not be null");
        this.organizer = nav.requireOrganizer();

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

    private OrganizerDashboardContent buildMainContent() {
        return new OrganizerDashboardContent(nav, organizer);
    }
}