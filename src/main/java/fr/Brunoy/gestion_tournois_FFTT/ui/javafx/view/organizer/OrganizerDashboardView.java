package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.TableauRow;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.TournamentRow;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Optional;

public class OrganizerDashboardView extends BorderPane {

    public OrganizerDashboardView(Navigator nav) {

        OrganizerAccount organizer = nav.getCurrentOrganizer();
        setLeft(createSidebar(nav, organizer));

        VBox main = new VBox(18);
        main.setPadding(new Insets(20));

        // --- Charger le tournoi courant depuis la DB ---
        Optional<String> currentId = nav.tournamentRepo().findCurrentTournamentId();

        if (currentId.isEmpty()) {
            //  Aucun tournoi -> bandeau + bouton créer
            main.getChildren().addAll(
                    createNoTournamentHeader(),
                    createCreateTournamentButton(nav));
        } else {
            //  Tournoi courant -> charger tournoi + tableaux
            TournamentRow t = nav.tournamentRepo()
                    .findById(currentId.get())
                    .orElse(null);

            if (t == null) {
                // cas incohérent : app_state pointe vers un id qui n’existe plus
                main.getChildren().addAll(
                        createNoTournamentHeader(),
                        createCreateTournamentButton(nav));
            } else {
                List<TableauRow> tableaux = nav.tableauRepo().listByTournamentId(t.id());

                main.getChildren().addAll(
                        createTournamentHeader(t),
                        createDashboardPanels(t, tableaux),
                        createCreateTournamentButton(nav));
            }
        }

        setCenter(main);
    }

    // ---------------- Sidebar (inchangé, juste copié) ----------------

    private VBox createSidebar(Navigator nav, OrganizerAccount organizer) {

        VBox sidebar = new VBox(14);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(280);
        sidebar.setStyle("-fx-background-color:#F4F4F4;");

        Label title = new Label("Profil Organisme");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        String clubName = organizer != null ? organizer.getClubName() : "(non connecté)";
        String email = organizer != null ? organizer.getEmail() : "";

        Label nameLabel = new Label(clubName);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label emailLabel = new Label(email);
        emailLabel.setStyle("-fx-opacity: 0.85;");

        Button editProfileBtn = new Button("Modifier le profil de l'organisme");
        editProfileBtn.setMaxWidth(Double.MAX_VALUE);

        VBox menu = new VBox(8);
        Button accueilBtn = new Button("Accueil");
        Button historiqueBtn = new Button("Historique");
        Button tournoiBtn = new Button("Tournoi");
        accueilBtn.setMaxWidth(Double.MAX_VALUE);
        historiqueBtn.setMaxWidth(Double.MAX_VALUE);
        tournoiBtn.setMaxWidth(Double.MAX_VALUE);

        menu.getChildren().addAll(accueilBtn, historiqueBtn, tournoiBtn);

        Button logoutBtn = new Button("Déconnexion");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> nav.logoutOrganizer());

        sidebar.getChildren().addAll(
                title, nameLabel, emailLabel, editProfileBtn,
                new Separator(),
                menu,
                new Separator(),
                logoutBtn);

