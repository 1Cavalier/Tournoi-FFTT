package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.components;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.AppRouter;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.TournamentCardDto;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Objects;

public class TournamentCard extends VBox {

        private final AppRouter nav;
        private final TournamentCardDto tournament;

        public TournamentCard(AppRouter nav, TournamentCardDto tournament) {
                this.nav = Objects.requireNonNull(nav, "nav must not be null");
                this.tournament = Objects.requireNonNull(tournament, "tournament must not be null");
                build();
        }

        private void build() {
                VBox content = new VBox(AppTheme.SPACE_MD);

                content.getChildren().add(buildHeader());
                content.getChildren().add(buildMainRow());
                content.getChildren().add(buildActionsRow());

                VBox card = AppTheme.card(content);
                card.setMaxWidth(Double.MAX_VALUE);

                getChildren().setAll(card);
        }

        // -------------------------------------------------------------------------
        // HEADER
        // -------------------------------------------------------------------------

        private HBox buildHeader() {
                HBox box = new HBox(10);
                box.setAlignment(Pos.CENTER_LEFT);

                Label title = new Label(OrganizerViewUtils.nvl(tournament.name()));
                AppTheme.applyCardTitle(title);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                StatusBadge statusBadge = new StatusBadge(tournament.status());

                box.getChildren().addAll(title, spacer, statusBadge);
                return box;
        }

        // -------------------------------------------------------------------------
        // MAIN
        // -------------------------------------------------------------------------

        private HBox buildMainRow() {
                HBox row = new HBox(12);
                row.setAlignment(Pos.TOP_LEFT);

                VBox general = buildGeneralSection();
                VBox regulation = buildRegulationSection();
                VBox tableaux = buildTableauxSection();
                VBox registrations = buildRegistrationSection();

                HBox.setHgrow(general, Priority.ALWAYS);
                HBox.setHgrow(regulation, Priority.ALWAYS);
                HBox.setHgrow(tableaux, Priority.ALWAYS);
                HBox.setHgrow(registrations, Priority.ALWAYS);

                general.setMaxWidth(Double.MAX_VALUE);
                regulation.setMaxWidth(Double.MAX_VALUE);
                tableaux.setMaxWidth(Double.MAX_VALUE);
                registrations.setMaxWidth(Double.MAX_VALUE);

                row.getChildren().addAll(general, regulation, tableaux, registrations);
                return row;
        }

        // -------------------------------------------------------------------------
        // GENERAL
        // -------------------------------------------------------------------------

        private VBox buildGeneralSection() {
                VBox content = new VBox(6);

                Label title = new Label("Général");
                AppTheme.applyCardTitle(title);

                content.getChildren().addAll(
                                infoRow("Nom", tournament.name()),
                                infoRow("Lieu", buildLocation()),
                                infoRow("Niveau", tournament.level()),
                                infoRow("Phase", tournament.phase()),
                                infoRow("Date", buildDates()),
                                infoRow("Homologation", tournament.homologationNumber()));

                Button edit = new Button("Modifier le tournoi");
                AppTheme.styleSecondary(edit);
                edit.setMaxWidth(Double.MAX_VALUE);
                edit.setOnAction(e -> nav.showInfo("À venir", "Modification du tournoi."));

                content.getChildren().add(verticalSpacer(6));
                content.getChildren().add(edit);

                return buildSection(content);
        }

        // -------------------------------------------------------------------------
        // REGULATION
        // -------------------------------------------------------------------------

        private VBox buildRegulationSection() {
                VBox content = new VBox(6);

                Label title = new Label("Règlement");
                AppTheme.applyCardTitle(title);

                content.getChildren().addAll(
                                infoRow("Nbr tables", tournament.numberOfTables()),
                                infoRow("Juge-arbitre", yesOrMissing(tournament.hasJudgeReferee())),
                                infoRow("Arbitre", yesOrMissing(tournament.hasReferee())),
                                infoRow("Tableaux / jour", tournament.maxTableauxPerDay()),
                                infoRow("Règle féminine", tournament.femaleRuleLabel()));

                Button edit = new Button("Modifier le règlement");
                AppTheme.styleSecondary(edit);
                edit.setMaxWidth(Double.MAX_VALUE);
                edit.setOnAction(e -> nav.showInfo("À venir", "Modification du règlement."));

                content.getChildren().add(verticalSpacer(6));
                content.getChildren().add(edit);

                return buildSection(content);
        }

        // -------------------------------------------------------------------------
        // TABLEAUX
        // -------------------------------------------------------------------------

