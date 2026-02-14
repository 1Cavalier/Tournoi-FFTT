package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.HomeView;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.LoginJoueurView;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.LoginOrganismeView;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Navigator {

    private final Stage stage;

    public Navigator(Stage stage) {
        this.stage = stage;
    }

    public void showHome() {
        stage.setScene(new Scene(new HomeView(this), 900, 600));
    }

    public void showLoginOrganisme() {
        stage.setScene(new Scene(new LoginOrganismeView(this), 900, 600));
    }

    public void showLoginJoueur() {
        stage.setScene(new Scene(new LoginJoueurView(this), 900, 600));
    }
}
