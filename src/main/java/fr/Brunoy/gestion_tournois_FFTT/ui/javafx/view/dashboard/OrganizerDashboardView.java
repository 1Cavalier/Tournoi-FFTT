package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.dashboard;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class OrganizerDashboardView extends BorderPane {

    public OrganizerDashboardView(Navigator nav) {

        OrganizerAccount organizer = nav.getCurrentOrganizer();

        setLeft(createSidebar(nav, organizer));
        setCenter(createMainContent());
    }

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
        editProfileBtn.setOnAction(e -> {
            // TODO plus tard (édition profil)
        });

        VBox menu = new VBox(8);
        Button accueilBtn = new Button("Accueil");
        accueilBtn.setMaxWidth(Double.MAX_VALUE);

        Button historiqueBtn = new Button("Historique");
        historiqueBtn.setMaxWidth(Double.MAX_VALUE);

        Button tournoiBtn = new Button("Tournoi");
        tournoiBtn.setMaxWidth(Double.MAX_VALUE);

        // Pour l’instant, ce sont des placeholders
        accueilBtn.setOnAction(e -> {
        });
        historiqueBtn.setOnAction(e -> {
        });
        tournoiBtn.setOnAction(e -> {
        });

        menu.getChildren().addAll(accueilBtn, historiqueBtn, tournoiBtn);

        Button logoutBtn = new Button("Déconnexion");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> nav.logoutOrganizer());

        sidebar.getChildren().addAll(
                title,
                nameLabel,
                emailLabel,
                editProfileBtn,
                new Separator(),
                menu,
                new Separator(),
                logoutBtn);

        return sidebar;
    }

    private VBox createMainContent() {

        VBox main = new VBox(18);
        main.setPadding(new Insets(20));

        main.getChildren().addAll(
                createCurrentTournamentHeader(),
                createDashboardPanels(),
                createCreateTournamentButton());

        return main;
    }

    private VBox createCurrentTournamentHeader() {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-border-color:black; -fx-border-radius:6; -fx-background-color:white;");

        Label title = new Label("Tournoi en cours :");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        // Mock : à remplacer par tes données réelles plus tard
        Label statusText = new Label("Aucun tournoi en cours n'a été trouvé");
        statusText.setStyle("-fx-opacity: 0.85;");

        card.getChildren().addAll(title, statusText);
        return card;
    }

    private HBox createDashboardPanels() {

        HBox row = new HBox(18);

        VBox left = createTournamentInfoCard();
        VBox center = createTableauxCard();
        VBox right = createActionsCard();

        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(center, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);

        left.setPrefWidth(320);
        center.setPrefWidth(360);
        right.setPrefWidth(320);

        row.getChildren().addAll(left, center, right);
        return row;
    }

    private VBox createTournamentInfoCard() {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-border-color:black; -fx-border-radius:6; -fx-background-color:white;");

        HBox header = new HBox(10);
        Label title = new Label("Tournoi en cours :");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // Mock badge
        StatusBadge badge = new StatusBadge(TournamentStatus.OPEN);
        header.getChildren().addAll(title, badge);

        VBox content = new VBox(4);
        content.getChildren().addAll(
                new Label("Tournoi : de Brunoy"),
                new Label("Club : Brunoy"),
                new Label("Niveau : National B"),
                new Label("Phase : 2"),
                new Label("Date : 14/02 au 15/02"));

        card.getChildren().addAll(header, new Separator(), content);
        return card;
    }

    private VBox createTableauxCard() {
        VBox card = new VBox(10);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-border-color:black; -fx-border-radius:6; -fx-background-color:white;");

        Label title = new Label("Les Tableaux");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        VBox placeholder = new VBox();
        placeholder.setPadding(new Insets(40));
        placeholder.setStyle("-fx-border-color:#999; -fx-border-style: dashed;");
        placeholder.getChildren().add(new Label("Tableau des tableaux du tournoi"));

        Button editTableaux = new Button("Modifier les tableaux");
        editTableaux.setOnAction(e -> {
            // TODO plus tard
        });

        card.getChildren().addAll(title, placeholder, editTableaux);
        return card;
    }

    private VBox createActionsCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-border-color:black; -fx-border-radius:6; -fx-background-color:white;");

        Label title = new Label("Fonctions");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Button editTournament = new Button("Modifier le tournoi");
        Button listPlayers = new Button("Liste des joueurs");
        Button launch = new Button("Lancer le tournoi");
        Button delete = new Button("Supprimer le tournoi");

        editTournament.setMaxWidth(Double.MAX_VALUE);
        listPlayers.setMaxWidth(Double.MAX_VALUE);
        launch.setMaxWidth(Double.MAX_VALUE);
        delete.setMaxWidth(Double.MAX_VALUE);

        // TODO plus tard
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

    private Button createCreateTournamentButton() {
        Button btn = new Button("Créer un nouveau Tournoi");
        btn.setPrefHeight(40);
        btn.setPrefWidth(260);
        btn.setOnAction(e -> {
            // TODO plus tard : nav.showCreateTournament();
        });
        return btn;
    }
}
