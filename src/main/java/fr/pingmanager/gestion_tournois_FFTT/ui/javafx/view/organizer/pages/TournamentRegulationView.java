package fr.pingmanager.gestion_tournois_FFTT.ui.javafx.view.organizer.pages;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Objects;

import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.app.AppRouter;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TournamentDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TournamentRegulationDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;

/**
 * Vue inline affichant le règlement du tournoi et permettant de le modifier.
 * Remplace l'ancien EditTournamentRegulationDialog (popup).
 */
public class TournamentRegulationView extends VBox {

    private final AppRouter nav;
    private final TournamentDto tournament;
    private final TournamentRegulationDto regulation;

    public TournamentRegulationView(AppRouter nav,
            TournamentDto tournament,
            TournamentRegulationDto regulation) {
        this.nav = Objects.requireNonNull(nav);
        this.tournament = Objects.requireNonNull(tournament);
        this.regulation = regulation;
        build();
    }

    private void build() {
        AppTheme.applyPage(this);

        VBox root = new VBox(AppTheme.SPACE_LG);
        root.setPadding(new Insets(28));
        root.setMaxWidth(Double.MAX_VALUE);

        root.getChildren().add(buildHeader());
        root.getChildren().add(buildContactCard());
        root.getChildren().add(buildVenueCard());
        root.getChildren().add(buildBallCard());
        root.getChildren().add(buildOfficialsCard());
        root.getChildren().add(buildActions());

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: transparent;");

        getChildren().setAll(scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
    }

    private VBox buildHeader() {
        Label title = new Label("Règlement");
        AppTheme.applyTitle(title);

        Label subtitle = new Label(
                "Informations réglementaires du tournoi : contact, lieu, matériel, officiels.");
        AppTheme.applyBody(subtitle);
        subtitle.setWrapText(true);

        return new VBox(AppTheme.SPACE_SM, title, subtitle);
    }

    private VBox buildContactCard() {
        return buildSection("Contact organisateur",
                buildRow("Nom du contact", regulation == null ? "—" : safe(regulation.organizerContactName())),
                buildRow("Email", regulation == null ? "—" : safe(regulation.organizerEmail())),
                buildRow("Téléphone", regulation == null ? "—" : safe(regulation.organizerPhone())));
    }

    private VBox buildVenueCard() {
        return buildSection("Aire de jeu",
                buildRow("Nombre de tables", regulation == null ? "—" : safeInt(regulation.numberOfTables())),
                buildRow("Marque / type", regulation == null ? "—" : safe(regulation.ballBrandAndType())),
                buildRow("Longueur (m)", regulation == null ? "—" : safeInt(regulation.playingAreaLengthMeters())),
                buildRow("Largeur (m)", regulation == null ? "—" : safeInt(regulation.playingAreaWidthMeters())));
    }

    private VBox buildBallCard() {
        return buildSection("Balles",
                buildRow("Balle homologuée", regulation == null ? "—" : safe(regulation.ballBrandAndType())),
                buildRow("Politique de fourniture", regulation == null ? "—" : safe(regulation.ballProvisionPolicy())));
    }

    private VBox buildOfficialsCard() {
        return buildSection("Officiels",
                buildRow("Grade JA requis", regulation == null ? "—" : safe(regulation.requiredJudgeGrade())),
                buildRow("Nombre de JA conseillé",
                        regulation == null ? "—" : safeInt(regulation.recommendedJudgeCount())),
                buildRow("Grade arbitre conseillé",
                        regulation == null ? "—" : safe(regulation.recommendedRefereeGrade())),
                buildRow("Nombre d'arbitres",
                        regulation == null ? "—" : safeInt(regulation.recommendedRefereeCount())));
    }

    private VBox buildSection(String title, HBox... rows) {
        Label sectionTitle = new Label(title);
        AppTheme.applyCardTitle(sectionTitle);

        VBox content = new VBox(AppTheme.SPACE_SM);
        content.getChildren().add(sectionTitle);
        content.getChildren().addAll(rows);

        VBox card = AppTheme.card(content);
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private HBox buildRow(String label, String value) {
        Label keyLabel = new Label(label + " :");
        AppTheme.applyBody(keyLabel);
        keyLabel.setMinWidth(200);
        keyLabel.setStyle("-fx-text-fill: #64748B;");

        Label valLabel = new Label(value);
        AppTheme.applyBody(valLabel);
        valLabel.setWrapText(true);
        valLabel.setStyle("-fx-font-weight: 600;");

        HBox row = new HBox(AppTheme.SPACE_MD, keyLabel, valLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private HBox buildActions() {
        Button editButton = new Button("Modifier le règlement");
        AppTheme.stylePrimary(editButton);
        editButton.setOnAction(e -> nav.showEditTournamentRegulationDialog(tournament));

        HBox actions = new HBox(editButton);
        actions.setAlignment(Pos.CENTER_LEFT);
        return actions;
    }

    private String safe(String v) {
        return (v == null || v.isBlank()) ? "—" : v.trim();
    }

    private String safeInt(Integer v) {
        return v == null ? "—" : String.valueOf(v);
    }
}