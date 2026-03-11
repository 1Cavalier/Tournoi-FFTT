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

    private final AppRouter nav;
    private final OrganizerDto organizer;
    private final ClubDto club;
    private final TournamentDto existingTournament;

    private final TextField nameField = new TextField();
    private final TextField address1Field = new TextField();
    private final TextField address2Field = new TextField();
    private final TextField cityField = new TextField();
    private final TextField departmentField = new TextField();

    private final ComboBox<TournamentLevel> levelBox = new ComboBox<>();
    private final ComboBox<RankingPhase> phaseBox = new ComboBox<>();

    private final DatePicker startDatePicker = new DatePicker();
    private final DatePicker endDatePicker = new DatePicker();

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

    private boolean isEditMode() {
        return existingTournament != null;
    }

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

        GridPane form = new GridPane();
        form.setHgap(14);
        form.setVgap(12);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(220);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);

        form.getColumnConstraints().addAll(col1, col2);

        int row = 0;
        addField(form, row++, "Nom du tournoi", nameField);
        addField(form, row++, "Adresse 1 du tournoi", address1Field);
        addField(form, row++, "Adresse 2 du tournoi", address2Field);
        addField(form, row++, "Ville", cityField);
        addField(form, row++, "Département", departmentField);
        addField(form, row++, "Niveau du tournoi", levelBox);
        addField(form, row++, "Phase de comptage des points", phaseBox);

        HBox datesRow = new HBox(12, startDatePicker, endDatePicker);
        datesRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(startDatePicker, Priority.ALWAYS);
        HBox.setHgrow(endDatePicker, Priority.ALWAYS);
        startDatePicker.setMaxWidth(Double.MAX_VALUE);
        endDatePicker.setMaxWidth(Double.MAX_VALUE);

        addField(form, row++, "Date du tournoi", datesRow);

        AppTheme.applyBody(daysInfoLabel);
        daysInfoLabel.setWrapText(true);
        form.add(new Label(""), 0, row);
        form.add(daysInfoLabel, 1, row);

        Button cancelButton = new Button("Annuler");
        AppTheme.styleSecondary(cancelButton);
        cancelButton.setOnAction(e -> close());

        saveButton.setText(isEditMode() ? "Enregistrer les modifications" : "Créer le tournoi");
        AppTheme.stylePrimary(saveButton);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(12, cancelButton, spacer, saveButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox cardContent = new VBox(AppTheme.SPACE_MD, form);
        VBox card = AppTheme.card(cardContent);
        card.setMaxWidth(Double.MAX_VALUE);

        root.getChildren().addAll(title, subtitle, card, actions);

        Scene scene = new Scene(root, 760, 560);
        setScene(scene);
    }

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

            if (existingTournament.startDate() != null && !existingTournament.startDate().isBlank()) {
                startDatePicker.setValue(LocalDate.parse(existingTournament.startDate()));
            }
            if (existingTournament.endDate() != null && !existingTournament.endDate().isBlank()) {
                endDatePicker.setValue(LocalDate.parse(existingTournament.endDate()));
            }
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
        }

        refreshDaysInfo();
    }

    private void selectLevel(String raw) {
        if (raw == null || raw.isBlank()) {
            levelBox.getSelectionModel().selectFirst();
            return;
        }
        try {
            levelBox.setValue(TournamentLevel.valueOf(raw));
        } catch (Exception e) {
            levelBox.getSelectionModel().selectFirst();
        }
    }

    private void selectPhase(String raw) {
        if (raw == null || raw.isBlank()) {
            phaseBox.getSelectionModel().selectFirst();
            return;
        }
        try {
            phaseBox.setValue(RankingPhase.valueOf(raw));
        } catch (Exception e) {
            phaseBox.getSelectionModel().selectFirst();
        }
    }

    private void configureDateLogic() {
        startDatePicker.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);

                if (isEditMode()) {
                    setDisable(empty);
                    return;
                }

                LocalDate today = LocalDate.now();
                setDisable(empty || item.isBefore(today));
            }
        });

        endDatePicker.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);

                LocalDate start = startDatePicker.getValue();

                if (isEditMode()) {
                    setDisable(empty || (start != null && item.isBefore(start)));
                    return;
                }

                LocalDate today = LocalDate.now();
                boolean disable = empty || item.isBefore(today);
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

    private void configureActions() {
        saveButton.setOnAction(e -> {
            if (isEditMode()) {
                onUpdateTournament();
            } else {
                onCreateTournament();
            }
        });
    }

    private void onCreateTournament() {
        try {
            String name = requireText(nameField, "Le nom du tournoi est obligatoire.");
            String address1 = optionalText(address1Field);
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

    private void onUpdateTournament() {
        try {
            String name = requireText(nameField, "Le nom du tournoi est obligatoire.");
            String address1 = optionalText(address1Field);
            String address2 = optionalText(address2Field);
            String city = requireText(cityField, "La ville est obligatoire.");
            String department = requireText(departmentField, "Le département est obligatoire.");

            TournamentLevel level = requireCombo(levelBox, "Le niveau du tournoi est obligatoire.");
            RankingPhase phase = requireCombo(phaseBox, "La phase est obligatoire.");

            LocalDate startDate = requireDate(startDatePicker, "La date de début est obligatoire.");
            LocalDate endDate = requireDate(endDatePicker, "La date de fin est obligatoire.");

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

    private void addField(GridPane grid, int row, String labelText, javafx.scene.Node field) {
        Label label = new Label(labelText + " :");
        AppTheme.applyBody(label);

        GridPane.setHgrow(field, Priority.ALWAYS);
        if (field instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
        }

        grid.add(label, 0, row);
        grid.add(field, 1, row);
    }

    private String requireText(TextField field, String message) {
        String value = field.getText();
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String optionalText(TextField field) {
        String value = field.getText();
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private LocalDate requireDate(DatePicker picker, String message) {
        LocalDate value = picker.getValue();
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private <T> T requireCombo(ComboBox<T> comboBox, String message) {
        T value = comboBox.getValue();
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String safeMessage(Exception ex) {
        return ex.getMessage() == null || ex.getMessage().isBlank()
                ? "Erreur inconnue."
                : ex.getMessage();
    }

    private String nvl(String value) {
        return value == null ? "" : value.trim();
    }
}