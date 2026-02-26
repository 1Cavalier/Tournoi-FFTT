package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.security.PasswordPolicy;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.SqliteClubRepository;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class OrganizerRegisterView extends VBox {

    public OrganizerRegisterView(Navigator nav) {
        setPadding(new Insets(24));
        setSpacing(12);

        Label title = new Label("Inscription Organisme");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField email = new TextField();
        email.setPromptText("Adresse mail");

        PasswordField password = new PasswordField();
        password.setPromptText("Mot de passe");

        Label rules = new Label(PasswordPolicy.rulesText());
        rules.setStyle("-fx-opacity: 0.8; -fx-font-size: 12px;");

        // ---- Choix : club existant ou nouveau ----
        ToggleGroup tg = new ToggleGroup();

        RadioButton joinExisting = new RadioButton("Rejoindre un club existant");
        joinExisting.setToggleGroup(tg);

        RadioButton createNew = new RadioButton("Créer un nouveau club");
        createNew.setToggleGroup(tg);

        createNew.setSelected(true);

        // ---- Bloc club existant (recherche + liste) ----
        TextField search = new TextField();
        search.setPromptText("Recherche club (nom ou numéro)");

        ListView<SqliteClubRepository.ClubRow> results = new ListView<>();
        results.setPrefHeight(140);

        results.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(SqliteClubRepository.ClubRow item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String num = (item.clubNumber() == null || item.clubNumber().isBlank()) ? "?" : item.clubNumber();
                    String name = (item.clubName() == null || item.clubName().isBlank()) ? "(sans nom)"
                            : item.clubName();
                    setText(num + " — " + name);
                }
            }
        });

        Button doSearchBtn = new Button("Rechercher");
        doSearchBtn.setOnAction(e -> {
            List<SqliteClubRepository.ClubRow> found = nav.clubRepo().search(search.getText(), 30);
            results.setItems(FXCollections.observableArrayList(found));
        });

        VBox existingBox = new VBox(8, search, doSearchBtn, results);

        // ---- Bloc nouveau club ----
        TextField newClubName = new TextField();
        newClubName.setPromptText("Nom du club (recommandé)");

        TextField newClubNumber = new TextField();
        newClubNumber.setPromptText("Numéro club FFTT (optionnel)");

        VBox newBox = new VBox(8, newClubName, newClubNumber);

        // active/désactive selon choix
        existingBox.disableProperty().bind(createNew.selectedProperty());
        newBox.disableProperty().bind(joinExisting.selectedProperty());

        Label message = new Label();
        message.setStyle("-fx-text-fill: #b00020;");

        Button createBtn = new Button("Créer le compte");
        createBtn.setDefaultButton(true);

        createBtn.setOnAction(e -> {
            try {
                String mail = email.getText() == null ? "" : email.getText().trim().toLowerCase();
                String pwd = password.getText();

                PasswordPolicy.validateOrThrow(pwd);

                String existingClubId = null;
                String clubName = null;

                if (joinExisting.isSelected()) {
                    var selected = results.getSelectionModel().getSelectedItem();
                    if (selected == null) {
                        throw new IllegalArgumentException("Sélectionne un club existant dans la liste.");
                    }
                    existingClubId = selected.id();
                } else {
                    clubName = newClubName.getText();
                    // Si tu veux, plus tard, on passera aussi clubNumber ici via un
                    // createClubWithNumber
                    // Pour l’instant l’auth service crée un club minimal (nom).
                }

                var acc = nav.organizerAuth().register(
                        mail,
                        pwd,
                        existingClubId,
                        clubName);

                // popup verification inscription
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

        Button backBtn = new Button("Retour");
        backBtn.setOnAction(e -> nav.showOrganizerLogin());

        Separator sep = new Separator();

        getChildren().addAll(
                title,
                email,
                password,
                rules,
                sep,
                new HBox(12, joinExisting, createNew),
                existingBox,
                newBox,
                createBtn,
                backBtn,
                message);
    }
}