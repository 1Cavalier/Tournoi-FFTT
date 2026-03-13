package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.dialogs;

import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.BallProvisionPolicy;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.PlayingAreaPreset;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.TournamentLevel;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.AppRouter;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.TournamentDto;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.TournamentRegulationDto;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDateTime;
import java.util.Objects;

public class EditTournamentRegulationDialog extends Stage {

    private final AppRouter nav;
    private final TournamentDto tournament;
    private final TournamentRegulationDto regulation;

    private final TextField organizerContactNameField = new TextField();
    private final TextField organizerEmailField = new TextField();
    private final TextField organizerPhoneField = new TextField();

    private final TextField numberOfTablesField = new TextField();

    private final ComboBox<PlayingAreaPreset> playingAreaPresetBox = new ComboBox<>();
    private final TextField tablesBrandField = new TextField();
    private final TextField playingAreaLengthField = new TextField();
    private final TextField playingAreaWidthField = new TextField();
    private final Label playingAreaHintLabel = new Label();

    private final ComboBox<BallProvisionPolicy> ballProvisionPolicyBox = new ComboBox<>();
    private final TextField ballBrandAndTypeField = new TextField();

    private final TextField registrationDeadlineField = new TextField();
    private final TextField checkInDeadlineField = new TextField();
    private final TextField firstMatchesStartField = new TextField();
    private final TextField expectedEndTimeField = new TextField();

    private final Button saveButton = new Button("Enregistrer le règlement");

    public EditTournamentRegulationDialog(
            AppRouter nav,
            TournamentDto tournament,
            TournamentRegulationDto regulation) {

        this.nav = Objects.requireNonNull(nav, "nav must not be null");
        this.tournament = Objects.requireNonNull(tournament, "tournament must not be null");
        this.regulation = Objects.requireNonNull(regulation, "regulation must not be null");

        initModality(Modality.APPLICATION_MODAL);
        setTitle("Modifier le règlement");

        build();
        configureDefaults();
        configureActions();
    }

