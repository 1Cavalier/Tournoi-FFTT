package fr.pingmanager.gestion_tournois_FFTT.ui.javafx.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Configuration de génération du règlement PDF.
 */
public final class RegulationPdfConfig {

    // ── Format dotations ───────────────────────────────────────────────────

    public enum DotationsFormat {
        TABLE_RECAP("Tableau récapitulatif"),
        LIST_PER_TABLEAU("Liste par tableau");

        private final String label;
        DotationsFormat(String label) { this.label = label; }
        public String label() { return label; }
        @Override public String toString() { return label; }
    }

    // ── Articles optionnels ────────────────────────────────────────────────

    public static final class ArticleOption {
        private String title;
        private String body;

        public ArticleOption(String title, String body) {
            this.title = Objects.requireNonNull(title);
            this.body  = Objects.requireNonNull(body);
        }

        public String title() { return title; }
        public String body()  { return body;  }
        public void setTitle(String t) { this.title = Objects.requireNonNull(t); }
        public void setBody(String b)  { this.body  = Objects.requireNonNull(b); }
    }

    public enum ArticleTemplate {
        RESTAURATION("Restauration",
                "Une buvette proposant sandwichs, boissons et confiseries sera assurée "
                + "par le club organisateur durant toute la durée du tournoi. "
                + "La compétition se déroulera sans interruption aux heures de repas."),
        PARKING("Stationnement",
                "Un parking gratuit est accessible à proximité immédiate de la salle. "
                + "Le comité organisateur décline toute responsabilité en cas de dommages "
                + "ou de vol survenus sur les véhicules stationnés."),
        HEBERGEMENT("Hébergement",
                "Des informations sur les hébergements à proximité sont disponibles "
                + "auprès du secrétariat du club ou sur le site internet du club organisateur."),
        SPONSORS("Partenaires",
                "Le tournoi est organisé avec le soutien de ses partenaires. "
                + "Le comité organisateur les remercie chaleureusement pour leur contribution."),
        ACCES("Accès",
                "La salle est accessible en transports en commun. "
                + "En voiture, suivre la signalétique mise en place le jour du tournoi."),
        MEDICAL("Dispositions médicales",
                "Un défibrillateur est présent dans l'enceinte sportive. "
                + "Tout joueur dont l'état de santé serait incompatible avec la pratique sportive "
                + "doit en informer le juge-arbitre avant le début de son tableau."),
        PHOTOGRAPHIE("Photographies",
                "Des photos et vidéos pourront être réalisées à des fins de communication du club. "
                + "Tout joueur s'y opposant doit en informer le secrétariat à son arrivée."),
        CUSTOM("Article personnalisé",
                "Saisissez ici le texte de votre article.");

        private final String defaultTitle;
        private final String defaultBody;

        ArticleTemplate(String t, String b) { this.defaultTitle = t; this.defaultBody = b; }
        public String defaultTitle() { return defaultTitle; }
        public String defaultBody()  { return defaultBody;  }
        public ArticleOption toArticleOption() { return new ArticleOption(defaultTitle, defaultBody); }
        @Override public String toString() { return defaultTitle; }
    }

    // ── Articles standards (réordonnables) ───────────────────────────────

    /**
     * Représente un article standard du règlement.
     * L'organisateur peut changer leur ordre mais pas leur contenu
     * (celui-ci est généré automatiquement depuis les données du tournoi).
     */
    public enum StandardArticle {
        PRESENTATION("Présentation et homologation"),
        LIEU("Lieu et infrastructure"),
        TABLEAUX("Tableaux"),
        DOTATIONS("Récompenses et dotations"),
        TARIFS("Frais d'engagement"),
        REGLEMENT_FFTT("Règlement fédéral"),
        OFFICIELS("Corps arbitral"),
        INSCRIPTIONS("Inscriptions"),
        REMBOURSEMENTS("Désistements et remboursements"),
        RESPONSABILITE("Responsabilité"),
        ACCEPTATION("Acceptation du règlement");

        private final String label;
        StandardArticle(String label) { this.label = label; }
        public String label() { return label; }
        @Override public String toString() { return label; }
    }

    // ── Champs supplémentaires ────────────────────────────────────────────

    // ── Champs ────────────────────────────────────────────────────────────

