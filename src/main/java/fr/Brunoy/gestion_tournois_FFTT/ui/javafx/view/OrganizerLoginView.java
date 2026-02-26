package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import javafx.geometry.Insets;
import javafx.scene.control.*;
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

        Button resendVerificationBtn = new Button("Renvoyer le code d’inscription");
        resendVerificationBtn.setDisable(true);

        loginBtn.setOnAction(e -> {
            try {
                String email = emailField.getText() == null ? "" : emailField.getText().trim().toLowerCase();

                // étape 1 : password OK => envoi OTP login
                nav.organizerAuth().loginStart(email, passwordField.getText());

                // popup OTP
                LoginOtpDialog otpDlg = new LoginOtpDialog(nav, email);
                otpDlg.showAndWait();

                var acc = otpDlg.getAuthenticated();
                if (acc != null) {
                    nav.setCurrentOrganizer(acc);
                    nav.showOrganizerDashboard();
                } else {
                    message.setStyle("-fx-text-fill: #b00020;");
                    message.setText("Connexion annulée.");
                }

                // désactiver actions vérif email (on n’est plus dans ce cas)
                verifyEmailBtn.setDisable(true);
                resendVerificationBtn.setDisable(true);

            } catch (IllegalArgumentException ex) {
                message.setStyle("-fx-text-fill: #b00020;");
                message.setText(ex.getMessage());

                boolean notVerified = ex.getMessage() != null
                        && ex.getMessage().toLowerCase().contains("non vérifié");

                verifyEmailBtn.setDisable(!notVerified);
                resendVerificationBtn.setDisable(!notVerified);
            }
        });

        verifyEmailBtn.setOnAction(e -> {
            String email = emailField.getText();
            if (email == null || email.isBlank()) {
                message.setText("Email obligatoire.");
                return;
            }

            EmailVerificationDialog dlg = new EmailVerificationDialog(nav, email.trim().toLowerCase());
            dlg.showAndWait();

            if (dlg.isVerified()) {
                message.setStyle("-fx-text-fill: #1b5e20;");
                message.setText("✅ Email vérifié. Reconnecte-toi.");
                verifyEmailBtn.setDisable(true);
                resendVerificationBtn.setDisable(true);
            } else {
                message.setStyle("-fx-text-fill: #b00020;");
                message.setText("Validation annulée ou code incorrect.");
            }
        });

        resendVerificationBtn.setOnAction(e -> {
            try {
                String email = emailField.getText();
                if (email == null || email.isBlank()) {
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
                resendVerificationBtn,
                registerBtn,
                backBtn,
                message);
    }
}