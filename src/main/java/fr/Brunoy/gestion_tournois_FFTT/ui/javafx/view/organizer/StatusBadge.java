package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Paint;

public class StatusBadge extends Label {

    public StatusBadge(TournamentStatus status) {
        super(status.getLabel());

        setPadding(new Insets(5, 10, 5, 10));
        setStyle("-fx-text-fill:white; -fx-font-weight:bold;");

        setBackground(new Background(
                new BackgroundFill(
                        Paint.valueOf(status.getColor()),
                        new CornerRadii(5),
                        Insets.EMPTY
                )
        ));
    }
}
