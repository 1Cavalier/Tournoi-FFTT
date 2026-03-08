package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.components;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.Objects;

public class OrganizerTopBar extends HBox {

    private final Navigator nav;
    private final OrganizerAccount organizer;

    public OrganizerTopBar(Navigator nav, OrganizerAccount organizer) {
        this.nav = Objects.requireNonNull(nav, "nav must not be null");
        this.organizer = Objects.requireNonNull(organizer, "organizer must not be null");

        build();
    }

    private void build() {
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(10, 18, 10, 18));
        setMinHeight(AppTheme.TOPBAR_HEIGHT);
        setSpacing(12);
        setStyle(AppTheme.TOPBAR_STYLE);

        ImageView logo = AppTheme.logoView(24);
        if (logo != null) {
            getChildren().add(logo);
        }

        Label appNameLabel = new Label("PingManager");
        AppTheme.applyTopBarAppName(appNameLabel);

        Label pageTitleLabel = new Label("Tableau de bord");
        AppTheme.applyTopBarPageTitle(pageTitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label dbStatusBadge = new Label("Base locale");
        dbStatusBadge.setStyle(AppTheme.topBarConnectionBadgeStyle(false));

        getChildren().addAll(
                appNameLabel,
                pageTitleLabel,
                spacer,
                dbStatusBadge);
    }
}