        return sidebar;
    }

    // ---------------- Header dynamique ----------------

    private VBox createNoTournamentHeader() {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-border-color:black; -fx-border-radius:6; -fx-background-color:white;");

        Label title = new Label("Tournoi en cours :");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        Label statusText = new Label("Aucun tournoi en cours n'a été trouvé");
        statusText.setStyle("-fx-opacity: 0.85;");

        card.getChildren().addAll(title, statusText);
        return card;
    }

    private VBox createTournamentHeader(TournamentRow t) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-border-color:black; -fx-border-radius:6; -fx-background-color:white;");

        HBox top = new HBox(10);

        Label title = new Label("Tournoi en cours : " + t.name());
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        StatusBadge badge = new StatusBadge(toStatusEnum(t.status()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        top.getChildren().addAll(title, spacer, badge);

        String dateTxt = ((t.startDate() != null ? t.startDate() : "?") + " → "
                + (t.endDate() != null ? t.endDate() : "?"));
        Label small = new Label("Dates : " + dateTxt);
        small.setStyle("-fx-opacity: 0.85;");

        card.getChildren().addAll(top, small);
        return card;
    }

    // ---------------- Panels dynamiques ----------------

    private HBox createDashboardPanels(TournamentRow t, List<TableauRow> tableaux) {

        HBox row = new HBox(18);

        VBox left = createTournamentInfoCard(t);
        VBox center = createTableauxCard(t, tableaux);
        VBox right = createActionsCard(t);

        left.setPrefWidth(320);
        center.setPrefWidth(360);
        right.setPrefWidth(320);

        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(center, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);

        row.getChildren().addAll(left, center, right);
        return row;
    }

    private VBox createTournamentInfoCard(TournamentRow t) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-border-color:black; -fx-border-radius:6; -fx-background-color:white;");

        HBox header = new HBox(10);

        Label title = new Label("Tournoi :");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        StatusBadge badge = new StatusBadge(toStatusEnum(t.status()));
        header.getChildren().addAll(title, badge);

        VBox content = new VBox(4);
        content.getChildren().addAll(
                new Label("Nom : " + t.name()),
                new Label("Niveau : " + t.level()),
                new Label("Phase : " + t.phase()),
                new Label("Début : " + (t.startDate() == null ? "-" : t.startDate())),
                new Label("Fin : " + (t.endDate() == null ? "-" : t.endDate())));

        card.getChildren().addAll(header, new Separator(), content);
        return card;
    }

    private VBox createTableauxCard(TournamentRow t, List<TableauRow> tableaux) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-border-color:black; -fx-border-radius:6; -fx-background-color:white;");

        Label title = new Label("Les Tableaux");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        VBox list = new VBox(6);
        list.setPadding(new Insets(10));
        list.setStyle("-fx-border-color:#999; -fx-border-style: dashed;");

        if (tableaux.isEmpty()) {
            list.getChildren().add(new Label("Aucun tableau"));
        } else {
            for (TableauRow tb : tableaux) {
                String txt = tb.code() + " — " + tb.label() + " — " + (tb.priceCents() / 100.0) + "€ — cap "
                        + tb.capacity();
                list.getChildren().add(new Label(txt));
            }
        }

        Button editTableaux = new Button("Modifier les tableaux");
        editTableaux.setOnAction(e -> {
            /* TODO plus tard */ });

        // Règle simple : modifier tableaux seulement en DRAFT
        boolean canEdit = toStatusEnum(t.status()) == TournamentStatus.DRAFT;
        editTableaux.setDisable(!canEdit);

        card.getChildren().addAll(title, list, editTableaux);
        return card;
    }

    private VBox createActionsCard(TournamentRow t) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-border-color:black; -fx-border-radius:6; -fx-background-color:white;");

        Label title = new Label("Fonctions");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        TournamentStatus st = toStatusEnum(t.status());

        Button editTournament = new Button("Modifier le tournoi");
        Button listPlayers = new Button("Liste des joueurs");
        Button launch = new Button("Lancer le tournoi");
        Button delete = new Button("Supprimer le tournoi");

        editTournament.setMaxWidth(Double.MAX_VALUE);
        listPlayers.setMaxWidth(Double.MAX_VALUE);
        launch.setMaxWidth(Double.MAX_VALUE);
        delete.setMaxWidth(Double.MAX_VALUE);

        // Activation selon status (logique V1)
        // DRAFT: modifier OK, lancer NON
        // OPEN: modifier limité, liste joueurs OK, lancer OUI
        // RUNNING: liste joueurs OK, modifier NON, lancer NON
        // FINISHED: tout en lecture, suppression éventuellement
        editTournament.setDisable(st != TournamentStatus.DRAFT);
        listPlayers.setDisable(st == TournamentStatus.DRAFT);
        launch.setDisable(!(st == TournamentStatus.OPEN));
        delete.setDisable(st == TournamentStatus.RUNNING); // évite suppression en cours (règle simple)

        editTournament.setOnAction(e -> {
        });
        listPlayers.setOnAction(e -> {
        });
        launch.setOnAction(e -> {
        });
        delete.setOnAction(e -> {
        });

        card.getChildren().addAll(title, new Separator(), editTournament, listPlayers, launch, delete);
        return card;
    }

    private Button createCreateTournamentButton(Navigator nav) {
        Button btn = new Button("Créer un nouveau Tournoi");
        btn.setPrefHeight(40);
        btn.setPrefWidth(260);
        btn.setOnAction(e -> {
            // TODO plus tard : nav.showCreateTournament();
        });
        return btn;
    }

    // ---------------- Mapping status DB -> enum badge ----------------

    private TournamentStatus toStatusEnum(String dbValue) {
        if (dbValue == null)
            return TournamentStatus.DRAFT;
        try {
            return TournamentStatus.valueOf(dbValue.trim().toUpperCase());
        } catch (Exception e) {
            return TournamentStatus.DRAFT;
        }
    }
}
