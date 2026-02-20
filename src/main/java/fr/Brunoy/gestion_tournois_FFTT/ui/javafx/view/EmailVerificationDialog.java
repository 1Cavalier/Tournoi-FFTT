package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class EmailVerificationDialog extends Stage {

    private boolean verified = false;

    public EmailVerificationDialog(Navigator nav, String email) {
        setTitle("Vérification email");
        initModality(Modality.APPLICATION_MODAL);

        Label info = new Label("Un code a été envoyé à : " + email);
        TextField codeField = new TextField();
        codeField.setPromptText("Code à 6 chiffres");

        Label message = new Label();
        message.setStyle("-fx-text-fill:#b00020;");

        Button verifyBtn = new Button("Valider le code");
        verifyBtn.setDefaultButton(true);

        verifyBtn.setOnAction(e -> {
            String code = codeField.getText() == null ? "" : codeField.getText().trim();
            boolean ok = nav.organizerAuth().verifyEmail(email, code);
            if (ok) {
                verified = true;
                close();
            } else {
                message.setText("Code invalide ou expiré.");
            }
        });

        Button cancelBtn = new Button("Annuler");
        cancelBtn.setOnAction(e -> close());

        VBox root = new VBox(10, info, codeField, verifyBtn, cancelBtn, message);
        root.setPadding(new Insets(18));

        setScene(new Scene(root, 380, 200));
    }

    public boolean isVerified() {
        return verified;
    }
}