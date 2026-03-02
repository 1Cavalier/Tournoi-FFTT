package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.components;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.TournamentRow;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.util.List;

public class OrganizerMainContent extends VBox {

    private final Navigator nav;
    private final OrganizerAccount organizer;

    public OrganizerMainContent(Navigator nav, OrganizerAccount organizer) {
        this.nav = nav;
        this.organizer = organizer;
        build();
    }

    private void build() {
        AppTheme.applyPage(this);

        VBox root = new VBox(AppTheme.SPACE_LG);
        root.setPadding(new Insets(18));
        root.setMaxWidth(Double.MAX_VALUE);

        if (organizer == null) {
            Label msg = new Label("Aucun organisme connecté.");
            AppTheme.applySubtitle(msg);
            root.getChildren().add(AppTheme.card(msg));
            setChildrenAsScroll(root);
            return;
        }

        // Header
        VBox header = new VBox(AppTheme.SPACE_SM);
        Label h1 = new Label("Tableau de bord");
        AppTheme.applyTitle(h1);

        Label h2 = new Label("Gérez vos tournois : création, publication, inscriptions, tableaux et résultats.");
        AppTheme.applySubtitle(h2);

        header.getChildren().addAll(h1, h2);

        // Data
        List<TournamentRow> active = nav.tournamentRepo().findActiveForOrganizer(organizer.getId());
        List<TournamentRow> drafts = nav.tournamentRepo().findDraftForOrganizer(organizer.getId());

        VBox activeBlock = buildSection(
                "Tournois actifs",
                "OPEN / RUNNING",
                active.isEmpty()
                        ? infoBanner("Aucun tournoi publié ou en cours.")
                        : UiUtils.tournamentList(nav, active, TournamentCard.Mode.ACTIVE));

        VBox draftBlock = buildSection(
                "Tournois en préparation",
                "DRAFT",
                drafts.isEmpty()
                        ? infoBanner("Aucun tournoi en brouillon.")
                        : UiUtils.tournamentList(nav, drafts, TournamentCard.Mode.DRAFT));

        // CTA
        Button createBtn = new Button("Créer un tournoi");
        AppTheme.stylePrimary(createBtn);
        createBtn.setOnAction(e -> nav.showCreateTournamentDialog());
        createBtn.setMaxWidth(260);

        HBox cta = new HBox(createBtn);
        cta.setAlignment(Pos.CENTER);
        cta.setPadding(new Insets(4, 0, 0, 0));

        root.getChildren().addAll(header, activeBlock, draftBlock, cta);
        setChildrenAsScroll(root);
    }

    private VBox buildSection(String title, String badgeText, Region content) {
        HBox head = new HBox(10);
        head.setAlignment(Pos.CENTER_LEFT);

        Label t = new Label(title);
        AppTheme.applyCardTitle(t);

        Label badge = new Label(badgeText);
        badge.setStyle(AppTheme.badgeStyle(AppTheme.COLOR_PRIMARY)); // bleu charte

        head.getChildren().addAll(t, badge);

        VBox box = new VBox(AppTheme.SPACE_MD, head, content);
        VBox card = AppTheme.card(box);
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    /**
     * Petit bandeau neutre quand une section est vide.
     * (On évite UiUtils.infoBanner pour centraliser le style.)
     */
    private Region infoBanner(String text) {
        Label l = new Label(text);
        AppTheme.applyBody(l);

        VBox box = new VBox(l);
        box.setPadding(new Insets(12));
        box.setStyle(
                "-fx-background-color: " + AppTheme.COLOR_SURFACE + ";" +
                        "-fx-background-radius: " + AppTheme.RADIUS + ";" +
                        "-fx-border-color: " + AppTheme.COLOR_BORDER + ";" +
                        "-fx-border-radius: " + AppTheme.RADIUS + ";");

        // Option: tu peux retourner AppTheme.card(...) mais ça ferait “double card”.
        // Ici on garde une banner simple.
        return box;
    }

    private void setChildrenAsScroll(VBox root) {
        ScrollPane sp = new ScrollPane(root);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setPadding(Insets.EMPTY);

        getChildren().setAll(sp);
        VBox.setVgrow(sp, Priority.ALWAYS);
    }
}