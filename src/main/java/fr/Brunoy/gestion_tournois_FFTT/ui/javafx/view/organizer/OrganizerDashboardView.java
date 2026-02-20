package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.ClubProfileRow;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.TableauRow;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.TournamentRow;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;

import java.util.List;
import java.util.Optional;

public class OrganizerDashboardView extends BorderPane {

    public OrganizerDashboardView(Navigator nav) {
        OrganizerAccount organizer = nav.getCurrentOrganizer();
        setLeft(createSidebar(nav, organizer));
        setCenter(createMainContent(nav, organizer));
    }

    // ================= SIDEBAR (inchangé) =================

    private VBox createSidebar(Navigator nav, OrganizerAccount organizer) {

        VBox sidebar = new VBox(14);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(280);
        sidebar.setStyle("-fx-background-color:#F4F4F4;");

        Label title = new Label("Profil Organisme");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        String clubName = organizer != null ? organizer.getClubName() : "(non connecté)";
        String email = organizer != null ? organizer.getEmail() : "";

        Optional<ClubProfileRow> pOpt = (organizer == null)
                ? Optional.empty()
                : nav.clubProfileRepo().findByOrganizerId(organizer.getId());

        StackPane logoContainer = new StackPane();
        logoContainer.setPrefSize(140, 140);
        logoContainer.setMaxSize(140, 140);
        logoContainer.setStyle("""
                    -fx-background-color:#E0E0E0;
                    -fx-background-radius:100;
                    -fx-border-color:black;
                    -fx-border-radius:100;
                    -fx-border-width:2;
                """);

        String logoPath = pOpt.map(ClubProfileRow::logoPath).orElse(null);

        if (logoPath != null && !logoPath.isBlank()) {
            try {
                ImageView imageView = new ImageView(new Image("file:" + logoPath, 140, 140, true, true));
                imageView.setFitWidth(140);
                imageView.setFitHeight(140);
                Circle clip = new Circle(70, 70, 70);
                imageView.setClip(clip);
                logoContainer.getChildren().add(imageView);
            } catch (Exception e) {
                Label placeholder = new Label("LOGO");
                placeholder.setStyle("-fx-font-weight:bold; -fx-opacity:0.6;");
                logoContainer.getChildren().add(placeholder);
            }
        } else {
            Label placeholder = new Label("LOGO");
            placeholder.setStyle("-fx-font-weight:bold; -fx-opacity:0.6;");
            logoContainer.getChildren().add(placeholder);
        }

        VBox logoBox = new VBox(10, logoContainer);
        logoBox.setFillWidth(true);
        logoBox.setPadding(new Insets(10, 0, 0, 0));
        logoBox.setStyle("-fx-alignment:center;");

        Label nameLabel = new Label(clubName);
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-alignment:center;");

        Label emailLabel = new Label(email);
        emailLabel.setMaxWidth(Double.MAX_VALUE);
        emailLabel.setStyle("-fx-opacity:0.85; -fx-alignment:center;");

        VBox identityBox = new VBox(4, nameLabel, emailLabel);
        identityBox.setStyle("-fx-alignment:center;");

        Button editProfileBtn = new Button("Modifier le profil de l'organisme");
        editProfileBtn.setMaxWidth(Double.MAX_VALUE);
        editProfileBtn.setDisable(organizer == null);
        editProfileBtn.setOnAction(e -> nav.showOrganizerProfileDialog());

        VBox details = new VBox(4);
        details.setPadding(new Insets(8, 0, 0, 0));

        if (organizer != null && pOpt.isPresent()) {
            ClubProfileRow p = pOpt.get();
            details.getChildren().addAll(
                    new Label("N° club : " + nvl(p.clubNumber())),
                    new Label("Nom club : " + nvl(p.clubName())),
                    new Label("Département : " + nvl(p.departementCode())),
                    new Label("Ville : " + nvl(p.city())),
                    new Label("Adresse 1 : " + nvl(p.address1())),
                    new Label("Adresse 2 : " + nvl(p.address2())),
                    new Label("Latitude : " + (p.latitude() == null ? "null" : p.latitude())),
                    new Label("Longitude : " + (p.longitude() == null ? "null" : p.longitude())),
                    new Label("Responsable : " + fullNameOrNull(p.contactFirstName(), p.contactLastName())));
        } else if (organizer != null) {
            details.getChildren().addAll(
                    new Label("N° club : null"),
                    new Label("Nom club : null"),
                    new Label("Département : null"),
                    new Label("Ville : null"),
                    new Label("Adresse 1 : null"),
                    new Label("Adresse 2 : null"),
                    new Label("Latitude : null"),
                    new Label("Longitude : null"),
                    new Label("Responsable : null"));
        } else {
            details.getChildren().add(new Label("Profil : null"));
        }

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
                title,
                logoBox,
                identityBox,
                editProfileBtn,
                details,
                new Separator(),
                menu,
                new Separator(),
                logoutBtn);

