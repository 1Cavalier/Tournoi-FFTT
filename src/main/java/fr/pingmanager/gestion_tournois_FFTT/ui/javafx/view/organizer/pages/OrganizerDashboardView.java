package fr.pingmanager.gestion_tournois_FFTT.ui.javafx.view.organizer.pages;

import javafx.scene.layout.BorderPane;

import java.util.Objects;

import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.app.AppRouter;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.OrganizerDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TournamentDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TournamentRegulationDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.view.organizer.layout.OrganizerSidebar;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.view.organizer.layout.OrganizerTopBar;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.view.organizer.layout.TournamentSection;

public class OrganizerDashboardView extends BorderPane {

    private final AppRouter nav;
    private final OrganizerDto organizer;
    private OrganizerSidebar sidebar;
    private TournamentDto activeTournament;
    private TournamentSection activeSection;

    public OrganizerDashboardView(AppRouter nav) {
        this(nav, null, TournamentSection.HOME);
    }

    public OrganizerDashboardView(AppRouter nav,
            TournamentDto tournament,
            TournamentSection section) {
        this.nav = Objects.requireNonNull(nav);
        this.organizer = nav.requireOrganizer();
        this.activeTournament = tournament;
        this.activeSection = section != null ? section : TournamentSection.HOME;

        AppTheme.applyPage(this);
        setTop(new OrganizerTopBar(nav, organizer));

        this.sidebar = new OrganizerSidebar(nav, organizer, activeTournament, activeSection, this);
        setLeft(sidebar);
        setCenter(buildContent(activeTournament, activeSection));
    }

    public void navigateTo(TournamentDto tournament, TournamentSection section) {
        this.activeTournament = tournament;
        this.activeSection = section;
        this.sidebar = new OrganizerSidebar(nav, organizer, tournament, section, this);
        setLeft(sidebar);
        setCenter(buildContent(tournament, section));
    }

    private javafx.scene.Node buildContent(TournamentDto tournament, TournamentSection section) {
        if (section == TournamentSection.HOME || tournament == null) {
            return new OrganizerDashboardContent(nav, organizer);
        }

        TournamentRegulationDto regulation = null;
        try {
            regulation = nav.tournamentService().getRegulation(tournament.id());
        } catch (Exception ignored) {
        }

        return switch (section) {
            case GENERAL -> new TournamentGeneralView(nav, tournament);
            case REGLEMENT -> new TournamentRegulationView(nav, tournament, regulation);
            case TABLEAUX -> new TournamentTableauxView(nav, tournament, regulation);
            case DOCUMENTS -> new TournamentDocumentsView(nav, tournament);
            default -> new OrganizerDashboardContent(nav, organizer);
        };
    }
}