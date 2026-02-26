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

        Button verifyEmailBtn = new Button("Valider mon email (code)");
        verifyEmailBtn.setDisable(true);

        Button resendCodeBtn = new Button("Renvoyer un code");
        resendCodeBtn.setDisable(true);

        loginBtn.setOnAction(e -> {
            try {
                var acc = nav.organizerAuth().login(emailField.getText(), passwordField.getText());
                nav.setCurrentOrganizer(acc);
                nav.showOrganizerDashboard();

            } catch (IllegalArgumentException ex) {
                message.setStyle("-fx-text-fill: #b00020;");
                message.setText(ex.getMessage());

                boolean notVerified = ex.getMessage() != null
                        && ex.getMessage().toLowerCase().contains("non vérifié");

                verifyEmailBtn.setDisable(!notVerified);
                resendCodeBtn.setDisable(!notVerified);
            }
        });

        verifyEmailBtn.setOnAction(e -> {
            String email = emailField.getText();
            if (email == null || email.isBlank()) {
                message.setStyle("-fx-text-fill: #b00020;");
                message.setText("Email obligatoire.");
                return;
            }

            EmailVerificationDialog dlg = new EmailVerificationDialog(nav, email.trim().toLowerCase());
            dlg.showAndWait();

            if (dlg.isVerified()) {
                message.setStyle("-fx-text-fill: #1b5e20;");
                message.setText("✅ Email vérifié. Vous pouvez vous connecter.");
                verifyEmailBtn.setDisable(true);
                resendCodeBtn.setDisable(true);
            } else {
                message.setStyle("-fx-text-fill: #b00020;");
                message.setText("Validation annulée ou code incorrect.");
            }
        });

        resendCodeBtn.setOnAction(e -> {
            try {
                String email = emailField.getText();
                if (email == null || email.isBlank()) {
                    message.setStyle("-fx-text-fill: #b00020;");
                    message.setText("Email obligatoire.");
                    return;
                }

                nav.organizerAuth().resendVerificationCode(email);
                message.setStyle("-fx-text-fill: #1b5e20;");
                message.setText("✅ Code renvoyé (regarde la console pour le moment).");

            } catch (IllegalArgumentException ex) {
                message.setStyle("-fx-text-fill: #b00020;");
                message.setText(ex.getMessage());
            }
        });

        Button registerBtn = new Button("Créer un compte organisme");
        registerBtn.setOnAction(e -> nav.showOrganizerRegister());

        Button backBtn = new Button("Retour");
        backBtn.setOnAction(e -> nav.showHome());

        getChildren().addAll(
                title,
                emailField,
                passwordField,
                loginBtn,
                verifyEmailBtn,
                resendCodeBtn,
                registerBtn,
                backBtn,
                message);
    }
}