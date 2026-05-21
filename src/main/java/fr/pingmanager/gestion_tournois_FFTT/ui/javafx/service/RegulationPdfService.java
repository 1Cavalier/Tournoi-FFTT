package fr.pingmanager.gestion_tournois_FFTT.ui.javafx.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.ClubDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TableauDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TournamentDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TournamentRegulationDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.service.RegulationPdfConfig.DotationsFormat;

/**
 * Orchestre la génération du règlement PDF.
 * Si un PDF custom est fourni dans la config, il est copié tel quel en sortie.
 */
public final class RegulationPdfService {

    private static final Logger LOG = Logger.getLogger(RegulationPdfService.class.getName());

    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter FR_LONG = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH);

    // ── Point d'entrée ────────────────────────────────────────────────────

    public void generate(
            TournamentDto tournament,
            TournamentRegulationDto regulation,
            List<TableauDto> tableaux,
            ClubDto club,
            RegulationPdfConfig config,
            String outputPath) throws RegulationPdfException {

        Objects.requireNonNull(tournament, "tournament");
        Objects.requireNonNull(tableaux,   "tableaux");
        Objects.requireNonNull(config,     "config");
        Objects.requireNonNull(outputPath, "outputPath");

        // Si règlement custom fourni → copie directe
        if (config.hasCustomPdf()) {
            try {
                java.nio.file.Files.copy(
                    java.nio.file.Paths.get(config.customPdfPath()),
                    java.nio.file.Paths.get(outputPath),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                return;
            } catch (IOException e) {
                throw new RegulationPdfException("Impossible de copier le règlement fourni : " + e.getMessage(), e);
            }
        }

        RegulationPdfModel model = buildModel(tournament, regulation, tableaux, club, config);
        try {
            RegulationPdfRenderer.render(model, outputPath);
        } catch (IOException | InterruptedException e) {
            LOG.log(Level.SEVERE, "Erreur génération PDF", e);
            throw new RegulationPdfException("La génération du règlement PDF a échoué : " + e.getMessage(), e);
        }
    }

    // ── Construction du modèle ────────────────────────────────────────────

    public RegulationPdfModel buildModel(
            TournamentDto t,
            TournamentRegulationDto reg,
            List<TableauDto> tableaux,
            ClubDto club,
            RegulationPdfConfig config) {

        RegulationPdfModel m = new RegulationPdfModel();

        m.tournamentName  = nvl(t.name(), "TOURNOI DE TENNIS DE TABLE");
        m.tournamentLevel = formatLevel(t.level());
        m.datesLabel      = formatDateRange(t.startDate(), t.endDate());
        m.homologationRef = nvl(t.homologationNumber(), "[En cours de validation]");
        m.clubName        = club != null ? nvl(club.clubName(), "") : "";
        m.logoPath        = club != null ? club.logoPath() : null;

        // Lieu : priorité aux données du tournoi, puis règlement
        if (reg != null) {
            m.venueName   = nvl(reg.venueName(), nvl(t.address1(), ""));
            m.venueStreet = nvl(reg.venueStreet(), nvl(t.address2(), ""));
            m.venueZip    = nvl(reg.venueZip(), "");
            m.venueCity   = nvl(reg.venueCity(), nvl(t.city(), ""));
        } else {
            m.venueName   = nvl(t.address1(), "");
            m.venueStreet = nvl(t.address2(), "");
            m.venueCity   = nvl(t.city(), "");
        }
        m.venueDepartment = nvl(t.department(), "");
        m.venueRegion     = "";  // à compléter si le champ est ajouté au DTO

        m.tableCount  = reg != null && reg.numberOfTables() != null ? reg.numberOfTables() : 0;
        m.ballBrand   = reg != null ? nvl(reg.ballBrandAndType(), "") : "";
        m.ballPolicy  = reg != null ? resolveBallPolicy(reg.ballProvisionPolicy()) : "fournies par le club organisateur";
        m.gymOpenTime = reg != null ? nvl(reg.gymOpenTime(), "") : "";
        m.regOpenTime = reg != null ? nvl(reg.registrationOpenTime(), "") : "";
        m.regDeadline = reg != null ? nvl(reg.registrationDeadline(), "") : "";
        m.officials   = reg != null && reg.assignedOfficials() != null ? reg.assignedOfficials() : List.of();

        m.tableaux        = tableaux;
        m.dotationsFormat = config.dotationsFormat();

        // Dotation totale (somme de toutes les primes de tous les tableaux)
        m.totalDotation = tableaux.stream()
                .flatMap(tb -> tb.prizeTiers() != null ? tb.prizeTiers().stream() : java.util.stream.Stream.empty())
                .filter(p -> p.rewardType() == fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.PrizeRewardTypeDto.CASH
                          && p.cashAmount() != null)
                .mapToInt(p -> {
                    int qty = (p.toRank() != null ? p.toRank() - p.fromRank() + 1 : 1);
                    return p.cashAmount() * qty;
                }).sum();

        m.prepaidFee = tableaux.stream()
                .filter(tb -> tb.prepaidFee() != null && tb.prepaidFee() > 0)
                .mapToInt(TableauDto::prepaidFee).max().orElse(0);
        m.onSiteFee = tableaux.stream()
                .filter(tb -> tb.onSiteFee() != null && tb.onSiteFee() > 0)
                .mapToInt(TableauDto::onSiteFee).max().orElse(0);

        m.allowReentryAfterElimination = config.allowReentryAfterElimination();
        m.enforcePrizePresenceRule     = config.enforcePrizePresenceRule();
        m.refundDeadlineLabel          = config.refundDeadlineLabel();
        m.showRefundPlatform           = config.showRefundPlatform();
        m.refundPlatformLabel          = config.refundPlatformLabel();
        m.accentColor                  = config.accentColor();
        m.articleOrder                 = config.articleOrder();
        m.extraArticles                = config.extraArticles();

        return m;
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private String formatDateRange(String start, String end) {
        String s = fmtFr(start);
        String e = fmtFr(end);
        if (s.isEmpty() && e.isEmpty()) return "";
        if (s.equals(e) || e.isEmpty()) return s;
        if (s.isEmpty()) return e;
        try {
            LocalDate ds = LocalDate.parse(start, ISO_FMT);
            LocalDate de = LocalDate.parse(end,   ISO_FMT);
            if (ds.getMonth() == de.getMonth() && ds.getYear() == de.getYear()) {
                String monthYear = ds.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH));
                return ds.getDayOfMonth() + " et " + de.getDayOfMonth() + " " + monthYear;
            }
        } catch (DateTimeParseException ignored) {}
        return s + " et " + e;
    }

    private String fmtFr(String iso) {
        if (iso == null || iso.isBlank()) return "";
        try { return LocalDate.parse(iso, ISO_FMT).format(FR_LONG); }
        catch (DateTimeParseException e) { return iso; }
    }

    private String formatLevel(String level) {
        if (level == null || level.isBlank()) return "National B";
        return switch (level.toUpperCase()) {
            case "NATIONAL_A", "NATIONAL A" -> "National A";
            case "NATIONAL_B", "NATIONAL B" -> "National B";
            case "REGIONAL"                 -> "Régional";
            case "DEPARTEMENTAL"            -> "Départemental";
            default                         -> level;
        };
    }

    /** Traduit le nom d'enum BallProvisionPolicy en libellé français lisible. */
    private static String resolveBallPolicy(String raw) {
        if (raw == null || raw.isBlank()) return "fournies par le club organisateur";
        return switch (raw.toUpperCase()) {
            case "PROVIDED_BY_CLUB"    -> "fournies par le club organisateur";
            case "PROVIDED_BY_PLAYERS" -> "apportées par les joueurs";
            case "MIXED_ALLOWED"       -> "fournie par le club ou apportées par les joueurs";
            default                    -> raw; // label déjà en clair si stocké ainsi
        };
    }

        static String nvl(String v, String fallback) {
        return (v != null && !v.isBlank()) ? v.trim() : fallback;
    }

    // ── Exception ─────────────────────────────────────────────────────────

    public static final class RegulationPdfException extends Exception {
        public RegulationPdfException(String msg, Throwable cause) { super(msg, cause); }
    }
}