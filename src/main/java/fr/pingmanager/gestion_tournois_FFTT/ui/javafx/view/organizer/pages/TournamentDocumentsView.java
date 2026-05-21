package fr.pingmanager.gestion_tournois_FFTT.ui.javafx.view.organizer.pages;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.app.AppRouter;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.ClubDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TableauDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TournamentDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TournamentRegulationDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.service.RegulationPdfConfig;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.service.RegulationPdfService;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.service.RegulationPdfService.RegulationPdfException;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.view.organizer.dialogs.RegulationPdfConfigDialog;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.view.organizer.layout.TournamentSection;

/**
 * Vue inline « Autres informations » — regroupe tous les documents et services
 * associés au tournoi : règlement PDF, affiches, convocations et paiement en ligne.
 *
 * <p>La carte Paiement en ligne n'est active que si au moins un tableau
 * comporte un tarif prépayé ({@code prepaidFee > 0}).
 */
public class TournamentDocumentsView extends VBox {

    private static final Logger LOG = Logger.getLogger(TournamentDocumentsView.class.getName());

    private final AppRouter               nav;
    private final TournamentDto           tournament;
    private final TournamentRegulationDto regulation;
    private final RegulationPdfService    pdfService = new RegulationPdfService();

    // -------------------------------------------------------------------------
    // CONSTRUCTEUR
    // -------------------------------------------------------------------------

    public TournamentDocumentsView(AppRouter nav,
                                   TournamentDto tournament,
                                   TournamentRegulationDto regulation) {
        this.nav        = Objects.requireNonNull(nav);
        this.tournament = Objects.requireNonNull(tournament);
        this.regulation = regulation;
        build();
    }

    // -------------------------------------------------------------------------
    // CONSTRUCTION
    // -------------------------------------------------------------------------

