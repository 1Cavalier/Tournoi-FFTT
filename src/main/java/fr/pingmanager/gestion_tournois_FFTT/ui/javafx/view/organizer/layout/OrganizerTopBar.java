package fr.pingmanager.gestion_tournois_FFTT.ui.javafx.view.organizer.layout;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.Objects;

import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.app.AppRouter;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.OrganizerDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;

public class OrganizerTopBar extends HBox {

    @SuppressWarnings("unused")
    private final AppRouter nav;
    @SuppressWarnings("unused")
    private final OrganizerDto organizer;

    public OrganizerTopBar(AppRouter nav, OrganizerDto organizer) {
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