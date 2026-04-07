package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.dialogs;

import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.TournamentLevel;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.RankingPhase;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.AppRouter;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.ClubDto;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.OrganizerDto;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.TournamentDto;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.service.CreateTournamentDraftCommand;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class CreateTournamentDialog extends Stage {

    /**
     * Libellé utilisé pour les champs obligatoires.
     */
    private static final String REQUIRED_SUFFIX = " *";

    /**
     * Libellé utilisé pour les champs facultatifs.
     */
    private static final String OPTIONAL_SUFFIX = " (facultatif)";

    /**
     * Texte affiché quand le numéro d'homologation n'est pas encore attribué.
     */
    private static final String HOMOLOGATION_PENDING_TEXT = "En attente de validation FFTT";

    private final AppRouter nav;
    private final OrganizerDto organizer;
    private final ClubDto club;
    private final TournamentDto existingTournament;

    private final TextField nameField = new TextField();
    private final TextField address1Field = new TextField();
    private final TextField address2Field = new TextField();
    private final TextField cityField = new TextField();
    private final TextField departmentField = new TextField();
    private final TextField homologationField = new TextField();

    private final ComboBox<TournamentLevel> levelBox = new ComboBox<>();
    private final ComboBox<RankingPhase> phaseBox = new ComboBox<>();

    private final DatePicker startDatePicker = new DatePicker();
    private final DatePicker endDatePicker = new DatePicker();

    private final Label infoLabel = new Label();
    private final Label daysInfoLabel = new Label("Veuillez sélectionner les dates du tournoi.");
    private final Button saveButton = new Button();

    public CreateTournamentDialog(AppRouter nav) {
        this(nav, null);
    }

    public CreateTournamentDialog(AppRouter nav, TournamentDto existingTournament) {
        this.nav = Objects.requireNonNull(nav, "nav must not be null");
        this.organizer = nav.requireOrganizer();
        this.club = nav.clubRepo()
                .findByOrganizerId(organizer.getId())
                .orElseThrow(() -> new IllegalStateException("Club introuvable pour cet organisateur"));
        this.existingTournament = existingTournament;

        initModality(Modality.APPLICATION_MODAL);
        setTitle(isEditMode() ? "Modifier le tournoi" : "Créer un tournoi");

        build();
        configureDefaults();
        configureDateLogic();
        configureActions();
    }

    /**
     * Retourne true si la boîte est utilisée pour modifier un tournoi existant.
     */
    private boolean isEditMode() {
        return existingTournament != null;
    }

    /**
     * Construit l'interface complète de la fenêtre.
     *
     * Règles métier rappelées à l'utilisateur :
     * - les champs marqués d'un * sont obligatoires
     * - l'adresse 2 est facultative
     * - le numéro d'homologation est informatif, en lecture seule, et attribué par
     * la FFTT
     */
    private void build() {
        VBox root = new VBox(AppTheme.SPACE_LG);
        root.setPadding(new Insets(20));
        AppTheme.applyPage(root);

        Label title = new Label(isEditMode() ? "Modifier le tournoi" : "Créer un tournoi");
        AppTheme.applyTitle(title);

        Label subtitle = new Label(
                isEditMode()
                        ? "Modifiez les informations générales du tournoi."
                        : "Renseignez d'abord le bloc général du tournoi. Le tournoi sera créé en brouillon puis complété ensuite.");
        AppTheme.applyBody(subtitle);
        subtitle.setWrapText(true);

        AppTheme.applyBody(infoLabel);
        infoLabel.setStyle("-fx-font-weight: bold;");
        infoLabel.setText("* : champ obligatoire");

        GridPane form = new GridPane();
        form.setHgap(14);
        form.setVgap(12);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(260);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);

        form.getColumnConstraints().addAll(col1, col2);

        int row = 0;
        addField(form, row++, requiredLabelNode("Nom du tournoi"), nameField);
        addField(form, row++, requiredLabelNode("Adresse 1 du tournoi"), address1Field);
        addField(form, row++, optionalLabelNode("Adresse 2 du tournoi"), address2Field);
        addField(form, row++, requiredLabelNode("Ville"), cityField);
        addField(form, row++, requiredLabelNode("Département"), departmentField);
        addField(form, row++, requiredLabelNode("Niveau du tournoi"), levelBox);
        addField(form, row++, requiredLabelNode("Phase de comptage des points"), phaseBox);

        HBox datesRow = new HBox(12, startDatePicker, endDatePicker);
        datesRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(startDatePicker, Priority.ALWAYS);
        HBox.setHgrow(endDatePicker, Priority.ALWAYS);
        startDatePicker.setMaxWidth(Double.MAX_VALUE);
        endDatePicker.setMaxWidth(Double.MAX_VALUE);

        addField(form, row++, requiredLabelNode("Date du tournoi"), datesRow);

        AppTheme.applyBody(daysInfoLabel);
        daysInfoLabel.setWrapText(true);
        form.add(new Label(""), 0, row);
        form.add(daysInfoLabel, 1, row);
        row++;

        homologationField.setEditable(false);
        homologationField.setFocusTraversable(false);
        homologationField.setMaxWidth(Double.MAX_VALUE);
        homologationField.setDisable(true);
        homologationField.setStyle(
                "-fx-background-color: #F0F0F0;" +
                        "-fx-text-fill: #555555;" +
                        "-fx-opacity: 1.0;");

        addField(form, row++, readonlyLabelNode("Numéro d'homologation"), homologationField);

        Button cancelButton = new Button("Annuler");
        AppTheme.styleSecondary(cancelButton);
        cancelButton.setOnAction(e -> close());

        saveButton.setText(isEditMode() ? "Enregistrer les modifications" : "Créer le tournoi");
        AppTheme.stylePrimary(saveButton);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(12, cancelButton, spacer, saveButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox cardContent = new VBox(AppTheme.SPACE_MD, infoLabel, form);
        VBox card = AppTheme.card(cardContent);
        card.setMaxWidth(Double.MAX_VALUE);

        root.getChildren().addAll(title, subtitle, card, actions);

        Scene scene = new Scene(root, 800, 620);
        setScene(scene);
    }

    /**
     * Applique les valeurs par défaut :
     * - prompts
     * - chargement des listes déroulantes
     * - mode création : préremplissage depuis le club
     * - mode modification : préremplissage depuis le tournoi
     * - affichage lecture seule de l'homologation
     */
    private void configureDefaults() {
        nameField.setPromptText("Ex : Tournoi du Club");
        address1Field.setPromptText("Adresse principale du tournoi");
        address2Field.setPromptText("Complément d'adresse");
        cityField.setPromptText("Ville");
        departmentField.setPromptText("Département");
        startDatePicker.setPromptText("Date de début");
        endDatePicker.setPromptText("Date de fin");

        levelBox.getItems().setAll(TournamentLevel.values());
        phaseBox.getItems().setAll(RankingPhase.values());

        if (isEditMode()) {
            nameField.setText(nvl(existingTournament.name()));
            address1Field.setText(nvl(existingTournament.address1()));
            address2Field.setText(nvl(existingTournament.address2()));
            cityField.setText(nvl(existingTournament.city()));
            departmentField.setText(nvl(existingTournament.department()));

            selectLevel(existingTournament.level());
            selectPhase(existingTournament.phase());

            if (hasText(existingTournament.startDate())) {
                startDatePicker.setValue(LocalDate.parse(existingTournament.startDate()));
            }
            if (hasText(existingTournament.endDate())) {
                endDatePicker.setValue(LocalDate.parse(existingTournament.endDate()));
            }

            homologationField.setText(displayHomologationValue(existingTournament.homologationNumber()));
        } else {
            address1Field.setText(nvl(club.address1()));
            address2Field.setText(nvl(club.address2()));
            cityField.setText(nvl(club.city()));
            departmentField.setText(nvl(club.departementCode()));

            if (!levelBox.getItems().isEmpty()) {
                levelBox.getSelectionModel().selectFirst();
            }
            if (!phaseBox.getItems().isEmpty()) {
                phaseBox.getSelectionModel().selectFirst();
            }

            homologationField.setText(HOMOLOGATION_PENDING_TEXT);
        }

        refreshDaysInfo();
    }

    /**
     * Sélectionne le niveau dans la combo à partir de la valeur brute.
     */
    private void selectLevel(String raw) {
        if (!hasText(raw)) {
            levelBox.getSelectionModel().selectFirst();
            return;
        }
        try {
            levelBox.setValue(TournamentLevel.valueOf(raw));
        } catch (Exception e) {
            levelBox.getSelectionModel().selectFirst();
        }
    }

    /**
     * Sélectionne la phase dans la combo à partir de la valeur brute.
     */
    private void selectPhase(String raw) {
        if (!hasText(raw)) {
            phaseBox.getSelectionModel().selectFirst();
            return;
        }
        try {
            phaseBox.setValue(RankingPhase.valueOf(raw));
        } catch (Exception e) {
            phaseBox.getSelectionModel().selectFirst();
        }
    }

    /**
     * Configure les règles de sélection des dates :
     * - en création : pas de date passée
     * - en modification : les dates existantes restent éditables
     * - la date de fin ne peut pas être avant la date de début
     */
    private void configureDateLogic() {
        startDatePicker.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setDisable(true);
                    return;
                }

                if (isEditMode()) {
                    setDisable(false);
                    return;
                }

                LocalDate today = LocalDate.now();
                setDisable(item.isBefore(today));
            }
        });

        endDatePicker.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setDisable(true);
                    return;
                }

                LocalDate start = startDatePicker.getValue();

                if (isEditMode()) {
                    setDisable(start != null && item.isBefore(start));
                    return;
                }

                LocalDate today = LocalDate.now();
                boolean disable = item.isBefore(today);
                if (start != null) {
                    disable = disable || item.isBefore(start);
                }
                setDisable(disable);
            }
        });

        startDatePicker.valueProperty().addListener((obs, oldValue, newValue) -> {
            LocalDate end = endDatePicker.getValue();
            if (newValue != null && end != null && end.isBefore(newValue)) {
                endDatePicker.setValue(newValue);
            }
            refreshDaysInfo();
        });

        endDatePicker.valueProperty().addListener((obs, oldValue, newValue) -> refreshDaysInfo());
    }

    /**
     * Branche le bouton principal sur la bonne action :
     * - création
     * - modification
     */
    private void configureActions() {
        saveButton.setOnAction(e -> {
            if (isEditMode()) {
                onUpdateTournament();
            } else {
                onCreateTournament();
            }
        });
    }

    /**
     * Création d'un tournoi brouillon.
     *
     * Champs obligatoires :
     * - nom
     * - adresse 1
     * - ville
     * - département
     * - niveau
     * - phase
     * - date de début
     * - date de fin
     *
     * Champ facultatif :
     * - adresse 2
     */
    private void onCreateTournament() {
        try {
            String name = requireText(nameField, "Le nom du tournoi est obligatoire.");
            String address1 = requireText(address1Field, "L'adresse 1 du tournoi est obligatoire.");
            String address2 = optionalText(address2Field);
            String city = requireText(cityField, "La ville est obligatoire.");
            String department = requireText(departmentField, "Le département est obligatoire.");

            TournamentLevel level = requireCombo(levelBox, "Le niveau du tournoi est obligatoire.");
            RankingPhase phase = requireCombo(phaseBox, "La phase est obligatoire.");

            LocalDate startDate = requireDate(startDatePicker, "La date de début est obligatoire.");
            LocalDate endDate = requireDate(endDatePicker, "La date de fin est obligatoire.");

            if (endDate.isBefore(startDate)) {
                throw new IllegalArgumentException("La date de fin ne peut pas être avant la date de début.");
            }

            CreateTournamentDraftCommand cmd = new CreateTournamentDraftCommand(
                    club.id(),
                    organizer.getId(),
                    name,
                    address1,
                    address2,
                    city,
                    department,
                    level,
                    phase,
                    startDate,
                    endDate);

            nav.tournamentService().createDraft(cmd);
            close();

        } catch (IllegalArgumentException ex) {
            showError("Validation", ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Erreur", "Impossible de créer le tournoi : " + safeMessage(ex));
        }
    }

    /**
     * Modification des informations générales du tournoi.
     *
     * Le numéro d'homologation n'est jamais modifié ici.
     * Il est conservé tel quel.
     */
    private void onUpdateTournament() {
        try {
            String name = requireText(nameField, "Le nom du tournoi est obligatoire.");
            String address1 = requireText(address1Field, "L'adresse 1 du tournoi est obligatoire.");
            String address2 = optionalText(address2Field);
            String city = requireText(cityField, "La ville est obligatoire.");
            String department = requireText(departmentField, "Le département est obligatoire.");

            TournamentLevel level = requireCombo(levelBox, "Le niveau du tournoi est obligatoire.");
            RankingPhase phase = requireCombo(phaseBox, "La phase est obligatoire.");

            LocalDate startDate = requireDate(startDatePicker, "La date de début est obligatoire.");
            LocalDate endDate = requireDate(endDatePicker, "La date de fin est obligatoire.");

            if (endDate.isBefore(startDate)) {
                throw new IllegalArgumentException("La date de fin ne peut pas être avant la date de début.");
            }

            TournamentDto updated = new TournamentDto(
                    existingTournament.id(),
                    existingTournament.clubId(),
                    existingTournament.organizerId(),
                    name,
                    address1,
                    address2,
                    city,
                    department,
                    level.name(),
                    phase.name(),
                    startDate.toString(),
                    endDate.toString(),
                    existingTournament.homologationNumber(),
                    existingTournament.status(),
                    existingTournament.createdAt(),
                    existingTournament.updatedAt());

            nav.tournamentService().updateGeneral(updated);
            close();

        } catch (IllegalArgumentException ex) {
            showError("Validation", ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Erreur", "Impossible de modifier le tournoi : " + safeMessage(ex));
        }
    }

    /**
     * Met à jour le texte d'information sur la durée du tournoi.
     */
    private void refreshDaysInfo() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        if (start == null && end == null) {
            daysInfoLabel.setText("Veuillez sélectionner les dates du tournoi.");
            return;
        }
        if (start != null && end == null) {
            daysInfoLabel.setText("Veuillez maintenant sélectionner la date de fin du tournoi.");
            return;
        }
        if (start == null) {
            daysInfoLabel.setText("Veuillez d'abord sélectionner la date de début du tournoi.");
            return;
        }
        if (end.isBefore(start)) {
            daysInfoLabel.setText("La date de fin doit être égale ou postérieure à la date de début.");
            return;
        }

        long days = ChronoUnit.DAYS.between(start, end) + 1;
        String dayWord = days > 1 ? "jours" : "jour";

        daysInfoLabel.setText(
                "Actuellement, vous avez sélectionné " + days + " " + dayWord
                        + " pour l'ensemble de votre tournoi.");
    }

    /**
     * Ajoute un libellé et son champ dans la grille du formulaire.
     */
    private void addField(GridPane grid, int row, javafx.scene.Node labelNode, javafx.scene.Node field) {
        Label colon = new Label(":");
        AppTheme.applyBody(colon);

        HBox labelBox = new HBox(4, labelNode, colon);
        labelBox.setAlignment(Pos.CENTER_LEFT);

        GridPane.setHgrow(field, Priority.ALWAYS);
        if (field instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
        }

        grid.add(labelBox, 0, row);
        grid.add(field, 1, row);
    }

    /**
     * Vérifie qu'un champ texte obligatoire est bien renseigné.
     */
    private String requireText(TextField field, String message) {
        String value = field.getText();
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    /**
     * Retourne la valeur d'un champ facultatif.
     * Si vide, retourne null.
     */
    private String optionalText(TextField field) {
        String value = field.getText();
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Vérifie qu'une date obligatoire est bien sélectionnée.
     */
    private LocalDate requireDate(DatePicker picker, String message) {
        LocalDate value = picker.getValue();
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    /**
     * Vérifie qu'une valeur obligatoire est bien sélectionnée dans une combo.
     */
    private <T> T requireCombo(ComboBox<T> comboBox, String message) {
        T value = comboBox.getValue();
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    /**
     * Affiche une erreur de validation ou de traitement.
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Sécurise le message d'une exception.
     */
    private String safeMessage(Exception ex) {
        return ex.getMessage() == null || ex.getMessage().isBlank()
                ? "Erreur inconnue."
                : ex.getMessage();
    }

    /**
     * Retourne une chaîne vide si la valeur est nulle.
     */
    private String nvl(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Vérifie qu'une chaîne contient du texte utile.
     */
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    /**
     * Prépare un libellé de champ obligatoire.
     */
    private Label requiredLabelNode(String labelText) {
        Label label = new Label(labelText);
        Label star = new Label("*");

        AppTheme.applyBody(label);
        AppTheme.applyBody(star);

        star.setStyle("-fx-text-fill: #D32F2F; -fx-font-weight: bold;");

        HBox box = new HBox(2, label, star);
        box.setAlignment(Pos.CENTER_LEFT);

        return wrapLabel(box);
    }

    private Label optionalLabelNode(String labelText) {
        Label label = new Label(labelText + " (facultatif)");
        AppTheme.applyBody(label);

        HBox box = new HBox(label);
        box.setAlignment(Pos.CENTER_LEFT);

        return wrapLabel(box);
    }

    private Label readonlyLabelNode(String labelText) {
        Label label = new Label(labelText + " (lecture seule)");
        AppTheme.applyBody(label);

        HBox box = new HBox(label);
        box.setAlignment(Pos.CENTER_LEFT);

        return wrapLabel(box);
    }

    private Label wrapLabel(HBox box) {
        Label wrapper = new Label();
        wrapper.setGraphic(box);
        return wrapper;
    }

    /**
     * Retourne la valeur affichée pour l'homologation.
     */
    private String displayHomologationValue(String homologationNumber) {
        return hasText(homologationNumber) ? homologationNumber.trim() : HOMOLOGATION_PENDING_TEXT;
    }
}