package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer;

import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.entity.Tableau;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.GenderPolicy;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.TableauPointsRuleType;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value.PrizeDistribution;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value.PrizeTier;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value.RegistrationFee;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public final class CreateTableauDialog extends Dialog<Tableau> {

    public CreateTableauDialog(LocalDate tournamentStart, LocalDate tournamentEnd) {

        setTitle("Créer un tableau");
        setHeaderText("Paramètres du tableau");

        ButtonType createBtn = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, createBtn);

        // ----------------- Champs -----------------

        TextField code = new TextField();
        code.setPromptText("A");

        TextField designation = new TextField();
        designation.setPromptText("Tableau A - Classique");

        DatePicker date = new DatePicker();
        if (tournamentStart != null)
            date.setValue(tournamentStart);

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
        });
        minPoints.setDisable(true);
        maxPoints.setDisable(true);

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

        // ----------------- Primes (V1 simple) -----------------
        CheckBox prizesEnabled = new CheckBox("Activer les primes");
        prizesEnabled.setSelected(false);

        Spinner<Integer> prizeFrom = new Spinner<>(1, 512, 1);
        Spinner<Integer> prizeTo = new Spinner<>(1, 512, 1);
        prizeFrom.setEditable(true);
        prizeTo.setEditable(true);

        TextField prizeAmountEuro = new TextField();
        prizeAmountEuro.setPromptText("0.00");

        HBox prizeRow = new HBox(10,
                new Label("Rang de"), prizeFrom,
                new Label("à"), prizeTo,
                new Label("Montant (€)"), prizeAmountEuro);
        prizeRow.setAlignment(Pos.CENTER_LEFT);

        // désactivation tant que checkbox off
        prizeRow.disableProperty().bind(prizesEnabled.selectedProperty().not());

        VBox prizesBox = new VBox(8, prizesEnabled, prizeRow);
        prizesBox.setPadding(new Insets(6, 0, 0, 0));

        // ----------------- Layout -----------------

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        int r = 0;
        grid.add(lbl("Code"), 0, r);
        grid.add(code, 1, r++);
        grid.add(lbl("Désignation"), 0, r);
        grid.add(designation, 1, r++);
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

        // ----------------- Bouton OK / validation -----------------

        Node okButton = getDialogPane().lookupButton(createBtn);
        okButton.disableProperty().bind(
                code.textProperty().isEmpty()
                        .or(designation.textProperty().isEmpty()));

        // ----------------- Result -----------------

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
                if (prepaidCents == null || onSiteCents == null)
                    throw new IllegalArgumentException("Prix invalides (ex: 6.00).");

                RegistrationFee fee = new RegistrationFee(prepaidCents, onSiteCents);

                LocalTime checkInEnd = LocalTime.of(checkH.getValue(), checkM.getValue());
                LocalTime start = LocalTime.of(startH.getValue(), startM.getValue());

                // --- Primes ---
                PrizeDistribution prizes;
                if (!prizesEnabled.isSelected()) {
                    // distribution "neutre" valide (aucun gain réel)
                    prizes = new PrizeDistribution(List.of(new PrizeTier(1, 1, 0)));
                } else {
                    int from = prizeFrom.getValue();
                    int to = prizeTo.getValue();
                    if (to < from)
                        throw new IllegalArgumentException("Rangs primes invalides (to < from).");

                    Integer amountCents = parseEuroToCents(prizeAmountEuro.getText());
                    if (amountCents == null)
                        throw new IllegalArgumentException("Montant prime invalide (ex: 10.00).");

                    prizes = new PrizeDistribution(List.of(new PrizeTier(from, to, amountCents)));
                }

                return new Tableau(
                        c, d, dt,
                        gp,
                        pr,
                        min, max,
                        cap,
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
