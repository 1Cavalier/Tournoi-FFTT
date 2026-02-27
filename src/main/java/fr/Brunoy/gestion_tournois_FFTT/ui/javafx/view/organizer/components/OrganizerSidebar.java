package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.components;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.SqliteClubRepository;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.util.Optional;

/**
 * Bloc UI: colonne de gauche (profil + navigation + déconnexion).
 * Aucun accès direct aux autres vues: uniquement via Navigator.
 */
public class OrganizerSidebar extends VBox {

    private final Navigator nav;
    private final OrganizerAccount organizer;

    public OrganizerSidebar(Navigator nav, OrganizerAccount organizer) {
        this.nav = nav;
        this.organizer = organizer;

        setSpacing(14);
        setPadding(new Insets(20));
        setPrefWidth(280);
        setStyle("-fx-background-color:#F4F4F4;");

        build();
    }

    private void build() {
        Label title = new Label("Profil Organisme");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        String email = organizer != null ? UiUtils.safe(organizer.getEmail()) : "";

        Optional<SqliteClubRepository.ClubRow> clubOpt = (organizer == null)
                ? Optional.empty()
                : nav.clubRepo().findByOrganizerId(organizer.getId());

        String clubName = clubOpt.map(SqliteClubRepository.ClubRow::clubName).orElse("(club non renseigné)");
        if (clubName == null || clubName.isBlank())
            clubName = "(club non renseigné)";

        StackPane logo = buildLogo(clubOpt.map(SqliteClubRepository.ClubRow::logoPath).orElse(null));

        VBox identityBox = new VBox(
                UiUtils.centeredLabel(clubName, "-fx-font-size: 14px; -fx-font-weight: bold;"),
                UiUtils.centeredLabel(email, "-fx-opacity:0.85;"));
        identityBox.setStyle("-fx-alignment:center;");

        Button editProfileBtn = new Button("Modifier le profil de l'organisme");
        editProfileBtn.setMaxWidth(Double.MAX_VALUE);
        editProfileBtn.setDisable(organizer == null);
        editProfileBtn.setOnAction(e -> nav.showOrganizerProfileDialog());

        VBox details = buildDetails(clubOpt);

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

        getChildren().addAll(
                title,
                UiUtils.centeredBox(logo, 10),
                identityBox,
                editProfileBtn,
                details,
                new Separator(),
                menu,
                new Separator(),
                logoutBtn);
    }

    private StackPane buildLogo(String logoPath) {
        StackPane container = new StackPane();
        container.setPrefSize(140, 140);
        container.setMaxSize(140, 140);
        container.setStyle("""
                -fx-background-color:#E0E0E0;
                -fx-background-radius:100;
                -fx-border-color:black;
                -fx-border-radius:100;
                -fx-border-width:2;
                """);

        if (logoPath != null && !logoPath.isBlank()) {
            try {
                ImageView imageView = new ImageView(new Image("file:" + logoPath, 140, 140, true, true));
                imageView.setFitWidth(140);
                imageView.setFitHeight(140);
                imageView.setClip(new Circle(70, 70, 70));
                container.getChildren().add(imageView);
                return container;
            } catch (Exception ignored) {
                // fallback ci-dessous
            }
        }

        Label placeholder = new Label("LOGO");
        placeholder.setStyle("-fx-font-weight:bold; -fx-opacity:0.6;");
        container.getChildren().add(placeholder);
        return container;
    }

    private VBox buildDetails(Optional<SqliteClubRepository.ClubRow> clubOpt) {
        VBox details = new VBox(4);
        details.setPadding(new Insets(8, 0, 0, 0));

        if (organizer == null) {
            details.getChildren().add(new Label("Profil : non connecté"));
            return details;
        }

        if (clubOpt.isEmpty()) {
            details.getChildren().add(new Label("Club : introuvable"));
            return details;
        }

        SqliteClubRepository.ClubRow c = clubOpt.get();
        details.getChildren().addAll(
                new Label("N° club : " + UiUtils.nvl(c.clubNumber())),
                new Label("Nom club : " + UiUtils.nvl(c.clubName())),
                new Label("Département : " + UiUtils.nvl(c.departementCode())),
                new Label("Ville : " + UiUtils.nvl(c.city())),
                new Label("Adresse 1 : " + UiUtils.nvl(c.address1())),
                new Label("Adresse 2 : " + UiUtils.nvl(c.address2())),
                new Label("Latitude : " + (c.latitude() == null ? "—" : c.latitude())),
                new Label("Longitude : " + (c.longitude() == null ? "—" : c.longitude())),
                new Label("Responsable : " + UiUtils.fullNameOrDash(c.contactFirstName(), c.contactLastName())));

        return details;
    }
}