package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.dialogs;

import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.entity.Tableau;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.GenderPolicy;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.TableauPointsRuleType;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value.AgeCategoryPolicy;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value.PrizeDistribution;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value.PrizeTier;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value.RegistrationFee;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public final class CreateTableauDialog extends Dialog<Tableau> {

    private static final int PRIZE_ROWS = 5;

    public CreateTableauDialog(LocalDate tournamentStart, LocalDate tournamentEnd) {

        int waitCap = 20;

        setTitle("Créer un tableau");
        setHeaderText("Paramètres du tableau");

        ButtonType createBtn = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, createBtn);

        // ================== Champs ==================

        TextField code = new TextField();
        code.setPromptText("A");

        TextField designation = new TextField();
        designation.setPromptText("Désignation (auto, modifiable)");

        // Auto designation (désactivée si l'utilisateur modifie manuellement)
        final boolean[] updatingDesignation = { false };
        final boolean[] autoDesignation = { true };

        designation.textProperty().addListener((obs, oldV, newV) -> {
            if (updatingDesignation[0])
                return;
            autoDesignation[0] = false;
        });

        Button autoBtn = new Button("Auto");
        autoBtn.setFocusTraversable(false);
        autoBtn.setOnAction(e -> {
            autoDesignation[0] = true;
            refreshDesignation(code, designation, null, null, null, updatingDesignation, autoDesignation);
        });

        HBox designationRow = new HBox(8, designation, autoBtn);
        HBox.setHgrow(designation, Priority.ALWAYS);

        DatePicker date = new DatePicker();
        if (tournamentStart != null)
            date.setValue(tournamentStart);

        if (tournamentStart != null && tournamentEnd != null) {
            date.setDayCellFactory(dp -> new DateCell() {
                @Override
                public void updateItem(LocalDate item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null)
                        return;

                    boolean out = item.isBefore(tournamentStart) || item.isAfter(tournamentEnd);
                    setDisable(out);
                    if (out) {
                        setStyle("-fx-opacity: 0.45;"); // optionnel
                        setTooltip(new Tooltip("Date hors du tournoi"));
                    }
                }
            });
        }

        ComboBox<GenderPolicy> genderPolicy = new ComboBox<>();
        genderPolicy.getItems().addAll(GenderPolicy.values());
        genderPolicy.setMaxWidth(Double.MAX_VALUE);

        ComboBox<TableauPointsRuleType> pointsRule = new ComboBox<>();
        pointsRule.getItems().addAll(TableauPointsRuleType.values());
        pointsRule.setMaxWidth(Double.MAX_VALUE);

        Spinner<Integer> minPoints = new Spinner<>(0, 50000, 0);
        Spinner<Integer> maxPoints = new Spinner<>(0, 50000, 0);
        minPoints.setEditable(true);
        maxPoints.setEditable(true);

        // activer/désactiver min/max selon rule
        pointsRule.valueProperty().addListener((obs, oldV, newV) -> {
            boolean minEnabled = newV == TableauPointsRuleType.RANGE_MIN_MAX;
            boolean maxEnabled = newV == TableauPointsRuleType.MAX_ONLY || newV == TableauPointsRuleType.RANGE_MIN_MAX;
            minPoints.setDisable(!minEnabled);
            maxPoints.setDisable(!maxEnabled);

            refreshDesignation(code, designation, pointsRule, minPoints, maxPoints, updatingDesignation,
                    autoDesignation);
        });
        minPoints.setDisable(true);
        maxPoints.setDisable(true);

        // listeners pour auto designation
        code.textProperty().addListener((obs, o, n) -> refreshDesignation(code, designation, pointsRule, minPoints,
                maxPoints, updatingDesignation, autoDesignation));
        minPoints.valueProperty().addListener((obs, o, n) -> refreshDesignation(code, designation, pointsRule,
                minPoints, maxPoints, updatingDesignation, autoDesignation));
        maxPoints.valueProperty().addListener((obs, o, n) -> refreshDesignation(code, designation, pointsRule,
                minPoints, maxPoints, updatingDesignation, autoDesignation));

        Spinner<Integer> maxPlayers = new Spinner<>(2, 512, 64);
        maxPlayers.setEditable(true);

        TextField prepaidEuro = new TextField();
        prepaidEuro.setPromptText("6.00");

        TextField onSiteEuro = new TextField();
        onSiteEuro.setPromptText("7.00");

        // Horaires
        Spinner<Integer> checkH = new Spinner<>(0, 23, 8);
        Spinner<Integer> checkM = new Spinner<>(0, 59, 30);
        checkH.setEditable(true);
        checkM.setEditable(true);

        Spinner<Integer> startH = new Spinner<>(0, 23, 9);
        Spinner<Integer> startM = new Spinner<>(0, 59, 0);
        startH.setEditable(true);
        startM.setEditable(true);

        HBox checkInBox = new HBox(8, new Label("H"), checkH, new Label("M"), checkM);
        HBox startBox = new HBox(8, new Label("H"), startH, new Label("M"), startM);

        // ================== Primes (5 lignes fixes) ==================

        CheckBox prizesEnabled = new CheckBox("Activer les primes");
        prizesEnabled.setSelected(false);

        VBox prizeRowsBox = new VBox(8);

        List<Spinner<Integer>> fromSpinners = new ArrayList<>();
        List<Spinner<Integer>> toSpinners = new ArrayList<>();
        List<TextField> amountFields = new ArrayList<>();

        for (int i = 0; i < PRIZE_ROWS; i++) {

            Spinner<Integer> from = new Spinner<>(1, 512, 1);
            Spinner<Integer> to = new Spinner<>(1, 512, 1);
            from.setEditable(true);
            to.setEditable(true);

            TextField amountEuro = new TextField();
            amountEuro.setPromptText("0.00");
            amountEuro.setPrefColumnCount(8);

            HBox row = new HBox(10,
                    new Label("Rang de"), from,
                    new Label("à"), to,
                    new Label("Montant (€)"), amountEuro);
            row.setAlignment(Pos.CENTER_LEFT);

            // ligne grisée si primes non activées
            row.disableProperty().bind(prizesEnabled.selectedProperty().not());

            prizeRowsBox.getChildren().add(row);

            fromSpinners.add(from);
            toSpinners.add(to);
            amountFields.add(amountEuro);
        }

        Label primesHint = new Label("Renseignez seulement les lignes nécessaires (max " + PRIZE_ROWS + ").");
        primesHint.setStyle("-fx-opacity:0.75;");

        VBox prizesBox = new VBox(8, prizesEnabled, primesHint, prizeRowsBox);
        prizesBox.setPadding(new Insets(6, 0, 0, 0));

        // 1er auto remplissage
        refreshDesignation(code, designation, pointsRule, minPoints, maxPoints, updatingDesignation, autoDesignation);

        // ================== Layout ==================

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        int r = 0;
        grid.add(lbl("Code"), 0, r);
        grid.add(code, 1, r++);

        grid.add(lbl("Désignation"), 0, r);
        grid.add(designationRow, 1, r++);

        grid.add(lbl("Date"), 0, r);
        grid.add(date, 1, r++);

        grid.add(new Separator(), 0, r++, 2, 1);

        grid.add(lbl("Règle sexe"), 0, r);
        grid.add(genderPolicy, 1, r++);

        grid.add(lbl("Règle points"), 0, r);
        grid.add(pointsRule, 1, r++);

        grid.add(lbl("Points min"), 0, r);
        grid.add(minPoints, 1, r++);

        grid.add(lbl("Points max"), 0, r);
        grid.add(maxPoints, 1, r++);

        grid.add(new Separator(), 0, r++, 2, 1);

        grid.add(lbl("Capacité (max joueurs)"), 0, r);
        grid.add(maxPlayers, 1, r++);

        grid.add(lbl("Prix prépayé (€)"), 0, r);
        grid.add(prepaidEuro, 1, r++);

        grid.add(lbl("Prix sur place (€)"), 0, r);
        grid.add(onSiteEuro, 1, r++);

        grid.add(new Separator(), 0, r++, 2, 1);

        grid.add(lbl("Fin pointage"), 0, r);
        grid.add(checkInBox, 1, r++);

        grid.add(lbl("Début tableau"), 0, r);
        grid.add(startBox, 1, r++);

        grid.add(new Separator(), 0, r++, 2, 1);

        grid.add(lbl("Primes"), 0, r);
        grid.add(prizesBox, 1, r++);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setMinWidth(170);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);

        getDialogPane().setContent(grid);

        // ================== Bouton OK / validation ==================

        Node okButton = getDialogPane().lookupButton(createBtn);
        okButton.disableProperty().bind(
                code.textProperty().isEmpty()
                        .or(designation.textProperty().isEmpty())
                        .or(date.valueProperty().isNull()));

        // ================== Result ==================

        setResultConverter(bt -> {
            if (bt != createBtn)
                return null;

            try {
                String c = safeUpper(code.getText());
                String d = safeTrim(designation.getText());

                LocalDate dt = date.getValue();
                if (dt == null)
                    throw new IllegalArgumentException("Date obligatoire.");

                if (tournamentStart != null && tournamentEnd != null) {
                    if (dt.isBefore(tournamentStart) || dt.isAfter(tournamentEnd)) {
                        throw new IllegalArgumentException(
                                "La date du tableau doit être comprise dans les dates du tournoi.");
                    }
                }

                GenderPolicy gp = genderPolicy.getValue();
                if (gp == null)
                    throw new IllegalArgumentException("Règle sexe obligatoire.");

                TableauPointsRuleType pr = pointsRule.getValue();
                if (pr == null)
                    throw new IllegalArgumentException("Règle points obligatoire.");

                Integer min = null;
                Integer max = null;

                if (pr == TableauPointsRuleType.MAX_ONLY) {
                    max = maxPoints.getValue();
                } else if (pr == TableauPointsRuleType.RANGE_MIN_MAX) {
                    min = minPoints.getValue();
                    max = maxPoints.getValue();
                }

                int cap = maxPlayers.getValue();
                if (cap <= 0)
                    throw new IllegalArgumentException("Capacité invalide.");

                Integer prepaidCents = parseEuroToCents(prepaidEuro.getText());
                Integer onSiteCents = parseEuroToCents(onSiteEuro.getText());
                if (prepaidCents == null || onSiteCents == null) {
                    throw new IllegalArgumentException("Prix invalides (ex: 6.00).");
                }

                RegistrationFee fee = new RegistrationFee(prepaidCents, onSiteCents);

                LocalTime checkInEnd = LocalTime.of(checkH.getValue(), checkM.getValue());
                LocalTime start = LocalTime.of(startH.getValue(), startM.getValue());

                // --- Primes ---
                PrizeDistribution prizes;
                if (!prizesEnabled.isSelected()) {
                    prizes = new PrizeDistribution(List.of(new PrizeTier(1, 1, 0))); // neutre
                } else {
                    List<PrizeTier> tiers = new ArrayList<>();

                    for (int i = 0; i < PRIZE_ROWS; i++) {
                        String txt = amountFields.get(i).getText();
                        if (txt == null || txt.isBlank()) {
                            continue; // ligne ignorée
                        }

                        int from = fromSpinners.get(i).getValue();
                        int to = toSpinners.get(i).getValue();
                        if (to < from) {
                            throw new IllegalArgumentException("Prime ligne " + (i + 1) + " : rang 'à' < rang 'de'.");
                        }

                        Integer amountCents = parseEuroToCents(txt);
                        if (amountCents == null) {
                            throw new IllegalArgumentException(
                                    "Prime ligne " + (i + 1) + " : montant invalide (ex: 10.00).");
                        }

                        tiers.add(new PrizeTier(from, to, amountCents));
                    }

                    if (tiers.isEmpty()) {
                        throw new IllegalArgumentException("Au moins une prime doit être renseignée.");
                    }

                    prizes = new PrizeDistribution(tiers);
                }

                return new Tableau(
                        c,
                        d,
                        dt,
                        gp,
                        AgeCategoryPolicy.any(), // <-- ajouté (ou ta policy UI)
                        pr,
                        min,
                        max,
                        cap,
                        waitCap,
                        fee,
                        checkInEnd,
                        start,
                        prizes);

            } catch (Exception ex) {
                showAlert(ex.getMessage() == null ? "Erreur" : ex.getMessage());
                return null;
            }
        });
    }

    // ================== Auto designation ==================

    private static void refreshDesignation(
            TextField code,
            TextField designation,
            ComboBox<TableauPointsRuleType> pointsRule,
            Spinner<Integer> minPoints,
            Spinner<Integer> maxPoints,
            boolean[] updating,
            boolean[] auto) {
        if (!auto[0])
            return;

        TableauPointsRuleType rule = (pointsRule == null) ? null : pointsRule.getValue();
        if (rule == null)
            rule = TableauPointsRuleType.TOUTES_SERIES;

        Integer min = null;
        Integer max = null;

        if (minPoints != null && maxPoints != null) {
            if (rule == TableauPointsRuleType.MAX_ONLY) {
                max = maxPoints.getValue();
            } else if (rule == TableauPointsRuleType.RANGE_MIN_MAX) {
                min = minPoints.getValue();
                max = maxPoints.getValue();
            }
        }

        String autoText = buildAutoDesignation(code.getText(), rule, min, max);

        updating[0] = true;
        designation.setText(autoText);
        updating[0] = false;
    }

    private static String buildAutoDesignation(String code, TableauPointsRuleType rule, Integer min, Integer max) {
        String c = (code == null || code.isBlank()) ? "?" : code.trim().toUpperCase();

        String pointsPart = switch (rule) {
            case TOUTES_SERIES -> "Toutes séries";
            case MAX_ONLY -> (max == null) ? "≤ ? pts" : "≤ " + max + " pts";
            case RANGE_MIN_MAX -> {
                String a = (min == null) ? "?" : String.valueOf(min);
                String b = (max == null) ? "?" : String.valueOf(max);
                yield a + " à " + b + " pts";
            }
        };

        return "Tableau " + c + " - " + pointsPart;
    }

    // ================== Utils ==================

    private static Label lbl(String t) {
        Label l = new Label(t);
        l.setMinWidth(170);
        return l;
    }

    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private static String safeUpper(String s) {
        return safeTrim(s).toUpperCase();
    }

    private static Integer parseEuroToCents(String txt) {
        if (txt == null)
            return null;
        String s = txt.trim().replace(",", ".");
        if (s.isBlank())
            return null;
        try {
            double euros = Double.parseDouble(s);
            return (int) Math.round(euros * 100.0);
        } catch (Exception e) {
            return null;
        }
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Erreur");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