    private void build() {
        AppTheme.applyPage(this);

        VBox root = new VBox(AppTheme.SPACE_LG);
        root.setPadding(new Insets(28));
        root.setMaxWidth(Double.MAX_VALUE);

        root.getChildren().add(buildHeader());
        root.getChildren().add(buildReglementCard());
        root.getChildren().add(buildAfficheCard());
        root.getChildren().add(buildConvocationsCard());
        root.getChildren().add(buildPaiementCard());

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: transparent;");

        getChildren().setAll(scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
    }

    // ---- En-tête ----

    private VBox buildHeader() {
        Label title = new Label("Autres informations");
        AppTheme.applyTitle(title);

        Label subtitle = new Label(
                "Générez les documents officiels du tournoi et configurez les services "
                + "associés : règlement PDF, affiches, convocations et paiement en ligne.");
        AppTheme.applyBody(subtitle);
        subtitle.setWrapText(true);

        VBox header = new VBox(AppTheme.SPACE_SM, title, subtitle);
        header.setMaxWidth(Double.MAX_VALUE);
        return header;
    }

    // =========================================================================
    // CARTES
    // =========================================================================

    // ---- Règlement PDF ----

    private VBox buildReglementCard() {
        Label desc = new Label(
                "Compilez toutes les informations réglementaires saisies "
                + "(lieu, tables, balles, officiels, tableaux, dotations…) en un document PDF "
                + "officiel prêt à soumettre à la F.F.T.T. pour homologation. "
                + "Choisissez le format d'affichage des dotations et ajoutez des articles "
                + "complémentaires (restauration, stationnement, sponsors…).");
        AppTheme.applyBody(desc);
        desc.setWrapText(true);

        Button btnGenerate = new Button("Configurer et générer le règlement PDF…");
        AppTheme.stylePrimary(btnGenerate);
        btnGenerate.setOnAction(e -> onGenerateReglement());

        return buildCard("📄  Règlement officiel (PDF)", desc, btnGenerate);
    }

    // ---- Affiche ----

    private VBox buildAfficheCard() {
        Label desc = new Label(
                "Générez automatiquement une affiche de présentation du tournoi "
                + "à partir des informations déjà saisies (nom, dates, lieu, tableaux, dotations…). "
                + "Format A4 ou A3, prêt à imprimer ou à publier en ligne.");
        AppTheme.applyBody(desc);
        desc.setWrapText(true);

        Button btnGenerate = new Button("Générer l'affiche");
        AppTheme.stylePrimary(btnGenerate);
        btnGenerate.setDisable(true); // TODO : implémenter la génération affiche

        return buildCard("🖼  Affiche du tournoi", desc, comingSoonBadge(), btnGenerate);
    }

    // ---- Convocations ----

    private VBox buildConvocationsCard() {
        Label desc = new Label(
                "Envoyez les convocations officielles aux joueurs inscrits et aux officiels désignés. "
                + "Chaque convocation reprend les informations du tournoi, du tableau concerné "
                + "et les détails pratiques (lieu, horaire, pointage).");
        AppTheme.applyBody(desc);
        desc.setWrapText(true);

        Button btnSend = new Button("Envoyer les convocations");
        AppTheme.stylePrimary(btnSend);
        btnSend.setDisable(true); // TODO : implémenter l'envoi de convocations

        Button btnPreview = new Button("Aperçu");
        AppTheme.styleSecondary(btnPreview);
        btnPreview.setDisable(true);

        HBox actions = new HBox(AppTheme.SPACE_SM, btnSend, btnPreview);
        actions.setAlignment(Pos.CENTER_LEFT);

        return buildCard("📧  Convocations", desc, comingSoonBadge(), actions);
    }

    // ---- Paiement en ligne ----

    private VBox buildPaiementCard() {
        List<TableauDto> tableaux = loadTableaux();
        boolean hasPrepaidTableau = tableaux.stream()
                .anyMatch(t -> t.prepaidFee() != null && t.prepaidFee() > 0);

        if (!hasPrepaidTableau) {
            Label desc = new Label(
                    "Le paiement en ligne n'est pas activé pour ce tournoi. "
                    + "Pour l'activer, définissez un tarif de préinscription (tarif en ligne) "
                    + "sur au moins un tableau dans la section « Tableaux ».");
            AppTheme.applyBody(desc);
            desc.setWrapText(true);

            Button btnGoTableaux = new Button("Configurer les tableaux");
            AppTheme.styleSecondary(btnGoTableaux);
            btnGoTableaux.setOnAction(e ->
                    nav.showTournamentSection(tournament, TournamentSection.TABLEAUX));

            Label infoLabel = new Label("ℹ  Aucun tarif de préinscription configuré sur les tableaux.");
            infoLabel.setStyle(
                    "-fx-background-color: #FFF8E1;"
                    + "-fx-text-fill: #795548;"
                    + "-fx-font-size: 12px;"
                    + "-fx-font-weight: 600;"
                    + "-fx-padding: 6 12 6 12;"
                    + "-fx-background-radius: 6;");
            infoLabel.setWrapText(true);

            return buildCard("💳  Paiement en ligne", desc, infoLabel, btnGoTableaux);
        }

        Label desc = new Label(
                "Configurez le lien ou le moyen de paiement en ligne utilisé lors des inscriptions prépayées. "
                + "Les joueurs pourront régler directement lors de leur inscription "
                + "via HelloAsso, SumUp, ou tout autre service compatible.");
        AppTheme.applyBody(desc);
        desc.setWrapText(true);

        VBox tableauxResume = new VBox(4);
        tableaux.stream()
                .filter(t -> t.prepaidFee() != null && t.prepaidFee() > 0)
                .forEach(t -> {
                    Label row = new Label("• " + t.code() + " — " + t.prepaidFee() + " €");
                    row.setStyle(
                            "-fx-font-family: '" + AppTheme.FONT_BODY + "';"
                            + "-fx-font-size: 13px;"
                            + "-fx-text-fill: " + AppTheme.COLOR_TEXT + ";");
                    tableauxResume.getChildren().add(row);
                });

        Label tableauxLabel = new Label("Tableaux avec tarif en ligne :");
        tableauxLabel.setStyle(
                "-fx-font-family: '" + AppTheme.FONT_BODY + "';"
                + "-fx-font-size: 13px;"
                + "-fx-font-weight: 700;"
                + "-fx-text-fill: " + AppTheme.COLOR_TEXT + ";");

        Button btnConfig = new Button("Configurer le moyen de paiement");
        AppTheme.stylePrimary(btnConfig);
        btnConfig.setDisable(true); // TODO : implémenter la configuration du lien de paiement

        Label activeBadge = new Label("✓  Paiement en ligne activé");
        activeBadge.setStyle(
                "-fx-background-color: #E8F5E9;"
                + "-fx-text-fill: #2E7D32;"
                + "-fx-font-size: 12px;"
                + "-fx-font-weight: 600;"
                + "-fx-padding: 4 12 4 12;"
                + "-fx-background-radius: 20;");

        return buildCard("💳  Paiement en ligne",
                desc, activeBadge, tableauxLabel, tableauxResume, comingSoonBadge(), btnConfig);
    }

    // =========================================================================
    // ACTION — GÉNÉRATION DU RÈGLEMENT
    // =========================================================================

    private void onGenerateReglement() {
        Stage owner = (Stage) getScene().getWindow();

        // Chemin par défaut : bureau / nom du tournoi
        String defaultName = tournament.name() != null
                ? tournament.name().replaceAll("[^a-zA-Z0-9_\\-]", "_") + "_reglement.pdf"
                : "reglement.pdf";
        String defaultPath = System.getProperty("user.home")
                + File.separator + defaultName;

        RegulationPdfConfigDialog dialog = new RegulationPdfConfigDialog(
                owner, tournament, regulation, loadTableaux(), loadClub(), defaultPath);
        dialog.showAndWait();

        if (!dialog.isConfirmed()) return;

        RegulationPdfConfig config = dialog.getConfig();
        List<TableauDto> tableaux  = loadTableaux();
        ClubDto club               = loadClub();

        try {
            pdfService.generate(tournament, regulation, tableaux, club, config, config.outputPath());
            nav.showInfo("Règlement généré",
                    "Le règlement PDF a été généré avec succès.\n\n"
                    + "Fichier : " + config.outputPath());
        } catch (RegulationPdfException ex) {
            LOG.log(Level.SEVERE, "Échec génération règlement PDF", ex);
            nav.showError("Erreur de génération",
                    "La génération du règlement PDF a échoué :\n" + ex.getMessage());
        }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private VBox buildCard(String cardTitle, Node... content) {
        Label titleLabel = new Label(cardTitle);
        AppTheme.applyCardTitle(titleLabel);

        VBox inner = new VBox(AppTheme.SPACE_SM);
        inner.getChildren().add(titleLabel);
        inner.getChildren().addAll(content);

        VBox card = AppTheme.card(inner);
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private Label comingSoonBadge() {
        Label badge = new Label("À venir dans une prochaine version");
        badge.setStyle(
                "-fx-background-color: #EFF6FF;"
                + "-fx-text-fill: #1E40AF;"
                + "-fx-font-weight: 600;"
                + "-fx-font-size: 11px;"
                + "-fx-padding: 4 12 4 12;"
                + "-fx-background-radius: 20;");
        return badge;
    }

    private List<TableauDto> loadTableaux() {
        try {
            return nav.tournamentService().findTableauxByTournamentId(tournament.id());
        } catch (Exception e) {
            return List.of();
        }
    }

    private ClubDto loadClub() {
        try {
            return nav.clubRepo().findById(tournament.clubId())
                    .orElseGet(() ->
                            nav.clubRepo().findByOrganizerId(tournament.organizerId())
                               .orElse(emptyClub()));
        } catch (Exception e) {
            return emptyClub();
        }
    }

    private static ClubDto emptyClub() {
        return new ClubDto(null, null, "", null, null, null, null,
                null, null, null, null, null, null, null);
    }
}