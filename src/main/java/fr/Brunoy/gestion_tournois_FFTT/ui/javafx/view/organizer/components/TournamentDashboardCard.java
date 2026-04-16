package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.components;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.AppRouter;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.TournamentDto;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.TournamentOfficialAssignmentDto;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.TournamentRegulationDto;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class TournamentDashboardCard extends VBox {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String MISSING_VALUE = "---";
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

    // =========================================================================
    // CONSTRUCTION
    // =========================================================================

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

        // Forcer une hauteur égale sur les 3 blocs :
        // chaque bloc prend la hauteur du plus grand.
        general.setMaxHeight(Double.MAX_VALUE);
        regulationBox.setMaxHeight(Double.MAX_VALUE);
        tableaux.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(general, Priority.ALWAYS);
        VBox.setVgrow(regulationBox, Priority.ALWAYS);
        VBox.setVgrow(tableaux, Priority.ALWAYS);

        row.getChildren().addAll(general, regulationBox, tableaux);
        return row;
    }

    // =========================================================================
    // BLOC GÉNÉRAL
    // =========================================================================

    private VBox buildGeneralSection() {
        Label title = new Label("Général");
        AppTheme.applyCardTitle(title);

        VBox infoBox = new VBox(SECTION_SPACING);
        infoBox.getChildren().addAll(
                buildSectionHeader(title, computeGeneralBlockState()),
                infoRow("Nom", tournament.name(), FieldState.required(tournament.name())),
                infoRow("Département", tournament.department(), FieldState.required(tournament.department())),
                infoRow("Adresse 1", tournament.address1(), FieldState.required(tournament.address1())),
                infoRow("Adresse 2", tournament.address2(), FieldState.optionalValue(tournament.address2())),
                infoRow("Ville", tournament.city(), FieldState.required(tournament.city())),
                infoRow("Niveau", prettyLevel(tournament.level()), FieldState.required(tournament.level())),
                infoRow("Phase", prettyPhase(tournament.phase()), FieldState.required(tournament.phase())),
                infoRow("Date", buildDatesValue(), computeDatesFieldState()),
                infoRow("Homologation", buildHomologationValue(), computeHomologationFieldState()));

        Button btn = fullWidthSecondaryButton("Modifier le tournoi",
                e -> nav.showEditTournamentGeneralDialog(tournament));

        VBox content = new VBox(SECTION_SPACING, infoBox, btn);
        VBox.setVgrow(infoBox, Priority.ALWAYS); // pousse le bouton vers le bas

        return buildSection(content);
    }

    // =========================================================================
    // BLOC RÈGLEMENT — retravaillé
    // =========================================================================

    private VBox buildRegulationSection() {
        Label title = new Label("Règlement");
        AppTheme.applyCardTitle(title);

        VBox infoBox = new VBox(SECTION_SPACING);
        infoBox.getChildren().addAll(
                buildSectionHeader(title, computeRegulationBlockState()),

                // Contact : Nom Prénom — Téléphone sur une ligne
                infoRow("Contact", buildContactValue(), computeContactFieldState()),

                // Nombre de tables
                infoRow("Tables", buildTablesValue(),
                        FieldState.required(regulation == null ? null : regulation.numberOfTables())),

                // Balles : si plusieurs, afficher le nombre
                infoRow("Balle(s)", buildBallValue(), computeBallFieldState()),

                // Inscriptions
                infoRow("Ouverture insc.", buildRegistrationOpenValue(),
                        FieldState.required(regulation == null ? null : regulation.registrationOpenTime())),
                infoRow("Fermeture insc.", buildRegistrationDeadlineValue(),
                        FieldState.required(regulation == null ? null : regulation.registrationDeadline())),

                // Gymnase : créneau unique ou résumé multi-jours
                infoRow("Gymnase", buildGymScheduleValue(), computeGymFieldState()),

                // JA : obligatoire — rouge si absent
                infoRow("Juge-arbitre", buildJaValue(), computeJaFieldState()),

                // Arbitres : recommandation — toujours vert
                infoRow("Arbitres", buildRefereeValue(), FieldState.valid("Recommandation.")));

        Button btn = fullWidthSecondaryButton("Modifier le règlement",
                e -> nav.showEditTournamentRegulationDialog(tournament));

        VBox content = new VBox(SECTION_SPACING, infoBox, btn);
        VBox.setVgrow(infoBox, Priority.ALWAYS);

        return buildSection(content);
    }

    // =========================================================================
    // BLOC TABLEAUX
    // =========================================================================

    private VBox buildTableauxSection() {
        Label title = new Label("Tableaux");
        AppTheme.applyCardTitle(title);

        VBox infoBox = new VBox(SECTION_SPACING);
        infoBox.getChildren().addAll(
                buildSectionHeader(title, BlockState.PARTIAL),
                infoRow("État", "À configurer",
                        FieldState.partial("Information partiellement renseignée.")),
                infoRow("Disponibilité", isDraft() ? "Brouillon" : "Disponible",
                        FieldState.valid("Information complète.")));

        Button btn = fullWidthSecondaryButton("Voir les tableaux",
                e -> nav.showTableauxManagementDialog(tournament));

        VBox content = new VBox(SECTION_SPACING, infoBox, btn);
        VBox.setVgrow(infoBox, Priority.ALWAYS);

        return buildSection(content);
    }

    // =========================================================================
    // ACTIONS
    // =========================================================================

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

    // =========================================================================
    // VALEURS — Règlement
    // =========================================================================

    /**
     * Contact : "Dupont Jean — 06 00 00 00 00"
     * Si le nom est absent mais le téléphone présent, affiche juste le téléphone.
     */
    private String buildContactValue() {
        if (regulation == null)
            return MISSING_VALUE;

        String name = optionalDisplay(regulation.organizerContactName());
        String phone = optionalDisplay(regulation.organizerPhone());

        if (name == null && phone == null)
            return MISSING_VALUE;
        if (name != null && phone != null)
            return name + " — " + phone;
        return name != null ? name : phone;
    }

    private FieldState computeContactFieldState() {
        if (regulation == null)
            return FieldState.missing("Contact non renseigné.");

        String name = optionalDisplay(regulation.organizerContactName());
        String phone = optionalDisplay(regulation.organizerPhone());

        if (name != null && phone != null)
            return FieldState.valid("Contact complet.");
        if (name != null || phone != null)
            return FieldState.partial("Contact partiellement renseigné.");
        return FieldState.missing("Contact non renseigné.");
    }

    /**
     * Tables : "16 tables" ou MISSING_VALUE
     */
    private String buildTablesValue() {
        if (regulation == null || regulation.numberOfTables() == null)
            return MISSING_VALUE;
        return regulation.numberOfTables() + " table" + (regulation.numberOfTables() > 1 ? "s" : "");
    }

    /**
     * Balles : liste sérialisée avec " | ".
     * - 1 balle → affiche le nom complet
     * - 2+ balles → "3 balles retenues"
     */
    private String buildBallValue() {
        if (regulation == null)
            return MISSING_VALUE;
        String raw = regulation.ballBrandAndType();
        if (raw == null || raw.isBlank())
            return MISSING_VALUE;

        // Séparateur canonique : " | " (espace-pipe-espace)
        // On utilise Pattern.quote pour éviter tout conflit avec les tirets — dans les
        // noms
        String[] parts = raw.split(java.util.regex.Pattern.quote(" | "));
        if (parts.length == 1)
            return parts[0].trim();
        return parts.length + " balles retenues";
    }

    private FieldState computeBallFieldState() {
        if (regulation == null)
            return FieldState.missing("Balle non renseignée.");
        String raw = regulation.ballBrandAndType();
        if (raw == null || raw.isBlank())
            return FieldState.missing("Balle non renseignée.");
        return FieldState.valid("Balle(s) renseignée(s).");
    }

    /**
     * Ouverture inscriptions : affichage lisible de la date stockée.
     * Format attendu en entrée : ISO ou texte libre.
     */
    private String buildRegistrationOpenValue() {
        if (regulation == null)
            return MISSING_VALUE;
        String raw = regulation.registrationOpenTime();
        if (raw == null || raw.isBlank())
            return MISSING_VALUE;
        String formatted = formatIsoToFr(raw);
        return formatted.isBlank() ? MISSING_VALUE : formatted;
    }

    /**
     * Fermeture inscriptions.
     */
    private String buildRegistrationDeadlineValue() {
        if (regulation == null)
            return MISSING_VALUE;
        String raw = regulation.registrationDeadline();
        if (raw == null || raw.isBlank())
            return MISSING_VALUE;
        String formatted = formatIsoToFr(raw);
        return formatted.isBlank() ? MISSING_VALUE : formatted;
    }

    /**
     * Gymnase :
     * - 0 créneau → MISSING_VALUE
     * - 1 créneau → affiche le créneau complet
     * - 2+ créneaux → "3 créneaux définis"
     */
    private String buildGymScheduleValue() {
        if (regulation == null)
            return MISSING_VALUE;
        String raw = regulation.gymOpenTime();
        if (raw == null || raw.isBlank())
            return MISSING_VALUE;

        String[] parts = raw.split(java.util.regex.Pattern.quote(" | "));
        if (parts.length == 1) {
            String formatted = formatIsoToFr(parts[0].trim());
            return formatted.isBlank() ? parts[0].trim() : formatted;
        }
        return parts.length + " créneaux définis";
    }

    private FieldState computeGymFieldState() {
        if (regulation == null)
            return FieldState.missing("Horaire gymnase non renseigné.");
        String raw = regulation.gymOpenTime();
        if (raw == null || raw.isBlank())
            return FieldState.missing("Horaire gymnase non renseigné.");
        return FieldState.valid("Horaire(s) gymnase renseigné(s).");
    }

    /**
     * Juge-arbitre : "2 JA assignés" ou "Aucun JA assigné" (obligatoire FFTT).
     */
    private String buildJaValue() {
        int count = countOfficials("JUGE_ARBITRE");
        if (count == 0)
            return "Aucun JA assigné";
        return count + " JA assigné" + (count > 1 ? "s" : "");
    }

    private FieldState computeJaFieldState() {
        int count = countOfficials("JUGE_ARBITRE");
        if (count == 0)
            return FieldState.missing("Au moins 1 juge-arbitre est obligatoire (FFTT).");
        return FieldState.valid(count + " juge-arbitre(s) assigné(s).");
    }

    /**
     * Arbitres : "2 arbitres assignés" ou "Aucun arbitre" (recommandation →
     * toujours vert).
     */
    private String buildRefereeValue() {
        int count = countOfficials("ARBITRE");
        if (count == 0)
            return "Aucun arbitre assigné";
        return count + " arbitre" + (count > 1 ? "s" : "") + " assigné" + (count > 1 ? "s" : "");
    }

    /**
     * Compte les officiels d'un rôle donné dans la liste assignée.
     */
    private int countOfficials(String roleType) {
        if (regulation == null || regulation.assignedOfficials() == null)
            return 0;
        return (int) regulation.assignedOfficials().stream()
                .filter(o -> roleType.equalsIgnoreCase(o.officialRoleType()))
                .count();
    }

    // =========================================================================
    // CALCUL ÉTAT GLOBAL RÈGLEMENT
    // =========================================================================

    private BlockState computeRegulationBlockState() {
        FieldState[] states = new FieldState[] {
                computeContactFieldState(),
                FieldState.required(regulation == null ? null : regulation.numberOfTables()),
                computeBallFieldState(),
                FieldState.required(regulation == null ? null : regulation.registrationOpenTime()),
                FieldState.required(regulation == null ? null : regulation.registrationDeadline()),
                computeGymFieldState(),
                computeJaFieldState()
                // Arbitres non inclus : recommandation, ne bloque pas l'état complet
        };

        int validCount = 0;
        for (FieldState s : states) {
            if (s.kind == FieldKind.VALID)
                validCount++;
        }

        return validCount == states.length ? BlockState.COMPLETE : BlockState.PARTIAL;
    }

    // =========================================================================
    // VALEURS — Général
    // =========================================================================

    private String buildDatesValue() {
        boolean hasStart = hasText(tournament.startDate());
        boolean hasEnd = hasText(tournament.endDate());

        if (!hasStart && !hasEnd)
            return MISSING_VALUE;

        if (hasStart && hasEnd) {
            try {
                LocalDate start = LocalDate.parse(OrganizerViewUtils.safe(tournament.startDate()));
                LocalDate end = LocalDate.parse(OrganizerViewUtils.safe(tournament.endDate()));
                return DATE_FORMAT.format(start) + " → " + DATE_FORMAT.format(end);
            } catch (Exception e) {
                return OrganizerViewUtils.nvl(tournament.startDate())
                        + " → " + OrganizerViewUtils.nvl(tournament.endDate());
            }
        }

        String start = hasStart ? OrganizerViewUtils.nvl(tournament.startDate()) : MISSING_VALUE;
        String end = hasEnd ? OrganizerViewUtils.nvl(tournament.endDate()) : MISSING_VALUE;
        return start + " → " + end;
    }

    private FieldState computeDatesFieldState() {
        boolean hasStart = hasText(tournament.startDate());
        boolean hasEnd = hasText(tournament.endDate());

        if (hasStart && hasEnd)
            return FieldState.valid("Dates complètes.");
        if (hasStart || hasEnd)
            return FieldState.partial("Dates partiellement renseignées.");
        return FieldState.missing("Dates obligatoires manquantes.");
    }

    private String buildHomologationValue() {
        if (!hasText(tournament.homologationNumber()))
            return MISSING_VALUE;
        return OrganizerViewUtils.nvl(tournament.homologationNumber());
    }

    private FieldState computeHomologationFieldState() {
        if (hasText(tournament.homologationNumber()))
            return FieldState.valid("Numéro d'homologation présent.");
        return FieldState.pendingFftt("En attente de vérification FFTT.");
    }

    private BlockState computeGeneralBlockState() {
        FieldState[] states = new FieldState[] {
                FieldState.required(tournament.name()),
                FieldState.required(tournament.department()),
                FieldState.required(tournament.address1()),
                FieldState.required(tournament.city()),
                FieldState.required(tournament.level()),
                FieldState.required(tournament.phase()),
                computeDatesFieldState()
        };

        int validCount = 0;
        for (FieldState s : states) {
            if (s.kind == FieldKind.VALID)
                validCount++;
        }
        return validCount == states.length ? BlockState.COMPLETE : BlockState.PARTIAL;
    }

    // =========================================================================
    // COMPOSANTS UI
    // =========================================================================

    private VBox buildSection(VBox content) {
        VBox box = new VBox(content);
        box.setPadding(new Insets(10));
        box.setPrefWidth(SECTION_WIDTH);
        box.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(content, Priority.ALWAYS); // contenu remplit la hauteur du wrapper
        box.setStyle(
                "-fx-background-color: " + AppTheme.COLOR_SURFACE + ";" +
                        "-fx-background-radius: " + AppTheme.RADIUS + ";" +
                        "-fx-border-color: " + AppTheme.COLOR_BORDER + ";" +
                        "-fx-border-radius: " + AppTheme.RADIUS + ";");
        return box;
    }

    private HBox buildSectionHeader(Label title, BlockState state) {
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label stateLabel = new Label(state.label);
        AppTheme.applyBody(stateLabel);
        stateLabel.setStyle("-fx-text-fill: " + state.color + "; -fx-font-weight: 800;");

        header.getChildren().addAll(title, spacer, stateLabel);
        return header;
    }

    private HBox infoRow(String label, Object value, FieldState state) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

        Label keyLabel = new Label(label + " :");
        AppTheme.applyBody(keyLabel);
        keyLabel.setMinWidth(110);

        Label valueLabel = new Label(formatDisplayValue(value));
        AppTheme.applyBody(valueLabel);
        valueLabel.setWrapText(true);
        valueLabel.setStyle("-fx-text-fill: #111111; -fx-font-weight: 700;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label symbolLabel = new Label(state.symbol);
        AppTheme.applyBody(symbolLabel);
        symbolLabel.setStyle(
                "-fx-text-fill: " + state.color + ";" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 15px;");

        Tooltip.install(symbolLabel, new Tooltip(
                state.tooltip == null || state.tooltip.isBlank()
                        ? "Information indisponible."
                        : state.tooltip));

        row.getChildren().addAll(keyLabel, valueLabel, spacer, symbolLabel);
        return row;
    }

    private Button fullWidthSecondaryButton(String text,
            javafx.event.EventHandler<javafx.event.ActionEvent> action) {
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

    private void configureSectionGrow(VBox section) {
        HBox.setHgrow(section, Priority.ALWAYS);
        section.setMaxWidth(Double.MAX_VALUE);
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    /**
     * Formate une valeur ISO "YYYY-MM-DDTHH:MM" en "JJ/MM/YYYY - XXhXXm".
     * Ex : "2026-05-12T07:30" → "12/05/2026 - 07h30m"
     * Si non reconnu (valeur legacy), retourne la valeur brute.
     */
    private static String formatIsoToFr(String iso) {
        if (iso == null || iso.isBlank())
            return "";
        try {
            java.time.LocalDateTime dt = java.time.LocalDateTime.parse(iso.trim(),
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
            return String.format("%02d/%02d/%04d - %02dh%02dm",
                    dt.getDayOfMonth(), dt.getMonthValue(), dt.getYear(),
                    dt.getHour(), dt.getMinute());
        } catch (Exception e) {
            return iso.trim();
        }
    }

    private boolean isDraft() {
        return "DRAFT".equalsIgnoreCase(OrganizerViewUtils.safe(tournament.status()));
    }

    private boolean hasText(String value) {
        return !OrganizerViewUtils.safe(value).isEmpty();
    }

    private String optionalDisplay(String value) {
        String safe = OrganizerViewUtils.safe(value);
        return safe.isEmpty() ? null : safe;
    }

    /**
     * Retourne la valeur ou MISSING_VALUE si vide.
     */
    private String nvlDisplay(String value) {
        String safe = OrganizerViewUtils.safe(value);
        return safe.isEmpty() ? MISSING_VALUE : safe;
    }

    private String formatDisplayValue(Object value) {
        if (value == null)
            return MISSING_VALUE;
        if (value instanceof String s) {
            String safe = OrganizerViewUtils.safe(s);
            return safe.isEmpty() ? MISSING_VALUE : safe;
        }
        return String.valueOf(value);
    }

    private String prettyLevel(String value) {
        String safe = OrganizerViewUtils.safe(value);
        if (safe.isEmpty())
            return OrganizerViewUtils.nvl(value);
        try {
            return fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.TournamentLevel.valueOf(safe).label();
        } catch (Exception e) {
            return safe;
        }
    }

    private String prettyPhase(String value) {
        return switch (OrganizerViewUtils.safe(value)) {
            case "PHASE_1" -> "Phase 1 (oct. → déc.)";
            case "PHASE_2" -> "Phase 2 (janv. → juillet)";
            default -> OrganizerViewUtils.nvl(value);
        };
    }

    // =========================================================================
    // FIELD STATE
    // =========================================================================

    private enum FieldKind {
        MISSING, PARTIAL, OPTIONAL, PENDING_FFTT, VALID
    }

    private static final class FieldState {
        private final FieldKind kind;
        private final String symbol;
        private final String color;
        private final String tooltip;

        private FieldState(FieldKind kind, String symbol, String color, String tooltip) {
            this.kind = kind;
            this.symbol = symbol;
            this.color = color;
            this.tooltip = tooltip;
        }

        private static FieldState required(Object value) {
            return isPresent(value) ? valid("Information complète.") : missing("Information obligatoire manquante.");
        }

        private static FieldState optionalValue(Object value) {
            return isPresent(value) ? valid("Information complète.") : optionalState("Information optionnelle.");
        }

        private static FieldState missing(String tooltip) {
            return new FieldState(FieldKind.MISSING, "✕", "#D32F2F", tooltip);
        }

        private static FieldState partial(String tooltip) {
            return new FieldState(FieldKind.PARTIAL, "≈≈", "#F57C00", tooltip);
        }

        private static FieldState optionalState(String tooltip) {
            return new FieldState(FieldKind.OPTIONAL, "~", "#B26A00", tooltip);
        }

        private static FieldState pendingFftt(String tooltip) {
            return new FieldState(FieldKind.PENDING_FFTT, "…", "#7B1FA2", tooltip);
        }

        private static FieldState valid(String tooltip) {
            return new FieldState(FieldKind.VALID, "✓", "#2E7D32", tooltip);
        }

        private static boolean isPresent(Object value) {
            if (value == null)
                return false;
            if (value instanceof String s)
                return !OrganizerViewUtils.safe(s).isEmpty();
            return true;
        }
    }

    // =========================================================================
    // BLOCK STATE
    // =========================================================================

    private enum BlockState {
        COMPLETE("Complet", "#2E7D32"),
        PARTIAL("Partiellement complet", "#F57C00");

        private final String label;
        private final String color;

        BlockState(String label, String color) {
            this.label = label;
            this.color = color;
        }
    }
}