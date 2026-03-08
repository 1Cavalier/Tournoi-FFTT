package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.components;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerTournamentCardModel;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class TournamentCard extends VBox {

        @SuppressWarnings("unused")
        private final Navigator nav;
        
        private final OrganizerTournamentCardModel t;

        public TournamentCard(Navigator nav, OrganizerTournamentCardModel tournament) {
                this.nav = nav;
                this.t = tournament;
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

        // ---------------------------------------------------------
        // HEADER
        // ---------------------------------------------------------

        private HBox buildHeader() {
                HBox box = new HBox(10);
                box.setAlignment(Pos.CENTER_LEFT);

                Label title = new Label(UiUtils.nvl(t.name()));
                AppTheme.applyCardTitle(title);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Label status = new Label(UiUtils.nvl(t.status()));
                status.setStyle(AppTheme.badgeStyle(AppTheme.COLOR_PRIMARY));

                box.getChildren().addAll(title, spacer, status);
                return box;
        }

        // ---------------------------------------------------------
        // MAIN ROW
        // ---------------------------------------------------------

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

        // ---------------------------------------------------------
        // GENERAL
        // ---------------------------------------------------------

        private VBox buildGeneralSection() {
                VBox content = new VBox(6);

                Label title = new Label("Général");
                AppTheme.applyCardTitle(title);

                content.getChildren().addAll(
                                row("Nom", t.name()),
                                row("Lieu", buildLocation()),
                                row("Niveau", t.level()),
                                row("Phase", t.phase()),
                                row("Date", buildDates()),
                                row("Homologation", t.homologationNumber()));

                Button edit = new Button("Modifier le tournoi");
                AppTheme.styleSecondary(edit);
                edit.setMaxWidth(Double.MAX_VALUE);
                edit.setOnAction(e -> UiUtils.info("À venir", "Modification du tournoi."));

                content.getChildren().add(spacer(6));
                content.getChildren().add(edit);

                return section(content);
        }

        // ---------------------------------------------------------
        // REGLEMENT
        // ---------------------------------------------------------

        private VBox buildRegulationSection() {
                VBox content = new VBox(6);

                Label title = new Label("Règlement");
                AppTheme.applyCardTitle(title);

                content.getChildren().addAll(
                                row("Nbr tables", t.numberOfTables()),
                                row("Juge-arbitre", boolLabel(t.hasJudgeReferee())),
                                row("Arbitre", boolLabel(t.hasReferee())),
                                row("Tableaux / jour", t.maxTableauxPerDay()),
                                row("Règle féminine", t.femaleRuleLabel()));

                Button edit = new Button("Modifier le règlement");
                AppTheme.styleSecondary(edit);
                edit.setMaxWidth(Double.MAX_VALUE);
                edit.setOnAction(e -> UiUtils.info("À venir", "Modification du règlement."));

                content.getChildren().add(spacer(6));
                content.getChildren().add(edit);

                return section(content);
        }

        // ---------------------------------------------------------
        // TABLEAUX
        // ---------------------------------------------------------

        private VBox buildTableauxSection() {
                VBox content = new VBox(6);

                Label title = new Label("Tableaux");
                AppTheme.applyCardTitle(title);

                content.getChildren().addAll(
                                row("Nombre", t.tableauCount()),
                                row("Sélection", t.selectionByLabel()),
                                row("Récompenses", t.totalRewardLabel()));

                Button view = new Button("Voir les tableaux");
                AppTheme.styleSecondary(view);
                view.setMaxWidth(Double.MAX_VALUE);
                view.setOnAction(e -> UiUtils.info("À venir", "Ouverture de la gestion des tableaux."));

                content.getChildren().add(spacer(6));
                content.getChildren().add(view);

                return section(content);
        }

        // ---------------------------------------------------------
        // INSCRIPTIONS
        // ---------------------------------------------------------

        private VBox buildRegistrationSection() {
                VBox content = new VBox(6);

                Label title = new Label("Inscriptions");
                AppTheme.applyCardTitle(title);

                Label text = new Label(
                                t.canManageRegistrations()
                                                ? "Les inscriptions sont disponibles."
                                                : "Disponibles après publication.");
                AppTheme.applyBody(text);
                text.setWrapText(true);

                Button manage = new Button("Gérer les inscriptions");
                AppTheme.styleSecondary(manage);
                manage.setMaxWidth(Double.MAX_VALUE);
                manage.setDisable(!t.canManageRegistrations());
                manage.setOnAction(e -> UiUtils.info("À venir", "Gestion des inscriptions."));

                content.getChildren().addAll(text, spacer(6), manage);

                return section(content);
        }

        // ---------------------------------------------------------
        // ACTIONS ROW
        // ---------------------------------------------------------

        private HBox buildActionsRow() {
                Button publish = new Button("Publier");
                AppTheme.stylePrimary(publish);
                publish.setDisable(!t.canPublish());
                publish.setOnAction(e -> UiUtils.info("À venir", "Publication du tournoi."));

                Button delete = new Button("Supprimer le tournoi");
                delete.setStyle(
                                "-fx-background-color: #C62828;" +
                                                "-fx-text-fill: white;" +
                                                "-fx-font-weight: bold;" +
                                                "-fx-background-radius: " + AppTheme.RADIUS + ";");
                delete.setOnAction(e -> UiUtils.info("À venir", "Suppression du tournoi."));

                HBox row = new HBox(10, publish, delete);
                row.setAlignment(Pos.CENTER_RIGHT);
                return row;
        }

        // ---------------------------------------------------------
        // HELPERS
        // ---------------------------------------------------------

        private VBox section(VBox content) {
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

        private HBox row(String label, Object value) {
                HBox row = new HBox(8);
                row.setAlignment(Pos.CENTER_LEFT);

                Label l = new Label(label + " :");
                AppTheme.applyBody(l);
                l.setMinWidth(110);

                Label v = new Label(formatValue(value));
                AppTheme.applyBody(v);
                v.setWrapText(true);

                if (value == null || (value instanceof String s && s.isBlank())) {
                        v.setStyle("-fx-text-fill:#D32F2F; -fx-font-weight: bold;");
                } else {
                        v.setStyle("-fx-text-fill:#2E7D32; -fx-font-weight: bold;");
                }

                row.getChildren().addAll(l, v);
                return row;
        }

        private String formatValue(Object v) {
                if (v == null) {
                        return "information manquante";
                }
                if (v instanceof String s) {
                        return s.isBlank() ? "information manquante" : s;
                }
                return String.valueOf(v);
        }

        private String buildLocation() {
                if (t.clubCity() == null || t.clubCity().isBlank()) {
                        return null;
                }
                if (t.clubDepartmentCode() == null || t.clubDepartmentCode().isBlank()) {
                        return t.clubCity();
                }
                return t.clubCity() + " (" + t.clubDepartmentCode() + ")";
        }

        private String buildDates() {
                if (t.startDate() == null || t.startDate().isBlank()) {
                        return null;
                }
                if (t.endDate() == null || t.endDate().isBlank() || t.startDate().equals(t.endDate())) {
                        return t.startDate();
                }
                return t.startDate() + " → " + t.endDate();
        }

        private String boolLabel(boolean value) {
                return value ? "Oui" : null;
        }

        private Region spacer(double height) {
                Region region = new Region();
                region.setMinHeight(height);
                return region;
        }
}