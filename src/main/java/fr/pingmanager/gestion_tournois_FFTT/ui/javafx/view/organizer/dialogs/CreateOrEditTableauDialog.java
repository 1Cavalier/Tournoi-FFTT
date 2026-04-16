package fr.pingmanager.gestion_tournois_FFTT.ui.javafx.view.organizer.dialogs;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;

import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.enums.GenderPolicy;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.enums.TableauPointsRuleType;
import fr.pingmanager.gestion_tournois_FFTT.domain.refdata.AgeCategory;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.app.AppRouter;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.PrizeRewardTypeDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.PrizeTierDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TableauDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TournamentRegulationDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;

public class CreateOrEditTableauDialog extends Stage {

    private enum AgePolicyChoice {
        ANY,
        RANGE,
        ALLOWED_SET
    }

    private final String tournamentId;
    private final List<LocalDate> tournamentDays;
    private final TournamentRegulationDto regulation;
    private final TableauDto existingTableau;

    private final TextField codeField = new TextField();
    private final TextField designationField = new TextField();
    private final CheckBox autoDesignationCheck = new CheckBox("Nom automatique : Tableau + code");
    private final DatePicker datePicker = new DatePicker();

    private final ComboBox<GenderPolicy> genderPolicyBox = new ComboBox<>();

    private final ComboBox<AgePolicyChoice> agePolicyBox = new ComboBox<>();
    private final ComboBox<AgeCategory> ageMinBox = new ComboBox<>();
    private final ComboBox<AgeCategory> ageMaxBox = new ComboBox<>();
    private final VBox ageRangeBox = new VBox();
    private final FlowPane ageAllowedPane = new FlowPane();
    private final VBox ageAllowedSetBox = new VBox();

    private final TextField checkInEndField = new TextField();
    private final TextField startTimeField = new TextField();

    private final ComboBox<TableauPointsRuleType> pointsRuleTypeBox = new ComboBox<>();
    private final TextField minPointsField = new TextField();
    private final TextField maxPointsField = new TextField();
    private final VBox pointsDynamicBox = new VBox();

    private final Spinner<Integer> maxPlayersSpinner = new Spinner<>(1, 512, 32);
    private final Spinner<Integer> waitlistCapacitySpinner = new Spinner<>(0, 512, 0);

    private final TextField prepaidFeeField = new TextField();
    private final TextField onSiteFeeField = new TextField();
    private final Label maxPlayersHintLabel = new Label();

    private final VBox prizeTiersContainer = new VBox(AppTheme.SPACE_SM);

    private final Label messageLabel = new Label();
    private final Button saveButton = new Button();

    private final EnumMap<AgeCategory, CheckBox> ageCategoryChecks = new EnumMap<>(AgeCategory.class);

    private TableauDto result;

    public CreateOrEditTableauDialog(
            AppRouter nav,
            String tournamentId,
            List<LocalDate> tournamentDays,
            TournamentRegulationDto regulation,
            TableauDto existingTableau) {

        Objects.requireNonNull(nav, "nav must not be null");
        this.tournamentId = Objects.requireNonNull(tournamentId, "tournamentId must not be null");
        this.tournamentDays = List.copyOf(Objects.requireNonNull(tournamentDays, "tournamentDays must not be null"));
        this.regulation = Objects.requireNonNull(regulation, "regulation must not be null");
        this.existingTableau = existingTableau;

        initModality(Modality.APPLICATION_MODAL);
        initOwner(nav.primaryStage());
        setTitle(isEditMode() ? "Modifier un tableau" : "Créer un tableau");

        build();
        configureDefaults();
        configureActions();
    }

    public TableauDto result() {
        return result;
    }

    private boolean isEditMode() {
        return existingTableau != null;
    }

