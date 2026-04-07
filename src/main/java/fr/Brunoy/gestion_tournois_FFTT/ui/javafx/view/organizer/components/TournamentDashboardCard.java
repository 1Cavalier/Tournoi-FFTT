package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.components;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.AppRouter;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.TournamentDto;
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
import java.util.Objects;

public class TournamentDashboardCard extends VBox {

    /**
     * Format d'affichage des dates dans la carte dashboard.
     */
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Texte affiché lorsqu'aucune valeur n'est disponible.
     */
    private static final String MISSING_VALUE = "---";

    /**
     * Largeur de base d'un bloc.
     */
    private static final double SECTION_WIDTH = 240;

    /**
     * Espacement vertical entre les lignes d'un bloc.
     */
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

    /**
     * Construction principale de la carte.
     */
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

    /**
     * En-tête de la carte : nom du tournoi + badge de statut.
     */
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

    /**
     * Ligne principale contenant les différents blocs.
     */
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

    /**
     * Bloc Général.
     */
    private VBox buildGeneralSection() {
        VBox content = new VBox(SECTION_SPACING);

        Label title = new Label("Général");
        AppTheme.applyCardTitle(title);

        HBox header = buildSectionHeader(title, computeGeneralBlockState());

        content.getChildren().addAll(
                header,
                infoRow("Nom", tournament.name(), FieldState.required(tournament.name())),
                infoRow("Département", tournament.department(), FieldState.required(tournament.department())),
                infoRow("Adresse 1", tournament.address1(), FieldState.required(tournament.address1())),
                infoRow("Adresse 2", tournament.address2(), FieldState.optionalValue(tournament.address2())),
                infoRow("Ville", tournament.city(), FieldState.required(tournament.city())),
                infoRow("Niveau", prettyLevel(tournament.level()), FieldState.required(tournament.level())),
                infoRow("Phase", prettyPhase(tournament.phase()), FieldState.required(tournament.phase())),
                infoRow("Date", buildDatesValue(), computeDatesFieldState()),
                infoRow("Homologation", buildHomologationValue(), computeHomologationFieldState()),
                verticalSpacer(6),
                fullWidthSecondaryButton("Modifier le tournoi",
                        e -> nav.showEditTournamentGeneralDialog(tournament)));

        return buildSection(content);
    }

    /**
     * Bloc Règlement.
     */
    private VBox buildRegulationSection() {
        VBox content = new VBox(SECTION_SPACING);

        Label title = new Label("Règlement");
        AppTheme.applyCardTitle(title);

        HBox header = buildSectionHeader(title, computeRegulationBlockState());

        content.getChildren().addAll(
                header,
                infoRow("Contact", buildContactValue(), computeContactFieldState()),
                infoRow("Nbr table", regulation == null ? null : regulation.numberOfTables(),
                        FieldState.required(regulation == null ? null : regulation.numberOfTables())),
                infoRow("Balle", regulation == null ? null : regulation.ballBrandAndType(),
                        FieldState.required(regulation == null ? null : regulation.ballBrandAndType())),
                infoRow("Ouverture insc.", regulation == null ? null : regulation.registrationOpenTime(),
                        FieldState.required(regulation == null ? null : regulation.registrationOpenTime())),
                infoRow("Fermeture insc.", regulation == null ? null : regulation.registrationDeadline(),
                        FieldState.required(regulation == null ? null : regulation.registrationDeadline())),
                infoRow("Ouverture gymnase", regulation == null ? null : regulation.gymOpenTime(),
                        FieldState.required(regulation == null ? null : regulation.gymOpenTime())),
                verticalSpacer(6),
                fullWidthSecondaryButton("Modifier le règlement",
                        e -> nav.showEditTournamentRegulationDialog(tournament)));

        return buildSection(content);
    }

    /**
     * Bloc Tableaux.
     * Cette partie reste encore simple pour l'instant, mais le système d'état
     * permet déjà d'afficher un cas partiel.
     */
    private VBox buildTableauxSection() {
        VBox content = new VBox(SECTION_SPACING);

        Label title = new Label("Tableaux");
        AppTheme.applyCardTitle(title);

        HBox header = buildSectionHeader(title, BlockState.PARTIAL);

        content.getChildren().addAll(
                header,
                infoRow("État", "À configurer",
                        FieldState.partial("Information partiellement renseignée.")),
                infoRow("Disponibilité", isDraft() ? "Brouillon" : "Disponible",
                        FieldState.valid("Information complète.")),
                verticalSpacer(6),
                fullWidthSecondaryButton("Voir les tableaux",
                        e -> nav.showTableauxManagementDialog(tournament)));

        return buildSection(content);
    }

    /**
     * Ligne des actions du bas.
     */
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

    /**
     * Construction visuelle d'un bloc.
     */
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

    /**
     * En-tête d'un bloc : titre à gauche, état global à droite.
     */
    private HBox buildSectionHeader(Label title, BlockState state) {
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label stateLabel = new Label(state.label);
        AppTheme.applyBody(stateLabel);
        stateLabel.setStyle(
                "-fx-text-fill: " + state.color + ";" +
                        "-fx-font-weight: 800;");

        header.getChildren().addAll(title, spacer, stateLabel);
        return header;
    }

