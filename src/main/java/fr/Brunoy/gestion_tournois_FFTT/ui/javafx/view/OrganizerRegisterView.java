package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.vm.AppState;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class OrganizerRegisterView extends VBox {

    public OrganizerRegisterView(AppState state, Navigator nav) {

        setPadding(new Insets(20));
        setSpacing(10);

        Label title = new Label("Inscription Organisme");
        title.setStyle("-fx-font-size:18px; -fx-font-weight:bold;");

        TextField nameField = new TextField();
        nameField.setPromptText("Nom du club / organisateur");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");

        Label infoLabel = new Label();
        infoLabel.setStyle("-fx-text-fill: #444;");

        Button createBtn = new Button("Créer le compte");
        createBtn.setOnAction(e -> {
            if (nameField.getText().isBlank() || emailField.getText().isBlank() || passwordField.getText().isBlank()) {
                infoLabel.setText("Veuillez remplir tous les champs.");
                return;
            }

            OrganizerAccount acc = new OrganizerAccount(
                    nameField.getText().trim(),
                    emailField.getText().trim(),
                    passwordField.getText()
            );

            state.getOrganizers().add(acc);
            state.setCurrentOrganizer(acc);

            nav.showOrganizerDashboard();
        });

        Button backBtn = new Button("Retour");
        backBtn.setOnAction(e -> nav.showOrganizerLogin());

        getChildren().addAll(title, nameField, emailField, passwordField, createBtn, backBtn, infoLabel);
    }
}
