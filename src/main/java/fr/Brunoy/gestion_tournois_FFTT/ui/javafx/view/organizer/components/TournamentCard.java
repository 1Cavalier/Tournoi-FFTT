package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.components;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.SqliteClubRepository;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.TableauRow;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.TournamentRow;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.Optional;

public class TournamentCard extends VBox {

    public enum Mode {
        ACTIVE, DRAFT
    }

    private final Navigator nav;
    private final TournamentRow t;
    private final Mode mode;

    public TournamentCard(Navigator nav, TournamentRow t, Mode mode) {
        this.nav = nav;
        this.t = t;
        this.mode = mode;

        setFillWidth(true);

        VBox content = new VBox(AppTheme.SPACE_MD);
        content.getChildren().addAll(buildHeader(), buildBody());

        VBox card = AppTheme.card(content);
        card.setMaxWidth(Double.MAX_VALUE);

        getChildren().add(card);
    }

    private HBox buildHeader() {
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(UiUtils.nvl(t.name()));
        AppTheme.applyCardTitle(title);

        StatusBadge badge = new StatusBadge(mapStatus(t.status()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(title, spacer, badge);
        return header;
    }

    private HBox buildBody() {
        HBox body = new HBox(16);
        body.setAlignment(Pos.TOP_LEFT);

        VBox leftInfos = buildInfos();
        VBox centerTableaux = buildTableaux();
        VBox rightActions = buildActions();

        leftInfos.setMinWidth(260);
        rightActions.setMinWidth(230);

        HBox.setHgrow(centerTableaux, Priority.ALWAYS);
        body.getChildren().addAll(leftInfos, centerTableaux, rightActions);
        return body;
    }

    private VBox buildInfos() {
        VBox box = new VBox(6);

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
                UiUtils.kv("Dates", t.startDate() + " → " + t.endDate()));

        return box;
    }

    private VBox buildTableaux() {
        VBox box = new VBox(8);

        Label title = new Label("Tableaux");
        AppTheme.applyCardTitle(title);
        title.setStyle(title.getStyle() + "-fx-font-size: 13px;"); // légèrement plus petit

        TableView<TableauRow> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(160);

        TableColumn<TableauRow, String> cCode = new TableColumn<>("Code");
        cCode.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().code()));

        TableColumn<TableauRow, String> cLabel = new TableColumn<>("Libellé");
        cLabel.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().label()));

        TableColumn<TableauRow, String> cDate = new TableColumn<>("Date");
        cDate.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().date()));

        TableColumn<TableauRow, String> cPrice = new TableColumn<>("Prix");
        cPrice.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().prepaidEuro() + "€ / " + d.getValue().onsiteEuro() + "€"));

        TableColumn<TableauRow, String> cCap = new TableColumn<>("Cap.");
        cCap.setCellValueFactory(
                d -> new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().capacity())));

        table.getItems().setAll(nav.tableauRepo().findByTournamentId(t.id()));

        VBox.setVgrow(table, Priority.ALWAYS);
        box.getChildren().addAll(title, table);
        return box;
    }

    private VBox buildActions() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(0, 0, 0, 0));

        if (mode == Mode.ACTIVE) {
            Button bPlayers = new Button("Joueurs");
            Button bStart = new Button("Lancer");
            Button bEdit = new Button("Modifier");

            AppTheme.styleSecondary(bPlayers);
            AppTheme.stylePrimary(bStart);
            AppTheme.styleSecondary(bEdit);

            bPlayers.setOnAction(e -> UiUtils.info("À venir", "Gestion des joueurs."));
            bStart.setOnAction(e -> UiUtils.info("À venir", "Lancement du tournoi."));
            bEdit.setOnAction(e -> UiUtils.info("À venir", "Modification tournoi actif."));

            box.getChildren().addAll(bPlayers, bStart, bEdit);
        } else {
            Button bEditInfo = new Button("Infos générales");
            Button bEditTabs = new Button("Tableaux");
            Button bPublish = new Button("Publier");

            AppTheme.styleSecondary(bEditInfo);
            AppTheme.styleSecondary(bEditTabs);
            AppTheme.stylePrimary(bPublish);

            bEditInfo.setOnAction(e -> UiUtils.info("À venir", "Édition infos générales."));
            bEditTabs.setOnAction(e -> UiUtils.info("À venir", "Édition des tableaux."));
            bPublish.setOnAction(e -> UiUtils.info("À venir", "Publication (DRAFT → OPEN)."));

            box.getChildren().addAll(bEditInfo, bEditTabs, bPublish);
        }

        return box;
    }

    private TournamentStatus mapStatus(String status) {
        if (status == null)
            return TournamentStatus.DRAFT;
        try {
            return TournamentStatus.valueOf(status.trim().toUpperCase());
        } catch (Exception e) {
            return TournamentStatus.DRAFT;
        }
    }
}