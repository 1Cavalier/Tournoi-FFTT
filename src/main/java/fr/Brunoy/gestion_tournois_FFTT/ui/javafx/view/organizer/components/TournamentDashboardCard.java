package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.components;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.AppRouter;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.TournamentDto;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.TournamentRegulationDto;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class TournamentDashboardCard extends VBox {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String MISSING_INFO = "information manquante";
    private static final double SECTION_WIDTH = 240;
    private static final double SECTION_SPACING = 6;

    private final AppRouter nav;
    private final TournamentDto tournament;
    private final TournamentRegulationDto regulation;

    public TournamentDashboardCard(AppRouter nav, TournamentDto tournament, TournamentRegulationDto regulation) {
        this.nav = Objects.requireNonNull(nav, "nav must not be null");
        this.tournament = Objects.requireNonNull(tournament, "tournament must not be null");
        this.regulation = regulation;
        build();
    }

    private void build() {
        VBox content = new VBox(AppTheme.SPACE_MD);
        content.getChildren().addAll(
                buildHeader(),
                buildMainRow(),
                buildActionsRow());

        VBox card = AppTheme.card(content);
        card.setMaxWidth(Double.MAX_VALUE);

        getChildren().setAll(card);
    }

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

    private HBox buildMainRow() {
        HBox row = new HBox(12);
        row.setAlignment(Pos.TOP_LEFT);

        VBox general = buildGeneralSection();
        VBox regulationBox = buildRegulationSection();
        VBox tableaux = buildTableauxSection();

        configureSectionGrow(general);
        configureSectionGrow(regulationBox);
        configureSectionGrow(tableaux);

        row.getChildren().addAll(general, regulationBox, tableaux);
        return row;
    }

    private VBox buildGeneralSection() {
        VBox content = new VBox(SECTION_SPACING);

        Label title = new Label("Général");
        AppTheme.applyCardTitle(title);

        content.getChildren().addAll(
                title,
                infoRow("Nom", tournament.name()),
                infoRow("Département", tournament.department()),
                infoRow("Adresse 1", tournament.address1()),
                infoRow("Adresse 2", tournament.address2()),
                infoRow("Ville", tournament.city()),
                infoRow("Niveau", prettyLevel(tournament.level())),
                infoRow("Phase", prettyPhase(tournament.phase())),
                infoRow("Date", buildDates()),
                infoRow("Homologation", tournament.homologationNumber()),
                verticalSpacer(6),
                fullWidthSecondaryButton("Modifier le tournoi", e -> nav.showEditTournamentGeneralDialog(tournament)));

        return buildSection(content);
    }

    private VBox buildRegulationSection() {
        VBox content = new VBox(SECTION_SPACING);

        Label title = new Label("Règlement");
        AppTheme.applyCardTitle(title);

        content.getChildren().addAll(
                title,
                infoRow("Contact", buildContactValue()),
                infoRow("Nbr table", regulation == null ? null : regulation.numberOfTables()),
                infoRow("Balle", regulation == null ? null : regulation.ballBrandAndType()),
                infoRow("Ouverture insc.", regulation == null ? null : regulation.registrationOpenTime()),
                infoRow("Fermeture insc.", regulation == null ? null : regulation.registrationDeadline()),
                infoRow("Ouverture gymnase", regulation == null ? null : regulation.gymOpenTime()),
                verticalSpacer(6),
                fullWidthSecondaryButton("Modifier le règlement",
                        e -> nav.showEditTournamentRegulationDialog(tournament)));

        return buildSection(content);
    }

    private VBox buildTableauxSection() {
        VBox content = new VBox(SECTION_SPACING);

        Label title = new Label("Tableaux");
        AppTheme.applyCardTitle(title);

        content.getChildren().addAll(
                title,
                infoRow("État", "À configurer"),
                infoRow("Disponibilité", isDraft() ? "Brouillon" : "Disponible"),
                verticalSpacer(6),
                fullWidthSecondaryButton("Voir les tableaux",
                        e -> nav.showTableauxManagementDialog(tournament)));

        return buildSection(content);
    }

    private HBox buildActionsRow() {
        Button publish = new Button("Publier");
        AppTheme.stylePrimary(publish);
        publish.setDisable(!isDraft());
        publish.setOnAction(e -> nav.showInfo("À venir", "Publication du tournoi."));

        Button delete = new Button("Supprimer le tournoi");
        delete.setStyle(
                "-fx-background-color: #C62828;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: 800;" +
                        "-fx-background-radius: " + AppTheme.RADIUS + ";" +
                        "-fx-padding: 10 14;" +
                        "-fx-cursor: hand;");
        delete.setOnAction(e -> nav.showInfo("À venir", "Suppression du tournoi."));

        HBox row = new HBox(10, publish, delete);
        row.setAlignment(Pos.CENTER_RIGHT);
        return row;
    }

    private VBox buildSection(VBox content) {
        VBox box = new VBox(content);
        box.setPadding(new Insets(10));
        box.setPrefWidth(SECTION_WIDTH);
        box.setStyle(
                "-fx-background-color: " + AppTheme.COLOR_SURFACE + ";" +
                        "-fx-background-radius: " + AppTheme.RADIUS + ";" +
                        "-fx-border-color: " + AppTheme.COLOR_BORDER + ";" +
                        "-fx-border-radius: " + AppTheme.RADIUS + ";");
        return box;
    }

    private HBox infoRow(String label, Object value) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

        Label keyLabel = new Label(label + " :");
        AppTheme.applyBody(keyLabel);
        keyLabel.setMinWidth(110);

        String displayValue = formatValue(value);

        Label valueLabel = new Label(displayValue);
        AppTheme.applyBody(valueLabel);
        valueLabel.setWrapText(true);

        if (MISSING_INFO.equalsIgnoreCase(displayValue)) {
            valueLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-weight: 700;");
        } else {
            valueLabel.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: 700;");
        }

        row.getChildren().addAll(keyLabel, valueLabel);
        return row;
    }

    private String buildDates() {
        try {
            LocalDate start = LocalDate.parse(OrganizerViewUtils.safe(tournament.startDate()));
            LocalDate end = LocalDate.parse(OrganizerViewUtils.safe(tournament.endDate()));
            return DATE_FORMAT.format(start) + " -> " + DATE_FORMAT.format(end);
        } catch (Exception e) {
            return OrganizerViewUtils.nvl(tournament.startDate()) + " -> "
                    + OrganizerViewUtils.nvl(tournament.endDate());
        }
    }

    private boolean isDraft() {
        return "DRAFT".equalsIgnoreCase(OrganizerViewUtils.safe(tournament.status()));
    }

    private String buildContactValue() {
        if (regulation == null) {
            return MISSING_INFO;
        }

        String name = optionalDisplay(regulation.organizerContactName());
        String phone = optionalDisplay(regulation.organizerPhone());

        if (name == null && phone == null) {
            return MISSING_INFO;
        }
        if (name != null && phone != null) {
            return name + " - " + phone;
        }
        return name != null ? name : phone;
    }

    private String optionalDisplay(String value) {
        String safe = OrganizerViewUtils.safe(value);
        return safe.isEmpty() ? null : safe;
    }

    private String prettyLevel(String value) {
        return switch (OrganizerViewUtils.safe(value)) {
            case "DEPARTEMENTAL" -> "Départemental";
            case "REGIONAL" -> "Régional";
            case "NATIONAL_B" -> "National B";
            case "NATIONAL_A" -> "National A";
            case "INTERNATIONAL" -> "International";
            default -> OrganizerViewUtils.nvl(value);
        };
    }

    private String prettyPhase(String value) {
        return switch (OrganizerViewUtils.safe(value)) {
            case "PHASE_1" -> "Phase 1";
            case "PHASE_2" -> "Phase 2";
            default -> OrganizerViewUtils.nvl(value);
        };
    }

    private String formatValue(Object value) {
        if (value == null) {
            return MISSING_INFO;
        }
        if (value instanceof String s) {
            String safe = OrganizerViewUtils.safe(s);
            return safe.isEmpty() ? MISSING_INFO : safe;
        }
        return String.valueOf(value);
    }

    private void configureSectionGrow(VBox section) {
        HBox.setHgrow(section, Priority.ALWAYS);
        section.setMaxWidth(Double.MAX_VALUE);
    }

    private Button fullWidthSecondaryButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        Button button = new Button(text);
        AppTheme.styleSecondary(button);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(action);
        return button;
    }

    private Region verticalSpacer(double height) {
        Region region = new Region();
        region.setMinHeight(height);
        return region;
    }
}