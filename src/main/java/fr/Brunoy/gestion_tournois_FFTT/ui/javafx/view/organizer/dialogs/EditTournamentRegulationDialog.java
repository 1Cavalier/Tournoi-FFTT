package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.dialogs;

import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.BallProvisionPolicy;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.PlayingAreaPreset;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.TournamentLevel;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.AppRouter;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.TournamentDto;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.TournamentOfficialAssignmentDto;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.TournamentRegulationDto;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
import java.util.List;
import java.util.Objects;

public class EditTournamentRegulationDialog extends Stage {

    private enum PlayingAreaChoice {
        STANDARD,
        CUSTOM
    }

    private final AppRouter nav;
    private final TournamentDto tournament;
    private final TournamentRegulationDto regulation;

    private final TextField organizerContactNameField = new TextField();
    private final TextField organizerEmailField = new TextField();
    private final TextField organizerPhoneField = new TextField();

    private final TextField numberOfTablesField = new TextField();

    private final ComboBox<PlayingAreaChoice> playingAreaChoiceBox = new ComboBox<>();
    private final TextField tablesBrandField = new TextField();
    private final TextField playingAreaLengthField = new TextField();
    private final TextField playingAreaWidthField = new TextField();
    private final Label playingAreaHintLabel = new Label();

    private final ComboBox<BallProvisionPolicy> ballProvisionPolicyBox = new ComboBox<>();
    private final TextField ballBrandAndTypeField = new TextField();

    private final TextField registrationOpenTimeField = new TextField();
    private final TextField registrationDeadlineField = new TextField();
    private final TextField gymOpenTimeField = new TextField();

    private final TextField judgeSearchField = new TextField();
    private final TextField refereeSearchField = new TextField();

    private final TableView<OfficialSearchRow> judgeTable = new TableView<>();
    private final TableView<OfficialSearchRow> refereeTable = new TableView<>();

    private final Label officialsRequirementLabel = new Label();
    private final Button saveButton = new Button("Enregistrer le règlement");

    private Label organizerSectionBadge;
    private Label playingAreaSectionBadge;
    private Label ballSectionBadge;
    private Label timingSectionBadge;
    private Label officialsSectionBadge;

    public EditTournamentRegulationDialog(
            AppRouter nav,
            TournamentDto tournament,
            TournamentRegulationDto regulation) {

        this.nav = Objects.requireNonNull(nav, "nav must not be null");
        this.tournament = Objects.requireNonNull(tournament, "tournament must not be null");
        this.regulation = Objects.requireNonNull(regulation, "regulation must not be null");

        initModality(Modality.APPLICATION_MODAL);
        initOwner(nav.primaryStage());
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

        organizerSectionBadge = createSectionBadge(false);
        playingAreaSectionBadge = createSectionBadge(false);
        ballSectionBadge = createSectionBadge(false);
        timingSectionBadge = createSectionBadge(false);
        officialsSectionBadge = createSectionBadge(false);

        VBox sections = new VBox(AppTheme.SPACE_MD);
        sections.getChildren().addAll(
                buildCollapsibleSection("Contact organisateur", buildOrganizerSection(), organizerSectionBadge),
                buildCollapsibleSection("Aire de jeu et matériel", buildPlayingAreaSection(), playingAreaSectionBadge),
                buildCollapsibleSection("Balles", buildBallSection(), ballSectionBadge),
                buildCollapsibleSection("Horaires réglementaires", buildTimingSection(), timingSectionBadge),
                buildCollapsibleSection("Juge-arbitre et arbitres", buildOfficialsSection(), officialsSectionBadge));

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

        setScene(new Scene(scroll));
        AppTheme.applyLargeDialogWindow(this);
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
        addField(grid, row++, "Aire de jeu", playingAreaChoiceBox);
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
        addField(grid, row++, "Ouverture des inscriptions", registrationOpenTimeField);
        addField(grid, row++, "Fermeture des inscriptions", registrationDeadlineField);
        addField(grid, row++, "Ouverture du gymnase", gymOpenTimeField);
        return new VBox(AppTheme.SPACE_MD, grid);
    }

