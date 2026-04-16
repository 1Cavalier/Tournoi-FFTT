package fr.pingmanager.gestion_tournois_FFTT.ui.javafx.view.organizer.dialogs;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.enums.ApprovedBall;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.enums.BallProvisionPolicy;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.enums.PlayingAreaPreset;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.enums.TournamentLevel;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.app.AppRouter;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.OfficialSelectablePlayerDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TournamentDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TournamentOfficialAssignmentDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TournamentRegulationDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;

public class EditTournamentRegulationDialog extends Stage {

    private enum PlayingAreaChoice {
        STANDARD,
        CUSTOM
    }

    private final AppRouter nav;
    private final TournamentDto tournament;
    private final TournamentRegulationDto regulation;

    // -------------------------------------------------------------------------
    // CHAMPS — Contact organisateur
    // -------------------------------------------------------------------------

    private final TextField organizerContactNameField = new TextField();
    private final TextField organizerEmailField = new TextField();
    private final TextField organizerPhoneField = new TextField();

    // -------------------------------------------------------------------------
    // CHAMPS — Aire de jeu
    // -------------------------------------------------------------------------

    private final TextField numberOfTablesField = new TextField();
    private final ComboBox<PlayingAreaChoice> playingAreaChoiceBox = new ComboBox<>();
    private final TextField tablesBrandField = new TextField();
    private final TextField playingAreaLengthField = new TextField();
    private final TextField playingAreaWidthField = new TextField();
    private final Label playingAreaHintLabel = new Label();

    // -------------------------------------------------------------------------
    // CHAMPS — Balles
    // -------------------------------------------------------------------------

    private final ComboBox<ApprovedBall> ballModelBox = new ComboBox<>();
    private final TextField ballCustomField = new TextField();
    private final TableView<String> ballSelectedList = new TableView<>();
    private final ComboBox<BallProvisionPolicy> ballProvisionPolicyBox = new ComboBox<>();

    // -------------------------------------------------------------------------
    // CHAMPS — Horaires
    // -------------------------------------------------------------------------

    // Inscriptions : un seul DateTime (ouverture + fermeture)
    // Stockage ISO interne (non affiché directement)
    private String registrationOpenIso = null;
    private String registrationDeadlineIso = null;

    // Labels d'affichage en format lisible JJ/MM/YYYY - XXhXXm
    private final Label registrationOpenDisplayLabel = new Label("Non renseigné");
    private final Label registrationDeadlineDisplayLabel = new Label("Non renseigné");

    // Gymnase : plusieurs créneaux possibles (un par jour de tournoi)
    private final TableView<String> gymScheduleList = new TableView<>();

    // -------------------------------------------------------------------------
    // CHAMPS — Officiels
    // -------------------------------------------------------------------------

    /** Texte OBLIGATOIRE FFTT — concerne uniquement le Juge-Arbitre. */
    private final Label jaRequirementLabel = new Label();

    /**
     * Texte RECOMMANDATION — concerne les arbitres, non imposé réglementairement.
     */
    private final Label refRecommendationLabel = new Label();

    // Juges-arbitres
    private final ComboBox<String> jaGradeFilterBox = new ComboBox<>();
    private final TextField jaSearchField = new TextField();
    private final TableView<TournamentOfficialAssignmentDto> jaResultTable = new TableView<>();
    private final TableView<TournamentOfficialAssignmentDto> jaAssignedTable = new TableView<>();

    // Arbitres
    private final ComboBox<String> refGradeFilterBox = new ComboBox<>();
    private final TextField refSearchField = new TextField();
    private final TableView<TournamentOfficialAssignmentDto> refResultTable = new TableView<>();
    private final TableView<TournamentOfficialAssignmentDto> refAssignedTable = new TableView<>();

    // -------------------------------------------------------------------------
    // BADGES
    // -------------------------------------------------------------------------

    private final Button saveButton = new Button("Enregistrer le règlement");

    private Label organizerSectionBadge;
    private Label playingAreaSectionBadge;
    private Label ballSectionBadge;
    private Label timingSectionBadge;
    private Label jaSectionBadge;
    private Label refSectionBadge;

    // =========================================================================
    // CONSTRUCTEUR
    // =========================================================================

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

    // =========================================================================
    // CONSTRUCTION UI
    // =========================================================================

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
        jaSectionBadge = createSectionBadge(false);
        refSectionBadge = createSectionBadge(true); // recommandation — toujours valide

        VBox sections = new VBox(AppTheme.SPACE_MD);
        sections.getChildren().addAll(
                buildCollapsibleSection("Contact organisateur", buildOrganizerSection(), organizerSectionBadge),
                buildCollapsibleSection("Aire de jeu et matériel", buildPlayingAreaSection(), playingAreaSectionBadge),
                buildCollapsibleSection("Balles", buildBallSection(), ballSectionBadge),
                buildCollapsibleSection("Horaires réglementaires", buildTimingSection(), timingSectionBadge),
                buildCollapsibleSection("Juge-arbitre", buildJaSection(), jaSectionBadge),
                buildCollapsibleSection("Arbitres", buildRefSection(), refSectionBadge));

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

    // =========================================================================
    // SECTIONS DU FORMULAIRE
    // =========================================================================

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

        // -- Sélecteur de balle --
        Label selectorLabel = new Label("Balle homologuée :");
        AppTheme.applyBody(selectorLabel);

        Button addBallBtn = new Button("+ Ajouter");
        AppTheme.stylePrimary(addBallBtn);

        HBox selectorRow = new HBox(AppTheme.SPACE_SM, ballModelBox, ballCustomField, addBallBtn);
        selectorRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(ballModelBox, Priority.ALWAYS);
        HBox.setHgrow(ballCustomField, Priority.ALWAYS);
        ballModelBox.setMaxWidth(Double.MAX_VALUE);

        // -- Tableau des balles sélectionnées --
        configureBallSelectedList();
        ballSelectedList.setPrefHeight(130);
        ballSelectedList.setPlaceholder(new Label("Aucune balle ajoutée."));

        Label selectedLabel = new Label("Balles retenues pour ce tournoi");
        AppTheme.applyBody(selectedLabel);

        // -- Fourniture --
        GridPane grid = createFormGrid();
        addField(grid, 0, "Fourniture des balles", ballProvisionPolicyBox);

        addBallBtn.setOnAction(e -> onAddBall());

