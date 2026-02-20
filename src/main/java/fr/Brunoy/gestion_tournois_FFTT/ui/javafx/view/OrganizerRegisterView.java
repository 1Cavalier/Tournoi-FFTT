package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.security.PasswordPolicy;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class OrganizerRegisterView extends VBox {

    public OrganizerRegisterView(Navigator nav) {
        setPadding(new Insets(24));
        setSpacing(12);

        var title = new Label("Inscription Organisme");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        var clubName = new TextField();
        clubName.setPromptText("Nom du club");

        var email = new TextField();
        email.setPromptText("Adresse mail");

        var password = new PasswordField();
        password.setPromptText("Mot de passe");

        var rules = new Label(PasswordPolicy.rulesText());
        rules.setStyle("-fx-opacity: 0.8; -fx-font-size: 12px;");

        var message = new Label();
        message.setStyle("-fx-text-fill: #b00020;");

        var createBtn = new Button("Créer le compte");
        createBtn.setOnAction(e -> {
            try {
                var acc = nav.organizerAuth().register(clubName.getText(), email.getText(), password.getText());

                EmailVerificationDialog dlg = new EmailVerificationDialog(nav, acc.getEmail());
                dlg.showAndWait();

                if (dlg.isVerified()) {
                    message.setStyle("-fx-text-fill: #1b5e20;");
                    message.setText("✅ Email vérifié ! Vous pouvez vous connecter.");
                    nav.showOrganizerLogin();
                } else {
                    message.setStyle("-fx-text-fill: #b00020;");
                    message.setText("⚠️ Compte créé mais email non vérifié (connexion impossible).");
                }

            } catch (IllegalArgumentException ex) {
                message.setStyle("-fx-text-fill: #b00020;");
                message.setText(ex.getMessage());
            }
        });

        var backBtn = new Button("Retour");
        backBtn.setOnAction(e -> nav.showOrganizerLogin());

        getChildren().addAll(title, clubName, email, password, rules, createBtn, backBtn, message);
    }
}
