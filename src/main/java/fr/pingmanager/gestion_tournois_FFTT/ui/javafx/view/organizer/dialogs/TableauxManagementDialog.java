package fr.pingmanager.gestion_tournois_FFTT.ui.javafx.view.organizer.dialogs;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.enums.GenderPolicy;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.app.AppRouter;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TableauDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TournamentDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TournamentRegulationDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;

public class TableauxManagementDialog extends Stage {

    private final AppRouter nav;
    private TournamentDto tournament;
    private final TournamentRegulationDto regulation;

    private final TableView<TableauRow> table = new TableView<>();

    // Section algorithme de tirage
    private final ComboBox<AlgoItem> algoBox = new ComboBox<>();

    /** Wrapper d'affichage pour l'algo de tirage dans la ComboBox. */
    private record AlgoItem(String value, String label, String description) {
        @Override
        public String toString() {
            return label;
        }
    }

    public TableauxManagementDialog(
            AppRouter nav,
            TournamentDto tournament,
            TournamentRegulationDto regulation) {

        this.nav = Objects.requireNonNull(nav, "nav must not be null");
        this.tournament = Objects.requireNonNull(tournament, "tournament must not be null");
        this.regulation = Objects.requireNonNull(regulation, "regulation must not be null");

        initModality(Modality.APPLICATION_MODAL);
        initOwner(nav.primaryStage());
        setTitle("Gestion des tableaux — " + tournament.name());

        build();
        configureTable();
        loadTableaux();
    }

    // =========================================================================
    // CONSTRUCTION DE L'INTERFACE
    // =========================================================================

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

        // ---- Section algorithme de tirage (niveau tournoi) ----
        VBox algoSection = buildAlgoSection();

        // ---- Bouton ajouter ----
        Button addButton = new Button("+ Ajouter un tableau");
        AppTheme.stylePrimary(addButton);
        addButton.setOnAction(e -> onAddTableau());

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        HBox topActions = new HBox(12, topSpacer, addButton);
        topActions.setAlignment(Pos.CENTER_RIGHT);

