package fr.pingmanager.gestion_tournois_FFTT.ui.javafx.view.organizer.dialogs;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.ClubDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TableauDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TournamentDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TournamentRegulationDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.service.PopplerManager;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.service.RegulationPdfConfig;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.service.RegulationPdfConfig.ArticleOption;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.service.RegulationPdfConfig.ArticleTemplate;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.service.RegulationPdfConfig.StandardArticle;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.service.RegulationPdfConfig.DotationsFormat;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.service.RegulationPdfModel;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.service.RegulationPdfRenderer;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.service.RegulationPdfService;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.theme.AppTheme;

/**
 * Dialog de configuration + prévisualisation du règlement PDF.
 *
 * <p>Split view : panneau de configuration à gauche, aperçu PDF paginé à droite.
 * L'aperçu se rafraîchit automatiquement 800 ms après la dernière modification.
 */
public class RegulationPdfConfigDialog extends Stage {

    private static final Logger LOG = Logger.getLogger(RegulationPdfConfigDialog.class.getName());

    // ── Données ───────────────────────────────────────────────────────────
    private final TournamentDto           tournament;
    private final TournamentRegulationDto regulation;
    private final List<TableauDto>        tableaux;
    private final ClubDto                 club;
    private final RegulationPdfService    service = new RegulationPdfService();
    private final RegulationPdfConfig     config  = new RegulationPdfConfig();
    private boolean confirmed = false;

