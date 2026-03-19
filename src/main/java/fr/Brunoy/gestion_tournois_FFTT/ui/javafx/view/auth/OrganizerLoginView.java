package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.auth;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.AppRouter;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.OrganizerDto;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class OrganizerLoginView extends BorderPane {

    private static final String ERROR_STYLE = "-fx-text-fill: #b00020; -fx-font-weight: 700;";
    private static final String SUCCESS_STYLE = "-fx-text-fill: #1b5e20; -fx-font-weight: 700;";

    private final AppRouter nav;
    private final Label messageLabel = new Label();

    public OrganizerLoginView(AppRouter nav) {
        this.nav = Objects.requireNonNull(nav, "nav must not be null");

        AppTheme.applyPage(this);
        setPadding(new Insets(AppTheme.PADDING_PAGE));

        VBox root = new VBox(AppTheme.SPACE_LG);
        root.setAlignment(Pos.TOP_CENTER);

        root.getChildren().addAll(
                buildHeader(),
                buildLoginCard(),
                buildBottomActions()
        );

        setCenter(root);
    }

    private VBox buildHeader() {
        VBox header = new VBox(AppTheme.SPACE_SM);
        header.setAlignment(Pos.TOP_CENTER);
        header.setMaxWidth(720);

        Label title = new Label("Connexion organisateur");
        AppTheme.applyTitle(title);

        Label subtitle = new Label(
                "Accédez à votre espace PingManager pour gérer vos tournois et inscriptions.");
        AppTheme.applySubtitle(subtitle);

        header.getChildren().addAll(title, subtitle);
        return header;
    }

    private VBox buildLoginCard() {
        TextField emailField = new TextField();
        emailField.setPromptText("Adresse email");
        emailField.setMaxWidth(Double.MAX_VALUE);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");
        passwordField.setMaxWidth(Double.MAX_VALUE);

        initMessageLabel();

        Button loginButton = new Button("Se connecter");
        AppTheme.stylePrimary(loginButton);

        Button forgotPasswordButton = new Button("Mot de passe oublié");
        AppTheme.styleLinkButton(forgotPasswordButton);

        Region linksSpacer = new Region();
        HBox.setHgrow(linksSpacer, Priority.ALWAYS);

        HBox linksRow = new HBox(12, linksSpacer, forgotPasswordButton);
        linksRow.setAlignment(Pos.CENTER_LEFT);

        loginButton.setOnAction(e -> doLogin(emailField, passwordField));
        emailField.setOnAction(e -> doLogin(emailField, passwordField));
        passwordField.setOnAction(e -> doLogin(emailField, passwordField));
        forgotPasswordButton.setOnAction(e -> handleForgotPassword(emailField));

        VBox card = AppTheme.card(
                sectionTitle("Connexion"),
                emailField,
                passwordField,
                linksRow,
                loginButton,
                messageLabel
        );
        card.setMaxWidth(520);

        emailField.requestFocus();
        return card;
    }

    private HBox buildBottomActions() {
        Button registerButton = new Button("Créer un compte organisateur");
        AppTheme.styleSecondary(registerButton);
        registerButton.setOnAction(e -> nav.showOrganizerRegister());

        Button backButton = new Button("Retour");
        AppTheme.styleSecondary(backButton);
        backButton.setOnAction(e -> nav.showHome());

        HBox bottom = new HBox(12, backButton, registerButton);
        bottom.setAlignment(Pos.CENTER);
        bottom.setMaxWidth(520);
        return bottom;
    }

    private Label sectionTitle(String text) {
        Label label = new Label(text);
        AppTheme.applyCardTitle(label);
        return label;
    }

    private void doLogin(TextField emailField, PasswordField passwordField) {
        clearMessage();

        try {
            String email = normalizeEmail(emailField.getText());
            String password = safeText(passwordField.getText());

            requireNotBlank(email, "Email obligatoire.");
            requireNotBlank(password, "Mot de passe obligatoire.");

            nav.organizerAuth().loginStart(email, password);

            OrganizerDto organizer = requestOtpAndAuthenticate(email);
            passwordField.clear();

            if (organizer == null) {
                showError("Connexion interrompue avant validation du code.");
                return;
            }

            nav.loginOrganizer(organizer);
            nav.showOrganizerDashboard();

        } catch (IllegalArgumentException ex) {
            passwordField.clear();
            showError(ex.getMessage());
        }
    }

    private OrganizerDto requestOtpAndAuthenticate(String email) {
        AtomicReference<OrganizerDto> organizerRef = new AtomicReference<>();

        CodeVerificationDialog dialog = new CodeVerificationDialog(
                nav.primaryStage(),
                "Code de connexion",
                "Un code de connexion a été envoyé à : " + email,
                code -> {
                    try {
                        OrganizerDto organizer = nav.organizerAuth().verifyLoginOtpAndFinish(email, code);
                        organizerRef.set(organizer);
                        return true;
                    } catch (IllegalArgumentException ex) {
                        return false;
                    }
                }
        );

        dialog.showAndWait();
        return dialog.isSuccess() ? organizerRef.get() : null;
    }

    private void handleForgotPassword(TextField emailField) {
        clearMessage();

        String email = normalizeEmail(emailField.getText());
        if (email.isBlank()) {
            showError("Saisis ton email pour la future procédure de réinitialisation.");
            return;
        }

        showSuccess("Fonction bientôt disponible : réinitialisation du mot de passe.");
    }

    private void initMessageLabel() {
        messageLabel.setWrapText(true);
        hideMessage();
        messageLabel.setStyle(ERROR_STYLE);
    }

    private void clearMessage() {
        messageLabel.setText("");
        messageLabel.setStyle(ERROR_STYLE);
        hideMessage();
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

    private void hideMessage() {
        messageLabel.setManaged(false);
        messageLabel.setVisible(false);
    }

    private static String normalizeEmail(String raw) {
        return safeText(raw).toLowerCase();
    }

    private static String safeText(String raw) {
        return raw == null ? "" : raw.trim();
    }

    private static void requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}