package fr.pingmanager.gestion_tournois_FFTT.ui.javafx.view.organizer.pages;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.app.AppRouter;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TableauDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TournamentDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TournamentRegulationDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.view.organizer.dialogs.CreateOrEditTableauDialog;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.view.organizer.layout.TournamentSection;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.enums.GenderPolicy;

/**
 * Vue inline de gestion des tableaux d'un tournoi.
 * Remplace l'ancien TableauxManagementDialog (popup).
 * Intègre la sélection de l'algorithme de tirage (niveau tournoi)
 * et la liste des tableaux avec leur formule de poule.
 */
public class TournamentTableauxView extends VBox {

    private final AppRouter nav;
    private TournamentDto tournament;
    private final TournamentRegulationDto regulation;

    private final TableView<TableauRow> table = new TableView<>();
    private final ComboBox<AlgoItem> algoBox = new ComboBox<>();

    private record AlgoItem(String value, String label, String description) {
        @Override
        public String toString() {
            return label;
        }
    }

    private record TableauRow(
            String status, String code, String designation,
            String gender, String categories, String pointsRule,
            String formula, String schedule, String capacity, String fee) {
    }

    public TournamentTableauxView(AppRouter nav,
            TournamentDto tournament,
            TournamentRegulationDto regulation) {
        this.nav = Objects.requireNonNull(nav);
        this.tournament = Objects.requireNonNull(tournament);
        this.regulation = regulation;
        build();
    }

    // =========================================================================
    // CONSTRUCTION
    // =========================================================================