        return new VBox(AppTheme.SPACE_MD,
                selectorLabel,
                selectorRow,
                selectedLabel,
                ballSelectedList,
                grid);
    }

    private void configureBallSelectedList() {
        ballSelectedList.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<String, String> nameCol = new TableColumn<>("Balle");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()));

        TableColumn<String, Void> actionCol = new TableColumn<>("");
        actionCol.setMaxWidth(90);
        actionCol.setMinWidth(90);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button removeBtn = new Button("Retirer");
            {
                removeBtn.setStyle(
                        "-fx-background-color: #C62828;"
                                + "-fx-text-fill: white;"
                                + "-fx-font-weight: 700;"
                                + "-fx-background-radius: " + AppTheme.RADIUS + ";"
                                + "-fx-padding: 6 10;"
                                + "-fx-cursor: hand;");
                removeBtn.setOnAction(e -> {
                    getTableView().getItems().remove(getIndex());
                    refreshSectionBadges();
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : removeBtn);
            }
        });

        ballSelectedList.getColumns().setAll(nameCol, actionCol);
    }

    private void onAddBall() {
        ApprovedBall selected = ballModelBox.getValue();
        if (selected == null)
            return;

        String label;
        if (selected.requiresCustomInput()) {
            String custom = optional(ballCustomField.getText());
            if (custom == null)
                return; // champ vide, on n'ajoute rien
            label = custom;
        } else {
            label = selected.label();
        }

        // Éviter les doublons
        if (ballSelectedList.getItems().contains(label))
            return;

        ballSelectedList.getItems().add(label);
        refreshSectionBadges();
    }

    private VBox buildTimingSection() {

        // -- Inscriptions (champ texte + bouton calendrier) --
        Label openLabel = new Label("Ouverture des inscriptions :");
        Label deadlineLabel = new Label("Fermeture des inscriptions :");
        AppTheme.applyBody(openLabel);
        AppTheme.applyBody(deadlineLabel);

        Button openPickerBtn = new Button("🗓");
        Button deadlinePickerBtn = new Button("🗓");
        AppTheme.styleSecondary(openPickerBtn);
        AppTheme.styleSecondary(deadlinePickerBtn);

        openPickerBtn.setOnAction(e -> {
            // Ouverture : avant le tournoi — pas de borne basse, max = veille du 1er jour
            java.time.LocalDate startDate = tournamentStartDate();
            java.time.LocalDate maxOpen = startDate != null ? startDate.minusDays(1) : null;
            String picked = showDateTimePickerDialog(
                    "Ouverture des inscriptions", null, maxOpen, null);
            if (picked != null) {
                registrationOpenIso = picked;
                registrationOpenDisplayLabel.setText(formatIsoToFr(picked));
                refreshSectionBadges();
            }
        });
        deadlinePickerBtn.setOnAction(e -> {
            // Fermeture : avant le tournoi — max = veille du 1er jour du tournoi
            java.time.LocalDate startDate2 = tournamentStartDate();
            java.time.LocalDate maxDeadline = startDate2 != null ? startDate2.minusDays(1) : null;
            String picked = showDateTimePickerDialog(
                    "Fermeture des inscriptions", null, maxDeadline, null);
            if (picked != null) {
                registrationDeadlineIso = picked;
                registrationDeadlineDisplayLabel.setText(formatIsoToFr(picked));
                refreshSectionBadges();
            }
        });

        // Style labels affichage
        registrationOpenDisplayLabel.setStyle(
                "-fx-padding: 6 10; -fx-background-color: " + AppTheme.COLOR_SURFACE
                        + "; -fx-border-color: " + AppTheme.COLOR_BORDER
                        + "; -fx-border-radius: " + AppTheme.RADIUS
                        + "; -fx-background-radius: " + AppTheme.RADIUS + ";");
        registrationDeadlineDisplayLabel.setStyle(
                "-fx-padding: 6 10; -fx-background-color: " + AppTheme.COLOR_SURFACE
                        + "; -fx-border-color: " + AppTheme.COLOR_BORDER
                        + "; -fx-border-radius: " + AppTheme.RADIUS
                        + "; -fx-background-radius: " + AppTheme.RADIUS + ";");
        AppTheme.applyBody(registrationOpenDisplayLabel);
        AppTheme.applyBody(registrationDeadlineDisplayLabel);
        HBox.setHgrow(registrationOpenDisplayLabel, Priority.ALWAYS);
        HBox.setHgrow(registrationDeadlineDisplayLabel, Priority.ALWAYS);
        registrationOpenDisplayLabel.setMaxWidth(Double.MAX_VALUE);
        registrationDeadlineDisplayLabel.setMaxWidth(Double.MAX_VALUE);

        HBox openRow = new HBox(AppTheme.SPACE_SM, registrationOpenDisplayLabel, openPickerBtn);
        HBox deadlineRow = new HBox(AppTheme.SPACE_SM, registrationDeadlineDisplayLabel, deadlinePickerBtn);

        VBox inscriptionsBlock = new VBox(AppTheme.SPACE_SM,
                openLabel, openRow,
                deadlineLabel, deadlineRow);

        // -- Gymnase (multi-jours, liste + bouton) --
        Label gymLabel = new Label("Ouvertures du gymnase :");
        AppTheme.applyBody(gymLabel);

        Label gymHint = new Label("Ajoutez un créneau par jour de tournoi si les horaires diffèrent.");
        AppTheme.applyBody(gymHint);
        gymHint.setWrapText(true);
        gymHint.setStyle("-fx-text-fill: " + AppTheme.COLOR_TEXT_MUTED + ";");

        Button addGymBtn = new Button("+ Ajouter un créneau");
        AppTheme.styleSecondary(addGymBtn);
        addGymBtn.setOnAction(e -> {
            // Restreindre aux jours du tournoi
            java.util.Set<java.time.LocalDate> tournamentDays = tournamentDaysSet();
            java.time.LocalDate gymMin = tournamentStartDate();
            java.time.LocalDate gymMax = tournamentEndDate();

            // Exclure les jours déjà couverts de l'allowedDates
            // pour indiquer visuellement ce qui reste à renseigner
            java.util.Set<java.time.LocalDate> remainingDays = null;
            if (tournamentDays != null) {
                remainingDays = new java.util.LinkedHashSet<>(tournamentDays);
                for (String item : gymScheduleList.getItems()) {
                    try {
                        java.time.LocalDate existingDate = java.time.LocalDateTime
                                .parse(item, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
                                .toLocalDate();
                        remainingDays.remove(existingDate);
                    } catch (Exception ignored) {
                    }
                }
                if (remainingDays.isEmpty())
                    remainingDays = tournamentDays;
            }

            GymSlotResult slotResult = showGymSlotDialog(gymMin, gymMax, tournamentDays, remainingDays);
            if (slotResult == null)
                return;

            if (slotResult.applyToAllRemaining() && remainingDays != null) {
                // Même heure pour tous les jours restants
                String time = slotResult.time(); // "HH:mm"
                for (java.time.LocalDate day : remainingDays) {
                    String iso = day + "T" + time;
                    if (!gymScheduleList.getItems().contains(iso)) {
                        gymScheduleList.getItems().add(iso);
                    }
                }
            } else {
                String iso = slotResult.isoDateTime();
                if (!gymScheduleList.getItems().contains(iso)) {
                    gymScheduleList.getItems().add(iso);
                }
            }
            // Trier par date
            gymScheduleList.getItems().sort(java.util.Comparator.naturalOrder());
            refreshSectionBadges();
        });

        configureGymScheduleList();
        gymScheduleList.setPrefHeight(130);
        gymScheduleList.setPlaceholder(new Label("Aucun créneau ajouté."));

        VBox gymBlock = new VBox(AppTheme.SPACE_SM,
                gymLabel, gymHint, addGymBtn, gymScheduleList);

        return new VBox(AppTheme.SPACE_LG, inscriptionsBlock, gymBlock);
    }

    /**
     * Configure la liste des créneaux d'ouverture du gymnase.
     */
    private void configureGymScheduleList() {
        gymScheduleList.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<String, String> slotCol = new TableColumn<>("Date et heure d'ouverture");
        slotCol.setCellValueFactory(d -> new SimpleStringProperty(formatIsoToFr(d.getValue())));

        TableColumn<String, Void> actionCol = new TableColumn<>("");
        actionCol.setMaxWidth(90);
        actionCol.setMinWidth(90);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button removeBtn = new Button("Retirer");
            {
                removeBtn.setStyle(
                        "-fx-background-color: #C62828;"
                                + "-fx-text-fill: white;"
                                + "-fx-font-weight: 700;"
                                + "-fx-background-radius: " + AppTheme.RADIUS + ";"
                                + "-fx-padding: 6 10;"
                                + "-fx-cursor: hand;");
                removeBtn.setOnAction(e -> {
                    gymScheduleList.getItems().remove(getIndex());
                    refreshSectionBadges();
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : removeBtn);
            }
        });

        gymScheduleList.getColumns().setAll(slotCol, actionCol);
    }

    /**
     * Mini-dialog DatePicker + Spinners heure/minute.
     * Retourne une String ISO "YYYY-MM-DDTHH:MM" ou null si annulé.
     */
    /**
     * Ouvre un mini-dialog de sélection date + heure.
     *
     * @param dialogTitle  titre affiché dans la fenêtre
     * @param minDate      date minimale autorisée (null = pas de borne basse)
     * @param maxDate      date maximale autorisée (null = pas de borne haute)
     * @param allowedDates si non null, seules ces dates sont sélectionnables
     *                     (utilisé pour restreindre aux jours du tournoi)
     */
    private String showDateTimePickerDialog(
            String dialogTitle,
            java.time.LocalDate minDate,
            java.time.LocalDate maxDate,
            java.util.Set<java.time.LocalDate> allowedDates) {

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(nav.primaryStage());
        dialog.setTitle(dialogTitle);

        // Valeur initiale : minDate si définie, sinon aujourd'hui
        java.time.LocalDate initialDate = minDate != null ? minDate : java.time.LocalDate.now();
        DatePicker datePicker = new DatePicker(initialDate);

        // Contraindre les cellules du calendrier
        datePicker.setDayCellFactory(dp -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(java.time.LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setDisable(true);
                    return;
                }
                boolean tooEarly = minDate != null && item.isBefore(minDate);
                boolean tooLate = maxDate != null && item.isAfter(maxDate);
                boolean notAllowed = allowedDates != null && !allowedDates.contains(item);
                setDisable(tooEarly || tooLate || notAllowed);
            }
        });

        Spinner<Integer> hourSpinner = new Spinner<>(0, 23, 8);
        Spinner<Integer> minuteSpinner = new Spinner<>(0, 59, 0, 5);
        hourSpinner.setEditable(true);
        minuteSpinner.setEditable(true);
        hourSpinner.setPrefWidth(75);
        minuteSpinner.setPrefWidth(75);

        Label sep = new Label("h");
        AppTheme.applyBody(sep);

        HBox timeRow = new HBox(AppTheme.SPACE_SM, hourSpinner, sep, minuteSpinner);
        timeRow.setAlignment(Pos.CENTER_LEFT);

        Label dateLabel = new Label("Date :");
        Label timeLabel = new Label("Heure :");
        AppTheme.applyBody(dateLabel);
        AppTheme.applyBody(timeLabel);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.add(dateLabel, 0, 0);
        grid.add(datePicker, 1, 0);
        grid.add(timeLabel, 0, 1);
        grid.add(timeRow, 1, 1);

        Button confirmBtn = new Button("Valider");
        Button cancelBtn = new Button("Annuler");
        AppTheme.stylePrimary(confirmBtn);
        AppTheme.styleSecondary(cancelBtn);

        final String[] result = { null };

        confirmBtn.setOnAction(e -> {
            java.time.LocalDate date = datePicker.getValue();
            if (date == null)
                return;

            // Vérification côté validation (saisie manuelle dans le DatePicker)
            if (minDate != null && date.isBefore(minDate))
                return;
            if (maxDate != null && date.isAfter(maxDate))
                return;
            if (allowedDates != null && !allowedDates.contains(date))
                return;

            int h = hourSpinner.getValue();
            int m = minuteSpinner.getValue();
            // Stockage en ISO : "YYYY-MM-DDTHH:MM"
            result[0] = String.format("%d-%02d-%02dT%02d:%02d",
                    date.getYear(), date.getMonthValue(), date.getDayOfMonth(), h, m);
            dialog.close();
        });
        cancelBtn.setOnAction(e -> dialog.close());

        HBox actions = new HBox(AppTheme.SPACE_SM, cancelBtn, confirmBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(AppTheme.SPACE_MD, grid, actions);
        root.setPadding(new Insets(20));
        AppTheme.applyPage(root);

        dialog.setScene(new Scene(root));
        dialog.setResizable(false);
        dialog.showAndWait();

        return result[0];
    }

    /**
     * Parse la date de début du tournoi depuis le DTO.
     * Retourne null si absente ou invalide.
     */
    private java.time.LocalDate tournamentStartDate() {
        try {
            String raw = tournament.startDate();
            return (raw == null || raw.isBlank()) ? null : java.time.LocalDate.parse(raw.trim());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parse la date de fin du tournoi depuis le DTO.
     * Retourne null si absente ou invalide.
     */
    private java.time.LocalDate tournamentEndDate() {
        try {
            String raw = tournament.endDate();
            return (raw == null || raw.isBlank()) ? null : java.time.LocalDate.parse(raw.trim());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Construit l'ensemble des dates couvertes par le tournoi (startDate → endDate
     * inclus).
     * Utilisé pour restreindre le calendrier gymnase aux seuls jours du tournoi.
     */
    private java.util.Set<java.time.LocalDate> tournamentDaysSet() {
        java.time.LocalDate start = tournamentStartDate();
        java.time.LocalDate end = tournamentEndDate();
        if (start == null || end == null)
            return null;

        java.util.Set<java.time.LocalDate> days = new java.util.LinkedHashSet<>();
        java.time.LocalDate current = start;
        while (!current.isAfter(end)) {
            days.add(current);
            current = current.plusDays(1);
        }
        return days;
    }

    /**
     * Formate une date en français lisible.
     * Ex : "Samedi 12 mai 2026"
     */
    private static String formatDateFr(java.time.LocalDate date) {
        return date.format(java.time.format.DateTimeFormatter
                .ofPattern("EEEE dd MMMM yyyy", java.util.Locale.FRENCH));
    }

    /** Résultat du dialog gymnase. */
    private record GymSlotResult(String isoDateTime, String time, boolean applyToAllRemaining) {
    }

    /**
     * Dialog spécifique gymnase : DatePicker restreint aux jours du tournoi
     * + case "Appliquer la même heure à tous les jours restants".
     */
    private GymSlotResult showGymSlotDialog(
            java.time.LocalDate minDate,
            java.time.LocalDate maxDate,
            java.util.Set<java.time.LocalDate> allowedDates,
            java.util.Set<java.time.LocalDate> remainingDays) {

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(nav.primaryStage());
        dialog.setTitle("Ouverture du gymnase");

        java.time.LocalDate initialDate = (remainingDays != null && !remainingDays.isEmpty())
                ? remainingDays.iterator().next()
                : (minDate != null ? minDate : java.time.LocalDate.now());

        DatePicker datePicker = new DatePicker(initialDate);
        datePicker.setDayCellFactory(dp -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(java.time.LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setDisable(true);
                    return;
                }
                boolean notAllowed = allowedDates != null && !allowedDates.contains(item);
                setDisable(notAllowed);
            }
        });

        Spinner<Integer> hourSpinner = new Spinner<>(0, 23, 7);
        Spinner<Integer> minuteSpinner = new Spinner<>(0, 59, 30, 5);
        hourSpinner.setEditable(true);
        minuteSpinner.setEditable(true);
        hourSpinner.setPrefWidth(75);
        minuteSpinner.setPrefWidth(75);

        Label sep = new Label("h");
        AppTheme.applyBody(sep);
        HBox timeRow = new HBox(AppTheme.SPACE_SM, hourSpinner, sep, minuteSpinner);
        timeRow.setAlignment(Pos.CENTER_LEFT);

        // Checkbox "appliquer à tous les jours restants"
        int remaining = remainingDays != null ? remainingDays.size() : 0;
        javafx.scene.control.CheckBox applyAllBox = new javafx.scene.control.CheckBox(
                "Appliquer le même horaire aux " + remaining + " jour(s) restant(s)");
        applyAllBox.setStyle(AppTheme.BODY_STYLE);
        applyAllBox.setDisable(remaining <= 1);

        // Si applyAll cochée, le DatePicker devient inutile
        applyAllBox.selectedProperty().addListener((obs, o, n) -> datePicker.setDisable(n));

        Label dateLabel = new Label("Date :");
        Label timeLabel = new Label("Heure d'ouverture :");
        AppTheme.applyBody(dateLabel);
        AppTheme.applyBody(timeLabel);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.add(dateLabel, 0, 0);
        grid.add(datePicker, 1, 0);
        grid.add(timeLabel, 0, 1);
        grid.add(timeRow, 1, 1);
        grid.add(applyAllBox, 0, 2);
        javafx.scene.layout.GridPane.setColumnSpan(applyAllBox, 2);

        Button confirmBtn = new Button("Valider");
        Button cancelBtn = new Button("Annuler");
        AppTheme.stylePrimary(confirmBtn);
        AppTheme.styleSecondary(cancelBtn);

        final GymSlotResult[] result = { null };

        confirmBtn.setOnAction(e -> {
            int h = hourSpinner.getValue();
            int m = minuteSpinner.getValue();
            String time = String.format("%02d:%02d", h, m);
            boolean all = applyAllBox.isSelected();

            if (all) {
                result[0] = new GymSlotResult(null, time, true);
            } else {
                java.time.LocalDate date = datePicker.getValue();
                if (date == null)
                    return;
                if (allowedDates != null && !allowedDates.contains(date))
                    return;
                String iso = String.format("%d-%02d-%02dT%02d:%02d",
                        date.getYear(), date.getMonthValue(), date.getDayOfMonth(), h, m);
                result[0] = new GymSlotResult(iso, time, false);
            }
            dialog.close();
        });
        cancelBtn.setOnAction(e -> dialog.close());

        HBox actions = new HBox(AppTheme.SPACE_SM, cancelBtn, confirmBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(AppTheme.SPACE_MD, grid, actions);
        root.setPadding(new Insets(20));
        AppTheme.applyPage(root);

        dialog.setScene(new Scene(root));
        dialog.setResizable(false);
        dialog.showAndWait();

        return result[0];
    }

    /**
     * Convertit une valeur ISO "YYYY-MM-DDTHH:MM" en affichage français lisible.
     * Ex : "2026-05-12T07:30" → "Mardi 12 mai 2026 — 07h30"
     * Si le format n'est pas reconnu, retourne la valeur brute.
     */
    /**
     * Formate une valeur ISO "YYYY-MM-DDTHH:MM" en "JJ/MM/YYYY - XXhXXm".
     * Ex : "2026-05-12T07:30" → "12/05/2026 - 07h30m"
     * Valeur legacy en texte libre : retournée telle quelle.
     */
    private static String formatIsoToFr(String iso) {
        if (iso == null || iso.isBlank())
            return "";
        try {
            java.time.LocalDateTime dt = java.time.LocalDateTime.parse(iso.trim(),
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
            return String.format("%02d/%02d/%04d - %02dh%02dm",
                    dt.getDayOfMonth(), dt.getMonthValue(), dt.getYear(),
                    dt.getHour(), dt.getMinute());
        } catch (Exception e) {
            return iso.trim();
        }
    }

    // =========================================================================
    // SECTION OFFICIELS — complète
    // =========================================================================

    private VBox buildJaSection() {
        String style = "-fx-padding: 10 12 10 12;"
                + "-fx-background-color: #FFF8F0;"
                + "-fx-border-color: #EF6C00;"
                + "-fx-border-radius: 8;"
                + "-fx-background-radius: 8;";
        AppTheme.applyBody(jaRequirementLabel);
        jaRequirementLabel.setWrapText(true);
        jaRequirementLabel.setStyle(style);

        return buildOfficialBlock(
                jaRequirementLabel,
                jaGradeFilterBox,
                List.of("Tous", "JA1", "JA2", "JA3", "JAN", "JAI"),
                jaSearchField,
                jaResultTable,
                jaAssignedTable,
                true);
    }

    private VBox buildRefSection() {
        String style = "-fx-padding: 10 12 10 12;"
                + "-fx-background-color: #FFF8F0;"
                + "-fx-border-color: #EF6C00;"
                + "-fx-border-radius: 8;"
                + "-fx-background-radius: 8;";
        AppTheme.applyBody(refRecommendationLabel);
        refRecommendationLabel.setWrapText(true);
        refRecommendationLabel.setStyle(style);

        return buildOfficialBlock(
                refRecommendationLabel,
                refGradeFilterBox,
                List.of("Tous", "Club", "Régional", "National", "International", "International BB"),
                refSearchField,
                refResultTable,
                refAssignedTable,
                false);
    }

    /**
     * Construit un bloc complet (JA ou Arbitres) :
     * filtre grade + champ recherche + tableau résultats + tableau assignés.
     */
    private VBox buildOfficialBlock(
            Label requirementLabel,
            ComboBox<String> gradeFilterBox,
            List<String> gradeOptions,
            TextField searchField,
            TableView<TournamentOfficialAssignmentDto> resultTable,
            TableView<TournamentOfficialAssignmentDto> assignedTable,
            boolean isJudge) {

        // Filtre grade
        gradeFilterBox.getItems().setAll(gradeOptions);
        gradeFilterBox.setValue(gradeOptions.get(0));

        Label gradeLabel = new Label("Filtrer par grade :");
        AppTheme.applyBody(gradeLabel);

        HBox gradeRow = new HBox(AppTheme.SPACE_SM, gradeLabel, gradeFilterBox);
        gradeRow.setAlignment(Pos.CENTER_LEFT);

        // Recherche
        searchField.setPromptText("Nom, prénom ou numéro de licence");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Button searchBtn = new Button("Rechercher");
        AppTheme.styleSecondary(searchBtn);

        HBox searchRow = new HBox(AppTheme.SPACE_SM, searchField, searchBtn);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        // Tableau résultats
        configureResultTable(resultTable, isJudge, assignedTable);
        resultTable.setPrefHeight(160);
        resultTable.setPlaceholder(new Label("Aucun résultat — lancez une recherche."));

        Label resultLabel = new Label("Résultats de recherche");
        AppTheme.applyBody(resultLabel);

        // Tableau assignés
        configureAssignedTable(assignedTable, isJudge);
        assignedTable.setPrefHeight(140);
        assignedTable.setPlaceholder(new Label("Aucun officiel assigné pour le moment."));

        Label assignedLabel = new Label("Assignés au tournoi");
        AppTheme.applyBody(assignedLabel);

        // Pré-charger les officiels déjà assignés
        preloadAssigned(assignedTable, isJudge);

        // Actions
        searchBtn.setOnAction(e -> onSearch(
                searchField.getText(), gradeFilterBox.getValue(),
                resultTable, assignedTable, isJudge));

        searchField.setOnAction(e -> onSearch(
                searchField.getText(), gradeFilterBox.getValue(),
                resultTable, assignedTable, isJudge));

        gradeFilterBox.valueProperty().addListener((obs, o, n) -> {
            if (!searchField.getText().isBlank()) {
                onSearch(searchField.getText(), n, resultTable, assignedTable, isJudge);
            }
        });

        return new VBox(AppTheme.SPACE_SM,
                requirementLabel,
                gradeRow,
                searchRow,
                resultLabel,
                resultTable,
                assignedLabel,
                assignedTable);
    }

    /**
     * Configure les colonnes du tableau de résultats de recherche.
     * La dernière colonne contient le bouton "+ Ajouter".
     */
    private void configureResultTable(
            TableView<TournamentOfficialAssignmentDto> table,
            boolean isJudge,
            TableView<TournamentOfficialAssignmentDto> assignedTable) {

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<TournamentOfficialAssignmentDto, String> lastNameCol = new TableColumn<>("Nom");
        lastNameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().lastName()));

        TableColumn<TournamentOfficialAssignmentDto, String> firstNameCol = new TableColumn<>("Prénom");
        firstNameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().firstName()));

        TableColumn<TournamentOfficialAssignmentDto, String> licenseCol = new TableColumn<>("Licence");
        licenseCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().playerLicenseNumber()));

        TableColumn<TournamentOfficialAssignmentDto, String> gradeCol = new TableColumn<>(
                isJudge ? "Grade JA" : "Grade arbitre");
        gradeCol.setCellValueFactory(d -> new SimpleStringProperty(
                isJudge ? prettyJaGrade(d.getValue().judgeGrade())
                        : prettyRefGrade(d.getValue().refereeGrade())));

        TableColumn<TournamentOfficialAssignmentDto, String> clubCol = new TableColumn<>("Club");
        clubCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().clubName()));

        TableColumn<TournamentOfficialAssignmentDto, Void> actionCol = new TableColumn<>("");
        actionCol.setMaxWidth(100);
        actionCol.setMinWidth(100);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button addBtn = new Button("+ Ajouter");
            {
                AppTheme.stylePrimary(addBtn);
                addBtn.setOnAction(e -> {
                    TournamentOfficialAssignmentDto item = getTableView().getItems().get(getIndex());
                    onAddOfficial(item, assignedTable);
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : addBtn);
            }
        });

        table.getColumns().setAll(lastNameCol, firstNameCol, licenseCol, gradeCol, clubCol, actionCol);
    }

    /**
     * Configure les colonnes du tableau des officiels assignés.
     * La dernière colonne contient le bouton "Retirer".
     */
    private void configureAssignedTable(
            TableView<TournamentOfficialAssignmentDto> table,
            boolean isJudge) {

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<TournamentOfficialAssignmentDto, String> lastNameCol = new TableColumn<>("Nom");
        lastNameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().lastName()));

        TableColumn<TournamentOfficialAssignmentDto, String> firstNameCol = new TableColumn<>("Prénom");
        firstNameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().firstName()));

        TableColumn<TournamentOfficialAssignmentDto, String> licenseCol = new TableColumn<>("Licence");
        licenseCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().playerLicenseNumber()));

        TableColumn<TournamentOfficialAssignmentDto, String> gradeCol = new TableColumn<>(
                isJudge ? "Grade JA" : "Grade arbitre");
        gradeCol.setCellValueFactory(d -> new SimpleStringProperty(
                isJudge ? prettyJaGrade(d.getValue().judgeGrade())
                        : prettyRefGrade(d.getValue().refereeGrade())));

        TableColumn<TournamentOfficialAssignmentDto, String> clubCol = new TableColumn<>("Club");
        clubCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().clubName()));

        TableColumn<TournamentOfficialAssignmentDto, Void> actionCol = new TableColumn<>("");
        actionCol.setMaxWidth(90);
        actionCol.setMinWidth(90);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button removeBtn = new Button("Retirer");
            {
                removeBtn.setStyle(
                        "-fx-background-color: #C62828;"
                                + "-fx-text-fill: white;"
                                + "-fx-font-weight: 700;"
                                + "-fx-background-radius: " + AppTheme.RADIUS + ";"
                                + "-fx-padding: 6 10;"
                                + "-fx-cursor: hand;");
                removeBtn.setOnAction(e -> {
                    TournamentOfficialAssignmentDto item = getTableView().getItems().get(getIndex());
                    getTableView().getItems().remove(item);
                    updateOfficialsSectionBadge();
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : removeBtn);
            }
        });

        table.getColumns().setAll(lastNameCol, firstNameCol, licenseCol, gradeCol, clubCol, actionCol);
    }

    /**
     * Pré-charge les officiels déjà assignés au tournoi depuis
     * regulation.assignedOfficials().
     */
    private void preloadAssigned(
            TableView<TournamentOfficialAssignmentDto> assignedTable,
            boolean isJudge) {

        if (regulation.assignedOfficials() == null)
            return;

        String role = isJudge ? "JUGE_ARBITRE" : "ARBITRE";

        regulation.assignedOfficials().stream()
                .filter(o -> role.equalsIgnoreCase(o.officialRoleType()))
                .forEach(o -> assignedTable.getItems().add(o));
    }

    /**
     * Recherche les officiels selon le texte et le filtre de grade.
     * Exclut les officiels déjà assignés pour éviter les doublons.
     */
    private void onSearch(
            String query,
            String gradeFilter,
            TableView<TournamentOfficialAssignmentDto> resultTable,
            TableView<TournamentOfficialAssignmentDto> assignedTable,
            boolean isJudge) {

        if (query == null || query.isBlank()) {
            resultTable.getItems().clear();
            return;
        }

        List<OfficialSelectablePlayerDto> found = isJudge
                ? nav.playerRepo().searchJudgeReferees(query.trim(), 50)
                : nav.playerRepo().searchReferees(query.trim(), 50);

        String gradeRaw = gradeFilter == null ? "Tous" : gradeFilter;

        List<String> alreadyAssigned = assignedTable.getItems().stream()
                .map(TournamentOfficialAssignmentDto::playerLicenseNumber)
                .toList();

        List<TournamentOfficialAssignmentDto> results = found.stream()
                .filter(p -> matchesGradeFilter(p, gradeRaw, isJudge))
                .filter(p -> !alreadyAssigned.contains(p.licenseNumber()))
                .map(p -> toAssignmentDto(p, isJudge))
                .toList();

        resultTable.getItems().setAll(results);
    }

    /**
     * Ajoute un officiel à la liste des assignés (unicité par licence).
     */
    private void onAddOfficial(
            TournamentOfficialAssignmentDto item,
            TableView<TournamentOfficialAssignmentDto> assignedTable) {

        boolean alreadyIn = assignedTable.getItems().stream()
                .anyMatch(o -> o.playerLicenseNumber().equalsIgnoreCase(item.playerLicenseNumber()));

        if (!alreadyIn) {
            assignedTable.getItems().add(item);
            updateOfficialsSectionBadge();
        }
    }

    /**
     * Collecte tous les officiels assignés (JA + arbitres) pour la sauvegarde.
     */
    private List<TournamentOfficialAssignmentDto> collectAllAssignedOfficials() {
        List<TournamentOfficialAssignmentDto> all = new ArrayList<>();
        all.addAll(jaAssignedTable.getItems());
        all.addAll(refAssignedTable.getItems());
        return all;
    }

    /**
     * Convertit un OfficialSelectablePlayerDto en TournamentOfficialAssignmentDto.
     */
    private TournamentOfficialAssignmentDto toAssignmentDto(OfficialSelectablePlayerDto p, boolean isJudge) {
        String role = isJudge ? "JUGE_ARBITRE" : "ARBITRE";
        String jaGrade = isJudge && !p.judgeGrades().isEmpty() ? p.judgeGrades().get(0) : null;
        String refGrade = !isJudge && !p.refereeGrades().isEmpty() ? p.refereeGrades().get(0) : null;

        return new TournamentOfficialAssignmentDto(
                p.licenseNumber(),
                p.firstName(),
                p.lastName(),
                p.clubName(),
                role,
                jaGrade,
                refGrade,
                false,
                false,
                false);
    }

    /**
     * Filtre un joueur selon le grade sélectionné dans la ComboBox.
     * "Tous" ne filtre rien.
     */
    private boolean matchesGradeFilter(OfficialSelectablePlayerDto p, String gradeFilter, boolean isJudge) {
        if ("Tous".equalsIgnoreCase(gradeFilter))
            return true;

        List<String> grades = isJudge ? p.judgeGrades() : p.refereeGrades();

        String gradeEnum = isJudge
                ? gradeFilter.toUpperCase().replace(" ", "_")
                : switch (gradeFilter) {
                    case "Club" -> "CLUB";
                    case "Régional" -> "REGIONAL";
                    case "National" -> "NATIONAL";
                    case "International" -> "INTERNATIONAL";
                    case "International BB" -> "INTERNATIONAL_BLUE_BADGE";
                    default -> gradeFilter.toUpperCase();
                };

        return grades.contains(gradeEnum);
    }

    /**
     * Met à jour le badge de la section officiels.
     * Complet si au moins un JA est assigné.
     */
    private void updateOfficialsSectionBadge() {
        // JA : obligatoire FFTT — badge rouge si absent
        updateSectionBadge(jaSectionBadge, !jaAssignedTable.getItems().isEmpty());
        // Arbitres : recommandation — badge toujours vert
        updateSectionBadge(refSectionBadge, true);
    }

    private static String prettyJaGrade(String raw) {
        return raw == null ? "" : raw; // JA1, JA2, JA3, JAN, JAI sont déjà lisibles
    }

    private static String prettyRefGrade(String raw) {
        if (raw == null)
            return "";
        return switch (raw) {
            case "CLUB" -> "Club";
            case "REGIONAL" -> "Régional";
            case "NATIONAL" -> "National";
            case "INTERNATIONAL" -> "International";
            case "INTERNATIONAL_BLUE_BADGE" -> "International BB";
            default -> raw;
        };
    }

    // =========================================================================
    // SECTION COLLAPSIBLE
    // =========================================================================

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

    // =========================================================================
    // BADGES
    // =========================================================================

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
        updateOfficialsSectionBadge();
    }

    /**
     * Sérialise la liste des balles sélectionnées en une seule String
     * séparée par " | " pour le stockage dans ballBrandAndType.
     * Ex : "Butterfly R40+ — Blanche | Nittaku Premium 3 étoiles — Orange"
     */
    /**
     * Sérialise la liste des créneaux gymnase en une String stockable.
     * Ex : "Samedi 12 mai 2026 — 07h30 | Dimanche 13 mai 2026 — 08h00"
     */
    private String resolveGymSchedule() {
        List<String> items = gymScheduleList.getItems();
        if (items.isEmpty())
            return null;
        return String.join(" | ", items);
    }

    /**
     * Pré-charge la liste des créneaux depuis la valeur stockée.
     * Compatible avec l'ancien format texte libre et le nouveau format " | ".
     */
    private void preloadGymSchedule(String storedValue) {
        if (isBlank(storedValue))
            return;
        String[] parts = storedValue.contains(" | ")
                ? storedValue.split(java.util.regex.Pattern.quote(" | "))
                : new String[] { storedValue.trim() };
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty() && !gymScheduleList.getItems().contains(trimmed)) {
                gymScheduleList.getItems().add(trimmed);
            }
        }
    }

    private String resolveBallBrandAndType() {
        List<String> items = ballSelectedList.getItems();
        if (items.isEmpty())
            return null;
        return String.join(" | ", items);
    }

    /**
     * Pré-charge la liste des balles depuis la valeur stockée.
     * Le séparateur est " | " (nouveau format) ou "," (ancien format texte libre).
     */
    private void preloadBallSelection(String storedValue) {
        if (isBlank(storedValue))
            return;

        String[] parts = storedValue.contains(" | ")
                ? storedValue.split(java.util.regex.Pattern.quote(" | "))
                : storedValue.split(",");

        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty() && !ballSelectedList.getItems().contains(trimmed)) {
                ballSelectedList.getItems().add(trimmed);
            }
        }
    }

    // =========================================================================
    // DEFAULTS ET LISTENERS
    // =========================================================================

    private void configureDefaults() {
        organizerContactNameField.setPromptText("Nom de la personne référente");
        organizerEmailField.setPromptText("contact@club.fr");
        organizerPhoneField.setPromptText("06 00 00 00 00");

        numberOfTablesField.setPromptText("Ex : 16");
        tablesBrandField.setPromptText("Ex : Cornilleau 740");
        playingAreaLengthField.setPromptText("Ex : 12");
        playingAreaWidthField.setPromptText("Ex : 6");

        // Balle : ComboBox avec toutes les valeurs ApprovedBall
        ballModelBox.getItems().setAll(ApprovedBall.values());
        ballModelBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(ApprovedBall v) {
                return v == null ? "" : v.label();
            }

            @Override
            public ApprovedBall fromString(String s) {
                return null;
            }
        });
        ballModelBox.setValue(ApprovedBall.BUTTERFLY_R40_PLUS_WHITE);

        // Champ libre visible uniquement si AUTRE sélectionné
        ballCustomField.setPromptText("Précisez la marque et le modèle");
        ballCustomField.setManaged(false);
        ballCustomField.setVisible(false);

        ballModelBox.valueProperty().addListener((obs, o, n) -> {
            boolean custom = n != null && n.requiresCustomInput();
            ballCustomField.setManaged(custom);
            ballCustomField.setVisible(custom);
            if (!custom)
                ballCustomField.clear();
        });

        // Pré-remplissage depuis regulation.ballBrandAndType (liste séparée par ", ")
        preloadBallSelection(regulation.ballBrandAndType());

        // Les labels n'ont pas de prompt — le placeholder "Non renseigné" suffit

        playingAreaChoiceBox.getItems().setAll(PlayingAreaChoice.values());
        playingAreaChoiceBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(PlayingAreaChoice v) {
                if (v == null)
                    return "";
                return switch (v) {
                    case STANDARD -> "Standard recommandé";
                    case CUSTOM -> "Personnalisé";
                };
            }

            @Override
            public PlayingAreaChoice fromString(String s) {
                return null;
            }
        });

        ballProvisionPolicyBox.getItems().setAll(BallProvisionPolicy.values());
        ballProvisionPolicyBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(BallProvisionPolicy v) {
                return v == null ? "" : prettyBallProvisionPolicy(v);
            }

            @Override
            public BallProvisionPolicy fromString(String s) {
                return null;
            }
        });

        // Pré-remplissage depuis le règlement existant
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
                regulation.playingAreaWidthMeters() == null ? ""
                        : String.valueOf(regulation.playingAreaWidthMeters()));

        selectBallProvisionPolicy();

        // Pré-remplissage depuis regulation
        if (!isBlank(regulation.registrationOpenTime())) {
            registrationOpenIso = regulation.registrationOpenTime().trim();
            registrationOpenDisplayLabel.setText(formatIsoToFr(registrationOpenIso));
        }
        if (!isBlank(regulation.registrationDeadline())) {
            registrationDeadlineIso = regulation.registrationDeadline().trim();
            registrationDeadlineDisplayLabel.setText(formatIsoToFr(registrationDeadlineIso));
        }
        // Gymnase : pré-charger la liste depuis la valeur stockée
        preloadGymSchedule(regulation.gymOpenTime());

        refreshPlayingAreaHint();
        applyPlayingAreaDefaults();
        refreshOfficialsRequirementText();

        // Listeners badges
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

        ballProvisionPolicyBox.valueProperty().addListener((obs, o, n) -> refreshSectionBadges());

        refreshSectionBadges();
    }

    private void configureActions() {
        saveButton.setOnAction(e -> onSave());
    }

    // =========================================================================
    // SAUVEGARDE
    // =========================================================================

    private void onSave() {
        try {
            Integer numberOfTables = parseOptionalInteger(
                    numberOfTablesField.getText(), "Le nombre de tables est invalide.");
            Integer playingAreaLength = parseOptionalInteger(
                    playingAreaLengthField.getText(), "La longueur de l'aire de jeu est invalide.");
            Integer playingAreaWidth = parseOptionalInteger(
                    playingAreaWidthField.getText(), "La largeur de l'aire de jeu est invalide.");

            validateOptionalDateTime(
                    registrationOpenIso,
                    "La date/heure d'ouverture des inscriptions est invalide.");
            validateOptionalDateTime(
                    registrationDeadlineIso,
                    "La date/heure de fermeture des inscriptions est invalide.");

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

                    resolveBallBrandAndType(),
                    ballProvisionPolicyBox.getValue() == null ? null
                            : ballProvisionPolicyBox.getValue().name(),

                    optional(registrationOpenIso),
                    optional(registrationDeadlineIso),
                    resolveGymSchedule(),

                    regulation.requiredJudgeGrade(),
                    regulation.recommendedJudgeCount(),
                    regulation.recommendedRefereeGrade(),
                    regulation.recommendedRefereeCount(),
                    collectAllAssignedOfficials(), // <-- remplace safeAssignedOfficials()

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

    // =========================================================================
    // AIRE DE JEU — logique
    // =========================================================================

    private void refreshOfficialsRequirementText() {
        TournamentLevel level = parseTournamentLevel(tournament.level());

        // --- Texte JA : ce qui est OBLIGATOIRE réglementairement ---
        String jaText;
        if (level == null) {
            jaText = "Niveau du tournoi non reconnu — le grade minimum requis pour le juge-arbitre ne peut pas être déterminé.";
        } else {
            jaText = switch (level) {
                case DEPARTEMENTAL ->
                    "⚠ Exigence FFTT : au moins 1 juge-arbitre de grade JA3 est obligatoire.";
                case REGIONAL ->
                    "⚠ Exigence FFTT : au moins 1 juge-arbitre de grade JA3 est obligatoire.";
                case NATIONAL_B ->
                    "⚠ Exigence FFTT : au moins 1 juge-arbitre de grade JA3 est obligatoire.";
                case NATIONAL_A ->
                    "⚠ Exigence FFTT : au moins 1 juge-arbitre de grade JAN est obligatoire.";
                case INTERNATIONAL ->
                    "⚠ Exigence FFTT : au moins 1 juge-arbitre de grade JAN est obligatoire.";
            };
        }
        jaRequirementLabel.setText(jaText);

        // --- Texte Arbitres : recommandations, rien n'est imposé ---
        String refText;
        if (level == null) {
            refText = "Niveau du tournoi non reconnu — aucune recommandation d'arbitres disponible.";
        } else {
            refText = switch (level) {
                case DEPARTEMENTAL ->
                    "Recommandation : aucun arbitre n'est imposé réglementairement. "
                            + "Il est toutefois conseillé de prévoir 2 arbitres pour assurer les demi-finales et la finale.";
                case REGIONAL ->
                    "Recommandation : aucun arbitre n'est imposé réglementairement. "
                            + "Il est conseillé de prévoir au moins 2 arbitres de niveau Régional ou supérieur.";
                case NATIONAL_B ->
                    "Recommandation : aucun arbitre n'est imposé réglementairement. "
                            + "Il est conseillé de prévoir 2 arbitres Régionaux et 1 arbitre National pour la finale.";
                case NATIONAL_A ->
                    "Recommandation : aucun arbitre n'est imposé réglementairement. "
                            + "Il est conseillé de prévoir au moins 2 arbitres Nationaux.";
                case INTERNATIONAL ->
                    "Recommandation : aucun arbitre n'est imposé réglementairement. "
                            + "Il est conseillé de prévoir au moins 2 arbitres Nationaux ou Internationaux.";
            };
        }
        refRecommendationLabel.setText(refText);
    }

    private void selectPlayingAreaChoice() {
        if ("CUSTOM".equalsIgnoreCase(nvl(regulation.playingAreaPreset()))) {
            playingAreaChoiceBox.setValue(PlayingAreaChoice.CUSTOM);
        } else {
            playingAreaChoiceBox.setValue(PlayingAreaChoice.STANDARD);
        }
    }

    private void selectBallProvisionPolicy() {
        if (isBlank(regulation.ballProvisionPolicy()))
            return;
        try {
            ballProvisionPolicyBox.setValue(BallProvisionPolicy.valueOf(regulation.ballProvisionPolicy()));
        } catch (Exception ignored) {
        }
    }

    private void refreshPlayingAreaHint() {
        String text = switch (resolveActualPreset()) {
            case DEPARTEMENTAL_STANDARD ->
                "Aires de jeu conformes à la réglementation FFTT (configuration départementale).";
            case REGIONAL_STANDARD -> "Aires de jeu conformes à la réglementation FFTT (configuration régionale).";
            case NATIONAL_STANDARD -> "Aires de jeu conformes à la réglementation FFTT (configuration nationale).";
            case INTERNATIONAL_STANDARD ->
                "Aires de jeu conformes aux standards internationaux (configuration internationale).";
            case CUSTOM -> "Aires de jeu personnalisées : renseignez librement les dimensions.";
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
        }
    }

    private PlayingAreaPreset resolveActualPreset() {
        return playingAreaChoiceBox.getValue() == PlayingAreaChoice.CUSTOM
                ? PlayingAreaPreset.CUSTOM
                : inferPresetFromTournamentLevel();
    }

    private PlayingAreaPreset inferPresetFromTournamentLevel() {
        TournamentLevel level = parseTournamentLevel(tournament.level());
        if (level == null)
            return PlayingAreaPreset.CUSTOM;
        return switch (level) {
            case DEPARTEMENTAL -> PlayingAreaPreset.DEPARTEMENTAL_STANDARD;
            case REGIONAL -> PlayingAreaPreset.REGIONAL_STANDARD;
            case NATIONAL_A, NATIONAL_B -> PlayingAreaPreset.NATIONAL_STANDARD;
            case INTERNATIONAL -> PlayingAreaPreset.INTERNATIONAL_STANDARD;
        };
    }

    private TournamentLevel parseTournamentLevel(String raw) {
        if (isBlank(raw))
            return null;
        try {
            return TournamentLevel.valueOf(raw.trim());
        } catch (Exception e) {
            return null;
        }
    }

    // =========================================================================
    // VALIDATIONS COMPLÉTUDE SECTIONS
    // =========================================================================

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
        return !ballSelectedList.getItems().isEmpty()
                && ballProvisionPolicyBox.getValue() != null;
    }

    private boolean isTimingSectionComplete() {
        if (isBlank(registrationOpenIso))
            return false;
        if (isBlank(registrationDeadlineIso))
            return false;

        // Chaque jour du tournoi doit avoir au moins un créneau gymnase
        java.util.Set<java.time.LocalDate> days = tournamentDaysSet();
        if (days == null || days.isEmpty()) {
            // Dates du tournoi pas encore définies : on accepte dès qu'un créneau est saisi
            return !gymScheduleList.getItems().isEmpty();
        }

        // Vérifier que chaque jour est couvert
        java.util.Set<java.time.LocalDate> covered = new java.util.HashSet<>();
        for (String item : gymScheduleList.getItems()) {
            try {
                java.time.LocalDate d = java.time.LocalDateTime
                        .parse(item, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
                        .toLocalDate();
                covered.add(d);
            } catch (Exception ignored) {
            }
        }
        return covered.containsAll(days);
    }

    // =========================================================================
    // HELPERS UI
    // =========================================================================

    private static String prettyBallProvisionPolicy(BallProvisionPolicy value) {
        if (value == null)
            return "";
        return value.label();
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
        if (value == null)
            return null;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(message);
        }
    }

    private void validateOptionalDateTime(String raw, String message) {
        String value = optional(raw);
        if (value == null)
            return;
        try {
            // Tente le parse ISO standard (avec ou sans secondes)
            try {
                LocalDateTime.parse(value);
                return; // OK format standard
            } catch (Exception ignored) {
            }
            // Format court "YYYY-MM-DDTHH:MM" produit par notre picker
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            java.time.LocalDateTime.parse(value, fmt);
        } catch (Exception e) {
            throw new IllegalArgumentException(message);
        }
    }

    private String optional(String value) {
        if (value == null)
            return null;
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
}