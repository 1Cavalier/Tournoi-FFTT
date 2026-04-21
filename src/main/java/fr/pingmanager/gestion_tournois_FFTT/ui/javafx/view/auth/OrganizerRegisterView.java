package fr.pingmanager.gestion_tournois_FFTT.ui.javafx.view.auth;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Objects;

import fr.pingmanager.gestion_tournois_FFTT.infra.security.PasswordPolicy;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.app.AppRouter;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.ClubDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;

public class OrganizerRegisterView extends BorderPane {

    private static final String ERROR_STYLE = "-fx-text-fill: #b00020; -fx-font-weight: 700;";
    private static final String SUCCESS_STYLE = "-fx-text-fill: #1b5e20; -fx-font-weight: 700;";

    private final AppRouter nav;
    private final Label messageLabel = new Label();

    public OrganizerRegisterView(AppRouter nav) {
        this.nav = Objects.requireNonNull(nav, "nav must not be null");

        AppTheme.applyPage(this);
        setPadding(new Insets(AppTheme.PADDING_PAGE));

        VBox root = new VBox(AppTheme.SPACE_LG);
        root.setAlignment(Pos.TOP_CENTER);

        root.getChildren().addAll(
                buildHeader(),
                buildRegisterCard(),
                buildBottomActions());

        setCenter(root);
    }

    private VBox buildHeader() {
        VBox header = new VBox(AppTheme.SPACE_SM);
        header.setAlignment(Pos.TOP_CENTER);
        header.setMaxWidth(760);

        Label title = new Label("Inscription organisateur");
        AppTheme.applyTitle(title);

        Label subtitle = new Label(
                "Créez votre compte PingManager et rattachez-le à un club existant.");
        AppTheme.applySubtitle(subtitle);

        header.getChildren().addAll(title, subtitle);
        return header;
    }

    private VBox buildRegisterCard() {
        TextField firstNameField = textField("Prénom");
        TextField lastNameField = textField("Nom");
        TextField emailField = textField("Adresse email");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");
        passwordField.setMaxWidth(Double.MAX_VALUE);

        Label rulesLabel = new Label(PasswordPolicy.rulesText());
        AppTheme.applyBody(rulesLabel);

        Label clubSectionTitle = sectionTitle("Club");
        Label clubSectionHint = new Label(
                "Recherchez votre club par nom ou numéro FFTT, puis sélectionnez-le dans la liste.");
        AppTheme.applyBody(clubSectionHint);

        TextField searchField = textField("Recherche club (nom ou numéro)");

        Button searchButton = new Button("Rechercher");
        AppTheme.styleSecondary(searchButton);

        ListView<ClubDto> resultsList = buildClubResultsList();
        HBox searchRow = new HBox(10, searchField, searchButton);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        initMessageLabel();

        Button createAccountButton = new Button("Créer le compte");
        AppTheme.stylePrimary(createAccountButton);

        searchButton.setOnAction(e -> performClubSearch(searchField, resultsList));
        searchField.setOnAction(e -> performClubSearch(searchField, resultsList));

        firstNameField
                .setOnAction(e -> createAccount(firstNameField, lastNameField, emailField, passwordField, resultsList));
        lastNameField
                .setOnAction(e -> createAccount(firstNameField, lastNameField, emailField, passwordField, resultsList));
        emailField
                .setOnAction(e -> createAccount(firstNameField, lastNameField, emailField, passwordField, resultsList));
        passwordField
                .setOnAction(e -> createAccount(firstNameField, lastNameField, emailField, passwordField, resultsList));
        createAccountButton
                .setOnAction(e -> createAccount(firstNameField, lastNameField, emailField, passwordField, resultsList));

        VBox card = AppTheme.card(
                sectionTitle("Compte organisateur"),
                firstNameField,
                lastNameField,
                emailField,
                passwordField,
                rulesLabel,
                new Separator(),
                clubSectionTitle,
                clubSectionHint,
                searchRow,
                resultsList,
                createAccountButton,
                messageLabel);
        card.setMaxWidth(640);

        firstNameField.requestFocus();
        return card;
    }

    private HBox buildBottomActions() {
        Button backButton = new Button("Retour");
        AppTheme.styleSecondary(backButton);
        backButton.setOnAction(e -> nav.showOrganizerLogin());

        HBox bottom = new HBox(12, backButton);
        bottom.setAlignment(Pos.CENTER);
        bottom.setMaxWidth(640);
        return bottom;
    }

