package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.auth;

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

import java.util.Objects;
import java.util.function.Function;

/**
 * Fenêtre modale générique pour saisir un code reçu par email.
 *
 * Utilisable pour :
 * - vérification d'email à l'inscription
 * - OTP de connexion
 *
 * La logique métier est injectée via une fonction de vérification.
 */
public class CodeVerificationDialog extends Stage {

    private static final String ERROR_STYLE = "-fx-text-fill: #b00020; -fx-font-weight: 700;";
    private static final double WIDTH = 420;
    private static final double HEIGHT = 220;

    private boolean success;

    public CodeVerificationDialog(
            String title,
            String infoText,
            Function<String, Boolean> verifier) {

        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(infoText, "infoText must not be null");
        Objects.requireNonNull(verifier, "verifier must not be null");

        setTitle(title);
        initModality(Modality.APPLICATION_MODAL);
        setResizable(false);

        Label infoLabel = new Label(infoText);
        infoLabel.setWrapText(true);

        TextField codeField = new TextField();
        codeField.setPromptText("Code à 6 chiffres");
        codeField.textProperty().addListener((obs, oldValue, newValue) -> {
            String digitsOnly = newValue == null ? "" : newValue.replaceAll("\\D", "");
            if (digitsOnly.length() > 6) {
                digitsOnly = digitsOnly.substring(0, 6);
            }
            if (!digitsOnly.equals(newValue)) {
                codeField.setText(digitsOnly);
            }
        });

        Label messageLabel = new Label();
        messageLabel.setStyle(ERROR_STYLE);
        messageLabel.setWrapText(true);
        messageLabel.setManaged(false);
        messageLabel.setVisible(false);

        Button verifyButton = new Button("Valider");
        verifyButton.setDefaultButton(true);

        Button cancelButton = new Button("Annuler");
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(e -> close());

        verifyButton.setOnAction(e -> runVerification(codeField, messageLabel, verifier));
        codeField.setOnAction(e -> runVerification(codeField, messageLabel, verifier));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox buttons = new HBox(10, spacer, cancelButton, verifyButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(12, infoLabel, codeField, messageLabel, buttons);
        root.setPadding(new Insets(18));

        setScene(new Scene(root, WIDTH, HEIGHT));

        setOnShown(e -> codeField.requestFocus());
    }

    public boolean isSuccess() {
        return success;
    }

    private void runVerification(
            TextField codeField,
            Label messageLabel,
            Function<String, Boolean> verifier) {

        hideMessage(messageLabel);

        String code = codeField.getText() == null ? "" : codeField.getText().trim();

        if (code.isEmpty()) {
            showError(messageLabel, "Saisis le code reçu par email.");
            return;
        }

        if (code.length() != 6) {
            showError(messageLabel, "Le code doit contenir 6 chiffres.");
            return;
        }

        boolean ok;
        try {
            ok = verifier.apply(code);
        } catch (Exception ex) {
            ok = false;
        }

        if (ok) {
            success = true;
            close();
        } else {
            showError(messageLabel, "Code invalide ou expiré.");
        }
    }

    private void showError(Label messageLabel, String text) {
        messageLabel.setText(text);
        messageLabel.setManaged(true);
        messageLabel.setVisible(true);
    }

    private void hideMessage(Label messageLabel) {
        messageLabel.setText("");
        messageLabel.setManaged(false);
        messageLabel.setVisible(false);
    }
}