    private VBox buildOfficialsSection() {
        Label introTitle = new Label("Juge-arbitre et arbitres");
        AppTheme.applyCardTitle(introTitle);

        AppTheme.applyBody(officialsRequirementLabel);
        officialsRequirementLabel.setWrapText(true);
        officialsRequirementLabel.setStyle(
                "-fx-padding: 10 12 10 12;"
                        + "-fx-background-color: white;"
                        + "-fx-border-color: #B0BEC5;"
                        + "-fx-border-radius: 10;"
                        + "-fx-background-radius: 10;");

        configureOfficialTable(judgeTable, true);
        configureOfficialTable(refereeTable, false);

        Label judgeTitle = new Label("Juges-arbitres");
        AppTheme.applyCardTitle(judgeTitle);

        Label refereeTitle = new Label("Arbitres");
        AppTheme.applyCardTitle(refereeTitle);

        judgeSearchField.setPromptText("Nom, prénom ou licence");
        refereeSearchField.setPromptText("Nom, prénom ou licence");

        Button judgeSearchButton = new Button("Rechercher");
        Button refereeSearchButton = new Button("Rechercher");
        AppTheme.styleSecondary(judgeSearchButton);
        AppTheme.styleSecondary(refereeSearchButton);

        HBox judgeSearchRow = new HBox(8, judgeSearchField, judgeSearchButton);
        HBox.setHgrow(judgeSearchField, Priority.ALWAYS);

        HBox refereeSearchRow = new HBox(8, refereeSearchField, refereeSearchButton);
        HBox.setHgrow(refereeSearchField, Priority.ALWAYS);

        VBox judgeBox = new VBox(10, judgeTitle, judgeSearchRow, judgeTable);
        VBox refereeBox = new VBox(10, refereeTitle, refereeSearchRow, refereeTable);

        VBox.setVgrow(judgeTable, Priority.ALWAYS);
        VBox.setVgrow(refereeTable, Priority.ALWAYS);

        HBox tablesRow = new HBox(16, judgeBox, refereeBox);
        HBox.setHgrow(judgeBox, Priority.ALWAYS);
        HBox.setHgrow(refereeBox, Priority.ALWAYS);

        judgeBox.setMaxWidth(Double.MAX_VALUE);
        refereeBox.setMaxWidth(Double.MAX_VALUE);

        judgeTable.setPrefHeight(260);
        refereeTable.setPrefHeight(260);

        judgeSearchButton.setOnAction(e -> onSearchJudges());
        refereeSearchButton.setOnAction(e -> onSearchReferees());

        judgeSearchField.setOnAction(e -> onSearchJudges());
        refereeSearchField.setOnAction(e -> onSearchReferees());

        return new VBox(14, introTitle, officialsRequirementLabel, tablesRow);
    }

    private void configureOfficialTable(TableView<OfficialSearchRow> table, boolean judgeMode) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPlaceholder(new Label("Aucun résultat pour le moment."));