    private ListView<ClubDto> buildClubResultsList() {
        ListView<ClubDto> resultsList = new ListView<>();
        resultsList.setPrefHeight(180);
        resultsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ClubDto item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    return;
                }

                String number = safeText(item.clubNumber(), "?");
                String name = safeText(item.clubName(), "(sans nom)");
                String city = safeText(item.city(), "ville inconnue");

                setText(number + " — " + name + " — " + city);
            }
        });
        return resultsList;
    }

    private Label sectionTitle(String text) {
        Label label = new Label(text);
        AppTheme.applyCardTitle(label);
        return label;
    }

    private TextField textField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setMaxWidth(Double.MAX_VALUE);
        return field;
    }

    private void performClubSearch(TextField searchField, ListView<ClubDto> resultsList) {
        clearMessage();

        String query = safeTrim(searchField.getText());
        if (query.isEmpty()) {
            resultsList.setItems(FXCollections.observableArrayList());
            showError("Saisis un nom ou un numéro de club pour rechercher.");
            return;
        }

        List<ClubDto> found = nav.clubRepo().search(query, 30);
        resultsList.setItems(FXCollections.observableArrayList(found));

        if (found.isEmpty()) {
            showError("Aucun club trouvé.");
        }
    }

    private void createAccount(
            TextField firstNameField,
            TextField lastNameField,
            TextField emailField,
            PasswordField passwordField,
            ListView<ClubDto> resultsList) {

        clearMessage();

        try {
            String firstName = safeTrim(firstNameField.getText());
            String lastName = safeTrim(lastNameField.getText());
            String email = normalizeEmail(emailField.getText());
            String password = safeText(passwordField.getText());

            requireNotBlank(firstName, "Prénom obligatoire.");
            requireNotBlank(lastName, "Nom obligatoire.");
            requireNotBlank(email, "Email obligatoire.");
            requireNotBlank(password, "Mot de passe obligatoire.");

            PasswordPolicy.validateOrThrow(password);

            ClubDto selectedClub = resultsList.getSelectionModel().getSelectedItem();
            if (selectedClub == null) {
                throw new IllegalArgumentException("Sélectionne un club dans la liste.");
            }

            if (!confirmRegistration(firstName, lastName, email, selectedClub)) {
                return;
            }

            var account = nav.organizerAuth().register(
                    firstName,
                    lastName,
                    email,
                    password,
                    selectedClub.id());

            CodeVerificationDialog dialog = new CodeVerificationDialog(
                    nav.primaryStage(),
                    "Vérification email",
                    "Saisissez le code de vérification transmis au club pour valider ce compte.",
                    code -> nav.organizerAuth().verifyEmail(account.getEmail(), code));

            dialog.showAndWait();
            passwordField.clear();

            if (dialog.isSuccess()) {
                showSuccess("Compte créé et email vérifié. Vous pouvez maintenant vous connecter.");
                nav.showOrganizerLogin();
            } else {
                showError(
                        "Compte créé, mais email non vérifié. La connexion reste bloquée tant que l'email n'est pas validé.");
            }

        } catch (IllegalArgumentException ex) {
            passwordField.clear();
            showError(ex.getMessage());
        }
    }

    private boolean confirmRegistration(String firstName, String lastName, String email, ClubDto selectedClub) {
        String clubName = safeText(selectedClub.clubName(), "Club inconnu");
        String clubNumber = safeText(selectedClub.clubNumber(), "numéro inconnu");
        String clubCity = safeText(selectedClub.city(), "ville inconnue");

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.initOwner(nav.primaryStage());
        confirm.setTitle("Confirmation de la demande");
        confirm.setHeaderText("Création du compte organisateur");
        confirm.setContentText(
                "Vous êtes sur le point de créer un compte pour :\n\n"
                        + firstName + " " + lastName + "\n"
                        + email + "\n\n"
                        + "Club sélectionné :\n"
                        + clubName + "\n"
                        + "Numéro FFTT : " + clubNumber + "\n"
                        + "Ville : " + clubCity + "\n\n"
                        + "Un code de vérification sera envoyé à l'adresse officielle du club.\n"
                        + "Êtes-vous sûr de vouloir continuer ?");

        confirm.getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);

        var result = confirm.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
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
        return safeTrim(raw).toLowerCase();
    }

    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private static String safeText(String s) {
        return s == null ? "" : s.trim();
    }

    private static String safeText(String s, String fallback) {
        return (s == null || s.isBlank()) ? fallback : s.trim();
    }

    private static void requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}