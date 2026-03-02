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
import javafx.scene.layout.Region;

public class OrganizerDashboardView extends BorderPane {

    public OrganizerDashboardView(Navigator nav) {
        AppTheme.applyPage(this);

        OrganizerAccount organizer = nav.getCurrentOrganizer();

        setTop(buildTopBar(nav, organizer));
        setLeft(new OrganizerSidebar(nav, organizer)); // tu pourras y appliquer AppTheme.SIDEBAR_STYLE
        setCenter(new OrganizerMainContent(nav, organizer));
    }

    private HBox buildTopBar(Navigator nav, OrganizerAccount org) {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10, 16, 10, 16));
        bar.setMinHeight(AppTheme.TOPBAR_HEIGHT);
        bar.setStyle(AppTheme.TOPBAR_STYLE);

        ImageView logo = AppTheme.logoView(26);
        if (logo != null)
            bar.getChildren().add(logo);

        Label appName = new Label("PingManager");
        appName.setStyle("-fx-font-size: 16px; -fx-font-weight: 900; -fx-text-fill: " + AppTheme.COLOR_TEXT + ";");

        Label orgName = new Label(org == null ? "" : "— " + safe(org.getClubName()));
        orgName.setStyle("-fx-font-size: 13px; -fx-text-fill: " + AppTheme.COLOR_TEXT_MUTED + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // Badge (ex: OFFLINE)
        Label mode = new Label("OFFLINE");
        mode.setStyle(AppTheme.badgeStyle("#64748B")); // gris
        // si plus tard tu sais online : "#16A34A"

        Button logout = new Button("Déconnexion");
        AppTheme.styleSecondary(logout);
        // logout.setOnAction(e -> nav.logoutOrganizerAndGoHome()); // à créer ou remplacer

        bar.getChildren().addAll(appName, orgName, spacer, mode, logout);
        return bar;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}