        TableColumn<OfficialSearchRow, String> lastNameCol = new TableColumn<>("Nom");
        lastNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().lastName()));

        TableColumn<OfficialSearchRow, String> firstNameCol = new TableColumn<>("Prénom");
        firstNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().firstName()));

        TableColumn<OfficialSearchRow, String> licenseCol = new TableColumn<>("Licence");
        licenseCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().licenseNumber()));

        TableColumn<OfficialSearchRow, String> gradeCol = new TableColumn<>(judgeMode ? "Grade JA" : "Grade arbitre");
        gradeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().grade()));

        table.getColumns().setAll(lastNameCol, firstNameCol, licenseCol, gradeCol);
    }

    private VBox buildCollapsibleSection(String titleText, Node content, Label badge) {
        Label arrow = new Label("▾");
        arrow.setStyle("-fx-font-weight: bold; -fx-font-size: 20;");

        Label title = new Label(titleText);
        AppTheme.applyCardTitle(title);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(10, arrow, title, spacer, badge);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-cursor: hand;");

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

    private Label createSectionBadge(boolean complete) {
        Label badge = new Label();
        updateSectionBadge(badge, complete);
        return badge;
    }

    private void updateSectionBadge(Label badge, boolean complete) {
        badge.setText(complete ? "Complet" : "Informations manquantes");
        badge.setStyle(AppTheme.badgeStyle(complete ? "#2E7D32" : "#EF6C00"));
    }

    private void refreshSectionBadges() {
        updateSectionBadge(organizerSectionBadge, isOrganizerComplete());
        updateSectionBadge(playingAreaSectionBadge, isPlayingAreaComplete());
        updateSectionBadge(ballSectionBadge, isBallSectionComplete());
        updateSectionBadge(timingSectionBadge, isTimingSectionComplete());
        updateSectionBadge(officialsSectionBadge, true);
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
        registrationOpenTimeField.setPromptText("Ex : 2026-05-01T09:00");
        registrationDeadlineField.setPromptText("Ex : 2026-05-10T18:00");
        gymOpenTimeField.setPromptText("Ex : 2026-05-18T07:30");

        playingAreaChoiceBox.getItems().setAll(PlayingAreaChoice.values());
        playingAreaChoiceBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(PlayingAreaChoice value) {
                if (value == null) {
                    return "";
                }
                return switch (value) {
                    case STANDARD -> "Standard recommandé";
                    case CUSTOM -> "Personnalisé";
                };
            }

            @Override
            public PlayingAreaChoice fromString(String string) {
                return null;
            }
        });

        ballProvisionPolicyBox.getItems().setAll(BallProvisionPolicy.values());
        ballProvisionPolicyBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(BallProvisionPolicy value) {
                return value == null ? "" : prettyBallProvisionPolicy(value);
            }

            @Override
            public BallProvisionPolicy fromString(String string) {
                return null;
            }
        });

        organizerContactNameField.setText(nvl(regulation.organizerContactName()));
        organizerEmailField.setText(nvl(regulation.organizerEmail()));
        organizerPhoneField.setText(nvl(regulation.organizerPhone()));

        numberOfTablesField.setText(
                regulation.numberOfTables() == null ? "" : String.valueOf(regulation.numberOfTables()));

        selectPlayingAreaChoice();
        tablesBrandField.setText(nvl(regulation.playingAreaInfoText()));
        playingAreaLengthField.setText(
                regulation.playingAreaLengthMeters() == null ? ""
                        : String.valueOf(regulation.playingAreaLengthMeters()));
        playingAreaWidthField.setText(
                regulation.playingAreaWidthMeters() == null ? "" : String.valueOf(regulation.playingAreaWidthMeters()));

        selectBallProvisionPolicy();
        ballBrandAndTypeField.setText(nvl(regulation.ballBrandAndType()));

        registrationOpenTimeField.setText(nvl(regulation.registrationOpenTime()));
        registrationDeadlineField.setText(nvl(regulation.registrationDeadline()));
        gymOpenTimeField.setText(nvl(regulation.gymOpenTime()));

        refreshPlayingAreaHint();
        applyPlayingAreaDefaults();
        refreshOfficialsRequirementText();

        organizerContactNameField.textProperty().addListener((obs, o, n) -> refreshSectionBadges());
        organizerEmailField.textProperty().addListener((obs, o, n) -> refreshSectionBadges());
        organizerPhoneField.textProperty().addListener((obs, o, n) -> refreshSectionBadges());

        numberOfTablesField.textProperty().addListener((obs, o, n) -> refreshSectionBadges());
        tablesBrandField.textProperty().addListener((obs, o, n) -> refreshSectionBadges());
        playingAreaLengthField.textProperty().addListener((obs, o, n) -> refreshSectionBadges());
        playingAreaWidthField.textProperty().addListener((obs, o, n) -> refreshSectionBadges());

        playingAreaChoiceBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            refreshPlayingAreaHint();
            applyPlayingAreaDefaults();
            refreshSectionBadges();
        });

        ballBrandAndTypeField.textProperty().addListener((obs, o, n) -> refreshSectionBadges());
        ballProvisionPolicyBox.valueProperty().addListener((obs, o, n) -> refreshSectionBadges());

        registrationOpenTimeField.textProperty().addListener((obs, o, n) -> refreshSectionBadges());
        registrationDeadlineField.textProperty().addListener((obs, o, n) -> refreshSectionBadges());
        gymOpenTimeField.textProperty().addListener((obs, o, n) -> refreshSectionBadges());

        refreshSectionBadges();
    }

    private void configureActions() {
        saveButton.setOnAction(e -> onSave());
    }

    private void onSave() {
        try {
            Integer numberOfTables = parseOptionalInteger(
                    numberOfTablesField.getText(),
                    "Le nombre de tables est invalide.");
            Integer playingAreaLength = parseOptionalInteger(
                    playingAreaLengthField.getText(),
                    "La longueur de l'aire de jeu est invalide.");
            Integer playingAreaWidth = parseOptionalInteger(
                    playingAreaWidthField.getText(),
                    "La largeur de l'aire de jeu est invalide.");

            validateOptionalDateTime(
                    registrationOpenTimeField.getText(),
                    "La date/heure d'ouverture des inscriptions est invalide.");
            validateOptionalDateTime(
                    registrationDeadlineField.getText(),
                    "La date/heure de fermeture des inscriptions est invalide.");
            validateOptionalDateTime(
                    gymOpenTimeField.getText(),
                    "La date/heure d'ouverture du gymnase est invalide.");

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

                    resolveActualPreset().name(),
                    optional(tablesBrandField.getText()),
                    playingAreaLength,
                    playingAreaWidth,
                    null,

                    optional(ballBrandAndTypeField.getText()),
                    ballProvisionPolicyBox.getValue() == null ? null : ballProvisionPolicyBox.getValue().name(),

                    optional(registrationOpenTimeField.getText()),
                    optional(registrationDeadlineField.getText()),
                    optional(gymOpenTimeField.getText()),

                    regulation.requiredJudgeGrade(),
                    regulation.recommendedJudgeCount(),
                    regulation.recommendedRefereeGrade(),
                    regulation.recommendedRefereeCount(),
                    safeAssignedOfficials(),

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

    private List<TournamentOfficialAssignmentDto> safeAssignedOfficials() {
        return regulation.assignedOfficials() == null ? List.of() : regulation.assignedOfficials();
    }

    private void onSearchJudges() {
        String query = optional(judgeSearchField.getText());

        if (query == null) {
            judgeTable.setItems(FXCollections.observableArrayList());
            return;
        }

        judgeTable.setItems(FXCollections.observableArrayList(
                new OfficialSearchRow("DUPONT", "Martin", "123456", "JA3"),
                new OfficialSearchRow("BERNARD", "Sophie", "654321", "JAN")));
    }

    private void onSearchReferees() {
        String query = optional(refereeSearchField.getText());

        if (query == null) {
            refereeTable.setItems(FXCollections.observableArrayList());
            return;
        }

        refereeTable.setItems(FXCollections.observableArrayList(
                new OfficialSearchRow("MARTIN", "Lucas", "111222", "REGIONAL"),
                new OfficialSearchRow("ROBERT", "Emma", "333444", "NATIONAL")));
    }

    private void refreshOfficialsRequirementText() {
        TournamentLevel level = parseTournamentLevel(tournament.level());

        String text;
        if (level == null) {
            text = "Le niveau du tournoi n'est pas reconnu. Les exigences JA / arbitres ne peuvent pas être déterminées automatiquement.";
        } else {
            text = switch (level) {
                case DEPARTEMENTAL ->
                    "Tournoi départemental : au minimum 1 juge-arbitre JA3. "
                            + "Aucun arbitre n'est imposé automatiquement, mais il est conseillé de prévoir au moins 2 arbitres pour les demi-finales et finales.";
                case REGIONAL ->
                    "Tournoi régional : au minimum 1 juge-arbitre JA3. "
                            + "Pour l'organisation, il est conseillé de prévoir au moins 2 arbitres régionaux.";
                case NATIONAL_B ->
                    "Tournoi national B : au minimum 1 juge-arbitre JA3. "
                            + "Pour l'organisation, il est conseillé d'avoir un second JA3 ou JAN en appui, ainsi que 2 arbitres régionaux et 1 arbitre national pour la finale.";
                case NATIONAL_A ->
                    "Tournoi national A : au minimum 1 juge-arbitre JAN. "
                            + "Pour l'organisation, il est conseillé d'avoir 1 à 2 JA supplémentaires en appui ainsi qu'au moins 2 arbitres nationaux.";
                case INTERNATIONAL ->
                    "Tournoi international : au minimum 1 juge-arbitre JAN. "
                            + "Pour l'organisation, il est conseillé de prévoir 2 JAN ou JAI et au moins 4 arbitres nationaux.";
            };
        }

        officialsRequirementLabel.setText(text);
    }

    private void selectPlayingAreaChoice() {
        if ("CUSTOM".equalsIgnoreCase(nvl(regulation.playingAreaPreset()))) {
            playingAreaChoiceBox.setValue(PlayingAreaChoice.CUSTOM);
        } else {
            playingAreaChoiceBox.setValue(PlayingAreaChoice.STANDARD);
        }
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
        String text = switch (resolveActualPreset()) {
            case DEPARTEMENTAL_STANDARD ->
                "Aires de jeu conformes à la réglementation FFTT (configuration départementale).";
            case REGIONAL_STANDARD ->
                "Aires de jeu conformes à la réglementation FFTT (configuration régionale).";
            case NATIONAL_STANDARD ->
                "Aires de jeu conformes à la réglementation FFTT (configuration nationale).";
            case INTERNATIONAL_STANDARD ->
                "Aires de jeu conformes aux standards internationaux (configuration internationale).";
            case CUSTOM ->
                "Aires de jeu personnalisées : renseignez librement les dimensions.";
        };

        AppTheme.applyBody(playingAreaHintLabel);
        playingAreaHintLabel.setWrapText(true);
        playingAreaHintLabel.setText(text);
    }

    private void applyPlayingAreaDefaults() {
        boolean custom = playingAreaChoiceBox.getValue() == PlayingAreaChoice.CUSTOM;

        playingAreaLengthField.setDisable(!custom);
        playingAreaWidthField.setDisable(!custom);

        if (!custom) {
            switch (inferPresetFromTournamentLevel()) {
                case DEPARTEMENTAL_STANDARD, REGIONAL_STANDARD -> {
                    playingAreaLengthField.setText("10");
                    playingAreaWidthField.setText("5");
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
                    playingAreaLengthField.clear();
                    playingAreaWidthField.clear();
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

    private PlayingAreaPreset resolveActualPreset() {
        return playingAreaChoiceBox.getValue() == PlayingAreaChoice.CUSTOM
                ? PlayingAreaPreset.CUSTOM
                : inferPresetFromTournamentLevel();
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
                && playingAreaChoiceBox.getValue() != null
                && !isBlank(playingAreaLengthField.getText())
                && !isBlank(playingAreaWidthField.getText());
    }

    private boolean isBallSectionComplete() {
        return !isBlank(ballBrandAndTypeField.getText())
                && ballProvisionPolicyBox.getValue() != null;
    }

    private boolean isTimingSectionComplete() {
        return !isBlank(registrationOpenTimeField.getText())
                && !isBlank(registrationDeadlineField.getText())
                && !isBlank(gymOpenTimeField.getText());
    }

    private String prettyBallProvisionPolicy(BallProvisionPolicy value) {
        String raw = value.name();
        return switch (raw) {
            case "CLUB_PROVIDED", "ORGANIZER_PROVIDED", "CLUB" -> "Club fournit";
            case "PARTICIPANT_PROVIDED", "PLAYER_PROVIDED", "PARTICIPANT" -> "Participant fournit";
            case "MIXED", "SHARED", "MIXED_CLUB_PARTICIPANT" -> "Mixte club et participant";
            default -> {
                String text = raw.toLowerCase().replace('_', ' ');
                if (text.isEmpty()) {
                    yield "";
                }
                yield Character.toUpperCase(text.charAt(0)) + text.substring(1);
            }
        };
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
        alert.initOwner(nav.primaryStage());
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

    private record OfficialSearchRow(
            String lastName,
            String firstName,
            String licenseNumber,
            String grade) {
    }
}