        // ---- Tableau des tableaux ----
        table.setPlaceholder(new Label("Aucun tableau défini pour le moment."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        VBox.setVgrow(table, Priority.ALWAYS);

        // ---- Bouton fermer uniquement ----
        Button closeButton = new Button("Fermer");
        AppTheme.styleSecondary(closeButton);
        closeButton.setOnAction(e -> close());

        Region bottomSpacer = new Region();
        HBox.setHgrow(bottomSpacer, Priority.ALWAYS);

        HBox bottomActions = new HBox(12, bottomSpacer, closeButton);
        bottomActions.setAlignment(Pos.CENTER_RIGHT);

        VBox card = AppTheme.card(table);
        card.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(card, Priority.ALWAYS);

        root.getChildren().addAll(
                title,
                subtitle,
                algoSection,
                topActions,
                card,
                bottomActions);

        setScene(new Scene(root));
        AppTheme.applyLargeDialogWindow(this);
    }

    /**
     * Construit la section de sélection de l'algorithme de tirage.
     * L'algo s'applique à l'ensemble des tableaux du tournoi.
     */
    private VBox buildAlgoSection() {
        Label sectionTitle = new Label("Algorithme de tirage des poules");
        AppTheme.applyCardTitle(sectionTitle);

        Label sectionDesc = new Label(
                "L'algorithme choisi s'applique à tous les tableaux de ce tournoi. "
                        + "Il détermine comment les joueurs sont répartis dans les poules.");
        AppTheme.applyBody(sectionDesc);
        sectionDesc.setWrapText(true);

        // ComboBox avec les deux algorithmes disponibles
        List<AlgoItem> items = List.of(
                new AlgoItem("SNAKE", "Serpent FFTT (recommandé)",
                        "Les joueurs sont répartis en zigzag selon leur classement."),
                new AlgoItem("RANDOM", "Tirage aléatoire",
                        "Les joueurs sont répartis aléatoirement dans les poules."));
        algoBox.setItems(FXCollections.observableArrayList(items));

        algoBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(AlgoItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(item.label() + "  —  " + item.description());
            }
        });
        algoBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(AlgoItem item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.label());
            }
        });

        // Initialiser depuis le tournoi courant
        String current = tournament.drawAlgorithmType() != null
                ? tournament.drawAlgorithmType()
                : "SNAKE";
        items.stream()
                .filter(i -> i.value().equals(current))
                .findFirst()
                .ifPresent(algoBox::setValue);

        algoBox.setMaxWidth(400);

        Button saveAlgoButton = new Button("Sauvegarder");
        AppTheme.stylePrimary(saveAlgoButton);
        saveAlgoButton.setOnAction(e -> onSaveAlgo());

        HBox algoRow = new HBox(AppTheme.SPACE_MD, algoBox, saveAlgoButton);
        algoRow.setAlignment(Pos.CENTER_LEFT);

        VBox section = AppTheme.card(new VBox(AppTheme.SPACE_SM, sectionTitle, sectionDesc, algoRow));
        section.setMaxWidth(Double.MAX_VALUE);
        return section;
    }

    // =========================================================================
    // TABLE
    // =========================================================================

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

        TableColumn<TableauRow, String> formulaCol = new TableColumn<>("Formule poules");
        formulaCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().formula()));

        TableColumn<TableauRow, String> scheduleCol = new TableColumn<>("Horaires");
        scheduleCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().schedule()));

        TableColumn<TableauRow, String> capacityCol = new TableColumn<>("Capacité");
        capacityCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().capacity()));

        TableColumn<TableauRow, String> feeCol = new TableColumn<>("Frais");
        feeCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().fee()));

        // Double-clic → éditer le tableau
        table.setRowFactory(tv -> {
            javafx.scene.control.TableRow<TableauRow> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    onEditTableau(row.getItem());
                }
            });
            return row;
        });

        table.getColumns().setAll(List.of(
                statusCol,
                codeCol,
                designationCol,
                genderCol,
                categoriesCol,
                pointsCol,
                formulaCol,
                scheduleCol,
                capacityCol,
                feeCol));
    }

    // =========================================================================
    // ACTIONS
    // =========================================================================

    /** Sauvegarde l'algorithme de tirage au niveau du tournoi. */
    private void onSaveAlgo() {
        AlgoItem selected = algoBox.getValue();
        if (selected == null)
            return;

        try {
            TournamentDto updated = new TournamentDto(
                    tournament.id(),
                    tournament.clubId(),
                    tournament.organizerId(),
                    tournament.name(),
                    tournament.address1(),
                    tournament.address2(),
                    tournament.city(),
                    tournament.department(),
                    tournament.level(),
                    tournament.phase(),
                    tournament.startDate(),
                    tournament.endDate(),
                    tournament.homologationNumber(),
                    tournament.status(),
                    selected.value(),
                    tournament.createdAt(),
                    LocalDateTime.now().toString());

            nav.tournamentService().updateGeneral(updated);
            this.tournament = updated;

            nav.showInfo("Algorithme sauvegardé",
                    "L'algorithme « " + selected.label() + " » sera utilisé pour tous les tableaux.");
        } catch (Exception ex) {
            nav.showInfo("Erreur", safeMessage(ex));
        }
    }

    private void onAddTableau() {
        List<LocalDate> days = buildTournamentDays();
        if (days.isEmpty()) {
            nav.showInfo("Dates manquantes",
                    "Veuillez d'abord définir les dates du tournoi avant d'ajouter un tableau.");
            return;
        }

        CreateOrEditTableauDialog dialog = new CreateOrEditTableauDialog(
                nav, tournament.id(), days, regulation, null);
        dialog.showAndWait();

        TableauDto created = dialog.result();
        if (created == null)
            return;

        try {
            nav.tournamentService().createTableau(created);
            loadTableaux();
        } catch (Exception ex) {
            nav.showInfo("Erreur", safeMessage(ex));
        }
    }

    private void onEditTableau(TableauRow row) {
        List<LocalDate> days = buildTournamentDays();

        try {
            TableauDto existing = nav.tournamentService()
                    .findTableauxByTournamentId(tournament.id())
                    .stream()
                    .filter(t -> safe(t.code()).equals(row.code()))
                    .findFirst()
                    .orElse(null);

            if (existing == null) {
                nav.showInfo("Erreur", "Tableau introuvable.");
                return;
            }

            CreateOrEditTableauDialog dialog = new CreateOrEditTableauDialog(
                    nav, tournament.id(), days, regulation, existing);
            dialog.showAndWait();

            TableauDto updated = dialog.result();
            if (updated == null)
                return;

            nav.tournamentService().updateTableau(updated);
            loadTableaux();

        } catch (Exception ex) {
            nav.showInfo("Erreur", safeMessage(ex));
        }
    }

    // =========================================================================
    // CHARGEMENT
    // =========================================================================

    private void loadTableaux() {
        table.getItems().clear();
        try {
            List<TableauDto> tableaux = nav.tournamentService()
                    .findTableauxByTournamentId(tournament.id());
            for (TableauDto dto : tableaux) {
                table.getItems().add(toRow(dto));
            }
        } catch (Exception ex) {
            nav.showInfo("Erreur", safeMessage(ex));
        }
    }

    // =========================================================================
    // FORMATAGE
    // =========================================================================

    private TableauRow toRow(TableauDto dto) {
        return new TableauRow(
                computeStatus(dto),
                safe(dto.code()),
                safe(dto.designation()),
                formatGender(dto.genderPolicy()),
                formatCategories(dto),
                formatPoints(dto),
                formatFormula(dto),
                formatSchedule(dto),
                formatCapacity(dto),
                formatFee(dto));
    }

    private String computeStatus(TableauDto dto) {
        boolean complete = !isBlank(dto.code())
                && !isBlank(dto.designation())
                && !isBlank(dto.date())
                && !isBlank(dto.genderPolicy())
                && !isBlank(dto.pointsRuleType())
                && dto.maxPlayers() != null
                && !isBlank(dto.checkInEnd())
                && !isBlank(dto.startTime())
                && dto.prepaidFee() != null
                && dto.onSiteFee() != null;

        if (complete)
            return "Complet";

        boolean partial = !isBlank(dto.code())
                || !isBlank(dto.designation())
                || !isBlank(dto.date())
                || !isBlank(dto.genderPolicy());

        return partial ? "Partiel" : "Vide";
    }

    private String formatGender(String value) {
        if (value == null || value.isBlank())
            return "—";
        try {
            return GenderPolicy.valueOf(value.trim().toUpperCase()).label();
        } catch (Exception e) {
            return safe(value);
        }
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
        if ("TOUTES_SERIES".equalsIgnoreCase(dto.pointsRuleType()))
            return "Toutes séries";
        if ("MAX_ONLY".equalsIgnoreCase(dto.pointsRuleType())) {
            return dto.maxPoints() == null ? "Max" : "Max " + dto.maxPoints();
        }
        if ("RANGE_MIN_MAX".equalsIgnoreCase(dto.pointsRuleType())) {
            return safeNumber(dto.minPoints()) + " - " + safeNumber(dto.maxPoints());
        }
        return safe(dto.pointsRuleType());
    }

    /**
     * Formate la formule de poule : taille + qualifiés.
     * Ex : "Poules de 3 — 2 qualifiés"
     */
    private String formatFormula(TableauDto dto) {
        int poolSize = dto.poolSize() != null ? dto.poolSize() : 3;
        int qualified = dto.qualifiedPerPool() != null ? dto.qualifiedPerPool() : 2;
        return "Poules de " + poolSize + " — " + qualified + " qualifié" + (qualified > 1 ? "s" : "");
    }

    private String formatSchedule(TableauDto dto) {
        String checkIn = safe(dto.checkInEnd());
        String start = safe(dto.startTime());
        if ("—".equals(checkIn) && "—".equals(start))
            return "—";
        return "Pointage " + checkIn + " | Début " + start;
    }

    private String formatCapacity(TableauDto dto) {
        return dto.maxPlayers() == null ? "—" : dto.maxPlayers() + " joueurs";
    }

    private String formatFee(TableauDto dto) {
        if (dto.prepaidFee() == null && dto.onSiteFee() == null)
            return "—";
        return safeNumber(dto.prepaidFee()) + "€ / " + safeNumber(dto.onSiteFee()) + "€";
    }

    // =========================================================================
    // UTILITAIRES
    // =========================================================================

    private List<LocalDate> buildTournamentDays() {
        String rawStart = tournament.startDate();
        String rawEnd = tournament.endDate();

        if (rawStart == null || rawStart.isBlank() || rawEnd == null || rawEnd.isBlank()) {
            return List.of();
        }
        try {
            LocalDate start = LocalDate.parse(rawStart.trim());
            LocalDate end = LocalDate.parse(rawEnd.trim());
            if (end.isBefore(start))
                return List.of();

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

    // =========================================================================
    // RECORD DE LIGNE
    // =========================================================================

    private record TableauRow(
            String status,
            String code,
            String designation,
            String gender,
            String categories,
            String pointsRule,
            String formula,
            String schedule,
            String capacity,
            String fee) {
    }
}