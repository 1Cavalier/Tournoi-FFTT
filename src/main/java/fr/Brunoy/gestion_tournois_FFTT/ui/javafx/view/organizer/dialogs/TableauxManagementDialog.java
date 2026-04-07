package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.dialogs;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.AppRouter;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.TableauDto;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.TournamentDto;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.TournamentRegulationDto;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TableauxManagementDialog extends Stage {

    private final AppRouter nav;
    private final TournamentDto tournament;
    private final TournamentRegulationDto regulation;

    private final TableView<TableauRow> table = new TableView<>();

    public TableauxManagementDialog(
            AppRouter nav,
            TournamentDto tournament,
            TournamentRegulationDto regulation) {

        this.nav = Objects.requireNonNull(nav, "nav must not be null");
        this.tournament = Objects.requireNonNull(tournament, "tournament must not be null");
        this.regulation = Objects.requireNonNull(regulation, "regulation must not be null");

        initModality(Modality.APPLICATION_MODAL);
        initOwner(nav.primaryStage());
        setTitle("Gestion des tableaux");

        build();
        configureTable();
        loadTableaux();
    }

    private void build() {
        VBox root = new VBox(AppTheme.SPACE_LG);
        root.setPadding(new Insets(20));
        AppTheme.applyPage(root);

        Label title = new Label("Gestion des tableaux");
        AppTheme.applyTitle(title);

        Label subtitle = new Label(
                "Définissez les tableaux du tournoi, leurs règles sportives, leurs horaires et leurs paramètres principaux.");
        AppTheme.applyBody(subtitle);
        subtitle.setWrapText(true);

        Button addButton = new Button("Ajouter un tableau");
        AppTheme.stylePrimary(addButton);
        addButton.setOnAction(e -> onAddTableau());

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        HBox topActions = new HBox(12, topSpacer, addButton);
        topActions.setAlignment(Pos.CENTER_LEFT);

        table.setPlaceholder(new Label("Aucun tableau défini pour le moment."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        VBox.setVgrow(table, Priority.ALWAYS);

        Button planningButton = new Button("Voir planning");
        AppTheme.styleSecondary(planningButton);
        planningButton.setOnAction(e -> nav.showInfo("À venir", "Visualisation du planning des tableaux."));

        Button refreshButton = new Button("Rafraîchir");
        AppTheme.styleSecondary(refreshButton);
        refreshButton.setOnAction(e -> loadTableaux());

        Button closeButton = new Button("Fermer");
        AppTheme.styleSecondary(closeButton);
        closeButton.setOnAction(e -> close());

        Region bottomSpacer = new Region();
        HBox.setHgrow(bottomSpacer, Priority.ALWAYS);

        HBox bottomActions = new HBox(12, planningButton, refreshButton, bottomSpacer, closeButton);
        bottomActions.setAlignment(Pos.CENTER_LEFT);

        VBox card = AppTheme.card(table);
        card.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(card, Priority.ALWAYS);

        root.getChildren().addAll(title, subtitle, topActions, card, bottomActions);

        setScene(new Scene(root));
        AppTheme.applyLargeDialogWindow(this);
    }

    private void configureTable() {
        TableColumn<TableauRow, String> statusCol = new TableColumn<>("État");
        statusCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().status()));
        statusCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                setText(item);

                if ("Complet".equalsIgnoreCase(item)) {
                    setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: 700;");
                } else if ("Partiel".equalsIgnoreCase(item)) {
                    setStyle("-fx-text-fill: #EF6C00; -fx-font-weight: 700;");
                } else {
                    setStyle("-fx-text-fill: #64748B; -fx-font-weight: 700;");
                }
            }
        });

        TableColumn<TableauRow, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().code()));

        TableColumn<TableauRow, String> designationCol = new TableColumn<>("Désignation");
        designationCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().designation()));

        TableColumn<TableauRow, String> genderCol = new TableColumn<>("Sexe");
        genderCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().gender()));

        TableColumn<TableauRow, String> categoriesCol = new TableColumn<>("Catégories");
        categoriesCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().categories()));

        TableColumn<TableauRow, String> pointsCol = new TableColumn<>("Points");
        pointsCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().pointsRule()));

        TableColumn<TableauRow, String> scheduleCol = new TableColumn<>("Horaires");
        scheduleCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().schedule()));

        TableColumn<TableauRow, String> capacityCol = new TableColumn<>("Capacité");
        capacityCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().capacity()));

        TableColumn<TableauRow, String> feeCol = new TableColumn<>("Frais");
        feeCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().fee()));

        table.getColumns().setAll(
                statusCol,
                codeCol,
                designationCol,
                genderCol,
                categoriesCol,
                pointsCol,
                scheduleCol,
                capacityCol,
                feeCol);
    }

    private void onAddTableau() {
        List<LocalDate> days = buildTournamentDays();

        if (days.isEmpty()) {
            nav.showInfo("Dates manquantes",
                    "Veuillez d'abord définir les dates du tournoi avant d'ajouter un tableau.");
            return;
        }

        CreateOrEditTableauDialog dialog = new CreateOrEditTableauDialog(
                nav,
                tournament.id(),
                days,
                regulation,
                null);
        dialog.showAndWait();

        TableauDto created = dialog.result();
        if (created == null) {
            return;
        }

        try {
            nav.tournamentService().createTableau(created);
            loadTableaux();
        } catch (Exception ex) {
            nav.showInfo("Erreur", safeMessage(ex));
        }
    }

    private void loadTableaux() {
        table.getItems().clear();

        try {
            List<TableauDto> tableaux = nav.tournamentService().findTableauxByTournamentId(tournament.id());
            for (TableauDto dto : tableaux) {
                table.getItems().add(toRow(dto));
            }
        } catch (Exception ex) {
            nav.showInfo("Erreur", safeMessage(ex));
        }
    }

    private List<LocalDate> buildTournamentDays() {
        String rawStart = tournament.startDate();
        String rawEnd = tournament.endDate();

        if (rawStart == null || rawStart.isBlank() || rawEnd == null || rawEnd.isBlank()) {
            return List.of();
        }

        try {
            LocalDate start = LocalDate.parse(rawStart.trim());
            LocalDate end = LocalDate.parse(rawEnd.trim());

            if (end.isBefore(start)) {
                return List.of();
            }

            List<LocalDate> days = new ArrayList<>();
            LocalDate current = start;
            while (!current.isAfter(end)) {
                days.add(current);
                current = current.plusDays(1);
            }
            return days;

        } catch (Exception e) {
            return List.of();
        }
    }

    private TableauRow toRow(TableauDto dto) {
        return new TableauRow(
                computeStatus(dto),
                safe(dto.code()),
                safe(dto.designation()),
                formatGender(dto.genderPolicy()),
                formatCategories(dto),
                formatPoints(dto),
                formatSchedule(dto),
                formatCapacity(dto),
                formatFee(dto));
    }

    private String computeStatus(TableauDto dto) {
        boolean hasCode = !isBlank(dto.code());
        boolean hasDesignation = !isBlank(dto.designation());
        boolean hasDate = !isBlank(dto.date());
        boolean hasGender = !isBlank(dto.genderPolicy());
        boolean hasPointsRule = !isBlank(dto.pointsRuleType());
        boolean hasMaxPlayers = dto.maxPlayers() != null;
        boolean hasCheckInEnd = !isBlank(dto.checkInEnd());
        boolean hasStartTime = !isBlank(dto.startTime());
        boolean hasFees = dto.prepaidFee() != null && dto.onSiteFee() != null;

        boolean complete = hasCode
                && hasDesignation
                && hasDate
                && hasGender
                && hasPointsRule
                && hasMaxPlayers
                && hasCheckInEnd
                && hasStartTime
                && hasFees;

        if (complete) {
            return "Complet";
        }

        boolean partial = hasCode
                || hasDesignation
                || hasDate
                || hasGender
                || hasPointsRule
                || hasMaxPlayers
                || hasCheckInEnd
                || hasStartTime
                || hasFees;

        return partial ? "Partiel" : "Vide";
    }

    private String formatGender(String value) {
        if ("FEMININ_ONLY".equalsIgnoreCase(value)) {
            return "Féminin";
        }
        if ("MIXTE".equalsIgnoreCase(value)) {
            return "Mixte";
        }
        return safe(value);
    }

    private String formatCategories(TableauDto dto) {
        if (dto.agePolicyType() == null || dto.agePolicyType().isBlank()
                || "ANY".equalsIgnoreCase(dto.agePolicyType())) {
            return "Toutes catégories";
        }

        if ("RANGE".equalsIgnoreCase(dto.agePolicyType())) {
            return safe(dto.ageMinCategory()) + " à " + safe(dto.ageMaxCategory());
        }

        if ("ALLOWED_SET".equalsIgnoreCase(dto.agePolicyType())) {
            if (dto.allowedAgeCategories() == null || dto.allowedAgeCategories().isEmpty()) {
                return "Sélection manuelle";
            }
            return String.join(", ", dto.allowedAgeCategories());
        }

        return safe(dto.agePolicyType());
    }

    private String formatPoints(TableauDto dto) {
        if ("TOUTES_SERIES".equalsIgnoreCase(dto.pointsRuleType())) {
            return "Toutes séries";
        }
        if ("MAX_ONLY".equalsIgnoreCase(dto.pointsRuleType())) {
            return dto.maxPoints() == null ? "Max" : "Max " + dto.maxPoints();
        }
        if ("RANGE_MIN_MAX".equalsIgnoreCase(dto.pointsRuleType())) {
            return safeNumber(dto.minPoints()) + " - " + safeNumber(dto.maxPoints());
        }
        return safe(dto.pointsRuleType());
    }

    private String formatSchedule(TableauDto dto) {
        String checkIn = safe(dto.checkInEnd());
        String start = safe(dto.startTime());

        if ("—".equals(checkIn) && "—".equals(start)) {
            return "—";
        }
        return "Pointage " + checkIn + " • Début " + start;
    }

    private String formatCapacity(TableauDto dto) {
        return dto.maxPlayers() == null ? "—" : dto.maxPlayers() + " joueurs";
    }

    private String formatFee(TableauDto dto) {
        if (dto.prepaidFee() == null && dto.onSiteFee() == null) {
            return "—";
        }
        return safeNumber(dto.prepaidFee()) + "€ / " + safeNumber(dto.onSiteFee()) + "€";
    }

    private String safeMessage(Exception ex) {
        String message = ex.getMessage();
        return (message == null || message.isBlank()) ? "Une erreur est survenue." : message;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String safe(String value) {
        return value == null || value.isBlank() ? "—" : value.trim();
    }

    private static String safeNumber(Integer value) {
        return value == null ? "—" : String.valueOf(value);
    }

    private record TableauRow(
            String status,
            String code,
            String designation,
            String gender,
            String categories,
            String pointsRule,
            String schedule,
            String capacity,
            String fee) {
    }
}