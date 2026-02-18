package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer;

import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.entity.Tableau;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.TournamentLevel;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value.FemaleExtraRuleType;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.enums.RankingPhase;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;

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
            boolean needsCode = newV == FemaleExtraRuleType.SPECIFIC_TABLEAU_CODE;
            femaleCode.setDisable(!needsCode);
            if (!needsCode)
                femaleCode.clear();
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

        Button removeTableauBtn = new Button("Supprimer le tableau sélectionné");
        removeTableauBtn.setMaxWidth(Double.MAX_VALUE);
        removeTableauBtn.disableProperty().bind(
                tableauxList.getSelectionModel().selectedItemProperty().isNull());

        addTableauBtn.setOnAction(e -> {
            // popup complète qui crée un Tableau domaine
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

        create.setOnAction(e -> {
            try {
                // Validations tournoi
                if (name.getText() == null || name.getText().isBlank())
                    throw new IllegalArgumentException("Nom obligatoire.");

                if (level.getValue() == null)
                    throw new IllegalArgumentException("Niveau obligatoire.");

                if (rankingPhase.getValue() == null)
                    throw new IllegalArgumentException("RankingPhase obligatoire.");

                LocalDate sd = startDate.getValue();
                LocalDate ed = endDate.getValue();

                if (sd == null || ed == null)
                    throw new IllegalArgumentException("Dates obligatoires.");

                if (ed.isBefore(sd))
                    throw new IllegalArgumentException("La date de fin doit être >= date début.");

                int mpd = maxPerDay.getValue();
                if (mpd <= 0)
                    throw new IllegalArgumentException("Max tableaux / jour invalide.");

                FemaleExtraRuleType rule = femaleRule.getValue();
                String code = femaleCode.getText();

                if (rule == FemaleExtraRuleType.SPECIFIC_TABLEAU_CODE) {
                    if (code == null || code.isBlank())
                        throw new IllegalArgumentException("Code tableau obligatoire si règle SPECIFIC_TABLEAU_CODE.");
                }

                // (optionnel) imposer au moins 1 tableau
                // if (tableaux.isEmpty()) throw new IllegalArgumentException("Ajoute au moins 1
                // tableau.");

                // Création tournoi DRAFT en DB (comme tu avais)
                nav.tournamentRepo().createDraftTournament(
                        org.getId(),
                        name.getText().trim(),
                        level.getValue().name(),
                        rankingPhase.getValue().name(),
                        sd,
                        ed,
                        mpd,
                        rule.name(),
                        code);

                // IMPORTANT : on ne persiste pas encore les tableaux ici (étape suivante)
                // -> on le fera ensuite via repository tableau (insert + mapping)

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
