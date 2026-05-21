package fr.pingmanager.gestion_tournois_FFTT.ui.javafx.service;

import java.util.List;

import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TableauDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TournamentOfficialAssignmentDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.service.RegulationPdfConfig.ArticleOption;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.service.RegulationPdfConfig.DotationsFormat;

/** Modèle plat pour le moteur de rendu PDF — toutes valeurs déjà formatées. */
public final class RegulationPdfModel {

    // En-tête
    String tournamentName;
    String tournamentLevel;
    String datesLabel;
    String homologationRef;   // "[En cours de validation]" si vide
    String clubName;
    String logoPath;

    // Lieu complet du tournoi (pas du club)
    String venueName;
    String venueStreet;
    String venueZip;
    String venueCity;
    String venueDepartment;   // ex. "Val-de-Marne (94)"
    String venueRegion;       // ex. "Île-de-France"
    int    tableCount;

    // Matériel
    String ballBrand;
    String ballPolicy;

    // Horaires
    String gymOpenTime;
    String regOpenTime;
    String regDeadline;

    // Officiels
    List<TournamentOfficialAssignmentDto> officials;

    // Tableaux
    List<TableauDto> tableaux;
    DotationsFormat  dotationsFormat;

    // Finances
    int totalDotation;
    int prepaidFee;
    int onSiteFee;

    // Options
    boolean allowReentryAfterElimination;
    boolean enforcePrizePresenceRule;
    String  refundDeadlineLabel;
    boolean showRefundPlatform;
    String  refundPlatformLabel;

    // Couleur des titres
    String accentColor;

    // Ordre des articles standards
    java.util.List<RegulationPdfConfig.StandardArticle> articleOrder;

    // Articles extra
    List<ArticleOption> extraArticles;
}