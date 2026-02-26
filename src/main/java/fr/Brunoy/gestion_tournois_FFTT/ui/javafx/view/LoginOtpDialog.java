package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class LoginOtpDialog extends Stage {

    private OrganizerAccount authenticated;

    public LoginOtpDialog(Navigator nav, String email) {
        setTitle("Code de connexion");
        initModality(Modality.APPLICATION_MODAL);

        Label info = new Label("Un code de connexion a été envoyé à : " + email);

        TextField codeField = new TextField();
        codeField.setPromptText("Code à 6 chiffres");

        Label message = new Label();
        message.setStyle("-fx-text-fill:#b00020;");

        Button verifyBtn = new Button("Valider");
        verifyBtn.setDefaultButton(true);

        verifyBtn.setOnAction(e -> {
            try {
                String code = codeField.getText() == null ? "" : codeField.getText().trim();
                authenticated = nav.organizerAuth().verifyLoginOtpAndFinish(email, code);
                close();
            } catch (IllegalArgumentException ex) {
                message.setText(ex.getMessage());
            }
        });

        Button cancelBtn = new Button("Annuler");
        cancelBtn.setOnAction(e -> close());

        VBox root = new VBox(10, info, codeField, verifyBtn, cancelBtn, message);
        root.setPadding(new Insets(18));

        setScene(new Scene(root, 380, 210));
    }

    public OrganizerAccount getAuthenticated() {
        return authenticated;
    }
}