        private VBox buildTableauxSection() {
                VBox content = new VBox(6);

                Label title = new Label("Tableaux");
                AppTheme.applyCardTitle(title);

                content.getChildren().addAll(
                                infoRow("Nombre", tournament.tableauCount()),
                                infoRow("Sélection", tournament.selectionByLabel()),
                                infoRow("Récompenses", tournament.totalRewardLabel()));

                Button view = new Button("Voir les tableaux");
                AppTheme.styleSecondary(view);
                view.setMaxWidth(Double.MAX_VALUE);
                view.setOnAction(e -> nav.showInfo("À venir", "Ouverture de la gestion des tableaux."));

                content.getChildren().add(verticalSpacer(6));
                content.getChildren().add(view);

                return buildSection(content);
        }

        // -------------------------------------------------------------------------
        // REGISTRATIONS
        // -------------------------------------------------------------------------

        private VBox buildRegistrationSection() {
                VBox content = new VBox(6);

                Label title = new Label("Inscriptions");
                AppTheme.applyCardTitle(title);

                Label text = new Label(
                                tournament.canManageRegistrations()
                                                ? "Les inscriptions sont disponibles."
                                                : "Disponibles après publication.");
                AppTheme.applyBody(text);
                text.setWrapText(true);

                Button manage = new Button("Gérer les inscriptions");
                AppTheme.styleSecondary(manage);
                manage.setMaxWidth(Double.MAX_VALUE);
                manage.setDisable(!tournament.canManageRegistrations());
                manage.setOnAction(e -> nav.showInfo("À venir", "Gestion des inscriptions."));

                content.getChildren().addAll(text, verticalSpacer(6), manage);

                return buildSection(content);
        }

        // -------------------------------------------------------------------------
        // ACTIONS
        // -------------------------------------------------------------------------

        private HBox buildActionsRow() {
                Button publish = new Button("Publier");
                AppTheme.stylePrimary(publish);
                publish.setDisable(!tournament.canPublish());
                publish.setOnAction(e -> nav.showInfo("À venir", "Publication du tournoi."));

                Button delete = new Button("Supprimer le tournoi");
                delete.setStyle(
                                "-fx-background-color: #C62828;" +
                                                "-fx-text-fill: white;" +
                                                "-fx-font-weight: bold;" +
                                                "-fx-background-radius: " + AppTheme.RADIUS + ";");
                delete.setOnAction(e -> nav.showInfo("À venir", "Suppression du tournoi."));

                HBox row = new HBox(10, publish, delete);
                row.setAlignment(Pos.CENTER_RIGHT);
                return row;
        }

        // -------------------------------------------------------------------------
        // HELPERS
        // -------------------------------------------------------------------------

        private VBox buildSection(VBox content) {
                VBox box = new VBox(8, content);
                box.setPadding(new Insets(10));
                box.setStyle(
                                "-fx-background-color:" + AppTheme.COLOR_SURFACE + ";" +
                                                "-fx-background-radius:" + AppTheme.RADIUS + ";" +
                                                "-fx-border-color:" + AppTheme.COLOR_BORDER + ";" +
                                                "-fx-border-radius:" + AppTheme.RADIUS + ";");
                box.setPrefWidth(240);
                return box;
        }

        private HBox infoRow(String label, Object value) {
                HBox row = new HBox(8);
                row.setAlignment(Pos.CENTER_LEFT);

                Label keyLabel = new Label(label + " :");
                AppTheme.applyBody(keyLabel);
                keyLabel.setMinWidth(110);

                Label valueLabel = new Label(formatValue(value));
                AppTheme.applyBody(valueLabel);
                valueLabel.setWrapText(true);

                if (isMissing(value)) {
                        valueLabel.setStyle("-fx-text-fill:#D32F2F; -fx-font-weight: bold;");
                } else {
                        valueLabel.setStyle("-fx-text-fill:#2E7D32; -fx-font-weight: bold;");
                }

                row.getChildren().addAll(keyLabel, valueLabel);
                return row;
        }

        private String formatValue(Object value) {
                if (value == null) {
                        return "information manquante";
                }
                if (value instanceof String s) {
                        return s.isBlank() ? "information manquante" : s;
                }
                return String.valueOf(value);
        }

        private boolean isMissing(Object value) {
                return value == null || (value instanceof String s && s.isBlank());
        }

        private String buildLocation() {
                if (isBlank(tournament.clubCity())) {
                        return null;
                }
                if (isBlank(tournament.clubDepartmentCode())) {
                        return tournament.clubCity();
                }
                return tournament.clubCity() + " (" + tournament.clubDepartmentCode() + ")";
        }

        private String buildDates() {
                if (isBlank(tournament.startDate())) {
                        return null;
                }
                if (isBlank(tournament.endDate()) || tournament.startDate().equals(tournament.endDate())) {
                        return tournament.startDate();
                }
                return tournament.startDate() + " → " + tournament.endDate();
        }

        private String yesOrMissing(boolean value) {
                return value ? "Oui" : null;
        }

        private boolean isBlank(String value) {
                return value == null || value.isBlank();
        }

        private Region verticalSpacer(double height) {
                Region region = new Region();
                region.setMinHeight(height);
                return region;
        }
}