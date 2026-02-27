package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.auth;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.security.PasswordPolicy;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.SqliteClubRepository;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Inscription organisme :
 * - Création compte + rattachement à un club (existant ou nouveau)
 * - Vérification email via CodeVerificationDialog
 */
public class OrganizerRegisterView extends VBox {

    private static final double FORM_SPACING = 12;

    private static final String TITLE_STYLE = "-fx-font-size: 18px; -fx-font-weight: bold;";
    private static final String RULES_STYLE = "-fx-opacity: 0.8; -fx-font-size: 12px;";
    private static final String ERROR_STYLE = "-fx-text-fill: #b00020;";
    private static final String SUCCESS_STYLE = "-fx-text-fill: #1b5e20;";

    private final Label messageLabel = new Label();

    public OrganizerRegisterView(Navigator nav) {
        setPadding(new Insets(24));
        setSpacing(FORM_SPACING);

        Label title = new Label("Inscription Organisme");
        title.setStyle(TITLE_STYLE);

        TextField emailField = new TextField();
        emailField.setPromptText("Adresse mail");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");

        Label rules = new Label(PasswordPolicy.rulesText());
        rules.setStyle(RULES_STYLE);

        // Choix : rejoindre un club existant ou créer un nouveau club
        ToggleGroup clubChoiceGroup = new ToggleGroup();

        RadioButton joinExistingRadio = new RadioButton("Rejoindre un club existant");
        joinExistingRadio.setToggleGroup(clubChoiceGroup);

        RadioButton createNewRadio = new RadioButton("Créer un nouveau club");
        createNewRadio.setToggleGroup(clubChoiceGroup);
        createNewRadio.setSelected(true);

        // Bloc "club existant" : recherche + résultats
        TextField searchField = new TextField();
        searchField.setPromptText("Recherche club (nom ou numéro)");

        Button searchButton = new Button("Rechercher");

        ListView<SqliteClubRepository.ClubRow> resultsList = new ListView<>();
        resultsList.setPrefHeight(140);
        resultsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(SqliteClubRepository.ClubRow item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                String num = isBlank(item.clubNumber()) ? "?" : item.clubNumber().trim();
                String name = isBlank(item.clubName()) ? "(sans nom)" : item.clubName().trim();
                setText(num + " — " + name);
            }
        });

        searchButton.setOnAction(e -> {
            clearMessage();
            String query = safeTrim(searchField.getText());

            if (query.isEmpty()) {
                resultsList.setItems(FXCollections.observableArrayList());
                showError("Saisis un nom ou un numéro de club pour rechercher.");
                return;
            }

            List<SqliteClubRepository.ClubRow> found = nav.clubRepo().search(query, 30);
            resultsList.setItems(FXCollections.observableArrayList(found));

            if (found.isEmpty()) {
                showError("Aucun club trouvé.");
            }
        });

        // Entrée dans le champ de recherche lance la recherche
        searchField.setOnAction(e -> searchButton.fire());

        VBox existingClubBox = new VBox(8, searchField, searchButton, resultsList);

        // Bloc "nouveau club"
        TextField newClubNameField = new TextField();
        newClubNameField.setPromptText("Nom du club (recommandé)");

        TextField newClubNumberField = new TextField();
        newClubNumberField.setPromptText("Numéro club FFTT (optionnel)");

        VBox newClubBox = new VBox(8, newClubNameField, newClubNumberField);

        // Active/désactive les blocs selon le choix
        existingClubBox.disableProperty().bind(createNewRadio.selectedProperty());
        newClubBox.disableProperty().bind(joinExistingRadio.selectedProperty());

        messageLabel.setStyle(ERROR_STYLE);

        Button createAccountButton = new Button("Créer le compte");
        createAccountButton.setDefaultButton(true);

        createAccountButton.setOnAction(e -> {
            try {
                clearMessage();

                String email = normalizeEmail(emailField.getText());
                String password = passwordField.getText();

                requireNotBlank(email, "Email obligatoire.");
                requireNotBlank(password, "Mot de passe obligatoire.");

                // Feedback immédiat côté UI (le service re-valide aussi)
                PasswordPolicy.validateOrThrow(password);

                String existingClubId = null;
                String newClubName = null;

                if (joinExistingRadio.isSelected()) {
                    SqliteClubRepository.ClubRow selected = resultsList.getSelectionModel().getSelectedItem();
                    if (selected == null) {
                        throw new IllegalArgumentException("Sélectionne un club existant dans la liste.");
                    }
                    existingClubId = selected.id();
                } else {
                    newClubName = safeTrim(newClubNameField.getText());
                    // newClubNumberField n'est pas encore utilisé par le service.
                    // Soit on le masque, soit on implémente plus tard createClubWithNumber.
                }

                var account = nav.organizerAuth().register(email, password, existingClubId, newClubName);

                // Vérification email via le dialog unique
                CodeVerificationDialog dlg = new CodeVerificationDialog(
                        "Vérification email",
                        "Un code a été envoyé à : " + account.getEmail(),
                        code -> nav.organizerAuth().verifyEmail(account.getEmail(), code));
                dlg.showAndWait();

                if (dlg.isSuccess()) {
                    showSuccess("Email vérifié. Vous pouvez vous connecter.");
                    nav.showOrganizerLogin();
                } else {
                    showError("Compte créé, mais email non vérifié (connexion impossible).");
                }

            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
            }
        });

        Button backButton = new Button("Retour");
        backButton.setOnAction(e -> nav.showOrganizerLogin());

        getChildren().addAll(
                title,
                emailField,
                passwordField,
                rules,
                new Separator(),
                new HBox(12, joinExistingRadio, createNewRadio),
                existingClubBox,
                newClubBox,
                createAccountButton,
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

    private static String normalizeEmail(String raw) {
        return safeTrim(raw).toLowerCase();
    }

    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static void requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}