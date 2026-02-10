package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ConfirmationView extends VBox {

    public ConfirmationView(Navigator nav) {
        setPadding(new Insets(20));
        setSpacing(12);

        var title = new Label("Confirmation");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        var msg = new Label("✅ Inscription V1 validée (aucune base, aucun serveur).");

        var restartBtn = new Button("Revenir à la sélection");
        restartBtn.setOnAction(e -> nav.showTableauSelection());

        getChildren().addAll(title, msg, restartBtn);
    }
}
