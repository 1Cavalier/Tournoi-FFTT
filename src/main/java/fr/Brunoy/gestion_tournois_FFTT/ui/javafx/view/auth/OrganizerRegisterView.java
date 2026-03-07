package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.auth;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.SqliteClubRepository;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.security.PasswordPolicy;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;
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

public class OrganizerRegisterView extends BorderPane {

    private static final String ERROR_STYLE = "-fx-text-fill: #b00020; -fx-font-weight: 700;";
    private static final String SUCCESS_STYLE = "-fx-text-fill: #1b5e20; -fx-font-weight: 700;";

    private final Navigator nav;
    private final Label messageLabel = new Label();

    public OrganizerRegisterView(Navigator nav) {
        this.nav = Objects.requireNonNull(nav, "nav must not be null");

        AppTheme.applyPage(this);
        setPadding(new Insets(AppTheme.PADDING_PAGE));

        VBox root = new VBox(AppTheme.SPACE_LG);
        root.setAlignment(Pos.TOP_CENTER);

        VBox header = buildHeader();
        VBox card = buildRegisterCard();
        HBox bottomActions = buildBottomActions();

        root.getChildren().addAll(header, card, bottomActions);
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
        TextField emailField = new TextField();
        emailField.setPromptText("Adresse email");
        emailField.setMaxWidth(Double.MAX_VALUE);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");
        passwordField.setMaxWidth(Double.MAX_VALUE);

        Label rulesLabel = new Label(PasswordPolicy.rulesText());
        AppTheme.applyBody(rulesLabel);

        Label clubSectionTitle = sectionTitle("Club");
        Label clubSectionHint = new Label(
                "Recherchez votre club par nom ou numéro FFTT, puis sélectionnez-le dans la liste.");
        AppTheme.applyBody(clubSectionHint);

        TextField searchField = new TextField();
        searchField.setPromptText("Recherche club (nom ou numéro)");
        searchField.setMaxWidth(Double.MAX_VALUE);

        Button searchButton = new Button("Rechercher");
        AppTheme.styleSecondary(searchButton);

        ListView<SqliteClubRepository.ClubRow> resultsList = new ListView<>();
        resultsList.setPrefHeight(180);
        resultsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(SqliteClubRepository.ClubRow item, boolean empty) {
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

        HBox searchRow = new HBox(10, searchField, searchButton);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        messageLabel.setManaged(false);
        messageLabel.setVisible(false);
        messageLabel.setWrapText(true);
        messageLabel.setStyle(ERROR_STYLE);

        Button createAccountButton = new Button("Créer le compte");
        AppTheme.stylePrimary(createAccountButton);

        searchButton.setOnAction(e -> performClubSearch(searchField, resultsList));
        searchField.setOnAction(e -> performClubSearch(searchField, resultsList));
        passwordField.setOnAction(e -> createAccount(emailField, passwordField, resultsList));
        emailField.setOnAction(e -> createAccount(emailField, passwordField, resultsList));
        createAccountButton.setOnAction(e -> createAccount(emailField, passwordField, resultsList));

        VBox card = AppTheme.card(
                sectionTitle("Compte organisateur"),
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

        emailField.requestFocus();
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

    private Label sectionTitle(String text) {
        Label label = new Label(text);
        AppTheme.applyCardTitle(label);
        return label;
    }

    private void performClubSearch(TextField searchField, ListView<SqliteClubRepository.ClubRow> resultsList) {
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
    }

    private void createAccount(
            TextField emailField,
            PasswordField passwordField,
            ListView<SqliteClubRepository.ClubRow> resultsList) {

        clearMessage();

        try {
            String email = normalizeEmail(emailField.getText());
            String password = passwordField.getText();

            requireNotBlank(email, "Email obligatoire.");
            requireNotBlank(password, "Mot de passe obligatoire.");

            PasswordPolicy.validateOrThrow(password);

            SqliteClubRepository.ClubRow selectedClub = resultsList.getSelectionModel().getSelectedItem();
            if (selectedClub == null) {
                throw new IllegalArgumentException("Sélectionne un club dans la liste.");
            }

            String clubName = safeText(selectedClub.clubName(), "Club inconnu");
            String clubNumber = safeText(selectedClub.clubNumber(), "numéro inconnu");
            String officialContactEmail = safeText(selectedClub.officialContactEmail(), "adresse inconnue");

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation de la demande");
            confirm.setHeaderText("Demande d'accès au club");

            confirm.setContentText(
                    "Vous êtes sur le point de créer un compte pour le club :\n\n"
                            + clubName + "\n"
                            + "Numéro FFTT : " + clubNumber + "\n\n"
                            + "Un email de vérification sera envoyé à l'adresse suivante :\n"
                            + officialContactEmail + "\n\n"
                            + "Cette adresse est rattachée à votre club dans la base FFTT.\n"
                            + "Êtes-vous sûr de vouloir continuer la démarche ?");

            confirm.getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);

            var result = confirm.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }

            var account = nav.organizerAuth().register(
                    email,
                    password,
                    selectedClub.id());

            CodeVerificationDialog dialog = new CodeVerificationDialog(
                    "Vérification email",
                    "Un code a été envoyé à : " + account.getEmail(),
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

    private void clearMessage() {
        messageLabel.setText("");
        messageLabel.setStyle(ERROR_STYLE);
        messageLabel.setManaged(false);
        messageLabel.setVisible(false);
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

    private static String normalizeEmail(String raw) {
        return safeTrim(raw).toLowerCase();
    }

    private static String safeTrim(String s) {
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