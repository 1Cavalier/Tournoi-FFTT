package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.components;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.TournamentRow;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Bloc UI: contenu central du dashboard (listes tournois + CTA).
 */
public class OrganizerMainContent extends VBox {

    private final Navigator nav;
    private final OrganizerAccount organizer;

    public OrganizerMainContent(Navigator nav, OrganizerAccount organizer) {
        this.nav = nav;
        this.organizer = organizer;

        build();
    }

    private void build() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));

        if (organizer == null) {
            root.getChildren().add(new Label("Aucun organisme connecté."));
            getChildren().add(root);
            return;
        }

        List<TournamentRow> active = nav.tournamentRepo().findActiveForOrganizer(organizer.getId());
        List<TournamentRow> drafts = nav.tournamentRepo().findDraftForOrganizer(organizer.getId());

        root.getChildren().add(UiUtils.sectionTitle("Tournois actifs (OPEN / RUNNING)"));
        if (active.isEmpty())
            root.getChildren().add(UiUtils.infoBanner("Actuellement aucun tournoi n'est en cours ni n'a été publié."));
        else
            root.getChildren().add(UiUtils.tournamentList(nav, active, TournamentCard.Mode.ACTIVE));

        root.getChildren().add(UiUtils.sectionTitle("Tournois en préparation (DRAFT)"));
        if (drafts.isEmpty())
            root.getChildren().add(UiUtils.infoBanner("Vous n'avez pas commencé à créer un tournoi."));
        else
            root.getChildren().add(UiUtils.tournamentList(nav, drafts, TournamentCard.Mode.DRAFT));

        Button createBtn = new Button("Créer un tournoi");
        createBtn.setOnAction(e -> nav.showCreateTournamentDialog());
        createBtn.setMaxWidth(240);

        HBox bottom = new HBox(createBtn);
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(new Insets(10, 0, 0, 0));
        root.getChildren().add(bottom);

        ScrollPane sp = new ScrollPane(root);
        sp.setFitToWidth(true);
        sp.setPadding(Insets.EMPTY);

        getChildren().add(sp);
        VBox.setVgrow(sp, Priority.ALWAYS);
    }
}