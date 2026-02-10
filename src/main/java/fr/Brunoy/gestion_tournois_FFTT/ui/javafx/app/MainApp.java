package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app;

import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        var navigator = new Navigator(stage);
        navigator.showTableauSelection();

        stage.setTitle("Tournoi FFTT - V1");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
