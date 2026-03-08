package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.dialogs;

import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.entity.Tableau;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.FemaleExtraRuleType;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.TournamentLevel;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.RankingPhase;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class CreateTournamentDialog extends Stage {

    public CreateTournamentDialog(Navigator nav) {
        setTitle("Créer un tournoi");
        initModality(Modality.APPLICATION_MODAL);

        OrganizerAccount org = nav.getCurrentOrganizer();
        if (org == null) {
            close();
            return;
        }

        // ================== Champs tournoi ==================

        TextField name = new TextField();
        name.setPromptText("Nom du tournoi");

        ComboBox<TournamentLevel> level = new ComboBox<>();
        level.getItems().addAll(TournamentLevel.values());
        level.setMaxWidth(Double.MAX_VALUE);

        ComboBox<RankingPhase> rankingPhase = new ComboBox<>();
        rankingPhase.getItems().addAll(RankingPhase.values());
        rankingPhase.setMaxWidth(Double.MAX_VALUE);

        DatePicker startDate = new DatePicker();
        DatePicker endDate = new DatePicker();

        // ================== Policy ==================

        Spinner<Integer> maxPerDay = new Spinner<>(1, 10, 2);
        maxPerDay.setEditable(true);

        ComboBox<FemaleExtraRuleType> femaleRule = new ComboBox<>();
        femaleRule.getItems().addAll(FemaleExtraRuleType.values());
        femaleRule.setValue(FemaleExtraRuleType.NONE);
        femaleRule.setMaxWidth(Double.MAX_VALUE);

        TextField femaleCode = new TextField();
        femaleCode.setPromptText("Ex: D (si SPECIFIC_TABLEAU_CODE)");
        femaleCode.setDisable(true);

        femaleRule.valueProperty().addListener((obs, oldV, newV) -> {
            boolean needsCode = newV == FemaleExtraRuleType.SPECIFIC_TABLEAU_ONCE
                    || newV == FemaleExtraRuleType.SPECIFIC_TABLEAU_PER_DAY;

            femaleCode.setDisable(!needsCode);

            if (!needsCode) {
                femaleCode.clear();
            }
        });

        // Petit "i" info à côté de règle féminine
        Button infoBtn = new Button("i");
        infoBtn.setFocusTraversable(false);
        infoBtn.setPrefSize(24, 24);
        infoBtn.setMinSize(24, 24);
        infoBtn.setMaxSize(24, 24);
        infoBtn.setStyle("""
                -fx-background-radius: 50;
                -fx-font-weight: bold;
                -fx-padding: 0;
                """);

        Tooltip tip = new Tooltip("""
                NONE : pas d’extra

                ANY_TABLEAU : +1 tableau autorisé sur n’importe quel tableau (1 seul par jour)

                SPECIFIC_TABLEAU_CODE : +1 tableau autorisé uniquement si la joueuse choisit un tableau précis (code)
                """);
        tip.setWrapText(true);
        tip.setMaxWidth(360);
        Tooltip.install(infoBtn, tip);

        // ================== Tableaux (métier en mémoire) ==================

        ObservableList<Tableau> tableaux = FXCollections.observableArrayList();
        ListView<Tableau> tableauxList = new ListView<>(tableaux);
        tableauxList.setPrefHeight(220);

        // affichage lisible (sinon toString() par défaut)
        tableauxList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Tableau item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    double prepaid = item.fee().prepaid() / 100.0;
                    double onSite = item.fee().onSite() / 100.0;
                    setText(item.code() + " — " + item.designation()
                            + " — " + item.date()
                            + " — cap " + item.maxPlayers()
                            + " — " + prepaid + "€ (online) / " + onSite + "€ (sur place)");
                }
            }
        });

        Button addTableauBtn = new Button("Créer un tableau");
        addTableauBtn.setMaxWidth(Double.MAX_VALUE);

        // On bloque si dates pas renseignées
        addTableauBtn.disableProperty().bind(
                startDate.valueProperty().isNull().or(endDate.valueProperty().isNull()));

        Button removeTableauBtn = new Button("Supprimer le tableau sélectionné");
        removeTableauBtn.setMaxWidth(Double.MAX_VALUE);
        removeTableauBtn.disableProperty().bind(
                tableauxList.getSelectionModel().selectedItemProperty().isNull());

        addTableauBtn.setOnAction(e -> {
            CreateTableauDialog dlg = new CreateTableauDialog(startDate.getValue(), endDate.getValue());
            dlg.showAndWait().ifPresent(tb -> {

                // 1) code unique
                boolean dup = tableaux.stream().anyMatch(x -> x.code().equalsIgnoreCase(tb.code()));
                if (dup) {
                    showAlert("Code déjà utilisé", "Un tableau avec ce code existe déjà.");
                    return;
                }

                // 2) si dates tournoi saisies, imposer que la date du tableau est dedans
                LocalDate sd = startDate.getValue();
                LocalDate ed = endDate.getValue();
                if (sd != null && ed != null) {
                    if (tb.date().isBefore(sd) || tb.date().isAfter(ed)) {
                        showAlert("Date invalide",
                                "La date du tableau doit être comprise entre la date début et la date fin du tournoi.");
                        return;
                    }
                }

                tableaux.add(tb);
            });
        });

        removeTableauBtn.setOnAction(e -> {
            Tableau sel = tableauxList.getSelectionModel().getSelectedItem();
            if (sel != null)
                tableaux.remove(sel);
        });

        VBox tableauxBox = new VBox(10);
        Label tableauxTitle = new Label("Les Tableaux");
        tableauxTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        tableauxBox.getChildren().addAll(tableauxTitle, tableauxList, addTableauBtn, removeTableauBtn);

        // ================== Erreurs + boutons ==================

        Label error = new Label();
        error.setStyle("-fx-text-fill:#b00020;");

        Button cancel = new Button("Annuler");
        Button create = new Button("Créer");
        create.setDefaultButton(true);

        cancel.setOnAction(e -> close());

        // ================== Règles de changement de dates (SHIFT ou CLEAR)
        // ==================
        // - Si on déplace le tournoi sans changer sa durée => on décale les tableaux
        // (1->8 etc.)
        // - Si on change la durée => on supprime les tableaux

        final LocalDate[] lastStart = { null };
        final LocalDate[] lastEnd = { null };
        final boolean[] internalChange = { false };

        Runnable handleTournamentRangeChange = () -> {
            if (internalChange[0])
                return;

            LocalDate ns = startDate.getValue();
            LocalDate ne = endDate.getValue();

            // pas encore défini -> on mémorise et on sort
            if (ns == null || ne == null) {
                lastStart[0] = ns;
                lastEnd[0] = ne;
                return;
            }

            // première fois qu'on a une plage complète
            if (lastStart[0] == null || lastEnd[0] == null) {
                lastStart[0] = ns;
                lastEnd[0] = ne;
                return;
            }

            LocalDate os = lastStart[0];
            LocalDate oe = lastEnd[0];

            if (ns.equals(os) && ne.equals(oe))
                return;

            long oldLen = ChronoUnit.DAYS.between(os, oe); // ex 1->2 = 1
            long newLen = ChronoUnit.DAYS.between(ns, ne);

            // durée différente => on supprime les tableaux
            if (oldLen != newLen) {
                if (!tableaux.isEmpty()) {
                    tableaux.clear();
                    error.setText("Durée du tournoi modifiée : tableaux supprimés (à recréer).");
                }
                lastStart[0] = ns;
                lastEnd[0] = ne;
                return;
            }

            // durée identique => on décale les tableaux selon le déplacement du début
            long delta = ChronoUnit.DAYS.between(os, ns);
            if (delta != 0 && !tableaux.isEmpty()) {
                internalChange[0] = true;
                try {
                    for (int i = 0; i < tableaux.size(); i++) {
                        Tableau tb = tableaux.get(i);
                        tableaux.set(i, copyWithDate(tb, tb.date().plusDays(delta)));
                    }
                    error.setText("");
                } finally {
                    internalChange[0] = false;
                }
            }

            lastStart[0] = ns;
            lastEnd[0] = ne;
        };

        startDate.valueProperty().addListener((obs, o, n) -> handleTournamentRangeChange.run());
        endDate.valueProperty().addListener((obs, o, n) -> handleTournamentRangeChange.run());

        // Bonus UX : empêcher fin < début + auto-ajuster fin si besoin
        endDate.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null)
                    return;

                LocalDate sd = startDate.getValue();
                if (sd == null)
                    return;

                boolean out = item.isBefore(sd);
                setDisable(out);
                if (out)
                    setTooltip(new Tooltip("La fin ne peut pas être avant le début"));
            }
        });

        startDate.valueProperty().addListener((obs, o, sd) -> {
            if (sd == null)
                return;
            LocalDate ed = endDate.getValue();
            if (ed == null || ed.isBefore(sd)) {
                endDate.setValue(sd);
            }
        });

        // ================== Validation live + disable Create ==================

        Runnable refreshError = () -> {
            String msg = validateTournamentAndTableaux(
                    name.getText(),
                    level.getValue(),
                    rankingPhase.getValue(),
                    startDate.getValue(),
                    endDate.getValue(),
                    maxPerDay.getValue(),
                    femaleRule.getValue(),
                    femaleCode.getText(),
                    tableaux);
            // Important : si on a déjà un message informatif (ex durée modifiée), on ne
            // l’écrase pas
            // sauf si tout est OK
            if (msg == null) {
                // ne pas effacer un message "durée modifiée" si tu veux le garder : commente la
                // ligne suivante
                // error.setText("");
                // ici on laisse tel quel si tu veux garder l'info, sinon décommente :
                // error.setText("");
            } else {
                error.setText(msg);
            }
        };

        name.textProperty().addListener((obs, o, n) -> refreshError.run());
        level.valueProperty().addListener((obs, o, n) -> refreshError.run());
        rankingPhase.valueProperty().addListener((obs, o, n) -> refreshError.run());
        startDate.valueProperty().addListener((obs, o, n) -> refreshError.run());
        endDate.valueProperty().addListener((obs, o, n) -> refreshError.run());
        femaleRule.valueProperty().addListener((obs, o, n) -> refreshError.run());
        femaleCode.textProperty().addListener((obs, o, n) -> refreshError.run());
        tableaux.addListener((ListChangeListener<Tableau>) c -> refreshError.run());

        BooleanBinding invalid = new BooleanBinding() {
            {
                bind(name.textProperty(),
                        startDate.valueProperty(),
                        endDate.valueProperty(),
                        level.valueProperty(),
                        rankingPhase.valueProperty(),
                        femaleRule.valueProperty(),
                        femaleCode.textProperty(),
                        tableaux);
            }

            @Override
            protected boolean computeValue() {
                return validateTournamentAndTableaux(
                        name.getText(),
                        level.getValue(),
                        rankingPhase.getValue(),
                        startDate.getValue(),
                        endDate.getValue(),
                        maxPerDay.getValue(),
                        femaleRule.getValue(),
                        femaleCode.getText(),
                        tableaux) != null;
            }
        };
        create.disableProperty().bind(invalid);

        // ================== Action "Créer" ==================

        create.setOnAction(e -> {
            try {
                String msg = validateTournamentAndTableaux(
                        name.getText(),
                        level.getValue(),
                        rankingPhase.getValue(),
                        startDate.getValue(),
                        endDate.getValue(),
                        maxPerDay.getValue(),
                        femaleRule.getValue(),
                        femaleCode.getText(),
                        tableaux);
                if (msg != null)
                    throw new IllegalArgumentException(msg);

                LocalDate sd = startDate.getValue();
                LocalDate ed = endDate.getValue();

                FemaleExtraRuleType rule = femaleRule.getValue();
                String code = femaleCode.getText();

                nav.tournamentRepo().createDraftTournament(
                        org.getId(),
                        name.getText().trim(),
                        level.getValue().name(),
                        rankingPhase.getValue().name(),
                        sd,
                        ed,
                        maxPerDay.getValue(),
                        rule.name(),
                        code);

                // IMPORTANT : tableaux non persistés ici (étape suivante)
                close();

            } catch (Exception ex) {
                error.setText(ex.getMessage() == null ? "Erreur" : ex.getMessage());
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bottom = new HBox(10, error, spacer, cancel, create);
        bottom.setPadding(new Insets(12, 18, 18, 18));

        // ================== Layout global ==================

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(18));

        int r = 0;
        form.add(label("Nom"), 0, r);
        form.add(name, 1, r++);

        form.add(label("Niveau"), 0, r);
        form.add(level, 1, r++);

        form.add(label("RankingPhase"), 0, r);
        form.add(rankingPhase, 1, r++);

        form.add(label("Date début"), 0, r);
        form.add(startDate, 1, r++);

        form.add(label("Date fin"), 0, r);
        form.add(endDate, 1, r++);

        form.add(new Separator(), 0, r++, 2, 1);

        form.add(label("Max tableaux / jour"), 0, r);
        form.add(maxPerDay, 1, r++);

        HBox femaleRow = new HBox(8, femaleRule, infoBtn);
        femaleRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(femaleRule, Priority.ALWAYS);

        form.add(label("Règle féminine"), 0, r);
        form.add(femaleRow, 1, r++);

        form.add(label("Code tableau féminin"), 0, r);
        form.add(femaleCode, 1, r++);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setMinWidth(170);

        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);

        form.getColumnConstraints().addAll(c1, c2);

        VBox center = new VBox(16, form, tableauxBox);
        center.setPadding(new Insets(0, 18, 0, 18));

        BorderPane root = new BorderPane();
        root.setCenter(center);
        root.setBottom(bottom);

        setScene(new Scene(root, 900, 760));
    }

    // ========= Validation centralisée (tournoi + sécurité tableaux) =========

    private static String validateTournamentAndTableaux(
            String tournamentName,
            TournamentLevel level,
            RankingPhase rankingPhase,
            LocalDate sd,
            LocalDate ed,
            int mpd,
            FemaleExtraRuleType femaleRule,
            String femaleCode,
            ObservableList<Tableau> tableaux) {
        if (tournamentName == null || tournamentName.isBlank())
            return "Nom obligatoire.";

        if (level == null)
            return "Niveau obligatoire.";

        if (rankingPhase == null)
            return "RankingPhase obligatoire.";

        if (sd == null || ed == null)
            return "Dates obligatoires.";

        if (ed.isBefore(sd))
            return "La date de fin doit être >= date début.";

        if (mpd <= 0)
            return "Max tableaux / jour invalide.";

        if (femaleRule == null)
            return "Règle féminine obligatoire.";

        if (femaleRule == FemaleExtraRuleType.SPECIFIC_TABLEAU_ONCE
                || femaleRule == FemaleExtraRuleType.SPECIFIC_TABLEAU_PER_DAY) {
            if (femaleCode == null || femaleCode.isBlank()) {
                return "Code tableau obligatoire si règle SPECIFIC_TABLEAU_*.";
            }
        }

        // sécurité : normalement impossible car on shift/clear, mais on garde un filet
        if (tableaux != null) {
            for (Tableau tb : tableaux) {
                if (tb.date().isBefore(sd) || tb.date().isAfter(ed)) {
                    return "Le tableau " + tb.code() + " (" + tb.date() + ") est hors des dates du tournoi.";
                }
            }
        }

        return null;
    }

    // ========= Copie d'un tableau en changeant uniquement la date =========
    // Si ça ne compile pas chez toi, colle Tableau.java et je te l'adapte
    // exactement.
    private static Tableau copyWithDate(Tableau tb, LocalDate newDate) {
        return new Tableau(
                tb.code(),
                tb.designation(),
                newDate,
                tb.genderPolicy(),
                tb.ageCategoryPolicy(), // <-- ajouté
                tb.pointsRuleType(),
                tb.minPoints(),
                tb.maxPoints(),
                tb.maxPlayers(),
                tb.waitlistCapacity(),
                tb.fee(),
                tb.checkInEnd(),
                tb.startTime(),
                tb.prizes());
    }

    private static Label label(String txt) {
        Label l = new Label(txt);
        l.setMinWidth(170);
        return l;
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}