package fr.pingmanager.gestion_tournois_FFTT.ui.javafx.view.auth;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.Objects;
import java.util.function.Function;

import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;

/**
 * Fenêtre modale pour saisir un code reçu par email.
 *
 * Utilisable pour :
 * - vérification d'email à l'inscription
 * - OTP de connexion
 */
public class CodeVerificationDialog extends Stage {

    private static final String ERROR_STYLE = "-fx-text-fill: #b00020; -fx-font-weight: 700;";
    private static final int CODE_LENGTH = 6;

    private boolean success;

    public CodeVerificationDialog(
            Window owner,
            String title,
            String infoText,
            Function<String, Boolean> verifyAction) {

        Objects.requireNonNull(infoText, "infoText must not be null");
        Objects.requireNonNull(verifyAction, "verifyAction must not be null");

        if (owner != null) {
            initOwner(owner);
        }

        setTitle(Objects.requireNonNull(title, "title must not be null"));
        initModality(Modality.APPLICATION_MODAL);

        Label infoLabel = new Label(infoText);
        infoLabel.setWrapText(true);

        TextField codeField = new TextField();
        codeField.setPromptText("Code à 6 chiffres");
        codeField.setMaxWidth(Double.MAX_VALUE);
        codeField.textProperty().addListener((obs, oldValue, newValue) -> {
            String digitsOnly = sanitizeCode(newValue);
            if (!digitsOnly.equals(newValue)) {
                codeField.setText(digitsOnly);
            }
        });

        Label messageLabel = new Label();
        messageLabel.setStyle(ERROR_STYLE);
        messageLabel.setWrapText(true);
        hideMessage(messageLabel);

        Button verifyButton = new Button("Valider");
        AppTheme.stylePrimary(verifyButton);
        verifyButton.setDefaultButton(true);
        verifyButton.setOnAction(e -> runVerification(codeField, messageLabel, verifyAction));

        Button cancelButton = new Button("Annuler");
        AppTheme.styleSecondary(cancelButton);
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(e -> close());

        codeField.setOnAction(e -> runVerification(codeField, messageLabel, verifyAction));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox buttons = new HBox(AppTheme.SPACE_SM, spacer, cancelButton, verifyButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(AppTheme.SPACE_MD, infoLabel, codeField, messageLabel, buttons);
        root.setPadding(new Insets(18));
        AppTheme.applyPage(root);

        setScene(new Scene(root));
        AppTheme.applySmallDialogWindow(this);

        setOnShown(e -> codeField.requestFocus());
    }

    public boolean isSuccess() {
        return success;
    }

    private void runVerification(
            TextField codeField,
            Label messageLabel,
            Function<String, Boolean> verifyAction) {

        hideMessage(messageLabel);

        String code = sanitizeCode(codeField.getText());

        if (code.isEmpty()) {
            showError(messageLabel, "Saisis le code reçu par email.");
            return;
        }

        if (code.length() != CODE_LENGTH) {
            showError(messageLabel, "Le code doit contenir 6 chiffres.");
            return;
        }

        try {
            if (Boolean.TRUE.equals(verifyAction.apply(code))) {
                success = true;
                close();
                return;
            }
        } catch (Exception ignored) {
            // Le message affiché reste volontairement générique
        }

        showError(messageLabel, "Code invalide ou expiré.");
    }

    private static String sanitizeCode(String raw) {
        String digitsOnly = raw == null ? "" : raw.replaceAll("\\D", "");
        return digitsOnly.length() > CODE_LENGTH
                ? digitsOnly.substring(0, CODE_LENGTH)
                : digitsOnly;
    }

    private static void showError(Label messageLabel, String text) {
        messageLabel.setText(text);
        messageLabel.setManaged(true);
        messageLabel.setVisible(true);
    }

    private static void hideMessage(Label messageLabel) {
        messageLabel.setText("");
        messageLabel.setManaged(false);
        messageLabel.setVisible(false);
    }
}