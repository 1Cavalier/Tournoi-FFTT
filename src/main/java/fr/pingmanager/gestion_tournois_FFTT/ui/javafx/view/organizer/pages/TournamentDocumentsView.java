package fr.pingmanager.gestion_tournois_FFTT.ui.javafx.view.organizer.pages;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.Objects;

import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.app.AppRouter;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TournamentDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;

/**
 * Vue inline Documents — affiche les documents générables du tournoi.
 * Section à implémenter : affiches, règlement PDF, convocations.
 */
public class TournamentDocumentsView extends VBox {

    private final AppRouter nav;
    private final TournamentDto tournament;

    public TournamentDocumentsView(AppRouter nav, TournamentDto tournament) {
        this.nav = Objects.requireNonNull(nav);
        this.tournament = Objects.requireNonNull(tournament);
        build();
    }

    private void build() {
        AppTheme.applyPage(this);
        setPadding(new Insets(28));
        setMaxWidth(Double.MAX_VALUE);
        setSpacing(AppTheme.SPACE_LG);

        Label title = new Label("Documents");
        AppTheme.applyTitle(title);

        Label subtitle = new Label(
                "Générez et téléchargez les documents officiels du tournoi : "
                        + "affiches, règlement PDF, convocations, feuilles de match.");
        AppTheme.applyBody(subtitle);
        subtitle.setWrapText(true);

        // ---- Carte Affiches ----
        VBox affichesCard = buildComingSoonCard(
                "🖼  Affiches",
                "Générez automatiquement une affiche de présentation du tournoi "
                        + "à partir des informations déjà saisies (nom, dates, lieu, tableaux…).");

        // ---- Carte Règlement PDF ----
        VBox reglementCard = buildComingSoonCard(
                "📄  Règlement complet (PDF)",
                "Compilez toutes les informations réglementaires en un document PDF "
                        + "prêt à envoyer à la FFTT pour homologation.");

        // ---- Carte Convocations ----
        VBox convocCard = buildComingSoonCard(
                "📧  Convocations",
                "Envoyez les convocations aux joueurs inscrits et aux officiels désignés.");

        getChildren().addAll(title, subtitle, affichesCard, reglementCard, convocCard);
    }

    private VBox buildComingSoonCard(String cardTitle, String description) {
        Label titleLabel = new Label(cardTitle);
        AppTheme.applyCardTitle(titleLabel);

        Label descLabel = new Label(description);
        AppTheme.applyBody(descLabel);
        descLabel.setWrapText(true);

        Label comingSoon = new Label("À venir dans une prochaine version");
        comingSoon.setStyle(
                "-fx-background-color: #EFF6FF;"
                        + "-fx-text-fill: #1E40AF;"
                        + "-fx-font-weight: 600;"
                        + "-fx-padding: 4 12 4 12;"
                        + "-fx-background-radius: 20;");

        Button btn = new Button("Non disponible");
        AppTheme.styleSecondary(btn);
        btn.setDisable(true);

        VBox content = new VBox(AppTheme.SPACE_SM,
                titleLabel, descLabel, comingSoon, btn);
        VBox card = AppTheme.card(content);
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }
}