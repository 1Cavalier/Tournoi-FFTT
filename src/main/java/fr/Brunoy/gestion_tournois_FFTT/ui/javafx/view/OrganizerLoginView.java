package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class OrganizerLoginView extends VBox {

    public OrganizerLoginView(Navigator nav) {
        setPadding(new Insets(24));
        setSpacing(12);

        var title = new Label("Connexion Organisme");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        var email = new TextField();
        email.setPromptText("Email");

        var password = new PasswordField();
        password.setPromptText("Mot de passe");

        var message = new Label();
        message.setStyle("-fx-text-fill: #b00020;");

        var loginBtn = new Button("Se connecter");
        loginBtn.setOnAction(e -> {
            try {
                var acc = nav.organizerAuth().login(email.getText(), password.getText());
                message.setStyle("-fx-text-fill: #1b5e20;");
                message.setText("✅ Connecté : " + acc.getClubName() + " (id=" + acc.getId() + ")");
                // prochaine étape : nav.showOrganizerDashboard();
            } catch (IllegalArgumentException ex) {
                message.setStyle("-fx-text-fill: #b00020;");
                message.setText(ex.getMessage());
            }
        });

        var registerBtn = new Button("Créer un compte organisme");
        registerBtn.setOnAction(e -> nav.showOrganizerRegister());

        var backBtn = new Button("Retour");
        backBtn.setOnAction(e -> nav.showHome());

        getChildren().addAll(title, email, password, loginBtn, registerBtn, backBtn, message);
    }
}