    // ── Scheduler prévisualisation ────────────────────────────────────────
    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "pdf-preview");
                t.setDaemon(true);
                return t;
            });
    private ScheduledFuture<?> pendingRefresh;
    private final AtomicBoolean previewBusy = new AtomicBoolean(false);

    // ── Prévisualisation ──────────────────────────────────────────────────
    private Path previewPdf;
    private final ImageView previewImage  = new ImageView();
    private final Label     previewStatus = new Label("Configurer les options pour afficher l'aperçu");
    private final Button    btnPrevPage   = new Button("◀");
    private final Button    btnNextPage   = new Button("▶");
    private final Label     pageLabel     = new Label("—");
    private int currentPage = 1;
    private int totalPages  = 0;
    private final java.util.List<Path> pageImages = new java.util.ArrayList<>();

    // ── Widgets config ────────────────────────────────────────────────────
    // Couleur accent
    private final ColorPicker colorPicker = new ColorPicker(Color.web("#1565C0"));
    // Ordre articles standards
    private final ObservableList<StandardArticle> stdItems =
            FXCollections.observableArrayList(StandardArticle.values());
    private final ListView<StandardArticle> stdList = new ListView<>(stdItems);
    // Règlement custom
    private final TextField  customPdfField  = new TextField();
    // Format dotations
    private final ToggleGroup dotationsGroup  = new ToggleGroup();
    private final ToggleButton btnRecap       = new ToggleButton(DotationsFormat.TABLE_RECAP.label());
    private final ToggleButton btnList        = new ToggleButton(DotationsFormat.LIST_PER_TABLEAU.label());
    // Options
    private final CheckBox chkReentry         = new CheckBox("Réinscription après élimination autorisée");
    private final CheckBox chkPresence        = new CheckBox("Récompense conditionnée à la présence en fin de tournoi");
    private final CheckBox chkRefundPlatform  = new CheckBox("Préciser la plateforme de remboursement");
    private final TextField refundDateField   = new TextField();
    private final TextField refundPlatField   = new TextField();
    // Articles optionnels
    private final ObservableList<ArticleOption> articleItems = FXCollections.observableArrayList();
    private final ListView<ArticleOption>        articleList  = new ListView<>(articleItems);
    private final ComboBox<ArticleTemplate>      templateBox  = new ComboBox<>();
    private final TextField titleField = new TextField();
    private final TextArea  bodyArea   = new TextArea();
    private ArticleOption editingArticle;
    // Sortie
    private final TextField outputField = new TextField();

    // ── Constructeur ─────────────────────────────────────────────────────

    public RegulationPdfConfigDialog(Stage owner,
                                      TournamentDto tournament,
                                      TournamentRegulationDto regulation,
                                      List<TableauDto> tableaux,
                                      ClubDto club,
                                      String defaultOutputPath) {
        this.tournament = Objects.requireNonNull(tournament);
        this.regulation = regulation;
        this.tableaux   = tableaux != null ? tableaux : List.of();
        this.club       = club;

        initOwner(owner);
        initModality(Modality.WINDOW_MODAL);
        setTitle("Règlement PDF — Configuration et aperçu");
        setMinWidth(1100);
        setMinHeight(700);

        outputField.setText(defaultOutputPath != null ? defaultOutputPath : "");

        build();

        // Prévisualisation initiale au démarrage
        scheduleRefresh();

        // Nettoyage à la fermeture
        setOnHidden(e -> {
            scheduler.shutdownNow();
            cleanTempFiles();
        });
    }

    // ── Construction de l'interface ───────────────────────────────────────

    private void build() {
        // ---- Panneau gauche : configuration ----
        VBox leftPanel = new VBox(AppTheme.SPACE_MD);
        leftPanel.setPadding(new Insets(20));
        leftPanel.setPrefWidth(460);
        leftPanel.setMinWidth(380);
        leftPanel.setStyle("-fx-background-color: " + AppTheme.COLOR_BG + ";");

        Label header = new Label("⚙  Configuration du règlement");
        AppTheme.applyTitle(header);
        header.setStyle(header.getStyle() + "-fx-font-size: 15px;");

        ScrollPane leftScroll = new ScrollPane(leftPanel);
        leftScroll.setFitToWidth(true);
        leftScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        leftScroll.setStyle("-fx-background-color: transparent;");

        leftPanel.getChildren().addAll(
                header,
                buildCustomPdfSection(),
                buildDotationsSection(),
                buildAccentColorSection(),
                buildArticleOrderSection(),
                buildOptionsSection(),
                buildArticlesSection(),
                buildOutputSection(),
                buildActions()
        );

        // ---- Panneau droit : aperçu ----
        VBox rightPanel = buildPreviewPanel();

        // ---- Split ----
        HBox root = new HBox(leftScroll, buildSeparator(), rightPanel);
        HBox.setHgrow(leftScroll, Priority.NEVER);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        Scene scene = new Scene(root, 1200, 740);
        setScene(scene);
    }

    // ── Section : règlement custom ────────────────────────────────────────

    private VBox buildCustomPdfSection() {
        Label title = new Label("Règlement existant (optionnel)");
        AppTheme.applyCardTitle(title);

        Label hint = new Label(
                "Si vous avez déjà un règlement PDF finalisé, glissez-le ici. "
                + "Il sera utilisé tel quel sans génération automatique.");
        AppTheme.applyBody(hint);
        hint.setWrapText(true);

        customPdfField.setPromptText("Aucun règlement fourni — génération automatique");
        customPdfField.setEditable(false);
        customPdfField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(customPdfField, Priority.ALWAYS);

        Button btnBrowse = new Button("Parcourir…");
        AppTheme.styleSecondary(btnBrowse);
        btnBrowse.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Sélectionner un règlement PDF");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
            File f = fc.showOpenDialog(this);
            if (f != null) {
                customPdfField.setText(f.getAbsolutePath());
                config.setCustomPdfPath(f.getAbsolutePath());
                scheduleRefresh();
            }
        });

        Button btnClear = new Button("✕");
        AppTheme.styleSecondary(btnClear);
        btnClear.setOnAction(e -> {
            customPdfField.clear();
            config.setCustomPdfPath(null);
            scheduleRefresh();
        });

        // Drag & drop
        customPdfField.getParent(); // sera configuré après création
        VBox card = AppTheme.card(new VBox(AppTheme.SPACE_SM, title, hint,
                new HBox(AppTheme.SPACE_SM, customPdfField, btnBrowse, btnClear)));

        // Drag & drop sur la carte
        card.setOnDragOver(e -> {
            if (e.getDragboard().hasFiles()) {
                e.acceptTransferModes(javafx.scene.input.TransferMode.COPY);
            }
            e.consume();
        });
        card.setOnDragDropped(e -> {
            javafx.scene.input.Dragboard db = e.getDragboard();
            if (db.hasFiles()) {
                File f = db.getFiles().stream()
                        .filter(x -> x.getName().toLowerCase().endsWith(".pdf"))
                        .findFirst().orElse(null);
                if (f != null) {
                    customPdfField.setText(f.getAbsolutePath());
                    config.setCustomPdfPath(f.getAbsolutePath());
                    scheduleRefresh();
                }
            }
            e.consume();
        });

        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    // ── Section : format dotations ────────────────────────────────────────

    private VBox buildDotationsSection() {
        Label title = new Label("Format des dotations");
        AppTheme.applyCardTitle(title);

        btnRecap.setToggleGroup(dotationsGroup);
        btnList.setToggleGroup(dotationsGroup);
        btnRecap.setSelected(true);
        styleToggle(btnRecap);
        styleToggle(btnList);

        dotationsGroup.selectedToggleProperty().addListener((obs, old, nw) -> {
            if (nw == null) { dotationsGroup.selectToggle(old); return; }
            config.setDotationsFormat(nw == btnRecap ? DotationsFormat.TABLE_RECAP : DotationsFormat.LIST_PER_TABLEAU);
            scheduleRefresh();
        });

        HBox row = new HBox(AppTheme.SPACE_SM, btnRecap, btnList);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox card = AppTheme.card(new VBox(AppTheme.SPACE_SM, title, row));
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    // ── Section : couleur des titres ─────────────────────────────────────

    private VBox buildAccentColorSection() {
        Label title = new Label("Couleur des titres");
        AppTheme.applyCardTitle(title);

        Label hint = new Label(
                "Couleur utilisée pour les titres d'articles et les en-têtes de tableaux.");
        AppTheme.applyBody(hint);
        hint.setWrapText(true);

        colorPicker.setPrefWidth(140);
        colorPicker.valueProperty().addListener((obs, old, nw) -> {
            if (nw != null) {
                String hex = String.format("#%02X%02X%02X",
                        (int)(nw.getRed()   * 255),
                        (int)(nw.getGreen() * 255),
                        (int)(nw.getBlue()  * 255));
                config.setAccentColor(hex);
                scheduleRefresh();
            }
        });

        // Palettes de couleurs prédéfinies (boutons carrés)
        HBox palette = new HBox(6);
        palette.setAlignment(Pos.CENTER_LEFT);
        String[][] presets = {
            {"#1565C0", "Bleu FFTT"},
            {"#1B5E20", "Vert"},
            {"#B71C1C", "Rouge"},
            {"#4A148C", "Violet"},
            {"#E65100", "Orange"},
            {"#212121", "Noir"},
            {"#37474F", "Gris ardoise"},
        };
        for (String[] preset : presets) {
            Button swatch = new Button();
            swatch.setTooltip(new Tooltip(preset[1]));
            swatch.setPrefSize(24, 24);
            swatch.setMinSize(24, 24);
            swatch.setStyle("-fx-background-color:" + preset[0] + ";"
                    + "-fx-background-radius:4;-fx-border-radius:4;"
                    + "-fx-border-color:rgba(0,0,0,0.18);-fx-border-width:1;");
            String hex = preset[0];
            swatch.setOnAction(e -> {
                colorPicker.setValue(Color.web(hex));
                config.setAccentColor(hex);
                scheduleRefresh();
            });
            palette.getChildren().add(swatch);
        }

        VBox card = AppTheme.card(new VBox(AppTheme.SPACE_SM, title, hint, palette, colorPicker));
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    // ── Section : ordre des articles standards ────────────────────────────

    private VBox buildArticleOrderSection() {
        Label title = new Label("Ordre des articles");
        AppTheme.applyCardTitle(title);

        Label hint = new Label(
                "Réordonnez les articles standards du règlement en les déplaçant avec les flèches.");
        AppTheme.applyBody(hint);
        hint.setWrapText(true);

        stdList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(StandardArticle item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label lbl = new Label("⋮  " + item.label());
                lbl.setStyle("-fx-font-size:12px;-fx-text-fill:" + AppTheme.COLOR_TEXT + ";");
                lbl.setPadding(new Insets(3, 6, 3, 6));
                setGraphic(lbl);
                setText(null);
            }
        });
        stdList.setPrefHeight(200);
        stdList.setMaxWidth(Double.MAX_VALUE);

        Button btnUp  = new Button("↑"); AppTheme.styleSecondary(btnUp);
        Button btnDn  = new Button("↓"); AppTheme.styleSecondary(btnDn);

        btnUp.setOnAction(e -> {
            StandardArticle sel = stdList.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            int i = stdItems.indexOf(sel);
            if (i > 0) {
                config.moveStandardUp(sel);
                stdItems.remove(i);
                stdItems.add(i - 1, sel);
                stdList.getSelectionModel().select(i - 1);
                scheduleRefresh();
            }
        });
        btnDn.setOnAction(e -> {
            StandardArticle sel = stdList.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            int i = stdItems.indexOf(sel);
            if (i >= 0 && i < stdItems.size() - 1) {
                config.moveStandardDown(sel);
                stdItems.remove(i);
                stdItems.add(i + 1, sel);
                stdList.getSelectionModel().select(i + 1);
                scheduleRefresh();
            }
        });

        HBox btns = new HBox(AppTheme.SPACE_SM, btnUp, btnDn);
        btns.setAlignment(Pos.CENTER_LEFT);

        VBox card = AppTheme.card(new VBox(AppTheme.SPACE_SM, title, hint, stdList, btns));
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    // ── Section : options ─────────────────────────────────────────────────

    private VBox buildOptionsSection() {
        Label title = new Label("Options supplémentaires");
        AppTheme.applyCardTitle(title);

        styleCheck(chkReentry);
        styleCheck(chkPresence);
        styleCheck(chkRefundPlatform);

        chkReentry.setOnAction(e -> { config.setAllowReentryAfterElimination(chkReentry.isSelected()); scheduleRefresh(); });
        chkPresence.setOnAction(e -> { config.setEnforcePrizePresenceRule(chkPresence.isSelected()); scheduleRefresh(); });
        chkRefundPlatform.setOnAction(e -> {
            config.setShowRefundPlatform(chkRefundPlatform.isSelected());
            refundPlatField.setDisable(!chkRefundPlatform.isSelected());
            scheduleRefresh();
        });

        refundDateField.setPromptText("Date limite de remboursement (ex. 30 mai 2026 à 20h00)");
        refundDateField.setMaxWidth(Double.MAX_VALUE);
        refundDateField.textProperty().addListener((obs, o, n) -> {
            config.setRefundDeadlineLabel(n);
            scheduleRefresh();
        });

        refundPlatField.setPromptText("Plateforme (ex. HelloAsso, par virement…)");
        refundPlatField.setMaxWidth(Double.MAX_VALUE);
        refundPlatField.setDisable(true);
        refundPlatField.textProperty().addListener((obs, o, n) -> {
            config.setRefundPlatformLabel(n);
            scheduleRefresh();
        });

        VBox content = new VBox(AppTheme.SPACE_SM,
                title,
                chkReentry,
                chkPresence,
                new Label("Date limite de remboursement :"),
                refundDateField,
                chkRefundPlatform,
                refundPlatField);

        VBox card = AppTheme.card(content);
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    // ── Section : articles optionnels ─────────────────────────────────────

    private VBox buildArticlesSection() {
        Label title = new Label("Articles optionnels");
        AppTheme.applyCardTitle(title);

        templateBox.setItems(FXCollections.observableArrayList(ArticleTemplate.values()));
        templateBox.setPromptText("Ajouter un article…");
        HBox.setHgrow(templateBox, Priority.ALWAYS);

        Button btnAdd = new Button("Ajouter");
        AppTheme.stylePrimary(btnAdd);
        btnAdd.setOnAction(e -> onAddArticle());

        articleList.setCellFactory(lv -> new ArticleCell());
        articleList.setPrefHeight(130);
        articleList.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, nw) -> onSelectArticle(nw));

        Button btnUp  = new Button("↑"); AppTheme.styleSecondary(btnUp);
        Button btnDn  = new Button("↓"); AppTheme.styleSecondary(btnDn);
        Button btnDel = new Button("Supprimer"); AppTheme.styleSecondary(btnDel);
        btnUp.setOnAction(e -> { onMove(-1); scheduleRefresh(); });
        btnDn.setOnAction(e -> { onMove(1);  scheduleRefresh(); });
        btnDel.setOnAction(e -> { onDelete(); scheduleRefresh(); });

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox listBtns = new HBox(AppTheme.SPACE_SM, btnUp, btnDn, spacer, btnDel);

        // Éditeur
        titleField.setPromptText("Titre de l'article");
        titleField.setMaxWidth(Double.MAX_VALUE);
        titleField.setDisable(true);
        titleField.textProperty().addListener((obs, o, n) -> applyEdit());

        bodyArea.setPromptText("Texte de l'article…");
        bodyArea.setWrapText(true);
        bodyArea.setPrefRowCount(3);
        bodyArea.setMaxWidth(Double.MAX_VALUE);
        bodyArea.setDisable(true);
        bodyArea.textProperty().addListener((obs, o, n) -> applyEdit());

        articleList.getSelectionModel().selectedItemProperty().addListener((obs, old, nw) -> {
            boolean sel = nw != null;
            titleField.setDisable(!sel);
            bodyArea.setDisable(!sel);
        });

        VBox card = AppTheme.card(new VBox(AppTheme.SPACE_SM,
                title,
                new HBox(AppTheme.SPACE_SM, templateBox, btnAdd),
                articleList,
                listBtns,
                new Label("Modifier l'article sélectionné :"),
                titleField,
                bodyArea));
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    // ── Section : sortie ──────────────────────────────────────────────────

    private VBox buildOutputSection() {
        Label title = new Label("Fichier de destination");
        AppTheme.applyCardTitle(title);

        outputField.setPromptText("Chemin du fichier PDF à générer…");
        outputField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(outputField, Priority.ALWAYS);

        Button btnBrowse = new Button("Parcourir…");
        AppTheme.styleSecondary(btnBrowse);
        btnBrowse.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Enregistrer le règlement PDF");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
            File f = fc.showSaveDialog(this);
            if (f != null) {
                String p = f.getAbsolutePath();
                if (!p.endsWith(".pdf")) p += ".pdf";
                outputField.setText(p);
            }
        });

        VBox card = AppTheme.card(new VBox(AppTheme.SPACE_SM, title,
                new HBox(AppTheme.SPACE_SM, outputField, btnBrowse)));
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    // ── Boutons de validation ─────────────────────────────────────────────

    private HBox buildActions() {
        Button btnCancel = new Button("Annuler");
        AppTheme.styleSecondary(btnCancel);
        btnCancel.setOnAction(e -> close());

        Button btnGenerate = new Button("Générer le règlement PDF");
        AppTheme.stylePrimary(btnGenerate);
        btnGenerate.setOnAction(e -> onConfirm());

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        HBox row = new HBox(AppTheme.SPACE_SM, sp, btnCancel, btnGenerate);
        row.setAlignment(Pos.CENTER_RIGHT);
        return row;
    }

    // ── Panneau aperçu ────────────────────────────────────────────────────

    private VBox buildPreviewPanel() {
        Label title = new Label("Aperçu du règlement");
        AppTheme.applyCardTitle(title);
        title.setPadding(new Insets(14, 16, 0, 16));

        previewStatus.setStyle(
                "-fx-font-size: 12px; -fx-text-fill: " + AppTheme.COLOR_TEXT_MUTED + ";");
        previewStatus.setPadding(new Insets(0, 16, 0, 16));

        previewImage.setPreserveRatio(true);
        previewImage.setFitWidth(500);
        previewImage.setSmooth(true);

        ScrollPane imgScroll = new ScrollPane(previewImage);
        imgScroll.setFitToWidth(true);
        imgScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        imgScroll.setStyle("-fx-background-color: #E2E8F0;");
        VBox.setVgrow(imgScroll, Priority.ALWAYS);

        // Navigation pages
        styleNavBtn(btnPrevPage);
        styleNavBtn(btnNextPage);
        btnPrevPage.setOnAction(e -> navigatePage(-1));
        btnNextPage.setOnAction(e -> navigatePage(1));
        btnPrevPage.setDisable(true);
        btnNextPage.setDisable(true);
        pageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + AppTheme.COLOR_TEXT_MUTED + ";");

        HBox nav = new HBox(AppTheme.SPACE_SM, btnPrevPage, pageLabel, btnNextPage);
        nav.setAlignment(Pos.CENTER);
        nav.setPadding(new Insets(8));

        VBox panel = new VBox(AppTheme.SPACE_SM, title, previewStatus, imgScroll, nav);
        panel.setStyle("-fx-background-color: #F1F5F9;");
        return panel;
    }

    private Region buildSeparator() {
        Region sep = new Region();
        sep.setPrefWidth(1);
        sep.setStyle("-fx-background-color: " + AppTheme.COLOR_BORDER + ";");
        return sep;
    }

    // ── Logique prévisualisation ──────────────────────────────────────────

    /** Planifie un rafraîchissement 800 ms après la dernière modification. */
    private void scheduleRefresh() {
        if (pendingRefresh != null && !pendingRefresh.isDone())
            pendingRefresh.cancel(false);
        pendingRefresh = scheduler.schedule(this::doRefreshPreview, 800, TimeUnit.MILLISECONDS);
    }

    // ── Résolution de pdftoppm déléguée à PopplerManager ────────────────────
    // PopplerManager cherche dans le système, puis dans data/poppler/,
    // et télécharge + extrait Poppler Windows si nécessaire.

    private void doRefreshPreview() {
        if (!previewBusy.compareAndSet(false, true)) return;
        try {
            Platform.runLater(() -> previewStatus.setText("Génération de l'aperçu…"));

            // 1. Générer le PDF temporaire
            if (previewPdf == null)
                previewPdf = Files.createTempFile("preview_", ".pdf");

            String outPath = previewPdf.toAbsolutePath().toString();

            if (config.hasCustomPdf()) {
                Files.copy(java.nio.file.Paths.get(config.customPdfPath()), previewPdf,
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } else {
                RegulationPdfModel model = service.buildModel(
                        tournament, regulation, tableaux, club, config);
                RegulationPdfRenderer.render(model, outPath);
            }

            // 2. Supprimer les anciennes images
            pageImages.forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException ignored) {} });
            pageImages.clear();

            // 3. Convertir PDF → PNG via pdftoppm ou fallback Python
            // Résolution via PopplerManager (télécharge si besoin sur Windows)
            Platform.runLater(() -> previewStatus.setText("Préparation de l'aperçu…"));
            String pdftoppm = PopplerManager.resolve();
            String prefix = previewPdf.getParent().resolve("prev_page").toAbsolutePath().toString();

            if (pdftoppm != null) {
                // Chemin natif pdftoppm disponible
                Process proc = new ProcessBuilder(pdftoppm, "-r", "130", "-png", outPath, prefix)
                        .redirectErrorStream(true).start();
                proc.waitFor(30, TimeUnit.SECONDS);
            } else {
                // Fallback : pdf2image via Python (disponible si reportlab est installé)
                // La f-string Python est construite côté Python via format() pour éviter
                // toute confusion avec la syntaxe Java.
                String safePdf    = outPath.replace("\\", "/");
                String safePrefix = prefix.replace("\\", "/");
                String pyScript = "from pdf2image import convert_from_path\n"
                        + "pages = convert_from_path('" + safePdf + "', dpi=130)\n"
                        + "for i, page in enumerate(pages, 1):\n"
                        + "    page.save('" + safePrefix + "-{:03d}.png'.format(i), 'PNG')\n";
                Path pyFile = Files.createTempFile("pdf2img_", ".py");
                try {
                    Files.writeString(pyFile, pyScript);
                    Process proc = new ProcessBuilder("python3", pyFile.toAbsolutePath().toString())
                            .redirectErrorStream(true).start();
                    proc.waitFor(30, TimeUnit.SECONDS);
                } finally {
                    Files.deleteIfExists(pyFile);
                }
            }

            // 4. Lister les pages générées (triées)
            File dir = new File(prefix).getParentFile();
            File[] pngs = dir.listFiles(
                    f -> f.getName().startsWith("prev_page") && f.getName().endsWith(".png"));

            if (pngs != null) {
                java.util.Arrays.sort(pngs, java.util.Comparator.comparing(File::getName));
                for (File png : pngs) pageImages.add(png.toPath());
            }

            totalPages  = pageImages.size();
            currentPage = Math.min(currentPage, Math.max(1, totalPages));

            if (totalPages == 0) {
                Platform.runLater(() -> previewStatus.setText(
                        "Aperçu indisponible — impossible de convertir le PDF en image."));
                return;
            }

            Platform.runLater(() -> {
                showPage(currentPage);
                previewStatus.setText(totalPages + " page" + (totalPages > 1 ? "s" : "") + " — aperçu");
            });
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Aperçu PDF échoué", e);
            Platform.runLater(() -> previewStatus.setText("Aperçu indisponible : " + e.getMessage()));
        } finally {
            previewBusy.set(false);
        }
    }

    private void showPage(int page) {
        if (page < 1 || page > totalPages || pageImages.isEmpty()) return;
        currentPage = page;
        Path imgPath = pageImages.get(page - 1);
        Image img = new Image(imgPath.toUri().toString(), true);
        previewImage.setImage(img);
        pageLabel.setText(page + " / " + totalPages);
        btnPrevPage.setDisable(page <= 1);
        btnNextPage.setDisable(page >= totalPages);
    }

    private void navigatePage(int delta) {
        showPage(currentPage + delta);
    }

    private void cleanTempFiles() {
        pageImages.forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException ignored) {} });
        if (previewPdf != null) { try { Files.deleteIfExists(previewPdf); } catch (IOException ignored) {} }
    }

    // ── Actions articles ──────────────────────────────────────────────────

    private void onAddArticle() {
        ArticleTemplate tpl = templateBox.getValue();
        if (tpl == null) return;
        ArticleOption art = tpl.toArticleOption();
        config.addArticle(art);
        articleItems.add(art);
        articleList.getSelectionModel().select(art);
        templateBox.setValue(null);
        scheduleRefresh();
    }

    private void onSelectArticle(ArticleOption art) {
        editingArticle = art;
        if (art == null) { titleField.clear(); bodyArea.clear(); return; }
        titleField.setText(art.title());
        bodyArea.setText(art.body());
    }

    private void applyEdit() {
        if (editingArticle == null) return;
        if (!titleField.getText().isBlank()) editingArticle.setTitle(titleField.getText());
        if (!bodyArea.getText().isBlank())   editingArticle.setBody(bodyArea.getText());
        int i = articleItems.indexOf(editingArticle);
        if (i >= 0) articleItems.set(i, editingArticle);
        scheduleRefresh();
    }

    private void onMove(int dir) {
        ArticleOption sel = articleList.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        int i = articleItems.indexOf(sel);
        int j = i + dir;
        if (j < 0 || j >= articleItems.size()) return;
        if (dir < 0) config.moveUp(sel); else config.moveDown(sel);
        articleItems.remove(i);
        articleItems.add(j, sel);
        articleList.getSelectionModel().select(j);
    }

    private void onDelete() {
        ArticleOption sel = articleList.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        config.removeArticle(sel);
        articleItems.remove(sel);
    }

    private void onConfirm() {
        String path = outputField.getText().trim();
        if (path.isBlank()) {
            outputField.setStyle("-fx-border-color: " + AppTheme.COLOR_DANGER + "; -fx-border-radius: 4;");
            outputField.requestFocus();
            return;
        }
        config.setOutputPath(path);
        confirmed = true;
        close();
    }

    // ── Résultat ─────────────────────────────────────────────────────────

    public boolean isConfirmed() { return confirmed; }
    public RegulationPdfConfig getConfig() { return config; }

    // ── Style helpers ─────────────────────────────────────────────────────

    private void styleToggle(ToggleButton btn) {
        btn.setPadding(new Insets(7, 16, 7, 16));
        Runnable update = () -> btn.setStyle(btn.isSelected()
            ? "-fx-background-color:" + AppTheme.COLOR_PRIMARY + ";-fx-background-radius:6;"
              + "-fx-text-fill:white;-fx-font-weight:600;-fx-font-size:11px;"
            : "-fx-background-color:" + AppTheme.COLOR_SURFACE + ";-fx-border-color:" + AppTheme.COLOR_PRIMARY
              + ";-fx-border-radius:6;-fx-background-radius:6;-fx-text-fill:" + AppTheme.COLOR_PRIMARY
              + ";-fx-font-weight:600;-fx-font-size:11px;");
        btn.selectedProperty().addListener((o, ov, nv) -> update.run());
        update.run();
    }

    private void styleCheck(CheckBox cb) {
        cb.setStyle("-fx-font-size: 12px; -fx-text-fill: " + AppTheme.COLOR_TEXT + ";");
    }

    private void styleNavBtn(Button btn) {
        btn.setStyle("-fx-background-color:" + AppTheme.COLOR_SURFACE + ";-fx-border-color:" + AppTheme.COLOR_BORDER
                + ";-fx-border-radius:4;-fx-background-radius:4;-fx-font-size:14px;");
    }

    // ── Cellule liste articles ────────────────────────────────────────────

    private static final class ArticleCell extends ListCell<ArticleOption> {
        @Override protected void updateItem(ArticleOption item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) { setGraphic(null); return; }
            Label t = new Label(item.title());
            t.setStyle("-fx-font-weight:700;-fx-font-size:12px;-fx-text-fill:" + AppTheme.COLOR_TEXT + ";");
            String preview = item.body().length() > 70 ? item.body().substring(0, 70) + "…" : item.body();
            Label b = new Label(preview);
            b.setStyle("-fx-font-size:11px;-fx-text-fill:" + AppTheme.COLOR_TEXT_MUTED + ";");
            VBox box = new VBox(2, t, b);
            box.setPadding(new Insets(3, 6, 3, 6));
            setGraphic(box);
            setText(null);
        }
    }
}