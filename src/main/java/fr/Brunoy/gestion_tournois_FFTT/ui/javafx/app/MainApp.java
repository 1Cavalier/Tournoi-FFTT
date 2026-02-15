package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.PrintWriter;
import java.io.StringWriter;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        // 1) Capture toutes les exceptions non gérées
        Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
            ex.printStackTrace();
            Platform.runLater(() -> showError("Erreur non gérée", ex));
        });

        try {
            Navigator nav = new Navigator(stage);
            nav.showHome();

            stage.setTitle("Tournoi FFTT");
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Erreur au démarrage", ex);
        }
    }

    private void showError(String title, Throwable ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(ex.getClass().getName());
        alert.setContentText(ex.getMessage());

        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        TextArea area = new TextArea(sw.toString());
        area.setEditable(false);
        area.setWrapText(false);
        area.setPrefWidth(900);
        area.setPrefHeight(500);

        alert.getDialogPane().setExpandableContent(area);
        alert.getDialogPane().setExpanded(true);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
