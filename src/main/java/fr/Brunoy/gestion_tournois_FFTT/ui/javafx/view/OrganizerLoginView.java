package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.vm.AppState;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class OrganizerLoginView extends VBox {

    public OrganizerLoginView(AppState state, Navigator nav) {

        setPadding(new Insets(20));
        setSpacing(10);

        Label title = new Label("Connexion Organisme");
        title.setStyle("-fx-font-size:18px; -fx-font-weight:bold;");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill:red;");

        Button loginBtn = new Button("Se connecter");
        loginBtn.setOnAction(e -> {
            for (OrganizerAccount acc : state.getOrganizers()) {
                if (acc.getEmail().equals(emailField.getText())
                        && acc.getPassword().equals(passwordField.getText())) {

                    state.setCurrentOrganizer(acc);
                    nav.showOrganizerDashboard();
                    return;
                }
            }
            errorLabel.setText("Email ou mot de passe incorrect");
        });

        Button registerBtn = new Button("Créer un compte");
        registerBtn.setOnAction(e -> nav.showOrganizerRegister());

        getChildren().addAll(title, emailField, passwordField, loginBtn, registerBtn, errorLabel);
    }
}
