package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.ClubProfileRow;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;

import java.util.Optional;

public class OrganizerDashboardView extends BorderPane {

    public OrganizerDashboardView(Navigator nav) {

        OrganizerAccount organizer = nav.getCurrentOrganizer();

        // Sidebar : tu gardes ta version actuelle (parfaite)
        setLeft(createSidebar(nav, organizer));

        // Centre : nouveau layout simple
        setCenter(createMainContent(nav));
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

        // Charger le profil UNE SEULE FOIS
        Optional<ClubProfileRow> pOpt = (organizer == null)
                ? Optional.empty()
                : nav.clubProfileRepo().findByOrganizerId(organizer.getId());

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

        // si logo en DB -> afficher image, sinon placeholder
        String logoPath = pOpt.map(ClubProfileRow::logoPath).orElse(null);

        if (logoPath != null && !logoPath.isBlank()) {
            try {
                ImageView imageView = new ImageView(new Image("file:" + logoPath, 140, 140, true, true));
                imageView.setFitWidth(140);
                imageView.setFitHeight(140);

                // clip cercle
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

        // ================= IDENTITE =================

        Label nameLabel = new Label(clubName);
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-alignment:center;");

        Label emailLabel = new Label(email);
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
            // connecté mais pas de profil encore
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

    private VBox createMainContent(Navigator nav) {

        VBox root = new VBox(18);
        root.setPadding(new Insets(20));

        VBox publishedBlock = createBlock("Tournoi Publié :", "Rien pour le moment");
        VBox draftBlock = createBlock("Tournoi Brouillon :", "Rien pour le moment");

        Button createBtn = new Button("Crée un Tournoi");
        createBtn.setMaxWidth(Double.MAX_VALUE);
        createBtn.setOnAction(e -> nav.showCreateTournamentDialog());

        VBox buttonRow = new VBox(createBtn);
        buttonRow.setAlignment(Pos.CENTER);
        buttonRow.setPadding(new Insets(8, 0, 0, 0));

        root.getChildren().addAll(publishedBlock, draftBlock, buttonRow);
        return root;
    }

    private VBox createBlock(String titleText, String centerText) {

        VBox block = new VBox(10);
        block.setPadding(new Insets(14));
        block.setStyle("-fx-border-color:black; -fx-border-width:3; -fx-background-color:white;");

        Label title = new Label(titleText);
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Region space = new Region();
        space.setMinHeight(60);

        Label center = new Label(centerText);
        center.setStyle("-fx-font-size: 14px; -fx-text-fill:#d07a4a; -fx-font-weight:bold;");
        center.setMaxWidth(Double.MAX_VALUE);
        center.setAlignment(Pos.CENTER);

        VBox content = new VBox(10, space, center);
        content.setAlignment(Pos.CENTER);
        content.setMinHeight(130);

        block.getChildren().addAll(title, content);
        return block;
    }
}