    /** Couleur des titres d'articles en hexadécimal (ex. "#1565C0" pour bleu). */
    private String accentColor = "#1565C0";

    /** Ordre des articles standards. Initialisé avec l'ordre canonique. */
    private final java.util.List<StandardArticle> articleOrder =
            new java.util.ArrayList<>(java.util.List.of(StandardArticle.values()));

    /** Chemin d'un règlement PDF personnalisé fourni par l'organisateur.
     *  Si non null, ce fichier est utilisé tel quel (la génération automatique est ignorée). */
    private String customPdfPath;

    private DotationsFormat dotationsFormat = DotationsFormat.TABLE_RECAP;
    private final List<ArticleOption> extraArticles = new ArrayList<>();
    private String outputPath;

    // Paramètres optionnels
    /** Si true : précise qu'un joueur éliminé d'un tableau peut s'inscrire dans un autre. */
    private boolean allowReentryAfterElimination = false;

    /** Si true : un lauréat absent en fin de tournoi ne peut réclamer sa récompense. */
    private boolean enforcePrizePresenceRule = false;

    /** Date limite de remboursement (texte libre, ex. "30 mai 2026 à 20h00"). */
    private String refundDeadlineLabel = "";

    /** Si true : précise l'organisme de remboursement dans l'article. */
    private boolean showRefundPlatform = false;
    private String refundPlatformLabel = "";

    // ── Accesseurs ────────────────────────────────────────────────────────

    public String accentColor() { return accentColor; }
    public void setAccentColor(String hex) { this.accentColor = hex != null ? hex : "#1565C0"; }

    public java.util.List<StandardArticle> articleOrder() { return articleOrder; }
    public void moveStandardUp(StandardArticle a) {
        int i = articleOrder.indexOf(a);
        if (i > 0) { articleOrder.remove(i); articleOrder.add(i - 1, a); }
    }
    public void moveStandardDown(StandardArticle a) {
        int i = articleOrder.indexOf(a);
        if (i >= 0 && i < articleOrder.size() - 1) { articleOrder.remove(i); articleOrder.add(i + 1, a); }
    }

    public String customPdfPath() { return customPdfPath; }
    public void setCustomPdfPath(String p) { this.customPdfPath = p; }
    public boolean hasCustomPdf() { return customPdfPath != null && !customPdfPath.isBlank(); }

    public DotationsFormat dotationsFormat() { return dotationsFormat; }
    public void setDotationsFormat(DotationsFormat f) { this.dotationsFormat = Objects.requireNonNull(f); }

    public List<ArticleOption> extraArticles() { return extraArticles; }
    public void addArticle(ArticleOption a) { extraArticles.add(Objects.requireNonNull(a)); }
    public void removeArticle(ArticleOption a) { extraArticles.remove(a); }
    public void moveUp(ArticleOption a) {
        int i = extraArticles.indexOf(a);
        if (i > 0) { extraArticles.remove(i); extraArticles.add(i - 1, a); }
    }
    public void moveDown(ArticleOption a) {
        int i = extraArticles.indexOf(a);
        if (i >= 0 && i < extraArticles.size() - 1) { extraArticles.remove(i); extraArticles.add(i + 1, a); }
    }

    public String outputPath() { return outputPath; }
    public void setOutputPath(String p) { this.outputPath = p; }

    public boolean allowReentryAfterElimination() { return allowReentryAfterElimination; }
    public void setAllowReentryAfterElimination(boolean v) { this.allowReentryAfterElimination = v; }

    public boolean enforcePrizePresenceRule() { return enforcePrizePresenceRule; }
    public void setEnforcePrizePresenceRule(boolean v) { this.enforcePrizePresenceRule = v; }

    public String refundDeadlineLabel() { return refundDeadlineLabel; }
    public void setRefundDeadlineLabel(String s) { this.refundDeadlineLabel = s == null ? "" : s; }

    public boolean showRefundPlatform() { return showRefundPlatform; }
    public void setShowRefundPlatform(boolean v) { this.showRefundPlatform = v; }

    public String refundPlatformLabel() { return refundPlatformLabel; }
    public void setRefundPlatformLabel(String s) { this.refundPlatformLabel = s == null ? "" : s; }
}