    private void build() {
        VBox page = new VBox(AppTheme.SPACE_LG);
        page.setPadding(new Insets(20));
        AppTheme.applyPage(page);

        Label title = new Label("Modifier le règlement");
        AppTheme.applyTitle(title);

        Label subtitle = new Label(
                "Complétez les informations réglementaires du tournoi. "
                        + "Le lieu général du tournoi est déjà défini dans la partie Général.");
        AppTheme.applyBody(subtitle);
        subtitle.setWrapText(true);

        VBox sections = new VBox(AppTheme.SPACE_MD);
        sections.getChildren().addAll(
                buildCollapsibleSection("Contact organisateur", buildOrganizerSection(), isOrganizerComplete()),
                buildCollapsibleSection("Aire de jeu et matériel", buildPlayingAreaSection(), isPlayingAreaComplete()),
                buildCollapsibleSection("Balles", buildBallSection(), isBallSectionComplete()),
                buildCollapsibleSection("Horaires réglementaires", buildTimingSection(), isTimingSectionComplete()));

        Button cancelButton = new Button("Annuler");
        AppTheme.styleSecondary(cancelButton);
        cancelButton.setOnAction(e -> close());

        AppTheme.stylePrimary(saveButton);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(12, cancelButton, spacer, saveButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        page.getChildren().addAll(title, subtitle, sections, actions);

        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        Scene scene = new Scene(scroll, 920, 760);
        setScene(scene);
        setMinWidth(760);
        setMinHeight(540);
    }

    private VBox buildOrganizerSection() {
        GridPane grid = createFormGrid();
        int row = 0;
        addField(grid, row++, "Nom du contact", organizerContactNameField);
        addField(grid, row++, "Email organisateur", organizerEmailField);
        addField(grid, row++, "Téléphone organisateur", organizerPhoneField);

        return new VBox(AppTheme.SPACE_MD, grid);
    }

    private VBox buildPlayingAreaSection() {
        GridPane grid = createFormGrid();
        int row = 0;
        addField(grid, row++, "Nombre de tables", numberOfTablesField);
        addField(grid, row++, "Configuration aire de jeu", playingAreaPresetBox);
        addField(grid, row++, "Rappel selon niveau", playingAreaHintLabel);
        addField(grid, row++, "Marque / type des tables", tablesBrandField);
        addField(grid, row++, "Longueur (m)", playingAreaLengthField);
        addField(grid, row++, "Largeur (m)", playingAreaWidthField);

        return new VBox(AppTheme.SPACE_MD, grid);
    }

    private VBox buildBallSection() {
        GridPane grid = createFormGrid();
        int row = 0;
        addField(grid, row++, "Marque / type de balle", ballBrandAndTypeField);
        addField(grid, row++, "Fourniture des balles", ballProvisionPolicyBox);

        return new VBox(AppTheme.SPACE_MD, grid);
    }

    private VBox buildTimingSection() {
        GridPane grid = createFormGrid();
        int row = 0;
        addField(grid, row++, "Date/heure limite inscription", registrationDeadlineField);
        addField(grid, row++, "Date/heure fin pointage", checkInDeadlineField);
        addField(grid, row++, "Date/heure début premières parties", firstMatchesStartField);
        addField(grid, row++, "Heure fin prévisionnelle", expectedEndTimeField);

        return new VBox(AppTheme.SPACE_MD, grid);
    }

    private VBox buildCollapsibleSection(String titleText, Node content, boolean initiallyComplete) {
        Label arrow = new Label("▾");
        arrow.setStyle("-fx-font-weight: bold; -fx-font-size: 18;");

        Label title = new Label(titleText);
        AppTheme.applyCardTitle(title);

        Label badge = new Label(initiallyComplete ? "Complet" : "Informations manquantes");
        badge.setStyle(AppTheme.badgeStyle(initiallyComplete ? "#2E7D32" : "#EF6C00"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(10, arrow, title, spacer, badge);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox body = new VBox(content);
        body.setManaged(true);
        body.setVisible(true);

        header.setOnMouseClicked(e -> {
            boolean visible = body.isVisible();
            body.setVisible(!visible);
            body.setManaged(!visible);
            arrow.setText(visible ? "▸" : "▾");
        });

        VBox inner = new VBox(AppTheme.SPACE_MD, header, body);
        VBox card = AppTheme.card(inner);
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private void configureDefaults() {
        organizerContactNameField.setPromptText("Nom de la personne référente");
        organizerEmailField.setPromptText("contact@club.fr");
        organizerPhoneField.setPromptText("06 00 00 00 00");

        numberOfTablesField.setPromptText("Ex : 16");
        tablesBrandField.setPromptText("Ex : Cornilleau 740");

        playingAreaLengthField.setPromptText("Ex : 12");
        playingAreaWidthField.setPromptText("Ex : 6");

        ballBrandAndTypeField.setPromptText("Ex : Butterfly R40+ blanche");
        registrationDeadlineField.setPromptText("Ex : 2026-05-10T18:00");
        checkInDeadlineField.setPromptText("Ex : 2026-05-18T08:30");
        firstMatchesStartField.setPromptText("Ex : 2026-05-18T09:00");
        expectedEndTimeField.setPromptText("Ex : 19:30");

        playingAreaPresetBox.getItems().setAll(PlayingAreaPreset.values());
        ballProvisionPolicyBox.getItems().setAll(BallProvisionPolicy.values());

        playingAreaPresetBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(PlayingAreaPreset value) {
                if (value == null)
                    return "";
                return switch (value) {
                    case DEPARTEMENTAL_STANDARD -> "Départemental standard";
                    case REGIONAL_STANDARD -> "Régional standard";
                    case NATIONAL_STANDARD -> "National standard";
                    case INTERNATIONAL_STANDARD -> "International standard";
                    case CUSTOM -> "Personnalisé";
                };
            }

            @Override
            public PlayingAreaPreset fromString(String string) {
                return null;
            }
        });

        ballProvisionPolicyBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(BallProvisionPolicy value) {
                if (value == null)
                    return "";
                return prettyBallProvisionPolicy(value);
            }

            @Override
            public BallProvisionPolicy fromString(String string) {
                return null;
            }
        });

        organizerContactNameField.setText(nvl(regulation.organizerContactName()));
        organizerEmailField.setText(nvl(regulation.organizerEmail()));
        organizerPhoneField.setText(nvl(regulation.organizerPhone()));

        numberOfTablesField
                .setText(regulation.numberOfTables() == null ? "" : String.valueOf(regulation.numberOfTables()));

        selectPlayingAreaPreset();
        tablesBrandField.setText(nvl(regulation.playingAreaInfoText()));
        playingAreaLengthField.setText(regulation.playingAreaLengthMeters() == null ? ""
                : String.valueOf(regulation.playingAreaLengthMeters()));
        playingAreaWidthField.setText(
                regulation.playingAreaWidthMeters() == null ? "" : String.valueOf(regulation.playingAreaWidthMeters()));

        selectBallProvisionPolicy();
        ballBrandAndTypeField.setText(nvl(regulation.ballBrandAndType()));

        registrationDeadlineField.setText(nvl(regulation.registrationDeadline()));
        checkInDeadlineField.setText(nvl(regulation.checkInDeadline()));
        firstMatchesStartField.setText(nvl(regulation.firstMatchesStart()));
        expectedEndTimeField.setText(nvl(regulation.expectedEndTime()));

        refreshPlayingAreaHint();
        applyPlayingAreaPresetDefaults();

        playingAreaPresetBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            refreshPlayingAreaHint();
            applyPlayingAreaPresetDefaults();
        });
    }

    private void configureActions() {
        saveButton.setOnAction(e -> onSave());
    }

    private void onSave() {
        try {
            Integer numberOfTables = parseOptionalInteger(numberOfTablesField.getText(),
                    "Le nombre de tables est invalide.");
            Integer playingAreaLength = parseOptionalInteger(playingAreaLengthField.getText(),
                    "La longueur de l'aire de jeu est invalide.");
            Integer playingAreaWidth = parseOptionalInteger(playingAreaWidthField.getText(),
                    "La largeur de l'aire de jeu est invalide.");

            validateOptionalDateTime(registrationDeadlineField.getText(), "La date limite d'inscription est invalide.");
            validateOptionalDateTime(checkInDeadlineField.getText(), "La date/heure de fin de pointage est invalide.");
            validateOptionalDateTime(firstMatchesStartField.getText(),
                    "La date/heure de début des premières parties est invalide.");

            TournamentRegulationDto updated = new TournamentRegulationDto(
                    regulation.tournamentId(),

                    optional(organizerContactNameField.getText()),
                    optional(organizerEmailField.getText()),
                    optional(organizerPhoneField.getText()),

                    regulation.venueName(),
                    regulation.venueStreet(),
                    regulation.venueZip(),
                    regulation.venueCity(),

                    numberOfTables,

                    playingAreaPresetBox.getValue() == null ? null : playingAreaPresetBox.getValue().name(),
                    optional(tablesBrandField.getText()),
                    playingAreaLength,
                    playingAreaWidth,
                    null,

                    optional(ballBrandAndTypeField.getText()),
                    ballProvisionPolicyBox.getValue() == null ? null : ballProvisionPolicyBox.getValue().name(),

                    optional(registrationDeadlineField.getText()),
                    optional(checkInDeadlineField.getText()),
                    optional(firstMatchesStartField.getText()),
                    optional(expectedEndTimeField.getText()),

                    regulation.createdAt(),
                    regulation.updatedAt());

            nav.tournamentService().updateRegulation(updated);
            close();

        } catch (IllegalArgumentException ex) {
            showError("Validation", ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Erreur", safeMessage(ex));
        }
    }

    private void selectPlayingAreaPreset() {
        if (!isBlank(regulation.playingAreaPreset())) {
            try {
                playingAreaPresetBox.setValue(PlayingAreaPreset.valueOf(regulation.playingAreaPreset()));
                return;
            } catch (Exception ignored) {
            }
        }
        playingAreaPresetBox.setValue(inferPresetFromTournamentLevel());
    }

    private void selectBallProvisionPolicy() {
        if (isBlank(regulation.ballProvisionPolicy())) {
            return;
        }
        try {
            ballProvisionPolicyBox.setValue(BallProvisionPolicy.valueOf(regulation.ballProvisionPolicy()));
        } catch (Exception ignored) {
        }
    }

    private void refreshPlayingAreaHint() {
        PlayingAreaPreset preset = playingAreaPresetBox.getValue();
        String text = switch (preset == null ? inferPresetFromTournamentLevel() : preset) {
            case DEPARTEMENTAL_STANDARD ->
                "Aires de jeu conformes à la réglementation FFTT (configuration départementale).";
            case REGIONAL_STANDARD ->
                "Aires de jeu conformes à la réglementation FFTT (configuration régionale).";
            case NATIONAL_STANDARD ->
                "Aires de jeu conformes à la réglementation FFTT (configuration nationale).";
            case INTERNATIONAL_STANDARD ->
                "Aires de jeu conformes aux standards internationaux (configuration internationale).";
            case CUSTOM ->
                "Aires de jeu : configuration personnalisée (voir détails).";
        };

        AppTheme.applyBody(playingAreaHintLabel);
        playingAreaHintLabel.setWrapText(true);
        playingAreaHintLabel.setText(text);
    }

    private void applyPlayingAreaPresetDefaults() {
        PlayingAreaPreset preset = playingAreaPresetBox.getValue();
        if (preset == null) {
            return;
        }

        boolean custom = preset == PlayingAreaPreset.CUSTOM;

        playingAreaLengthField.setDisable(!custom);
        playingAreaWidthField.setDisable(!custom);

        if (!custom) {
            switch (preset) {
                case DEPARTEMENTAL_STANDARD -> {
                    playingAreaLengthField.setText("10");
                    playingAreaWidthField.setText("5");
                }
                case REGIONAL_STANDARD -> {
                    playingAreaLengthField.setText("10");
                    playingAreaWidthField.setText("5.5");
                }
                case NATIONAL_STANDARD -> {
                    playingAreaLengthField.setText("12");
                    playingAreaWidthField.setText("6");
                }
                case INTERNATIONAL_STANDARD -> {
                    playingAreaLengthField.setText("14");
                    playingAreaWidthField.setText("7");
                }
                case CUSTOM -> {
                }
            }
        } else {
            if (isBlank(playingAreaLengthField.getText())) {
                playingAreaLengthField.clear();
            }
            if (isBlank(playingAreaWidthField.getText())) {
                playingAreaWidthField.clear();
            }
        }
    }

    private PlayingAreaPreset inferPresetFromTournamentLevel() {
        TournamentLevel level = parseTournamentLevel(tournament.level());
        if (level == null) {
            return PlayingAreaPreset.CUSTOM;
        }
        return switch (level) {
            case DEPARTEMENTAL -> PlayingAreaPreset.DEPARTEMENTAL_STANDARD;
            case REGIONAL -> PlayingAreaPreset.REGIONAL_STANDARD;
            case NATIONAL_A, NATIONAL_B -> PlayingAreaPreset.NATIONAL_STANDARD;
            case INTERNATIONAL -> PlayingAreaPreset.INTERNATIONAL_STANDARD;
        };
    }

    private TournamentLevel parseTournamentLevel(String raw) {
        if (isBlank(raw)) {
            return null;
        }
        try {
            return TournamentLevel.valueOf(raw.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isOrganizerComplete() {
        return !isBlank(organizerContactNameField.getText())
                && !isBlank(organizerEmailField.getText())
                && !isBlank(organizerPhoneField.getText());
    }

    private boolean isPlayingAreaComplete() {
        return !isBlank(numberOfTablesField.getText())
                && playingAreaPresetBox.getValue() != null
                && !isBlank(playingAreaLengthField.getText())
                && !isBlank(playingAreaWidthField.getText());
    }

    private boolean isBallSectionComplete() {
        return !isBlank(ballBrandAndTypeField.getText())
                && ballProvisionPolicyBox.getValue() != null;
    }

    private boolean isTimingSectionComplete() {
        return !isBlank(registrationDeadlineField.getText())
                && !isBlank(checkInDeadlineField.getText())
                && !isBlank(firstMatchesStartField.getText())
                && !isBlank(expectedEndTimeField.getText());
    }

    private String prettyBallProvisionPolicy(BallProvisionPolicy value) {
        String raw = value.name().toLowerCase().replace('_', ' ');
        if (raw.isEmpty()) {
            return "";
        }
        return Character.toUpperCase(raw.charAt(0)) + raw.substring(1);
    }

    private GridPane createFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(12);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(230);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().addAll(col1, col2);
        return grid;
    }

    private void addField(GridPane grid, int row, String labelText, Node field) {
        Label label = new Label(labelText + " :");
        AppTheme.applyBody(label);

        GridPane.setHgrow(field, Priority.ALWAYS);
        if (field instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
        }

        grid.add(label, 0, row);
        grid.add(field, 1, row);
    }

    private Integer parseOptionalInteger(String raw, String message) {
        String value = optional(raw);
        if (value == null) {
            return null;
        }
        try {
            if (value.contains(".")) {
                throw new NumberFormatException();
            }
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(message);
        }
    }

    private void validateOptionalDateTime(String raw, String message) {
        String value = optional(raw);
        if (value == null) {
            return;
        }
        try {
            LocalDateTime.parse(value);
        } catch (Exception e) {
            throw new IllegalArgumentException(message);
        }
    }

    private String optional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String nvl(String value) {
        return value == null ? "" : value.trim();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message == null || message.isBlank() ? "Erreur inconnue." : message);
        alert.showAndWait();
    }

    private String safeMessage(Exception ex) {
        return ex.getMessage() == null || ex.getMessage().isBlank()
                ? "Impossible d'enregistrer le règlement."
                : ex.getMessage();
    }
}