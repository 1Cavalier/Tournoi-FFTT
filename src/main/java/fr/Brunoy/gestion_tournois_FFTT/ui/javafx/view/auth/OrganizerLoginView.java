package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.auth;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Connexion organisme :
 * - Étape 1 : email + mot de passe => envoi d'un OTP
 * - Étape 2 : saisie OTP via CodeVerificationDialog => ouverture session
 */
public class OrganizerLoginView extends VBox {

    private static final String TITLE_STYLE = "-fx-font-size: 18px; -fx-font-weight: bold;";
    private static final String ERROR_STYLE = "-fx-text-fill: #b00020;";
    private static final String SUCCESS_STYLE = "-fx-text-fill: #1b5e20;";

    private final Label messageLabel = new Label();

    public OrganizerLoginView(Navigator nav) {
        setPadding(new Insets(24));
        setSpacing(12);

        Label title = new Label("Connexion Organisme");
        title.setStyle(TITLE_STYLE);

        TextField emailField = new TextField();
        emailField.setPromptText("Adresse mail");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");

        messageLabel.setStyle(ERROR_STYLE);

        Button loginButton = new Button("Se connecter");

        // Actions utiles si l'utilisateur a un compte non vérifié
        Button verifyEmailButton = new Button("Valider mon email (code)");
        verifyEmailButton.setDisable(true);

        Button resendVerificationButton = new Button("Renvoyer le code d’inscription");
        resendVerificationButton.setDisable(true);

        loginButton.setOnAction(e -> {
            try {
                clearMessage();

                String email = normalizeEmail(emailField.getText());
                String password = passwordField.getText();

                requireNotBlank(email, "Email obligatoire.");
                requireNotBlank(password, "Mot de passe obligatoire.");

                // Étape 1 : mot de passe OK => envoi OTP
                nav.organizerAuth().loginStart(email, password);

                // Étape 2 : saisie OTP => récupération OrganizerAccount
                final OrganizerAccount[] holder = new OrganizerAccount[1];

                CodeVerificationDialog dlg = new CodeVerificationDialog(
                        "Code de connexion",
                        "Un code de connexion a été envoyé à : " + email,
                        code -> {
                            try {
                                holder[0] = nav.organizerAuth().verifyLoginOtpAndFinish(email, code);
                                return true;
                            } catch (IllegalArgumentException ex) {
                                return false;
                            }
                        });

                dlg.showAndWait();

                if (dlg.isSuccess() && holder[0] != null) {
                    nav.setCurrentOrganizer(holder[0]);
                    nav.showOrganizerDashboard();
                } else {
                    showError("Connexion annulée ou code incorrect.");
                }

                // Si on est ici, on n'est plus dans le cas "email non vérifié"
                verifyEmailButton.setDisable(true);
                resendVerificationButton.setDisable(true);

            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());

                // Astuce temporaire : détection par message. À remplacer plus tard par une
                // erreur typée.
                boolean emailNotVerified = isEmailNotVerifiedError(ex);
                verifyEmailButton.setDisable(!emailNotVerified);
                resendVerificationButton.setDisable(!emailNotVerified);
            }
        });

        verifyEmailButton.setOnAction(e -> {
            clearMessage();

            String email = normalizeEmail(emailField.getText());
            if (email.isBlank()) {
                showError("Email obligatoire.");
                return;
            }

            CodeVerificationDialog dlg = new CodeVerificationDialog(
                    "Vérification email",
                    "Un code a été envoyé à : " + email,
                    code -> nav.organizerAuth().verifyEmail(email, code));

            dlg.showAndWait();

            if (dlg.isSuccess()) {
                showSuccess("Email vérifié. Reconnecte-toi.");
                verifyEmailButton.setDisable(true);
                resendVerificationButton.setDisable(true);
            } else {
                showError("Validation annulée ou code incorrect.");
            }
        });

        resendVerificationButton.setOnAction(e -> {
            try {
                clearMessage();

                String email = normalizeEmail(emailField.getText());
                if (email.isBlank()) {
                    showError("Email obligatoire.");
                    return;
                }

                nav.organizerAuth().resendVerificationCode(email);
                showSuccess("Code renvoyé (console pour le moment).");

            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
            }
        });

        Button registerButton = new Button("Créer un compte organisme");
        registerButton.setOnAction(e -> nav.showOrganizerRegister());

        Button backButton = new Button("Retour");
        backButton.setOnAction(e -> nav.showHome());

        getChildren().addAll(
                title,
                emailField,
                passwordField,
                loginButton,
                verifyEmailButton,
                resendVerificationButton,
                registerButton,
                backButton,
                messageLabel);
    }

    private void clearMessage() {
        messageLabel.setText("");
        messageLabel.setStyle(ERROR_STYLE);
    }

    private void showError(String text) {
        messageLabel.setStyle(ERROR_STYLE);
        messageLabel.setText(text);
    }

    private void showSuccess(String text) {
        messageLabel.setStyle(SUCCESS_STYLE);
        messageLabel.setText(text);
    }

    private boolean isEmailNotVerifiedError(IllegalArgumentException ex) {
        String msg = ex.getMessage();
        return msg != null && msg.toLowerCase().contains("non vérifié");
    }

    private static String normalizeEmail(String raw) {
        return raw == null ? "" : raw.trim().toLowerCase();
    }

    private static void requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}