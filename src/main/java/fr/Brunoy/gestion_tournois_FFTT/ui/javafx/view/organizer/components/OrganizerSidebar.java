package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.components;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.SqliteClubRepository;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;

import java.util.Optional;

public class OrganizerSidebar extends VBox {

    private final Navigator nav;
    private final OrganizerAccount organizer;

    public OrganizerSidebar(Navigator nav, OrganizerAccount organizer) {
        this.nav = nav;
        this.organizer = organizer;

        setSpacing(AppTheme.SPACE_MD);
        setPadding(new Insets(16));
        setPrefWidth(300);
        setStyle(AppTheme.SIDEBAR_STYLE);

        build();
    }

    private void build() {
        Optional<SqliteClubRepository.ClubRow> clubOpt = (organizer == null)
                ? Optional.empty()
                : nav.clubRepo().findByOrganizerId(organizer.getId());

        String email = organizer != null ? UiUtils.safe(organizer.getEmail()) : "";
        String clubName = clubOpt.map(SqliteClubRepository.ClubRow::clubName).orElse("(club non renseigné)");
        if (clubName == null || clubName.isBlank())
            clubName = "(club non renseigné)";

        // Header (petit)
        Label title = new Label("Espace Organisateur");
        AppTheme.applyCardTitle(title);

        StackPane logo = buildLogo(clubOpt.map(SqliteClubRepository.ClubRow::logoPath).orElse(null));

        Label clubLabel = new Label(clubName);
        clubLabel.setStyle("-fx-font-weight: 900; -fx-text-fill: " + AppTheme.COLOR_TEXT + ";");
        clubLabel.setWrapText(true);

        Label emailLabel = new Label(email);
        AppTheme.applyBody(emailLabel);

        VBox identity = new VBox(6, logo, clubLabel, emailLabel);
        identity.setAlignment(Pos.CENTER);
        identity.setPadding(new Insets(8, 8, 8, 8));

        Button editProfileBtn = new Button("Profil du club");
        AppTheme.styleSecondary(editProfileBtn);
        editProfileBtn.setDisable(organizer == null);
        editProfileBtn.setOnAction(e -> nav.showOrganizerProfileDialog());

        // Menu
        VBox menu = new VBox(10);
        Button dashboardBtn = new Button("Dashboard");
        Button historiqueBtn = new Button("Historique");
        Button tournoiBtn = new Button("Tournois");

        AppTheme.styleSecondary(dashboardBtn);
        AppTheme.styleSecondary(historiqueBtn);
        AppTheme.styleSecondary(tournoiBtn);

        // TODO: branche tes routes
        // dashboardBtn.setOnAction(e -> nav.showOrganizerDashboard());
        // tournoiBtn.setOnAction(e -> nav.showOrganizerTournaments());
        // etc.

        menu.getChildren().addAll(dashboardBtn, tournoiBtn, historiqueBtn);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("Déconnexion");
        AppTheme.styleSecondary(logoutBtn);
        logoutBtn.setOnAction(e -> nav.logoutOrganizer());

        // (Optionnel) détails club -> mets-les dans un dialog plutôt que de saturer la
        // sidebar
        Button clubInfosBtn = new Button("Infos club");
        AppTheme.styleLinkButton(clubInfosBtn);
        clubInfosBtn.setOnAction(e -> UiUtils.info("Infos club", buildClubInfoText(clubOpt)));

        getChildren().addAll(
                title,
                identity,
                editProfileBtn,
                clubInfosBtn,
                new Separator(),
                menu,
                spacer,
                new Separator(),
                logoutBtn);
    }

    private String buildClubInfoText(Optional<SqliteClubRepository.ClubRow> clubOpt) {
        if (organizer == null)
            return "Profil : non connecté";
        if (clubOpt.isEmpty())
            return "Club : introuvable";
        SqliteClubRepository.ClubRow c = clubOpt.get();
        return ""
                + "N° club : " + UiUtils.nvl(c.clubNumber()) + "\n"
                + "Nom club : " + UiUtils.nvl(c.clubName()) + "\n"
                + "Département : " + UiUtils.nvl(c.departementCode()) + "\n"
                + "Ville : " + UiUtils.nvl(c.city()) + "\n"
                + "Adresse 1 : " + UiUtils.nvl(c.address1()) + "\n"
                + "Adresse 2 : " + UiUtils.nvl(c.address2()) + "\n"
                + "Latitude : " + (c.latitude() == null ? "—" : c.latitude()) + "\n"
                + "Longitude : " + (c.longitude() == null ? "—" : c.longitude()) + "\n"
                + "Responsable : " + UiUtils.fullNameOrDash(c.contactFirstName(), c.contactLastName());
    }

    private StackPane buildLogo(String logoPath) {
        double size = 84;

        StackPane container = new StackPane();
        container.setPrefSize(size, size);
        container.setMaxSize(size, size);
        container.setStyle(
                "-fx-background-color: rgba(21,101,192,0.08);" +
                        "-fx-background-radius: 999;" +
                        "-fx-border-color: " + AppTheme.COLOR_BORDER + ";" +
                        "-fx-border-radius: 999;");

        if (logoPath != null && !logoPath.isBlank()) {
            try {
                ImageView iv = new ImageView(new Image("file:" + logoPath, size, size, true, true));
                iv.setFitWidth(size);
                iv.setFitHeight(size);
                iv.setClip(new Circle(size / 2, size / 2, size / 2));
                container.getChildren().add(iv);
                return container;
            } catch (Exception ignored) {
            }
        }

        Label placeholder = new Label("LOGO");
        placeholder.setStyle("-fx-font-weight: 900; -fx-text-fill: rgba(30,41,59,0.55);");
        container.getChildren().add(placeholder);
        return container;
    }
}