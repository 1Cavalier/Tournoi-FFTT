package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.vm.AppState;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class TableauSelectionView extends BorderPane {

    public TableauSelectionView(AppState state, Navigator nav) {
        setPadding(new Insets(12));

        var title = new Label("V1 — Sélection des tableaux (données mémoire)");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        var availableList = new ListView<>(state.available());
        availableList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        var selectedList = new ListView<>(state.selected());
        selectedList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        var addBtn = new Button("Ajouter →");
        addBtn.setOnAction(e -> {
            var item = availableList.getSelectionModel().getSelectedItem();
            if (item != null && !state.selected().contains(item)) {
                state.selected().add(item);
            }
        });

        var removeBtn = new Button("← Retirer");
        removeBtn.setOnAction(e -> {
            var item = selectedList.getSelectionModel().getSelectedItem();
            if (item != null) {
                state.selected().remove(item);
            }
        });

        var buttons = new VBox(8, addBtn, removeBtn);
        buttons.setPadding(new Insets(0, 10, 0, 10));

        var center = new HBox(10,
                new VBox(6, new Label("Tableaux disponibles"), availableList),
                buttons,
                new VBox(6, new Label("Sélection"), selectedList)
        );

        var nextBtn = new Button("Suivant (Récap)");
        nextBtn.setOnAction(e -> nav.showRecap());

        var footer = new HBox(nextBtn);
        footer.setPadding(new Insets(12, 0, 0, 0));

        setTop(new VBox(10, title));
        setCenter(center);
        setBottom(footer);
    }
}
