package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.components;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.SqliteClubRepository;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.TableauRow;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.TournamentRow;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.Optional;

/**
 * Bloc UI: une "carte" tournoi.
 */
public class TournamentCard extends VBox {

    public enum Mode { ACTIVE, DRAFT }

    private final Navigator nav;
    private final TournamentRow t;
    private final Mode mode;

    public TournamentCard(Navigator nav, TournamentRow t, Mode mode) {
        this.nav = nav;
        this.t = t;
        this.mode = mode;

        setSpacing(10);
        setPadding(new Insets(14));
        setStyle("-fx-border-color:black; -fx-border-width:3; -fx-background-color:white;");

        build();
    }

    private void build() {
        getChildren().addAll(buildHeader(), new Separator(), buildBody());
    }

    private HBox buildHeader() {
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(t.name());
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        StatusBadge badge = new StatusBadge(mapStatus(t.status()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(title, spacer, badge);
        return header;
    }

    private HBox buildBody() {
        HBox body = new HBox(14);
        body.setAlignment(Pos.TOP_LEFT);

        VBox leftInfos = buildInfos();
        VBox centerTableaux = buildTableaux();
        VBox rightActions = buildActions();

        HBox.setHgrow(centerTableaux, Priority.ALWAYS);
        body.getChildren().addAll(leftInfos, centerTableaux, rightActions);
        return body;
    }

    private VBox buildInfos() {
        VBox box = new VBox(6);
        box.setMinWidth(240);

        Optional<SqliteClubRepository.ClubRow> clubOpt = nav.clubRepo().findByOrganizerId(t.organizerId());
        String ville = clubOpt.map(SqliteClubRepository.ClubRow::city).orElse(null);
        String dep = clubOpt.map(SqliteClubRepository.ClubRow::departementCode).orElse(null);

        String lieu = (ville == null || ville.isBlank())
                ? "—"
                : (ville + (dep == null || dep.isBlank() ? "" : " (" + dep + ")"));

        box.getChildren().addAll(
                UiUtils.kv("Nom", t.name()),
                UiUtils.kv("Lieu", lieu),
                UiUtils.kv("Niveau", t.level()),
                UiUtils.kv("Phase", t.phase()),
                UiUtils.kv("Dates", t.startDate() + " → " + t.endDate())
        );

        return box;
    }

    private VBox buildTableaux() {
        VBox box = new VBox(8);

        Label title = new Label("Liste des tableaux");
        title.setStyle("-fx-font-weight:bold;");

        TableView<TableauRow> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(140);

        TableColumn<TableauRow, String> cCode = new TableColumn<>("Code");
        cCode.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().code()));

        TableColumn<TableauRow, String> cLabel = new TableColumn<>("Libellé");
        cLabel.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().label()));

        TableColumn<TableauRow, String> cDate = new TableColumn<>("Date");
        cDate.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().date()));

        TableColumn<TableauRow, String> cPrice = new TableColumn<>("Prix");
        cPrice.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().prepaidEuro() + "€ / " + data.getValue().onsiteEuro() + "€"));

        TableColumn<TableauRow, String> cCap = new TableColumn<>("Cap.");
        cCap.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().capacity())));

        table.getColumns().addAll(cCode, cLabel, cDate, cPrice, cCap);
        table.getItems().setAll(nav.tableauRepo().findByTournamentId(t.id()));

        box.getChildren().addAll(title, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return box;
    }

    private VBox buildActions() {
        VBox box = new VBox(10);
        box.setMinWidth(220);

        if (mode == Mode.ACTIVE) {
            Button bPlayers = new Button("Gestion des joueurs");
            Button bStart = new Button("Lancement du tournoi");
            Button bEdit = new Button("Modification du tournoi");

            bPlayers.setMaxWidth(Double.MAX_VALUE);
            bStart.setMaxWidth(Double.MAX_VALUE);
            bEdit.setMaxWidth(Double.MAX_VALUE);

            bPlayers.setOnAction(e -> UiUtils.info("À venir", "Gestion des joueurs (à implémenter)."));
            bStart.setOnAction(e -> UiUtils.info("À venir", "Lancement du tournoi (à implémenter)."));
            bEdit.setOnAction(e -> UiUtils.info("À venir", "Modification tournoi actif (à implémenter)."));

            box.getChildren().addAll(bPlayers, bStart, bEdit);
        } else {
            Button bEditInfo = new Button("Modifier infos générales");
            Button bEditTabs = new Button("Modifier les tableaux");
            Button bPublish = new Button("Publier le tournoi");

            bEditInfo.setMaxWidth(Double.MAX_VALUE);
            bEditTabs.setMaxWidth(Double.MAX_VALUE);
            bPublish.setMaxWidth(Double.MAX_VALUE);

            bEditInfo.setOnAction(e -> UiUtils.info("À venir", "Édition infos générales (à implémenter)."));
            bEditTabs.setOnAction(e -> UiUtils.info("À venir", "Édition des tableaux (à implémenter)."));
            bPublish.setOnAction(e -> UiUtils.info("À venir", "Publication (DRAFT → OPEN) (à implémenter)."));

            box.getChildren().addAll(bEditInfo, bEditTabs, bPublish);
        }

        return box;
    }

    private TournamentStatus mapStatus(String status) {
        if (status == null) return TournamentStatus.DRAFT;
        try {
            return TournamentStatus.valueOf(status.trim().toUpperCase());
        } catch (Exception e) {
            return TournamentStatus.DRAFT;
        }
    }
}