    /**
     * Création d'une ligne d'information :
     * - libellé
     * - valeur en noir
     * - symbole coloré à droite
     * - tooltip sur le symbole
     */
    private HBox infoRow(String label, Object value, FieldState state) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

        Label keyLabel = new Label(label + " :");
        AppTheme.applyBody(keyLabel);
        keyLabel.setMinWidth(110);

        String displayValue = formatDisplayValue(value);

        Label valueLabel = new Label(displayValue);
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

    /**
     * Construction de la valeur d'affichage des dates.
     * Peut retourner :
     * - les deux dates formatées
     * - une valeur partielle si une seule date est présente
     * - --- si aucune date n'est présente
     */
    private String buildDatesValue() {
        boolean hasStart = hasText(tournament.startDate());
        boolean hasEnd = hasText(tournament.endDate());

        if (!hasStart && !hasEnd) {
            return MISSING_VALUE;
        }

        if (hasStart && hasEnd) {
            try {
                LocalDate start = LocalDate.parse(OrganizerViewUtils.safe(tournament.startDate()));
                LocalDate end = LocalDate.parse(OrganizerViewUtils.safe(tournament.endDate()));
                return DATE_FORMAT.format(start) + " -> " + DATE_FORMAT.format(end);
            } catch (Exception e) {
                return OrganizerViewUtils.nvl(tournament.startDate()) + " -> "
                        + OrganizerViewUtils.nvl(tournament.endDate());
            }
        }

        String start = hasStart ? OrganizerViewUtils.nvl(tournament.startDate()) : MISSING_VALUE;
        String end = hasEnd ? OrganizerViewUtils.nvl(tournament.endDate()) : MISSING_VALUE;
        return start + " -> " + end;
    }

    /**
     * Détermination de l'état du champ Date.
     */
    private FieldState computeDatesFieldState() {
        boolean hasStart = hasText(tournament.startDate());
        boolean hasEnd = hasText(tournament.endDate());

        if (hasStart && hasEnd) {
            return FieldState.valid("Information complète.");
        }
        if (hasStart || hasEnd) {
            return FieldState.partial("Information partiellement renseignée.");
        }
        return FieldState.missing("Information obligatoire manquante.");
    }

    /**
     * Vérifie si le tournoi est en brouillon.
     */
    private boolean isDraft() {
        return "DRAFT".equalsIgnoreCase(OrganizerViewUtils.safe(tournament.status()));
    }

    /**
     * Construction de la valeur Contact à partir du règlement.
     */
    private String buildContactValue() {
        if (regulation == null) {
            return MISSING_VALUE;
        }

        String name = optionalDisplay(regulation.organizerContactName());
        String phone = optionalDisplay(regulation.organizerPhone());

        if (name == null && phone == null) {
            return MISSING_VALUE;
        }
        if (name != null && phone != null) {
            return name + " - " + phone;
        }
        return name != null ? name : phone;
    }

    /**
     * Détermination de l'état du champ Contact.
     */
    private FieldState computeContactFieldState() {
        if (regulation == null) {
            return FieldState.missing("Information obligatoire manquante.");
        }

        String name = optionalDisplay(regulation.organizerContactName());
        String phone = optionalDisplay(regulation.organizerPhone());

        if (name != null && phone != null) {
            return FieldState.valid("Information complète.");
        }
        if (name != null || phone != null) {
            return FieldState.partial("Information partiellement renseignée.");
        }
        return FieldState.missing("Information obligatoire manquante.");
    }

    /**
     * Valeur affichée pour l'homologation.
     * Si vide, on reste volontairement sur --- car le numéro n'est pas encore
     * attribué.
     */
    private String buildHomologationValue() {
        if (!hasText(tournament.homologationNumber())) {
            return MISSING_VALUE;
        }
        return OrganizerViewUtils.nvl(tournament.homologationNumber());
    }

    /**
     * Etat spécial pour l'homologation :
     * - si présent : valide
     * - si absent : en attente de vérification FFTT
     */
    private FieldState computeHomologationFieldState() {
        if (hasText(tournament.homologationNumber())) {
            return FieldState.valid("Numéro d'homologation présent.");
        }
        return FieldState.pendingFftt("En attente de vérification FFTT.");
    }

    /**
     * Retourne null si la chaîne est vide.
     */
    private String optionalDisplay(String value) {
        String safe = OrganizerViewUtils.safe(value);
        return safe.isEmpty() ? null : safe;
    }

    /**
     * Conversion technique -> affichage lisible du niveau.
     */
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

    /**
     * Conversion technique -> affichage lisible de la phase.
     */
    private String prettyPhase(String value) {
        return switch (OrganizerViewUtils.safe(value)) {
            case "PHASE_1" -> "Phase 1";
            case "PHASE_2" -> "Phase 2";
            default -> OrganizerViewUtils.nvl(value);
        };
    }