        return sidebar;
    }

    private String nvl(String s) {
        return (s == null || s.isBlank()) ? "null" : s.trim();
    }

    private String fullNameOrNull(String firstName, String lastName) {
        String fn = (firstName == null) ? "" : firstName.trim();
        String ln = (lastName == null) ? "" : lastName.trim();
        String full = (fn + " " + ln).trim();
        return full.isEmpty() ? "null" : full;
    }

    // ================= MAIN CONTENT (refait) =================

    private VBox createMainContent(Navigator nav, OrganizerAccount organizer) {

        VBox root = new VBox(16);
        root.setPadding(new Insets(20));

        if (organizer == null) {
            root.getChildren().add(new Label("Aucun organisme connecté."));
            return root;
        }

        // récupérer les listes
        List<TournamentRow> active = nav.tournamentRepo().findActiveForOrganizer(organizer.getId());
        List<TournamentRow> drafts = nav.tournamentRepo().findDraftForOrganizer(organizer.getId());

        // --- Zone tournois actifs ---
        root.getChildren().add(sectionTitle("Tournois actifs (OPEN / RUNNING)"));

        if (active.isEmpty()) {
            root.getChildren().add(infoBanner("Actuellement aucun tournoi n'est en cours ni n'a été publié."));
        } else {
            root.getChildren().add(tournamentsListBox(nav, active, Mode.ACTIVE));
        }

        // --- Zone drafts ---
        root.getChildren().add(sectionTitle("Tournois en préparation (DRAFT)"));

        if (drafts.isEmpty()) {
            root.getChildren().add(infoBanner("Vous n'avez pas commencé à créer un tournoi."));
        } else {
            root.getChildren().add(tournamentsListBox(nav, drafts, Mode.DRAFT));
        }

        Button createBtn = new Button("Créer un tournoi");
        createBtn.setOnAction(e -> nav.showCreateTournamentDialog());
        createBtn.setMaxWidth(240);

        HBox bottom = new HBox(createBtn);
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(new Insets(10, 0, 0, 0));

        root.getChildren().add(bottom);

        // scroll si beaucoup de tournois
        ScrollPane sp = new ScrollPane(root);
        sp.setFitToWidth(true);
        sp.setPadding(Insets.EMPTY);

        VBox container = new VBox(sp);
        VBox.setVgrow(sp, Priority.ALWAYS);
        return container;
    }

    private Label sectionTitle(String txt) {
        Label l = new Label(txt);
        l.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        return l;
    }

    private VBox infoBanner(String txt) {
        VBox box = new VBox();
        box.setPadding(new Insets(12));
        box.setStyle("-fx-border-color:black; -fx-border-width:2; -fx-background-color:white;");
        Label l = new Label(txt);
        l.setStyle("-fx-font-weight:bold;");
        box.getChildren().add(l);
        return box;
    }

    private VBox tournamentsListBox(Navigator nav, List<TournamentRow> tournaments, Mode mode) {
        VBox list = new VBox(12);
        for (TournamentRow t : tournaments) {
            list.getChildren().add(tournamentCard(nav, t, mode));
        }
        return list;
    }

    private VBox tournamentCard(Navigator nav, TournamentRow t, Mode mode) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(14));
        card.setStyle("-fx-border-color:black; -fx-border-width:3; -fx-background-color:white;");

        // header: titre + badge
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(t.name());
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        StatusBadge badge = new StatusBadge(mapStatus(t.status()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(title, spacer, badge);

        // body : 3 colonnes (infos | liste tableaux | actions)
        HBox body = new HBox(14);
        body.setAlignment(Pos.TOP_LEFT);

        VBox leftInfos = tournamentInfosBox(nav, t);
        VBox centerTableaux = tableauxBox(nav, t.id());
        VBox rightActions = actionsBox(t, mode);

        HBox.setHgrow(centerTableaux, Priority.ALWAYS);

        body.getChildren().addAll(leftInfos, centerTableaux, rightActions);

        card.getChildren().addAll(header, new Separator(), body);
        return card;
    }

    private VBox tournamentInfosBox(Navigator nav, TournamentRow t) {
        VBox box = new VBox(6);
        box.setMinWidth(240);

        Optional<ClubProfileRow> pOpt = nav.clubProfileRepo().findByOrganizerId(t.organizerId());

        String ville = pOpt.map(ClubProfileRow::city).orElse(null);
        String dep = pOpt.map(ClubProfileRow::departementCode).orElse(null);

        box.getChildren().addAll(
                kv("Nom", t.name()),
                kv("Lieu",
                        (ville == null || ville.isBlank()) ? "—"
                                : (ville + (dep == null || dep.isBlank() ? "" : " (" + dep + ")"))),
                kv("Niveau", t.level()),
                kv("Phase", t.phase()),
                kv("Dates", t.startDate() + " → " + t.endDate()));
        return box;
    }

    private Label kv(String k, String v) {
        Label l = new Label(k + " : " + (v == null || v.isBlank() ? "—" : v));
        l.setStyle("-fx-opacity:0.9;");
        return l;
    }

    private VBox tableauxBox(Navigator nav, String tournamentId) {
        VBox box = new VBox(8);
        Label title = new Label("Liste des tableaux");
        title.setStyle("-fx-font-weight:bold;");

        TableView<TableauRow> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(140);

        TableColumn<TableauRow, String> cCode = new TableColumn<>("Code");
        cCode.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().code()));

        TableColumn<TableauRow, String> cLabel = new TableColumn<>("Libellé");
        cLabel.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().label()));

        TableColumn<TableauRow, String> cDate = new TableColumn<>("Date");
        cDate.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().date()));

        TableColumn<TableauRow, String> cPrice = new TableColumn<>("Prix");
        cPrice.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().prepaidEuro() + "€ / " + data.getValue().onsiteEuro() + "€"));

        TableColumn<TableauRow, String> cCap = new TableColumn<>("Cap.");
        cCap.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().capacity())));

        table.getColumns().addAll(cCode, cLabel, cDate, cPrice, cCap);

        // load data
        table.getItems().setAll(nav.tableauRepo().findByTournamentId(tournamentId));

        box.getChildren().addAll(title, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return box;
    }

    private VBox actionsBox(TournamentRow t, Mode mode) {
        VBox box = new VBox(10);
        box.setMinWidth(220);

        if (mode == Mode.ACTIVE) {
            Button bPlayers = new Button("Gestion des joueurs");
            Button bStart = new Button("Lancement du tournoi");
            Button bEdit = new Button("Modification du tournoi");

            bPlayers.setMaxWidth(Double.MAX_VALUE);
            bStart.setMaxWidth(Double.MAX_VALUE);
            bEdit.setMaxWidth(Double.MAX_VALUE);

            // placeholders
            bPlayers.setOnAction(e -> info("À venir", "Gestion des joueurs (à implémenter)."));
            bStart.setOnAction(e -> info("À venir", "Lancement du tournoi (à implémenter)."));
            bEdit.setOnAction(e -> info("À venir", "Modification tournoi actif (à implémenter)."));

            box.getChildren().addAll(bPlayers, bStart, bEdit);

        } else {
            Button bEditInfo = new Button("Modifier infos générales");
            Button bEditTabs = new Button("Modifier les tableaux");
            Button bPublish = new Button("Publier le tournoi");

            bEditInfo.setMaxWidth(Double.MAX_VALUE);
            bEditTabs.setMaxWidth(Double.MAX_VALUE);
            bPublish.setMaxWidth(Double.MAX_VALUE);

            bEditInfo.setOnAction(e -> info("À venir", "Édition infos générales (à implémenter)."));
            bEditTabs.setOnAction(e -> info("À venir", "Édition des tableaux (à implémenter)."));
            bPublish.setOnAction(e -> info("À venir", "Publication (DRAFT → OPEN) (à implémenter)."));

            box.getChildren().addAll(bEditInfo, bEditTabs, bPublish);
        }

        return box;
    }

    private void info(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private TournamentStatus mapStatus(String status) {
        if (status == null)
            return TournamentStatus.DRAFT;
        try {
            return TournamentStatus.valueOf(status.trim().toUpperCase());
        } catch (Exception e) {
            return TournamentStatus.DRAFT;
        }
    }

    private enum Mode {
        ACTIVE, DRAFT
    }
}