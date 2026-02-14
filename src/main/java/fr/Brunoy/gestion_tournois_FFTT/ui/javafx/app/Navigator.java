package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.OrganizerAccountStore;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.HomeView;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.OrganizerLoginView;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.OrganizerRegisterView;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.nio.file.Path;

public class Navigator {

    private final Stage stage;

    private final OrganizerAuthService organizerAuth = new OrganizerAuthService(
            new OrganizerAccountStore(Path.of("data", "organizers.json")));

    public Navigator(Stage stage) {
        this.stage = stage;
    }

    public OrganizerAuthService organizerAuth() {
        return organizerAuth;
    }

    public void showHome() {
        stage.setScene(new Scene(new HomeView(this), 900, 600));
    }

    public void showOrganizerLogin() {
        stage.setScene(new Scene(new OrganizerLoginView(this), 900, 600));
    }

    public void showOrganizerRegister() {
        stage.setScene(new Scene(new OrganizerRegisterView(this), 900, 600));
    }
}
