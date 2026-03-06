package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.components.OrganizerMainContent;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.components.OrganizerSidebar;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.Objects;

public class OrganizerDashboardView extends BorderPane {

    private final Navigator nav;
    private final OrganizerAccount organizer;

    public OrganizerDashboardView(Navigator nav) {
        this.nav = Objects.requireNonNull(nav, "nav must not be null");
        this.organizer = nav.requireOrganizerSession();

        AppTheme.applyPage(this);

        setTop(buildTopBar());
        setLeft(buildSidebar());
        setCenter(buildMainContent());
    }

    private HBox buildTopBar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10, 16, 10, 16));
        bar.setMinHeight(AppTheme.TOPBAR_HEIGHT);
        bar.setStyle(AppTheme.TOPBAR_STYLE);

        ImageView logo = AppTheme.logoView(26);
        if (logo != null) {
            bar.getChildren().add(logo);
        }

        Label appName = new Label("PingManager");
        appName.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-font-weight: 900;" +
                        "-fx-text-fill: " + AppTheme.COLOR_TEXT + ";");

        Label organizerLabel = new Label(buildOrganizerLabelText());
        organizerLabel.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-text-fill: " + AppTheme.COLOR_TEXT_MUTED + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label modeBadge = new Label("OFFLINE");
        modeBadge.setStyle(AppTheme.badgeStyle("#64748B"));

        Button logoutButton = new Button("Déconnexion");
        AppTheme.styleSecondary(logoutButton);
        logoutButton.setMaxWidth(Region.USE_PREF_SIZE);
        logoutButton.setOnAction(e -> nav.logoutOrganizer());

        bar.getChildren().addAll(appName, organizerLabel, spacer, modeBadge, logoutButton);
        return bar;
    }

    private OrganizerSidebar buildSidebar() {
        OrganizerSidebar sidebar = new OrganizerSidebar(nav, organizer);
        sidebar.setStyle(AppTheme.SIDEBAR_STYLE);
        return sidebar;
    }

    private OrganizerMainContent buildMainContent() {
        return new OrganizerMainContent(nav, organizer);
    }

    private String buildOrganizerLabelText() {
        String clubName = safeTrim(organizer.getClubName());
        if (clubName.isEmpty()) {
            return "— Club non renseigné";
        }
        return "— " + clubName;
    }

    private static String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}