package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.auth;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.function.Function;

/**
 * Fenêtre modale générique pour saisir un code reçu par email.
 * Utilisable pour :
 * - vérification d'email (inscription)
 * - OTP de connexion (login)
 *
 * La logique métier est injectée via une fonction "verifier".
 */
public class CodeVerificationDialog extends Stage {

    private static final String ERROR_STYLE = "-fx-text-fill:#b00020;";

    private boolean success;

    public CodeVerificationDialog(
            String title,
            String infoText,
            Function<String, Boolean> verifier) {
        setTitle(title);
        initModality(Modality.APPLICATION_MODAL);

        Label info = new Label(infoText);

        TextField codeField = new TextField();
        codeField.setPromptText("Code à 6 chiffres");

        Label message = new Label();
        message.setStyle(ERROR_STYLE);

        Button verifyButton = new Button("Valider");
        verifyButton.setDefaultButton(true);

        Runnable verifyAction = () -> {
            String code = codeField.getText() == null ? "" : codeField.getText().trim();

            boolean ok;
            try {
                ok = verifier.apply(code);
            } catch (Exception ex) {
                // En UI, on affiche un message lisible plutôt que laisser remonter une
                // exception.
                ok = false;
            }

            if (ok) {
                success = true;
                close();
            } else {
                message.setText("Code invalide ou expiré.");
            }
        };

        verifyButton.setOnAction(e -> verifyAction.run());
        codeField.setOnAction(e -> verifyAction.run());

        Button cancelButton = new Button("Annuler");
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(e -> close());

        VBox root = new VBox(10, info, codeField, verifyButton, cancelButton, message);
        root.setPadding(new Insets(18));

        setScene(new Scene(root, 380, 200));

        // Focus immédiat pour accélérer la saisie
        codeField.requestFocus();
    }

    public boolean isSuccess() {
        return success;
    }
}