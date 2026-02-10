package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.ConfirmationView;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.RecapView;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.TableauSelectionView;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.vm.AppState;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Navigator {

    private final Stage stage;
    private final AppState state = new AppState();

    public Navigator(Stage stage) {
        this.stage = stage;
    }

    public void showTableauSelection() {
        var root = new TableauSelectionView(state, this);
        stage.setScene(new Scene(root, 900, 600));
    }

    public void showRecap() {
        var root = new RecapView(state, this);
        stage.setScene(new Scene(root, 900, 600));
    }

    public void showConfirmation() {
        var root = new ConfirmationView(this);
        stage.setScene(new Scene(root, 900, 600));
    }
}
