package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.ClubProfileRow;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.TableauRow;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.TournamentRow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
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

        Optional<String> currentId = nav.tournamentRepo().findCurrentTournamentId();

        if (currentId.isEmpty()) {
            main.getChildren().addAll(
                    createNoTournamentHeader(),
                    createCreateTournamentButton(nav));
        } else {

            TournamentRow t = nav.tournamentRepo()
                    .findById(currentId.get())
                    .orElse(null);

            if (t == null) {
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

    // ================= SIDEBAR =================

    private VBox createSidebar(Navigator nav, OrganizerAccount organizer) {

        VBox sidebar = new VBox(14);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(280);
        sidebar.setStyle("-fx-background-color:#F4F4F4;");

        Label title = new Label("Profil Organisme");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        String clubName = organizer != null ? organizer.getClubName() : "(non connecté)";
        String email = organizer != null ? organizer.getEmail() : "";

        // ================= LOGO =================

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

        Optional<ClubProfileRow> pOpt = organizer == null
                ? Optional.empty()
                : nav.clubProfileRepo().findByOrganizerId(organizer.getId());

        if (pOpt.isPresent() && pOpt.get().logoPath() != null && !pOpt.get().logoPath().isBlank()) {

            try {

                ImageView imageView = new ImageView(new Image("file:" + pOpt.get().logoPath(), 140, 140, true, true));
                imageView.setFitWidth(140);
                imageView.setFitHeight(140);

                // clip cercle
                Circle clip = new Circle(70, 70, 70);
                imageView.setClip(clip);

                logoContainer.getChildren().add(imageView);

            } catch (Exception e) {
                logoContainer.getChildren().add(new Label("LOGO"));
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

        // ================= IDENTITE =================

        Label nameLabel = new Label(clubName);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-alignment:center;");

        Label emailLabel = new Label(email);
        emailLabel.setStyle("-fx-opacity: 0.85;");
        emailLabel.setMaxWidth(Double.MAX_VALUE);
        emailLabel.setStyle("-fx-opacity:0.85; -fx-alignment:center;");

        VBox identityBox = new VBox(4, nameLabel, emailLabel);
        identityBox.setStyle("-fx-alignment:center;");

        // ================= BOUTON =================

        Button editProfileBtn = new Button("Modifier le profil de l'organisme");
        editProfileBtn.setMaxWidth(Double.MAX_VALUE);
        editProfileBtn.setDisable(organizer == null);
        editProfileBtn.setOnAction(e -> nav.showOrganizerProfileDialog());

        // ================= DETAILS =================

        VBox details = new VBox(4);
        details.setPadding(new Insets(8, 0, 0, 0));

        if (organizer != null) {
    
            if (pOpt.isPresent()) {

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
            }
        }

        // ================= MENU =================

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

    // ================= HEADER =================

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

    // ================= PANELS =================

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
                String txt = tb.code() + " — " + tb.label() + " — "
                        + (tb.priceCents() / 100.0) + "€ — cap " + tb.capacity();
                list.getChildren().add(new Label(txt));
            }
        }

        Button editTableaux = new Button("Modifier les tableaux");
        editTableaux.setDisable(toStatusEnum(t.status()) != TournamentStatus.DRAFT);

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

        editTournament.setDisable(st != TournamentStatus.DRAFT);
        listPlayers.setDisable(st == TournamentStatus.DRAFT);
        launch.setDisable(st != TournamentStatus.OPEN);
        delete.setDisable(st == TournamentStatus.RUNNING);

        card.getChildren().addAll(title, new Separator(), editTournament, listPlayers, launch, delete);
        return card;
    }

    private Button createCreateTournamentButton(Navigator nav) {

        Button btn = new Button("Créer un nouveau Tournoi");
        btn.setPrefHeight(40);
        btn.setPrefWidth(260);
        return btn;
    }

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
