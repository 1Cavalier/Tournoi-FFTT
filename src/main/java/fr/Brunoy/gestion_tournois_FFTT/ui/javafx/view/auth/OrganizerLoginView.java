package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.auth;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;

public class OrganizerLoginView extends BorderPane {

    private static final String ERROR_STYLE = "-fx-text-fill: #b00020; -fx-font-weight: 700;";
    private static final String SUCCESS_STYLE = "-fx-text-fill: #1b5e20; -fx-font-weight: 700;";

    private final Label messageLabel = new Label();

    public OrganizerLoginView(Navigator nav) {
        AppTheme.applyPage(this);
        setPadding(new Insets(AppTheme.PADDING_PAGE));

        // Centre global
        VBox root = new VBox(AppTheme.SPACE_LG);
        root.setAlignment(Pos.TOP_CENTER);

        // Header
        VBox header = new VBox(AppTheme.SPACE_SM);
        header.setAlignment(Pos.TOP_CENTER);
        header.setMaxWidth(720);

        Label title = new Label("Connexion Organisateur");
        AppTheme.applyTitle(title);

        Label subtitle = new Label("Accédez à l’espace club pour gérer inscriptions, tableaux, matchs et résultats.");
        AppTheme.applySubtitle(subtitle);

        header.getChildren().addAll(title, subtitle);

        // Form fields
        TextField emailField = new TextField();
        emailField.setPromptText("Adresse email");
        emailField.setMaxWidth(Double.MAX_VALUE);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");
        passwordField.setMaxWidth(Double.MAX_VALUE);

        // Message label
        messageLabel.setManaged(false);
        messageLabel.setVisible(false);
        messageLabel.setStyle(ERROR_STYLE);
        messageLabel.setWrapText(true);

        // Action buttons
        Button loginButton = new Button("Se connecter");
        AppTheme.stylePrimary(loginButton);

        // Links row: "email oublié" / "mot de passe oublié"
        Button forgotEmailBtn = new Button("Email oublié");
        AppTheme.styleLinkButton(forgotEmailBtn);

        Button forgotPasswordBtn = new Button("Mot de passe oublié");
        AppTheme.styleLinkButton(forgotPasswordBtn);

        Region linksSpacer = new Region();
        HBox.setHgrow(linksSpacer, Priority.ALWAYS);

        HBox linksRow = new HBox(12, forgotEmailBtn, linksSpacer, forgotPasswordBtn);
        linksRow.setAlignment(Pos.CENTER_LEFT);

        // Card container (pro)
        VBox card = AppTheme.card(
                sectionTitle("Connexion"),
                emailField,
                passwordField,
                linksRow,
                loginButton,
                messageLabel);
        card.setMaxWidth(520);

        // Bottom actions
        Button registerButton = new Button("Créer un compte organisateur");
        AppTheme.styleSecondary(registerButton);

        Button backButton = new Button("Retour");
        AppTheme.styleSecondary(backButton);

        HBox bottom = new HBox(12, backButton, registerButton);
        bottom.setAlignment(Pos.CENTER);
        bottom.setMaxWidth(520);

        // Wiring actions
        loginButton.setOnAction(e -> doLogin(nav, emailField, passwordField));
        registerButton.setOnAction(e -> nav.showOrganizerRegister());
        backButton.setOnAction(e -> nav.showHome());

        // Placeholders (à relier à ton auth service quand prêt)
        forgotPasswordBtn.setOnAction(e -> {
            clearMessage();
            String email = normalizeEmail(emailField.getText());
            if (email.isBlank()) {
                showError("Saisis ton email pour recevoir la procédure de réinitialisation.");
                return;
            }
            // TODO: nav.organizerAuth().sendPasswordReset(email);
            showSuccess("Fonction à venir : réinitialisation du mot de passe.");
        });

        forgotEmailBtn.setOnAction(e -> {
            clearMessage();
            // TODO: nav.showRecoverEmail(); ou un dialog
            showSuccess("Fonction à venir : aide pour retrouver l’email du compte.");
        });

        root.getChildren().addAll(header, card, bottom);
        setCenter(root);
    }

    private Label sectionTitle(String text) {
        Label l = new Label(text);
        AppTheme.applyCardTitle(l);
        return l;
    }

    private void doLogin(Navigator nav, TextField emailField, PasswordField passwordField) {
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

        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void clearMessage() {
        messageLabel.setText("");
        messageLabel.setStyle(ERROR_STYLE);
        messageLabel.setManaged(false);
        messageLabel.setVisible(false);
    }

    private void showError(String text) {
        messageLabel.setStyle(ERROR_STYLE);
        messageLabel.setText(text);
        messageLabel.setManaged(true);
        messageLabel.setVisible(true);
    }

    private void showSuccess(String text) {
        messageLabel.setStyle(SUCCESS_STYLE);
        messageLabel.setText(text);
        messageLabel.setManaged(true);
        messageLabel.setVisible(true);
    }

    private static String normalizeEmail(String raw) {
        return raw == null ? "" : raw.trim().toLowerCase();
    }

    private static void requireNotBlank(String value, String message) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException(message);
    }
}