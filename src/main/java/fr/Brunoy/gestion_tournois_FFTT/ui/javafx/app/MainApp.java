package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Point d'entrée JavaFX.
 *
 * Responsabilités :
 * - Démarrer l'application JavaFX
 * - Installer un handler global pour les exceptions non gérées
 * - Créer le contexte applicatif (AppContext)
 * - Démarrer la navigation (Navigator)
 */
public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        // Capture toutes les exceptions non gérées (threads non-JavaFX inclus)
        Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
            ex.printStackTrace();
            Platform.runLater(() -> showError("Erreur non gérée", ex));
        });

        try {
            // Contexte applicatif central (DB, repos, services)
            AppContext context = new AppContext();

            // Navigation + session
            Navigator nav = new Navigator(stage, context);

            // Le Navigator fait déjà stage.show() dans setScene(...)
            nav.showHome();

        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Erreur au démarrage", ex);
        }
    }

    private void showError(String title, Throwable ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(ex.getClass().getName());

        String msg = (ex.getMessage() == null || ex.getMessage().isBlank())
                ? "Une erreur est survenue."
                : ex.getMessage();
        alert.setContentText(msg);

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