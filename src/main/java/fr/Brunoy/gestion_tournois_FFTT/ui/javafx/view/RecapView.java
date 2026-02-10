package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.vm.AppState;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class RecapView extends BorderPane {

    public RecapView(AppState state, Navigator nav) {
        setPadding(new Insets(12));

        var title = new Label("Récapitulatif");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        var list = new ListView<>(state.selected());

        var total = new Label();
        total.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        total.setText("Total : " + state.totalPrice() + " €");

        // Mise à jour si l'utilisateur revient en arrière puis re-sélectionne
        state.selected().addListener((javafx.collections.ListChangeListener<? super AppState.TableauItem>) c -> total
                .setText("Total : " + state.totalPrice() + " €"));

        var backBtn = new Button("Retour");
        backBtn.setOnAction(e -> nav.showTableauSelection());

        var confirmBtn = new Button("Confirmer");
        confirmBtn.setOnAction(e -> nav.showConfirmation());

        var footer = new HBox(10, backBtn, confirmBtn);
        footer.setPadding(new Insets(12, 0, 0, 0));

        setTop(new VBox(10, title));
        setCenter(list);
        setBottom(new VBox(10, total, footer));
    }
}