    private void build() {
        AppTheme.applyPage(this);

        VBox root = new VBox(AppTheme.SPACE_LG);
        root.setPadding(new Insets(28));
        root.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(root, Priority.ALWAYS);

        root.getChildren().add(buildHeader());
        root.getChildren().add(buildAlgoSection());
        root.getChildren().add(buildTableauxSection());

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        getChildren().setAll(scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        loadTableaux();
    }

    private VBox buildHeader() {
        Label title = new Label("Tableaux");
        AppTheme.applyTitle(title);

        Label subtitle = new Label(
                "Définissez les tableaux du tournoi, l'algorithme de tirage et la formule de qualification de chaque tableau.");
        AppTheme.applyBody(subtitle);
        subtitle.setWrapText(true);

        return new VBox(AppTheme.SPACE_SM, title, subtitle);
    }

    // ---- Section algorithme de tirage ----

    private VBox buildAlgoSection() {
        Label sectionTitle = new Label("Algorithme de tirage des poules");
        AppTheme.applyCardTitle(sectionTitle);

        Label sectionDesc = new Label(
                "S'applique à tous les tableaux de ce tournoi. "
                        + "Détermine comment les joueurs sont répartis dans les poules.");
        AppTheme.applyBody(sectionDesc);
        sectionDesc.setWrapText(true);

        List<AlgoItem> items = List.of(
                new AlgoItem("SNAKE", "Serpent FFTT (recommandé)",
                        "Répartition en zigzag selon le classement."),
                new AlgoItem("RANDOM", "Tirage aléatoire",
                        "Répartition aléatoire dans les poules."));
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
        String current = tournament.drawAlgorithmType() != null
                ? tournament.drawAlgorithmType()
                : "SNAKE";
        items.stream().filter(i -> i.value().equals(current))
                .findFirst().ifPresent(algoBox::setValue);
        algoBox.setMaxWidth(420);

        Button saveAlgoBtn = new Button("Sauvegarder");
        AppTheme.stylePrimary(saveAlgoBtn);
        saveAlgoBtn.setOnAction(e -> onSaveAlgo());

        HBox algoRow = new HBox(AppTheme.SPACE_MD, algoBox, saveAlgoBtn);
        algoRow.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(AppTheme.SPACE_SM, sectionTitle, sectionDesc, algoRow);
        VBox card = AppTheme.card(content);
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    // ---- Section liste des tableaux ----

    private VBox buildTableauxSection() {
        Label sectionTitle = new Label("Liste des tableaux");
        AppTheme.applyCardTitle(sectionTitle);

        Button addBtn = new Button("+ Ajouter un tableau");
        AppTheme.stylePrimary(addBtn);
        addBtn.setOnAction(e -> onAddTableau());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(spacer, addBtn);
        topBar.setAlignment(Pos.CENTER_RIGHT);

        configureTable();
        table.setPlaceholder(new Label("Aucun tableau défini pour le moment."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefHeight(360);
        VBox.setVgrow(table, Priority.ALWAYS);

        Label hint = new Label("Double-cliquez sur un tableau pour le modifier.");
        AppTheme.applyBody(hint);
        hint.setStyle("-fx-text-fill: #94A3B8; -fx-font-style: italic;");

        VBox content = new VBox(AppTheme.SPACE_MD, sectionTitle, topBar, table, hint);
        VBox card = AppTheme.card(content);
        card.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(card, Priority.ALWAYS);
        return card;
    }

    private void configureTable() {
        TableColumn<TableauRow, String> statusCol = new TableColumn<>("État");
        statusCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().status()));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);
                setStyle("Complet".equalsIgnoreCase(item)
                        ? "-fx-text-fill: #2E7D32; -fx-font-weight: 700;"
                        : "Partiel".equalsIgnoreCase(item)
                                ? "-fx-text-fill: #EF6C00; -fx-font-weight: 700;"
                                : "-fx-text-fill: #64748B; -fx-font-weight: 700;");
            }
        });

        TableColumn<TableauRow, String> codeCol = col("Code", r -> r.code());
        TableColumn<TableauRow, String> designationCol = col("Désignation", r -> r.designation());
        TableColumn<TableauRow, String> genderCol = col("Sexe", r -> r.gender());
        TableColumn<TableauRow, String> categoriesCol = col("Catégories", r -> r.categories());
        TableColumn<TableauRow, String> pointsCol = col("Points", r -> r.pointsRule());
        TableColumn<TableauRow, String> formulaCol = col("Formule", r -> r.formula());
        TableColumn<TableauRow, String> scheduleCol = col("Horaires", r -> r.schedule());
        TableColumn<TableauRow, String> capacityCol = col("Capacité", r -> r.capacity());
        TableColumn<TableauRow, String> feeCol = col("Frais", r -> r.fee());

        table.setRowFactory(tv -> {
            TableRow<TableauRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    onEditTableau(row.getItem());
                }
            });
            return row;
        });

        table.getColumns().setAll(List.of(
                statusCol, codeCol, designationCol, genderCol,
                categoriesCol, pointsCol, formulaCol,
                scheduleCol, capacityCol, feeCol));
    }

    private <T> TableColumn<T, String> col(String title,
            java.util.function.Function<T, String> extractor) {
        TableColumn<T, String> col = new TableColumn<>(title);
        col.setCellValueFactory(c -> new ReadOnlyStringWrapper(extractor.apply(c.getValue())));
        return col;
    }

    // =========================================================================
    // ACTIONS
    // =========================================================================

    private void onSaveAlgo() {
        AlgoItem selected = algoBox.getValue();
        if (selected == null)
            return;
        try {
            TournamentDto updated = new TournamentDto(
                    tournament.id(), tournament.clubId(), tournament.organizerId(),
                    tournament.name(), tournament.address1(), tournament.address2(),
                    tournament.city(), tournament.department(), tournament.level(),
                    tournament.phase(), tournament.startDate(), tournament.endDate(),
                    tournament.homologationNumber(), tournament.status(),
                    selected.value(),
                    tournament.createdAt(), LocalDateTime.now().toString());
            nav.tournamentService().updateGeneral(updated);
            this.tournament = updated;
            nav.showInfo("Sauvegardé", "Algorithme « " + selected.label() + " » enregistré.");
        } catch (Exception ex) {
            nav.showInfo("Erreur", ex.getMessage());
        }
    }

    private void onAddTableau() {
        List<LocalDate> days = buildDays();
        if (days.isEmpty()) {
            nav.showInfo("Dates manquantes",
                    "Définissez d'abord les dates du tournoi dans la section Général.");
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
            nav.showInfo("Erreur", ex.getMessage());
        }
    }

    private void onEditTableau(TableauRow row) {
        List<LocalDate> days = buildDays();
        try {
            TableauDto existing = nav.tournamentService()
                    .findTableauxByTournamentId(tournament.id())
                    .stream()
                    .filter(t -> safe(t.code()).equals(row.code()))
                    .findFirst().orElse(null);
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
            nav.showInfo("Erreur", ex.getMessage());
        }
    }

    // =========================================================================
    // CHARGEMENT & FORMATAGE
    // =========================================================================

    private void loadTableaux() {
        table.getItems().clear();
        try {
            nav.tournamentService().findTableauxByTournamentId(tournament.id())
                    .forEach(dto -> table.getItems().add(toRow(dto)));
        } catch (Exception ex) {
            nav.showInfo("Erreur", ex.getMessage());
        }
    }

    private TableauRow toRow(TableauDto dto) {
        return new TableauRow(
                computeStatus(dto), safe(dto.code()), safe(dto.designation()),
                formatGender(dto.genderPolicy()), formatCategories(dto),
                formatPoints(dto), formatFormula(dto),
                formatSchedule(dto), formatCapacity(dto), formatFee(dto));
    }

    private String computeStatus(TableauDto dto) {
        boolean complete = !isBlank(dto.code()) && !isBlank(dto.designation())
                && !isBlank(dto.date()) && !isBlank(dto.genderPolicy())
                && !isBlank(dto.pointsRuleType()) && dto.maxPlayers() != null
                && !isBlank(dto.checkInEnd()) && !isBlank(dto.startTime())
                && dto.prepaidFee() != null && dto.onSiteFee() != null;
        if (complete)
            return "Complet";
        boolean partial = !isBlank(dto.code()) || !isBlank(dto.designation())
                || !isBlank(dto.date()) || !isBlank(dto.genderPolicy());
        return partial ? "Partiel" : "Vide";
    }

    private String formatGender(String v) {
        if (isBlank(v))
            return "—";
        try {
            return GenderPolicy.valueOf(v.trim().toUpperCase()).label();
        } catch (Exception e) {
            return safe(v);
        }
    }

    private String formatCategories(TableauDto dto) {
        if (isBlank(dto.agePolicyType()) || "ANY".equalsIgnoreCase(dto.agePolicyType()))
            return "Toutes catégories";
        if ("RANGE".equalsIgnoreCase(dto.agePolicyType()))
            return safe(dto.ageMinCategory()) + " à " + safe(dto.ageMaxCategory());
        if ("ALLOWED_SET".equalsIgnoreCase(dto.agePolicyType())) {
            if (dto.allowedAgeCategories() == null || dto.allowedAgeCategories().isEmpty())
                return "Sélection manuelle";
            return String.join(", ", dto.allowedAgeCategories());
        }
        return safe(dto.agePolicyType());
    }

    private String formatPoints(TableauDto dto) {
        if ("TOUTES_SERIES".equalsIgnoreCase(dto.pointsRuleType()))
            return "Toutes séries";
        if ("MAX_ONLY".equalsIgnoreCase(dto.pointsRuleType()))
            return dto.maxPoints() == null ? "Max" : "Max " + dto.maxPoints();
        if ("RANGE_MIN_MAX".equalsIgnoreCase(dto.pointsRuleType()))
            return safeNum(dto.minPoints()) + " - " + safeNum(dto.maxPoints());
        return safe(dto.pointsRuleType());
    }

    private String formatFormula(TableauDto dto) {
        int ps = dto.poolSize() != null ? dto.poolSize() : 3;
        int qp = dto.qualifiedPerPool() != null ? dto.qualifiedPerPool() : 2;
        return "Poules de " + ps + " — " + qp + " qualifié" + (qp > 1 ? "s" : "");
    }

    private String formatSchedule(TableauDto dto) {
        String ci = safe(dto.checkInEnd()), st = safe(dto.startTime());
        if ("—".equals(ci) && "—".equals(st))
            return "—";
        return "Pointage " + ci + " | Début " + st;
    }

    private String formatCapacity(TableauDto dto) {
        return dto.maxPlayers() == null ? "—" : dto.maxPlayers() + " joueurs";
    }

    private String formatFee(TableauDto dto) {
        if (dto.prepaidFee() == null && dto.onSiteFee() == null)
            return "—";
        return safeNum(dto.prepaidFee()) + "€ / " + safeNum(dto.onSiteFee()) + "€";
    }

    private List<LocalDate> buildDays() {
        try {
            if (isBlank(tournament.startDate()) || isBlank(tournament.endDate()))
                return List.of();
            LocalDate start = LocalDate.parse(tournament.startDate().trim());
            LocalDate end = LocalDate.parse(tournament.endDate().trim());
            if (end.isBefore(start))
                return List.of();
            List<LocalDate> days = new ArrayList<>();
            LocalDate cur = start;
            while (!cur.isAfter(end)) {
                days.add(cur);
                cur = cur.plusDays(1);
            }
            return days;
        } catch (Exception e) {
            return List.of();
        }
    }

    private static boolean isBlank(String v) {
        return v == null || v.isBlank();
    }

    private static String safe(String v) {
        return isBlank(v) ? "—" : v.trim();
    }

    private static String safeNum(Integer v) {
        return v == null ? "—" : String.valueOf(v);
    }
}