    /**
     * Formate une valeur pour affichage.
     * Si vide -> ---.
     */
    private String formatDisplayValue(Object value) {
        if (value == null) {
            return MISSING_VALUE;
        }
        if (value instanceof String s) {
            String safe = OrganizerViewUtils.safe(s);
            return safe.isEmpty() ? MISSING_VALUE : safe;
        }
        return String.valueOf(value);
    }

    /**
     * Permet à un bloc de prendre la largeur disponible.
     */
    private void configureSectionGrow(VBox section) {
        HBox.setHgrow(section, Priority.ALWAYS);
        section.setMaxWidth(Double.MAX_VALUE);
    }

    /**
     * Création d'un bouton secondaire prenant toute la largeur.
     */
    private Button fullWidthSecondaryButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        Button button = new Button(text);
        AppTheme.styleSecondary(button);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(action);
        return button;
    }

    /**
     * Petit espace vertical.
     */
    private Region verticalSpacer(double height) {
        Region region = new Region();
        region.setMinHeight(height);
        return region;
    }

    /**
     * Calcul du statut global du bloc Général.
     * Les champs optionnels ou externes ne bloquent pas le statut complet.
     */
    private BlockState computeGeneralBlockState() {
        int validCount = 0;

        FieldState[] states = new FieldState[] {
                FieldState.required(tournament.name()),
                FieldState.required(tournament.department()),
                FieldState.required(tournament.address1()),
                FieldState.required(tournament.city()),
                FieldState.required(tournament.level()),
                FieldState.required(tournament.phase()),
                computeDatesFieldState()
        };

        for (FieldState state : states) {
            if (state.kind == FieldKind.VALID) {
                validCount++;
            }
        }

        if (validCount == states.length) {
            return BlockState.COMPLETE;
        }
        return BlockState.PARTIAL;
    }

    /**
     * Calcul du statut global du bloc Règlement.
     */
    private BlockState computeRegulationBlockState() {
        FieldState[] states = new FieldState[] {
                computeContactFieldState(),
                FieldState.required(regulation == null ? null : regulation.numberOfTables()),
                FieldState.required(regulation == null ? null : regulation.ballBrandAndType()),
                FieldState.required(regulation == null ? null : regulation.registrationOpenTime()),
                FieldState.required(regulation == null ? null : regulation.registrationDeadline()),
                FieldState.required(regulation == null ? null : regulation.gymOpenTime())
        };

        int validCount = 0;

        for (FieldState state : states) {
            if (state.kind == FieldKind.VALID) {
                validCount++;
            }
        }

        if (validCount == states.length) {
            return BlockState.COMPLETE;
        }
        return BlockState.PARTIAL;
    }

    /**
     * Vérifie qu'une chaîne contient bien une information.
     */
    private boolean hasText(String value) {
        return !OrganizerViewUtils.safe(value).isEmpty();
    }

    /**
     * Nature interne d'un état de champ.
     */
    private enum FieldKind {
        MISSING,
        PARTIAL,
        OPTIONAL,
        PENDING_FFTT,
        VALID
    }

    /**
     * Décrit l'état visuel et fonctionnel d'un champ.
     */
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

        /**
         * Champ obligatoire :
         * - présent -> valide
         * - absent -> manquant
         */
        private static FieldState required(Object value) {
            if (isPresent(value)) {
                return valid("Information complète.");
            }
            return missing("Information obligatoire manquante.");
        }

        /**
         * Champ optionnel :
         * - présent -> valide
         * - absent -> optionnel
         */
        private static FieldState optionalValue(Object value) {
            if (isPresent(value)) {
                return valid("Information complète.");
            }
            return optionalState("Information optionnelle.");
        }

        /**
         * Etat manquant.
         */
        private static FieldState missing(String tooltip) {
            return new FieldState(FieldKind.MISSING, "✕", "#D32F2F", tooltip);
        }

        /**
         * Etat partiel.
         */
        private static FieldState partial(String tooltip) {
            return new FieldState(FieldKind.PARTIAL, "≈≈", "#F57C00", tooltip);
        }

        /**
         * Etat optionnel.
         */
        private static FieldState optionalState(String tooltip) {
            return new FieldState(FieldKind.OPTIONAL, "~", "#B26A00", tooltip);
        }

        /**
         * Etat spécial pour l'attente FFTT.
         */
        private static FieldState pendingFftt(String tooltip) {
            return new FieldState(FieldKind.PENDING_FFTT, "…", "#7B1FA2", tooltip);
        }

        /**
         * Etat valide.
         */
        private static FieldState valid(String tooltip) {
            return new FieldState(FieldKind.VALID, "✓", "#2E7D32", tooltip);
        }

        /**
         * Vérifie qu'une valeur est présente.
         */
        private static boolean isPresent(Object value) {
            if (value == null) {
                return false;
            }
            if (value instanceof String s) {
                return !OrganizerViewUtils.safe(s).isEmpty();
            }
            return true;
        }
    }

    /**
     * Etat global d'un bloc.
     */
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