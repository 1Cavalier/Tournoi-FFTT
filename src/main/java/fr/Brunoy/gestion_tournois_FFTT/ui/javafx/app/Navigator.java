package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.ConfirmationView;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.HomeView;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.OrganizerLoginView;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.OrganizerDashboardView;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.OrganizerRegisterView;
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

    public void showHome() {
        var root = new HomeView(state, this);
        stage.setScene(new Scene(root, 900, 600));
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

    public AppState state() { // optionnel si tu veux l’exposer
        return state;
    }

    public void showOrganizerLogin() {
        stage.setScene(new Scene(new OrganizerLoginView(state, this), 900, 600));
    }

    public void showOrganizerRegister() {
        stage.setScene(new Scene(new OrganizerRegisterView(state, this), 900, 600));
    }

    public void showOrganizerDashboard() {
        stage.setScene(new Scene(new OrganizerDashboardView(state, this), 900, 600));
    }

}