    private void build() {
        VBox page = new VBox(AppTheme.SPACE_LG);
        page.setPadding(new Insets(20));
        AppTheme.applyPage(page);

        Label title = new Label(isEditMode() ? "Modifier un tableau" : "Créer un tableau");
        AppTheme.applyTitle(title);

        Label subtitle = new Label(
                "Définissez les règles générales du tableau, ses paramètres sportifs, ses horaires et ses dotations.");
        AppTheme.applyBody(subtitle);
        subtitle.setWrapText(true);

        VBox generalCard = AppTheme.card(buildGeneralSection());
        VBox sportCard = AppTheme.card(buildSportSection());
        VBox prizeCard = AppTheme.card(buildPrizeSection());

        initMessageLabel();

        Button cancelButton = new Button("Annuler");
        AppTheme.styleSecondary(cancelButton);
        cancelButton.setOnAction(e -> close());

        saveButton.setText(isEditMode() ? "Enregistrer" : "Créer");
        AppTheme.stylePrimary(saveButton);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(12, messageLabel, spacer, cancelButton, saveButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        page.getChildren().addAll(title, subtitle, generalCard, sportCard, prizeCard, actions);

        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        setScene(new Scene(scroll));
        AppTheme.applyLargeDialogWindow(this);
    }

    private VBox buildGeneralSection() {
        Label sectionTitle = new Label("Identification du tableau");
        AppTheme.applyCardTitle(sectionTitle);

        GridPane grid = createFormGrid();
        int row = 0;

        addField(grid, row++, "Code", codeField);
        addField(grid, row++, "Nom / désignation", designationField);
        addField(grid, row++, "", autoDesignationCheck);
        addField(grid, row++, "Date", datePicker);
        addField(grid, row++, "Genre", genderPolicyBox);
        addField(grid, row++, "Politique d'âge", agePolicyBox);
        addField(grid, row++, "", ageRangeBox);
        addField(grid, row++, "", ageAllowedSetBox);
        addField(grid, row++, "Fin du pointage", checkInEndField);
        addField(grid, row++, "Début des matchs", startTimeField);

        return new VBox(AppTheme.SPACE_MD, sectionTitle, grid);
    }

    private VBox buildSportSection() {
        Label sectionTitle = new Label("Paramètres sportifs");
        AppTheme.applyCardTitle(sectionTitle);

        GridPane grid = createFormGrid();
        int row = 0;

        addField(grid, row++, "Règle de points", pointsRuleTypeBox);
        addField(grid, row++, "", pointsDynamicBox);
        addField(grid, row++, "Nombre max de joueurs", maxPlayersSpinner);

        AppTheme.applyBody(maxPlayersHintLabel);
        maxPlayersHintLabel.setWrapText(true);
        grid.add(new Label(""), 0, row);
        grid.add(maxPlayersHintLabel, 1, row++);

        addField(grid, row++, "Capacité liste d'attente", waitlistCapacitySpinner);
        addField(grid, row++, "Tarif préinscription / en ligne (€)", prepaidFeeField);
        addField(grid, row++, "Tarif sur place (€)", onSiteFeeField);

        return new VBox(AppTheme.SPACE_MD, sectionTitle, grid);
    }

    private VBox buildPrizeSection() {
        Label sectionTitle = new Label("Dotations");
        AppTheme.applyCardTitle(sectionTitle);

        Label hint = new Label(
                "Ajoutez les gains si besoin. Vous pouvez laisser cette partie vide et la compléter plus tard.");
        AppTheme.applyBody(hint);
        hint.setWrapText(true);

        Button addTierButton = new Button("Ajouter un palier");
        AppTheme.styleSecondary(addTierButton);
        addTierButton.setOnAction(e -> addPrizeTierRow(null));

        return new VBox(AppTheme.SPACE_MD, sectionTitle, hint, prizeTiersContainer, addTierButton);
    }

    private void configureDefaults() {
        codeField.setPromptText("Ex : A");
        designationField.setPromptText("Ex : Tableau C");
        autoDesignationCheck.setSelected(true);
        designationField.setDisable(true);

        checkInEndField.setPromptText("Ex : 08:30");
        startTimeField.setPromptText("Ex : 09:00");

        prepaidFeeField.setPromptText("Ex : 8");
        onSiteFeeField.setPromptText("Ex : 10");

        genderPolicyBox.setItems(FXCollections.observableArrayList(GenderPolicy.values()));
        genderPolicyBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(GenderPolicy item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : prettyGender(item));
            }
        });
        genderPolicyBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(GenderPolicy item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : prettyGender(item));
            }
        });
        genderPolicyBox.setValue(GenderPolicy.MIXTE);

        agePolicyBox.setItems(FXCollections.observableArrayList(AgePolicyChoice.values()));
        agePolicyBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(AgePolicyChoice item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : prettyAgeChoice(item));
            }
        });
        agePolicyBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(AgePolicyChoice item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : prettyAgeChoice(item));
            }
        });
        agePolicyBox.setValue(AgePolicyChoice.ANY);

        ageMinBox.setItems(FXCollections.observableArrayList(AgeCategory.values()));
        ageMinBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(AgeCategory item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.label());
            }
        });
        ageMinBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(AgeCategory item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.label());
            }
        });

        ageMaxBox.setItems(FXCollections.observableArrayList(AgeCategory.values()));
        ageMaxBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(AgeCategory item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.label());
            }
        });
        ageMaxBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(AgeCategory item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.label());
            }
        });

        ageRangeBox.setSpacing(AppTheme.SPACE_SM);
        ageRangeBox.getChildren().setAll(
                labeledInline("Catégorie minimum", ageMinBox),
                labeledInline("Catégorie maximum", ageMaxBox));
        ageRangeBox.setManaged(false);
        ageRangeBox.setVisible(false);

        ageAllowedPane.setHgap(10);
        ageAllowedPane.setVgap(8);
        ageAllowedPane.setPrefWrapLength(700);

        for (AgeCategory ageCategory : AgeCategory.values()) {
            CheckBox check = new CheckBox(prettyAgeCategory(ageCategory));
            ageCategoryChecks.put(ageCategory, check);
            ageAllowedPane.getChildren().add(check);
        }

        ageAllowedSetBox.setSpacing(AppTheme.SPACE_SM);
        ageAllowedSetBox.getChildren().setAll(
                new Label("Catégories autorisées"),
                ageAllowedPane);
        ageAllowedSetBox.setManaged(false);
        ageAllowedSetBox.setVisible(false);

        pointsRuleTypeBox.setItems(FXCollections.observableArrayList(TableauPointsRuleType.values()));
        pointsRuleTypeBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(TableauPointsRuleType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : prettyPointsRule(item));
            }
        });
        pointsRuleTypeBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(TableauPointsRuleType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : prettyPointsRule(item));
            }
        });
        pointsRuleTypeBox.setValue(TableauPointsRuleType.MAX_ONLY);

        minPointsField.setPromptText("Ex : 1300");
        maxPointsField.setPromptText("Ex : 1599");

        pointsDynamicBox.setSpacing(AppTheme.SPACE_SM);
        refreshPointsFields(false);

        maxPlayersSpinner.setEditable(true);
        waitlistCapacitySpinner.setEditable(true);

        applySuggestedMaxPlayers();

        if (isEditMode()) {
            applyExistingValues();
        }
    }

    private void configureActions() {
        autoDesignationCheck.selectedProperty().addListener((obs, oldValue, selected) -> {
            designationField.setDisable(selected);
            if (selected) {
                updateAutoDesignation();
            }
        });

        codeField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (autoDesignationCheck.isSelected()) {
                updateAutoDesignation();
            }
        });

        designationField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!designationField.isDisabled() && newValue != null && !newValue.isBlank()) {
                autoDesignationCheck.setSelected(false);
            }
        });

        agePolicyBox.valueProperty().addListener((obs, oldValue, newValue) -> refreshAgePolicyFields());

        pointsRuleTypeBox.valueProperty().addListener((obs, oldValue, newValue) -> refreshPointsFields(true));

        checkInEndField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (isBlank(startTimeField.getText())) {
                LocalTime parsed = tryParseTime(newValue);
                if (parsed != null) {
                    startTimeField.setText(parsed.plusMinutes(30).toString());
                }
            }
        });

        datePicker.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setDisable(empty || !tournamentDays.contains(item));
            }
        });

        saveButton.setOnAction(e -> onSave());
    }

    private void applyExistingValues() {
        codeField.setText(safe(existingTableau.code()));
        designationField.setText(safe(existingTableau.designation()));
        datePicker.setValue(parseDate(existingTableau.date()));
        genderPolicyBox.setValue(parseGenderPolicy(existingTableau.genderPolicy()));

        if (isBlank(existingTableau.designation())
                || existingTableau.designation().equalsIgnoreCase("Tableau " + safe(existingTableau.code()))) {
            autoDesignationCheck.setSelected(true);
            designationField.setDisable(true);
        } else {
            autoDesignationCheck.setSelected(false);
            designationField.setDisable(false);
        }

        AgePolicyChoice ageChoice = parseAgeChoice(existingTableau.agePolicyType());
        agePolicyBox.setValue(ageChoice);
        refreshAgePolicyFields();

        if (ageChoice == AgePolicyChoice.RANGE) {
            ageMinBox.setValue(parseAgeCategory(existingTableau.ageMinCategory()));
            ageMaxBox.setValue(parseAgeCategory(existingTableau.ageMaxCategory()));
        } else if (ageChoice == AgePolicyChoice.ALLOWED_SET && existingTableau.allowedAgeCategories() != null) {
            for (String raw : existingTableau.allowedAgeCategories()) {
                AgeCategory category = parseAgeCategory(raw);
                if (category != null && ageCategoryChecks.containsKey(category)) {
                    ageCategoryChecks.get(category).setSelected(true);
                }
            }
        }

        pointsRuleTypeBox.setValue(parsePointsRule(existingTableau.pointsRuleType()));
        refreshPointsFields(false);

        minPointsField.setText(existingTableau.minPoints() == null ? "" : String.valueOf(existingTableau.minPoints()));
        maxPointsField.setText(existingTableau.maxPoints() == null ? "" : String.valueOf(existingTableau.maxPoints()));

        if (existingTableau.maxPlayers() != null) {
            maxPlayersSpinner.getValueFactory().setValue(existingTableau.maxPlayers());
        }
        if (existingTableau.waitlistCapacity() != null) {
            waitlistCapacitySpinner.getValueFactory().setValue(existingTableau.waitlistCapacity());
        }

        checkInEndField.setText(safe(existingTableau.checkInEnd()));
        startTimeField.setText(safe(existingTableau.startTime()));
        prepaidFeeField
                .setText(existingTableau.prepaidFee() == null ? "" : String.valueOf(existingTableau.prepaidFee()));
        onSiteFeeField.setText(existingTableau.onSiteFee() == null ? "" : String.valueOf(existingTableau.onSiteFee()));

        prizeTiersContainer.getChildren().clear();
        if (existingTableau.prizeTiers() != null && !existingTableau.prizeTiers().isEmpty()) {
            for (PrizeTierDto tier : existingTableau.prizeTiers()) {
                addPrizeTierRow(tier);
            }
        }
    }

    private void refreshAgePolicyFields() {
        AgePolicyChoice choice = agePolicyBox.getValue();
        boolean range = choice == AgePolicyChoice.RANGE;
        boolean allowedSet = choice == AgePolicyChoice.ALLOWED_SET;

        ageRangeBox.setManaged(range);
        ageRangeBox.setVisible(range);

        ageAllowedSetBox.setManaged(allowedSet);
        ageAllowedSetBox.setVisible(allowedSet);
    }

    private void refreshPointsFields(boolean clearValues) {
        VBox box = new VBox(AppTheme.SPACE_SM);
        TableauPointsRuleType rule = pointsRuleTypeBox.getValue();

        if (rule == TableauPointsRuleType.MAX_ONLY) {
            box.getChildren().add(labeledInline("Points maximum", maxPointsField));
        } else if (rule == TableauPointsRuleType.RANGE_MIN_MAX) {
            box.getChildren().addAll(
                    labeledInline("Points minimum", minPointsField),
                    labeledInline("Points maximum", maxPointsField));
        }

        pointsDynamicBox.getChildren().setAll(box);

        if (clearValues) {
            minPointsField.clear();
            maxPointsField.clear();
        }
    }

    private void applySuggestedMaxPlayers() {
        Integer numberOfTables = regulation.numberOfTables();

        if (numberOfTables == null || numberOfTables <= 0) {
            maxPlayersHintLabel.setText(
                    "Aucune recommandation automatique : le nombre de tables n'est pas encore renseigné dans le règlement.");
            return;
        }

        int suggested = numberOfTables * 3;
        maxPlayersHintLabel.setText(
                "Vous avez renseigné " + numberOfTables + " table(s) dans le règlement. "
                        + "Une capacité conseillée de " + suggested + " joueurs est proposée pour ce tableau.");

        if (!isEditMode() || existingTableau.maxPlayers() == null) {
            maxPlayersSpinner.getValueFactory().setValue(suggested);
        }
    }

    private void addPrizeTierRow(PrizeTierDto dto) {
        PrizeTierRow row = new PrizeTierRow(dto);
        prizeTiersContainer.getChildren().add(row.root());
    }

    private void onSave() {
        try {
            result = buildResultDto();
            hideMessage();
            close();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private TableauDto buildResultDto() {
        String code = optionalText(codeField);
        String designation = optionalText(designationField);
        LocalDate date = datePicker.getValue();

        GenderPolicy genderPolicy = genderPolicyBox.getValue();

        AgePolicyChoice ageChoice = agePolicyBox.getValue();
        String agePolicyType = ageChoice == null ? null : ageChoice.name();
        String ageMin = null;
        String ageMax = null;
        List<String> allowedAgeCategories = null;

        if (ageChoice == AgePolicyChoice.RANGE) {
            AgeCategory minCategory = ageMinBox.getValue();
            AgeCategory maxCategory = ageMaxBox.getValue();
            ageMin = minCategory == null ? null : minCategory.name();
            ageMax = maxCategory == null ? null : maxCategory.name();

            if (minCategory != null && maxCategory != null && minCategory.ordinal() > maxCategory.ordinal()) {
                throw new IllegalArgumentException(
                        "La catégorie minimum ne peut pas être supérieure à la catégorie maximum.");
            }
        } else if (ageChoice == AgePolicyChoice.ALLOWED_SET) {
            allowedAgeCategories = ageCategoryChecks.entrySet().stream()
                    .filter(e -> e.getValue().isSelected())
                    .map(e -> e.getKey().name())
                    .toList();
        }

        TableauPointsRuleType pointsRule = pointsRuleTypeBox.getValue();
        Integer minPoints = optionalInteger(minPointsField);
        Integer maxPoints = optionalInteger(maxPointsField);

        if (minPoints != null && minPoints < 0) {
            throw new IllegalArgumentException("Les points minimum ne peuvent pas être négatifs.");
        }
        if (maxPoints != null && maxPoints < 0) {
            throw new IllegalArgumentException("Les points maximum ne peuvent pas être négatifs.");
        }
        if (pointsRule == TableauPointsRuleType.MAX_ONLY && minPoints != null) {
            minPoints = null;
        }
        if (pointsRule == TableauPointsRuleType.TOUTES_SERIES) {
            minPoints = null;
            maxPoints = null;
        }
        if (pointsRule == TableauPointsRuleType.RANGE_MIN_MAX
                && minPoints != null && maxPoints != null && minPoints > maxPoints) {
            throw new IllegalArgumentException("Les points minimum ne peuvent pas être supérieurs aux points maximum.");
        }

        Integer maxPlayers = maxPlayersSpinner.getValue();
        Integer waitlistCapacity = waitlistCapacitySpinner.getValue();

        LocalTime checkInEnd = tryParseTime(checkInEndField.getText());
        LocalTime startTime = tryParseTime(startTimeField.getText());

        if (checkInEnd != null && startTime != null && !checkInEnd.isBefore(startTime)) {
            throw new IllegalArgumentException("La fin du pointage doit être avant le début des matchs.");
        }

        Integer prepaidFee = optionalInteger(prepaidFeeField);
        Integer onSiteFee = optionalInteger(onSiteFeeField);

        if (prepaidFee != null && prepaidFee < 0) {
            throw new IllegalArgumentException("Le tarif préinscription / en ligne ne peut pas être négatif.");
        }
        if (onSiteFee != null && onSiteFee < 0) {
            throw new IllegalArgumentException("Le tarif sur place ne peut pas être négatif.");
        }

        List<PrizeTierDto> prizeTiers = collectValidPrizeTiers();

        return new TableauDto(
                isEditMode() ? existingTableau.id() : null,
                tournamentId,
                code,
                designation,
                date == null ? null : date.toString(),
                genderPolicy == null ? null : genderPolicy.name(),
                agePolicyType,
                ageMin,
                ageMax,
                allowedAgeCategories,
                pointsRule == null ? null : pointsRule.name(),
                minPoints,
                maxPoints,
                maxPlayers,
                waitlistCapacity,
                checkInEnd == null ? null : checkInEnd.toString(),
                startTime == null ? null : startTime.toString(),
                prepaidFee,
                onSiteFee,
                prizeTiers,
                isEditMode() ? existingTableau.createdAt() : null,
                isEditMode() ? existingTableau.updatedAt() : null);
    }

    private List<PrizeTierDto> collectValidPrizeTiers() {
        List<PrizeTierDto> tiers = new ArrayList<>();

        for (Node node : prizeTiersContainer.getChildren()) {
            Object userData = node.getUserData();
            if (!(userData instanceof PrizeTierRow row)) {
                continue;
            }

            if (row.isEmpty()) {
                continue;
            }

            tiers.add(row.toDto());
        }

        return tiers;
    }

    private GridPane createFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(12);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(220);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().addAll(col1, col2);
        return grid;
    }

    private void addField(GridPane grid, int row, String labelText, Node field) {
        Label label = new Label(labelText == null ? "" : labelText);
        AppTheme.applyBody(label);

        GridPane.setHgrow(field, Priority.ALWAYS);
        if (field instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
        }

        grid.add(label, 0, row);
        grid.add(field, 1, row);
    }

    private HBox labeledInline(String labelText, Node node) {
        Label label = new Label(labelText + " :");
        AppTheme.applyBody(label);
        label.setMinWidth(150);

        if (node instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(region, Priority.ALWAYS);
        }

        HBox box = new HBox(10, label, node);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private void updateAutoDesignation() {
        String code = safe(codeField.getText());
        designationField.setText(code.isBlank() ? "" : "Tableau " + code.toUpperCase());
    }

    private void initMessageLabel() {
        messageLabel.setWrapText(true);
        messageLabel.setManaged(false);
        messageLabel.setVisible(false);
        messageLabel.setStyle("-fx-text-fill: #b00020; -fx-font-weight: 700;");
    }

    private void showError(String text) {
        messageLabel.setText(text);
        messageLabel.setManaged(true);
        messageLabel.setVisible(true);
    }

    private void hideMessage() {
        messageLabel.setText("");
        messageLabel.setManaged(false);
        messageLabel.setVisible(false);
    }

    private static String prettyGender(GenderPolicy policy) {
        return switch (policy) {
            case MIXTE -> "Mixte";
            case MASCULIN -> "Masculin uniquement";
            case FEMININ -> "Féminin uniquement";
        };
    }

    private static String prettyPointsRule(TableauPointsRuleType type) {
        return switch (type) {
            case TOUTES_SERIES -> "Toutes séries";
            case MAX_ONLY -> "Maximum seulement";
            case RANGE_MIN_MAX -> "Tranche min / max";
        };
    }

    private static String prettyAgeChoice(AgePolicyChoice choice) {
        return switch (choice) {
            case ANY -> "Toutes catégories";
            case RANGE -> "Intervalle de catégories";
            case ALLOWED_SET -> "Sélection manuelle";
        };
    }

    private static String prettyAgeCategory(AgeCategory category) {
        return category.label();
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isBlank();
    }

    private String optionalText(TextField field) {
        String value = field.getText();
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static Integer optionalInteger(TextField field) {
        String value = safe(field.getText());
        if (value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Valeur numérique invalide : " + value);
        }
    }

    private static LocalTime tryParseTime(String raw) {
        try {
            return isBlank(raw) ? null : LocalTime.parse(raw.trim());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Format d'heure invalide. Utilisez HH:mm.");
        }
    }

    private static LocalDate parseDate(String raw) {
        try {
            return isBlank(raw) ? null : LocalDate.parse(raw.trim());
        } catch (Exception ex) {
            return null;
        }
    }

    private static GenderPolicy parseGenderPolicy(String raw) {
        try {
            return isBlank(raw) ? GenderPolicy.MIXTE : GenderPolicy.valueOf(raw.trim());
        } catch (Exception ex) {
            return GenderPolicy.MIXTE;
        }
    }

    private static AgePolicyChoice parseAgeChoice(String raw) {
        try {
            return isBlank(raw) ? AgePolicyChoice.ANY : AgePolicyChoice.valueOf(raw.trim());
        } catch (Exception ex) {
            return AgePolicyChoice.ANY;
        }
    }

    private static AgeCategory parseAgeCategory(String raw) {
        try {
            return isBlank(raw) ? null : AgeCategory.valueOf(raw.trim());
        } catch (Exception ex) {
            return null;
        }
    }

    private static TableauPointsRuleType parsePointsRule(String raw) {
        try {
            return isBlank(raw) ? TableauPointsRuleType.MAX_ONLY : TableauPointsRuleType.valueOf(raw.trim());
        } catch (Exception ex) {
            return TableauPointsRuleType.MAX_ONLY;
        }
    }

    private final class PrizeTierRow {
        private final HBox root = new HBox(10);

        private final TextField fromRankField = new TextField();
        private final TextField toRankField = new TextField();
        private final ComboBox<PrizeRewardTypeDto> rewardTypeBox = new ComboBox<>();
        private final TextField cashAmountField = new TextField();
        private final TextField discountPercentField = new TextField();
        private final VBox dynamicValueBox = new VBox();
        private final Button removeButton = new Button("Supprimer");

        private PrizeTierRow(PrizeTierDto dto) {
            fromRankField.setPromptText("Début");
            toRankField.setPromptText("Fin");
            cashAmountField.setPromptText("Montant €");
            discountPercentField.setPromptText("Réduction %");

            rewardTypeBox.setItems(FXCollections.observableArrayList(PrizeRewardTypeDto.values()));
            rewardTypeBox.setValue(PrizeRewardTypeDto.CASH);

            rewardTypeBox.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(PrizeRewardTypeDto item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : prettyRewardType(item));
                }
            });
            rewardTypeBox.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(PrizeRewardTypeDto item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : prettyRewardType(item));
                }
            });

            AppTheme.styleSecondary(removeButton);
            removeButton.setOnAction(e -> prizeTiersContainer.getChildren().remove(root));

            rewardTypeBox.valueProperty().addListener((obs, oldValue, newValue) -> refreshDynamicValueField());

            root.setAlignment(Pos.CENTER_LEFT);
            root.getChildren().addAll(
                    labeledCompact("Du rang", fromRankField),
                    labeledCompact("Au rang", toRankField),
                    labeledCompact("Type", rewardTypeBox),
                    dynamicValueBox,
                    removeButton);
            root.setUserData(this);

            refreshDynamicValueField();

            if (dto != null) {
                fromRankField.setText(dto.fromRank() == null ? "" : String.valueOf(dto.fromRank()));
                toRankField.setText(dto.toRank() == null ? "" : String.valueOf(dto.toRank()));
                if (dto.rewardType() != null) {
                    rewardTypeBox.setValue(dto.rewardType());
                }
                cashAmountField.setText(dto.cashAmount() == null ? "" : String.valueOf(dto.cashAmount()));
                discountPercentField.setText(dto.registrationDiscountPercent() == null
                        ? ""
                        : String.valueOf(dto.registrationDiscountPercent()));
                refreshDynamicValueField();
            }
        }

        private Node root() {
            return root;
        }

        private boolean isEmpty() {
            return isBlank(fromRankField.getText())
                    && isBlank(toRankField.getText())
                    && isBlank(cashAmountField.getText())
                    && isBlank(discountPercentField.getText());
        }

        private PrizeTierDto toDto() {
            Integer fromRank = optionalInteger(fromRankField);
            Integer toRank = optionalInteger(toRankField);

            if (fromRank == null || toRank == null) {
                throw new IllegalArgumentException(
                        "Un palier de dotation commencé doit avoir un rang de début et de fin.");
            }
            if (fromRank <= 0 || toRank <= 0) {
                throw new IllegalArgumentException("Les rangs doivent être supérieurs à 0.");
            }
            if (fromRank > toRank) {
                throw new IllegalArgumentException("Le rang de début ne peut pas être supérieur au rang de fin.");
            }

            PrizeRewardTypeDto rewardType = rewardTypeBox.getValue();
            if (rewardType == null) {
                throw new IllegalArgumentException("Le type de récompense est obligatoire.");
            }

            Integer cashAmount = null;
            Integer discountPercent = null;

            if (rewardType == PrizeRewardTypeDto.CASH) {
                cashAmount = optionalInteger(cashAmountField);
                if (cashAmount == null) {
                    throw new IllegalArgumentException("Le montant en euros est obligatoire pour ce palier.");
                }
                if (cashAmount < 0) {
                    throw new IllegalArgumentException("Le montant d'un gain ne peut pas être négatif.");
                }
            } else {
                discountPercent = optionalInteger(discountPercentField);
                if (discountPercent == null) {
                    throw new IllegalArgumentException("Le pourcentage de réduction est obligatoire pour ce palier.");
                }
                if (discountPercent < 0 || discountPercent > 100) {
                    throw new IllegalArgumentException("La réduction doit être comprise entre 0 et 100%.");
                }
            }

            return new PrizeTierDto(fromRank, toRank, rewardType, cashAmount, discountPercent);
        }

        private void refreshDynamicValueField() {
            PrizeRewardTypeDto type = rewardTypeBox.getValue();
            if (type == PrizeRewardTypeDto.REGISTRATION_DISCOUNT_PERCENT) {
                dynamicValueBox.getChildren().setAll(labeledCompact("Réduction", discountPercentField));
            } else {
                dynamicValueBox.getChildren().setAll(labeledCompact("Montant", cashAmountField));
            }
        }

        private HBox labeledCompact(String labelText, Node field) {
            Label label = new Label(labelText);
            AppTheme.applyBody(label);

            if (field instanceof Region region) {
                region.setPrefWidth(100);
            }

            HBox box = new HBox(6, label, field);
            box.setAlignment(Pos.CENTER_LEFT);
            return box;
        }

        private String prettyRewardType(PrizeRewardTypeDto type) {
            return switch (type) {
                case CASH -> "Montant en €";
                case REGISTRATION_DISCOUNT_PERCENT -> "Réduction inscription %";
            };
        }
    }
}