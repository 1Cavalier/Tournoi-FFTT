package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.OrganizerAccountStore;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.HomeView;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.OrganizerLoginView;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.OrganizerRegisterView;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.dashboard.OrganizerDashboardView;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.nio.file.Path;

public class Navigator {

    private final Stage stage;

    private final OrganizerAuthService organizerAuth = new OrganizerAuthService(
            new OrganizerAccountStore(Path.of("data", "organizers.json")));

    private OrganizerAccount currentOrganizer;

    public Navigator(Stage stage) {
        this.stage = stage;
    }

    // --- Services accessibles aux Views ---
    public OrganizerAuthService organizerAuth() {
        return organizerAuth;
    }

    // --- Session organisme ---
    public void setCurrentOrganizer(OrganizerAccount currentOrganizer) {
        this.currentOrganizer = currentOrganizer;
    }

    public OrganizerAccount getCurrentOrganizer() {
        return currentOrganizer;
    }

    public void logoutOrganizer() {
        this.currentOrganizer = null;
        showHome();
    }

    // --- Navigation ---
    public void showHome() {
        stage.setScene(new Scene(new HomeView(this), 900, 600));
    }

    public void showOrganizerLogin() {
        stage.setScene(new Scene(new OrganizerLoginView(this), 900, 600));
    }

    public void showOrganizerRegister() {
        stage.setScene(new Scene(new OrganizerRegisterView(this), 900, 600));
    }

    public void showOrganizerDashboard() {
        stage.setScene(new Scene(new OrganizerDashboardView(this), 1200, 700));
    }
}
