package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.pages;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.AppRouter;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.OrganizerDto;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.TournamentRow;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OrganizerDashboardContent extends VBox {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AppRouter nav;
    private final OrganizerDto organizer;

    public OrganizerDashboardContent(AppRouter nav, OrganizerDto organizer) {
        this.nav = Objects.requireNonNull(nav, "nav must not be null");
        this.organizer = Objects.requireNonNull(organizer, "organizer must not be null");
        build();
    }

    private void build() {
        AppTheme.applyPage(this);

        VBox root = new VBox(AppTheme.SPACE_LG);
        root.setPadding(new Insets(20));
        root.setMaxWidth(Double.MAX_VALUE);

        root.getChildren().add(buildWelcomeSection());
        root.getChildren().add(buildClubTournamentsSection());
        root.getChildren().add(buildHelpSection());

        setChildrenAsScroll(root);
    }

    private VBox buildWelcomeSection() {
        VBox content = new VBox(AppTheme.SPACE_SM);

        Label title = new Label(buildWelcomeTitle());
        AppTheme.applyTitle(title);

        Label intro = new Label(
                "Vous vous trouvez actuellement sur la page d'accueil de PingManager, "
                        + "le logiciel de gestion de tournois de tennis de table.");
        AppTheme.applyBody(intro);
        intro.setWrapText(true);

        Label details = new Label(
                "Depuis cet espace, vous pouvez créer un tournoi en brouillon, "
                        + "retrouver les tournois du club, puis compléter progressivement "
                        + "les parties général, règlement et tableaux.");
        AppTheme.applyBody(details);
        details.setWrapText(true);

        content.getChildren().addAll(title, intro, details);

        VBox card = AppTheme.card(content);
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private String buildWelcomeTitle() {
        String clubName = organizer.getClubName();
        if (clubName == null || clubName.isBlank()) {
            return "Bienvenue sur l'espace organisateur";
        }
        return "Bienvenue sur le compte du club : " + clubName;
    }

    private VBox buildClubTournamentsSection() {
        List<TournamentRow> draft = loadDraftTournaments();
        List<TournamentRow> active = loadActiveTournaments();

        List<TournamentRow> allTournaments = new ArrayList<>();
        allTournaments.addAll(draft);
        allTournaments.addAll(active);

        VBox content = new VBox(AppTheme.SPACE_MD);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Tournois du club");
        AppTheme.applyCardTitle(title);

        Label badge = new Label(String.valueOf(allTournaments.size()));
        badge.setStyle(AppTheme.badgeStyle(AppTheme.COLOR_PRIMARY));

        header.getChildren().addAll(title, badge);
        content.getChildren().add(header);

        if (allTournaments.isEmpty()) {
            content.getChildren().add(buildEmptyTournamentState());
        } else {
            content.getChildren().add(buildTournamentCardList(allTournaments));
        }

        content.getChildren().add(buildCreateTournamentRow());

        VBox card = AppTheme.card(content);
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private VBox buildEmptyTournamentState() {
        VBox box = new VBox(AppTheme.SPACE_SM);
        box.setPadding(new Insets(14));
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle(
                "-fx-background-color:" + AppTheme.COLOR_SURFACE + ";" +
                        "-fx-background-radius:" + AppTheme.RADIUS + ";" +
                        "-fx-border-color:" + AppTheme.COLOR_BORDER + ";" +
                        "-fx-border-radius:" + AppTheme.RADIUS + ";");

        Label title = new Label("Aucun tournoi n'a encore été créé.");
        AppTheme.applyBody(title);

        Label subtitle = new Label(
                "Commencez par créer votre premier tournoi pour démarrer l'organisation du club.");
        AppTheme.applyBody(subtitle);
        subtitle.setWrapText(true);

        box.getChildren().addAll(title, subtitle);
        return box;
    }

    private VBox buildTournamentCardList(List<TournamentRow> tournaments) {
        VBox box = new VBox(12);

        for (TournamentRow tournament : tournaments) {
            box.getChildren().add(buildTournamentCard(tournament));
        }

        return box;
    }

    private VBox buildTournamentCard(TournamentRow tournament) {
        VBox root = new VBox(12);
        root.setPadding(new Insets(16));
        root.setStyle(
                "-fx-background-color:" + AppTheme.COLOR_SURFACE + ";" +
                        "-fx-background-radius:" + AppTheme.RADIUS + ";" +
                        "-fx-border-color:" + AppTheme.COLOR_BORDER + ";" +
                        "-fx-border-radius:" + AppTheme.RADIUS + ";");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label title = new Label(safe(tournament.name()));
        AppTheme.applyCardTitle(title);

        Label subTitle = new Label(buildTournamentSubtitle(tournament));
        AppTheme.applyBody(subTitle);
        subTitle.setWrapText(true);

        titleBox.getChildren().addAll(title, subTitle);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Label statusBadge = new Label(safe(tournament.status()));
        statusBadge.setStyle(AppTheme.badgeStyle(resolveStatusColor(tournament.status())));

        header.getChildren().addAll(titleBox, statusBadge);

        HBox sections = new HBox(10);
        sections.setAlignment(Pos.CENTER_LEFT);

        Button generalBtn = new Button("Général");
        AppTheme.styleSecondary(generalBtn);
        generalBtn.setOnAction(e -> nav.showInfo(
                "Général",
                "La section Général du tournoi \"" + safe(tournament.name())
                        + "\" sera reliée dans la prochaine étape."));

        Button regulationBtn = new Button("Règlement");
        AppTheme.styleSecondary(regulationBtn);
        regulationBtn.setOnAction(e -> nav.showInfo(
                "Règlement",
                "La section Règlement du tournoi \"" + safe(tournament.name())
                        + "\" sera reliée dans la prochaine étape."));

        Button tableauxBtn = new Button("Tableaux");
        AppTheme.styleSecondary(tableauxBtn);
        tableauxBtn.setOnAction(e -> nav.showInfo(
                "Tableaux",
                "La section Tableaux du tournoi \"" + safe(tournament.name())
                        + "\" sera reliée dans la prochaine étape."));

        sections.getChildren().addAll(generalBtn, regulationBtn, tableauxBtn);

        root.getChildren().addAll(header, sections);
        return root;
    }

    private String buildTournamentSubtitle(TournamentRow tournament) {
        String city = safe(tournament.city());
        String level = prettyEnumName(tournament.level());
        String phase = prettyPhase(tournament.phase());
        String dates = formatDateRange(tournament.startDate(), tournament.endDate());

        return city + " • " + level + " • " + phase + " • " + dates;
    }

    private String formatDateRange(String startDateRaw, String endDateRaw) {
        try {
            LocalDate start = LocalDate.parse(startDateRaw);
            LocalDate end = LocalDate.parse(endDateRaw);

            if (start.equals(end)) {
                return DATE_FORMAT.format(start);
            }
            return DATE_FORMAT.format(start) + " - " + DATE_FORMAT.format(end);
        } catch (Exception e) {
            return safe(startDateRaw) + " - " + safe(endDateRaw);
        }
    }

    private HBox buildCreateTournamentRow() {
        Button button = new Button("Créer un tournoi");
        AppTheme.stylePrimary(button);
        button.setOnAction(e -> nav.showCreateTournamentDialog());
        button.setMaxWidth(260);

        HBox row = new HBox(button);
        row.setAlignment(Pos.CENTER);
        row.setPadding(new Insets(8, 0, 0, 0));
        return row;
    }

    private List<TournamentRow> loadDraftTournaments() {
        String clubId = nav.clubRepo()
                .findByOrganizerId(organizer.getId())
                .orElseThrow(() -> new IllegalStateException("Club introuvable pour cet organisateur"))
                .id();

        return nav.tournamentService().findDraftForClub(clubId);
    }

    private List<TournamentRow> loadActiveTournaments() {
        String clubId = nav.clubRepo()
                .findByOrganizerId(organizer.getId())
                .orElseThrow(() -> new IllegalStateException("Club introuvable pour cet organisateur"))
                .id();

        return nav.tournamentService().findActiveForClub(clubId);
    }

    private VBox buildHelpSection() {
        VBox content = new VBox(AppTheme.SPACE_MD);

        Label title = new Label("Besoin d'aide ?");
        AppTheme.applyCardTitle(title);

        Label text = new Label(
                "En cas de question, de retour ou de problème, utilisez le bouton ci-dessous pour accéder à l'aide.");
        AppTheme.applyBody(text);
        text.setWrapText(true);

        Button helpButton = new Button("Obtenir de l'aide");
        AppTheme.styleSecondary(helpButton);
        helpButton.setOnAction(e -> onHelp());

        HBox buttonRow = new HBox(helpButton);
        buttonRow.setAlignment(Pos.CENTER_LEFT);

        content.getChildren().addAll(title, text, buttonRow);

        VBox card = AppTheme.card(content);
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private void onHelp() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Aide");
        alert.setHeaderText("Section d'aide");
        alert.setContentText("La section d'aide sera bientôt disponible.");
        alert.showAndWait();
    }

    private void setChildrenAsScroll(VBox content) {
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setPadding(Insets.EMPTY);

        getChildren().setAll(scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String prettyEnumName(String raw) {
        String value = safe(raw);
        if (value.isBlank()) {
            return "-";
        }

        return switch (value) {
            case "DEPARTEMENTAL" -> "Départemental";
            case "REGIONAL" -> "Régional";
            case "NATIONAL_B" -> "National B";
            case "NATIONAL_A" -> "National A";
            case "INTERNATIONAL" -> "International";
            case "DRAFT" -> "Brouillon";
            case "OPEN" -> "Ouvert";
            case "RUNNING" -> "En cours";
            case "FINISHED" -> "Terminé";
            case "CANCELLED" -> "Annulé";
            default -> value.replace('_', ' ');
        };
    }

    private String prettyPhase(String raw) {
        String value = safe(raw);
        return switch (value) {
            case "PHASE_1" -> "Phase 1";
            case "PHASE_2" -> "Phase 2";
            default -> value;
        };
    }

    private String resolveStatusColor(String status) {
        String value = safe(status);

        return switch (value) {
            case "DRAFT" -> AppTheme.COLOR_PRIMARY;
            case "OPEN" -> "#2E7D32";
            case "RUNNING" -> "#EF6C00";
            case "FINISHED" -> "#455A64";
            case "CANCELLED" -> "#C62828";
            default -> AppTheme.COLOR_PRIMARY;
        };
    }
}