package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class OrganizerLoginView extends VBox {

    public OrganizerLoginView(Navigator nav) {

        setPadding(new Insets(24));
        setSpacing(12);

        Label title = new Label("Connexion Organisme");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField emailField = new TextField();
        emailField.setPromptText("Adresse mail");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");

        Label message = new Label();
        message.setStyle("-fx-text-fill: #b00020;");

        Button loginBtn = new Button("Se connecter");
        loginBtn.setOnAction(e -> {
            try {
                var acc = nav.organizerAuth().login(emailField.getText(), passwordField.getText());
                nav.setCurrentOrganizer(acc);
                nav.showOrganizerDashboard();

            } catch (IllegalArgumentException ex) {
                message.setText(ex.getMessage());
            }
        });

        Button registerBtn = new Button("Créer un compte organisme");
        registerBtn.setOnAction(e -> nav.showOrganizerRegister());

        Button backBtn = new Button("Retour");
        backBtn.setOnAction(e -> nav.showHome());

        getChildren().addAll(title, emailField, passwordField, loginBtn, registerBtn, backBtn, message);
    }
}
