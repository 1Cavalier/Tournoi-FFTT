package fr.pingmanager.gestion_tournois_FFTT.ui.javafx.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.PrizeRewardTypeDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.PrizeTierDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TableauDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TournamentOfficialAssignmentDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.service.RegulationPdfConfig;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.service.RegulationPdfConfig.ArticleOption;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.service.RegulationPdfConfig.DotationsFormat;

/**
 * Moteur de rendu PDF ReportLab.
 * Génère un script Python temporaire, l'exécute, puis le supprime.
 */
public final class RegulationPdfRenderer {

    private RegulationPdfRenderer() {}

    public static void render(RegulationPdfModel m, String outputPath)
            throws IOException, InterruptedException {
        Path script = Files.createTempFile("regulation_", ".py");
        try {
            Files.writeString(script, buildScript(m, outputPath), StandardCharsets.UTF_8);
            Process proc = new ProcessBuilder("python3", script.toAbsolutePath().toString())
                    .inheritIO().start();
            int exit = proc.waitFor();
            if (exit != 0) throw new IOException("Script ReportLab terminé avec code " + exit);
        } finally {
            Files.deleteIfExists(script);
        }
    }

    // ── Construction du script ────────────────────────────────────────────

    private static String buildScript(RegulationPdfModel m, String out) {
        Sb sb = new Sb();

        sb.l("# -*- coding: utf-8 -*-");
        sb.l("from reportlab.lib.pagesizes import A4");
        sb.l("from reportlab.lib import colors");
        sb.l("from reportlab.lib.units import cm");
        sb.l("from reportlab.lib.styles import ParagraphStyle");
        sb.l("from reportlab.lib.enums import TA_CENTER, TA_LEFT, TA_JUSTIFY");
        sb.l("from reportlab.platypus import (");
        sb.l("    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle,");
        sb.l("    HRFlowable, Image)");
        sb.l("import os");
        sb.l("");

        // Couleurs
        sb.l("W, H       = A4");
        sb.f("BLUE       = colors.HexColor('%s')", m.accentColor);
        sb.f("BLUE_LIGHT = colors.HexColor('%s')", lightenHex(m.accentColor));
        sb.l("GREY_ROW   = colors.HexColor('#F4F7FB')");
        sb.l("GREY_LINE  = colors.HexColor('#CBD5E1')");
        sb.l("BLACK      = colors.HexColor('#1E293B')");

        // Styles
        sb.l("def ps(name, **kw):");
        sb.l("    base = dict(fontName='Helvetica', fontSize=10, leading=14,");
        sb.l("                textColor=BLACK, spaceAfter=4)");
        sb.l("    base.update(kw)");
        sb.l("    return ParagraphStyle(name, **base)");
        sb.l("");
        sb.l("S_TITLE    = ps('T', fontName='Helvetica-Bold', fontSize=19, leading=25,");
        sb.l("                textColor=BLUE, alignment=TA_CENTER, spaceAfter=3)");
        sb.l("S_SUB      = ps('S', fontSize=12, leading=16, alignment=TA_CENTER, spaceAfter=2)");
        sb.l("S_REF      = ps('R', fontName='Helvetica-Oblique', fontSize=9,");
        sb.l("                textColor=colors.HexColor('#64748B'), alignment=TA_CENTER, spaceAfter=10)");
        sb.l("S_ART      = ps('A', fontName='Helvetica-Bold', fontSize=11, leading=15,");
        sb.l("                textColor=BLUE, spaceBefore=14, spaceAfter=5)");
        sb.l("S_BODY     = ps('B', alignment=TA_JUSTIFY)");
        sb.l("S_BOLD     = ps('BB', fontName='Helvetica-Bold')");
        sb.l("S_TH       = ps('TH', fontName='Helvetica-Bold', fontSize=9, leading=12,");
        sb.l("                textColor=colors.white, alignment=TA_CENTER)");
        sb.l("S_TD       = ps('TD', fontSize=9, leading=12, alignment=TA_CENTER)");
        sb.l("S_TD_L     = ps('TDL', fontSize=9, leading=12)");

        // Pied de page
        sb.l("");
        sb.l("class Footer:");
        sb.l("    def __init__(self, name): self.name = name");
        sb.l("    def __call__(self, canvas, doc):");
        sb.l("        canvas.saveState()");
        sb.l("        canvas.setFont('Helvetica', 8)");
        sb.l("        canvas.setFillColor(colors.HexColor('#94A3B8'))");
        sb.l("        canvas.drawCentredString(W/2, 1.2*cm, f'{self.name}  —  Page {doc.page}')");
        sb.l("        canvas.setStrokeColor(GREY_LINE)");
        sb.l("        canvas.line(2*cm, 1.65*cm, W-2*cm, 1.65*cm)");
        sb.l("        canvas.restoreState()");

        // Document
        sb.l("");
        sb.f("doc = SimpleDocTemplate(%s, pagesize=A4,", py(out));
        sb.l("    leftMargin=2.2*cm, rightMargin=2.2*cm,");
        sb.l("    topMargin=2.5*cm, bottomMargin=2.5*cm)");
        sb.f("footer = Footer(%s)", py(m.tournamentName));
        sb.l("story = []");
        sb.l("");

        appendHeader(sb, m);
        int nextArt = appendStandardArticles(sb, m);
        appendExtraArticles(sb, m, nextArt);

        sb.l("doc.build(story, onFirstPage=footer, onLaterPages=footer)");
        return sb.toString();
    }

    // ── En-tête ───────────────────────────────────────────────────────────

    private static void appendHeader(Sb sb, RegulationPdfModel m) {
        if (m.logoPath != null && !m.logoPath.isBlank()) {
            sb.f("_lp = %s", py(m.logoPath));
            sb.l("if os.path.exists(_lp):");
            sb.l("    try:");
            sb.l("        _img = Image(_lp, width=3*cm, height=3*cm)");
            sb.l("        _img.hAlign = 'CENTER'");
            sb.l("        story.append(_img)");
            sb.l("        story.append(Spacer(1, 0.3*cm))");
            sb.l("    except Exception: pass");
        }
        sb.f("story.append(Paragraph(%s, S_TITLE))", py(m.tournamentName));
        if (!m.tournamentLevel.isBlank())
            sb.f("story.append(Paragraph(%s, S_SUB))", py("Tournoi " + m.tournamentLevel));
        if (!m.datesLabel.isBlank())
            sb.f("story.append(Paragraph(%s, S_SUB))", py(m.datesLabel));
        sb.f("story.append(Paragraph(%s, S_REF))", py("Homologué sous le N°\u00a0" + m.homologationRef));
        sb.l("story.append(HRFlowable(width='100%', thickness=1.5, color=BLUE, spaceAfter=14))");
        sb.l("");
    }

    // ── Articles standards (ordre configurable) ──────────────────────────

    private static int appendStandardArticles(Sb sb, RegulationPdfModel m) {
        int n = 1;
        List<RegulationPdfConfig.StandardArticle> order = m.articleOrder != null
                ? m.articleOrder
                : List.of(RegulationPdfConfig.StandardArticle.values());

        for (RegulationPdfConfig.StandardArticle art : order) {
            n = switch (art) {
                case PRESENTATION   -> art(sb, n, art.label(), buildArt1(m));
                case LIEU           -> art(sb, n, art.label(), buildArt2(m));
                case TABLEAUX       -> artTableaux(sb, n, m);
                case DOTATIONS      -> artDotations(sb, n, m);
                case TARIFS         -> art(sb, n, art.label(), buildArt5(m));
                case REGLEMENT_FFTT -> art(sb, n, art.label(), buildArt6(m));
                case OFFICIELS      -> artOfficiels(sb, n, m);
                case INSCRIPTIONS   -> art(sb, n, art.label(), buildArt8(m));
                case REMBOURSEMENTS -> art(sb, n, art.label(), buildArt9(m));
                case RESPONSABILITE -> art(sb, n, art.label(),
                        "Le comité organisateur décline toute responsabilité pour tout accident, "
                        + "vol ou perte d'effets personnels pendant la durée du tournoi. "
                        + "L'organisation se réserve le droit d'annuler un tableau si le nombre "
                        + "d'inscrits est insuffisant.");
                case ACCEPTATION    -> art(sb, n, art.label(),
                        "Toute inscription au présent tournoi vaut acceptation pleine et entière "
                        + "du présent règlement ainsi que des règlements fédéraux de la F.F.T.T.");
            };
        }
        return n;
    }

    // ---- Art. 1 ----
    private static String buildArt1(RegulationPdfModel m) {
        StringBuilder s = new StringBuilder();
        s.append("Le présent tournoi, homologué par la Fédération Française de Tennis de Table "
               + "sous la référence N°\u00a0").append(m.homologationRef).append(", "
               + "est organisé par ");
        if (!m.clubName.isBlank()) s.append("le club ").append(m.clubName);
        else s.append("le comité organisateur");
        if (!m.datesLabel.isBlank()) s.append(" les ").append(m.datesLabel);
        s.append(". Les règlements fédéraux seront appliqués dans leur intégralité. "
               + "Dans tous les tableaux, les parties se joueront au meilleur des 5 manches, "
               + "une manche étant gagnée à 11 points avec 2 points d'écart minimum.");
        return s.toString();
    }

    // ---- Art. 2 ----
    private static String buildArt2(RegulationPdfModel m) {
        StringBuilder s = new StringBuilder();
        s.append("Le tournoi se déroulera");
        if (!m.venueName.isBlank()) s.append(" au ").append(m.venueName);

        // Adresse complète
        StringBuilder addr = new StringBuilder();
        if (!m.venueStreet.isBlank()) addr.append(m.venueStreet);
        if (!m.venueZip.isBlank() || !m.venueCity.isBlank()) {
            if (!addr.isEmpty()) addr.append(", ");
            if (!m.venueZip.isBlank()) addr.append(m.venueZip).append(" ");
            addr.append(m.venueCity);
        }
        if (!m.venueDepartment.isBlank()) {
            if (!addr.isEmpty()) addr.append(" — ");
            addr.append(m.venueDepartment);
        }
        if (!m.venueRegion.isBlank()) {
            if (!addr.isEmpty()) addr.append(", ");
            addr.append(m.venueRegion);
        }
        if (!addr.isEmpty()) s.append(", ").append(addr);
        s.append(".");

        if (m.tableCount > 0)
            s.append(" La salle comporte ").append(m.tableCount)
             .append(" tables de jeu aux dimensions réglementaires.");
        if (!m.gymOpenTime.isBlank())
            s.append(" La salle ouvrira à ").append(m.gymOpenTime).append(".");
        return s.toString();
    }

    // ---- Art. 5 ----
    private static String buildArt5(RegulationPdfModel m) {
        if (m.prepaidFee <= 0 && m.onSiteFee <= 0)
            return "Le tarif d'engagement sera précisé par le comité organisateur.";
        StringBuilder s = new StringBuilder("Le tarif d'engagement est fixé à ");
        if (m.prepaidFee > 0) {
            s.append(m.prepaidFee).append("\u00a0€ par tableau");
            if (m.onSiteFee > 0 && m.onSiteFee != m.prepaidFee)
                s.append(" (inscription en ligne)");
        }
        if (m.onSiteFee > 0 && m.onSiteFee != m.prepaidFee) {
            if (m.prepaidFee > 0) s.append(", ");
            s.append(m.onSiteFee).append("\u00a0€ par tableau (inscription sur place)");
        }
        s.append(". Seuls les joueurs ayant acquitté leurs frais d'engagement "
               + "seront maintenus dans les tableaux concernés.");
        return s.toString();
    }

    // ---- Art. 6 ----
    private static String buildArt6(RegulationPdfModel m) {
        StringBuilder s = new StringBuilder();
        s.append("Les règles de jeu de la F.F.T.T. seront appliquées dans leur intégralité. ");
        if (!m.ballBrand.isBlank())
            s.append("Les balles utilisées seront de marque/type « ").append(m.ballBrand)
             .append(" », homologuées I.T.T.F. (***), ").append(m.ballPolicy).append(". ");
        else
            s.append("Les balles utilisées seront homologuées I.T.T.F. (***). ");

        s.append("Une tenue sportive réglementaire est exigée ; les tenues entièrement blanches "
               + "(maillot ou short) ne sont pas autorisées. "
               + "Seuls pourront prendre part au tournoi les joueurs dont la licence F.F.T.T. "
               + "saison en cours est validée, ou en mesure d'en justifier le jour du tournoi. "
               + "Le forfait est prononcé 10 minutes après l'appel du joueur. "
               + "En cas de forfait ou d'absence non excusée, la commission sportive fédérale "
               + "appliquera l'article IV.202 des règlements administratifs. "
               + "Dans chaque poule, le perdant est tenu d'arbitrer la partie suivante "
               + "à la demande du juge-arbitre.");

        if (m.allowReentryAfterElimination)
            s.append(" Un joueur éliminé d'un tableau peut s'inscrire dans un autre tableau "
                   + "dans la limite des places disponibles.");

        return s.toString();
    }

    // ---- Art. 8 ----
    private static String buildArt8(RegulationPdfModel m) {
        StringBuilder s = new StringBuilder("Les inscriptions doivent être effectuées");
        if (!m.regDeadline.isBlank()) s.append(" avant le ").append(m.regDeadline);
        s.append(".");
        if (!m.regOpenTime.isBlank())
            s.append(" Les inscriptions sur place sont possibles à partir de ")
             .append(m.regOpenTime).append(", dans la limite des places disponibles.");
        s.append(" Les inscriptions sont prises dans l'ordre d'arrivée.");
        return s.toString();
    }

    // ---- Art. 9 ----
    private static String buildArt9(RegulationPdfModel m) {
        StringBuilder s = new StringBuilder();
        if (!m.refundDeadlineLabel.isBlank()) {
            s.append("Les demandes de remboursement sont acceptées jusqu'au ")
             .append(m.refundDeadlineLabel).append(". ");
        } else {
            s.append("Les demandes de remboursement sont acceptées jusqu'à 15 jours "
                   + "avant la date d'ouverture du tournoi. ");
        }
        if (m.showRefundPlatform && !m.refundPlatformLabel.isBlank())
            s.append("Le remboursement s'effectue via ").append(m.refundPlatformLabel).append(". ");

        s.append("Au-delà de cette date, aucun remboursement ne sera accordé sauf "
               + "présentation d'un certificat médical. Des listes d'attente seront "
               + "constituées pour pallier les désistements tardifs justifiés.");
        return s.toString();
    }

    // ---- Art. Tableaux ----
    private static int artTableaux(Sb sb, int n, RegulationPdfModel m) {
        sb.f("story.append(Paragraph(%s, S_ART))", py("Article " + n + "  —  Tableaux"));

        if (m.tableaux.isEmpty()) {
            sb.f("story.append(Paragraph(%s, S_BODY))",
                    py("Les tableaux seront communiqués par le comité organisateur."));
            return n + 1;
        }

        String intro = "Le tournoi comporte les tableaux suivants. "
                + "Tous les tableaux sont disputés par poules de 3 joueurs "
                + "(ou 4 si le nombre d'inscrits le nécessite). "
                + "Les 2 premiers de chaque poule sont qualifiés pour le tableau final "
                + "à élimination directe. "
                + "Chaque tableau débute 30 minutes après la clôture du pointage.";
        sb.f("story.append(Paragraph(%s, S_BODY))", py(intro));
        sb.l("story.append(Spacer(1, 0.3*cm))");

        // Tableau récap horaires
        sb.l("_td = [[");
        sb.l("  Paragraph('Tableau', S_TH),");
        sb.l("  Paragraph('Catégorie', S_TH),");
        sb.l("  Paragraph('Fin de pointage', S_TH),");
        sb.l("  Paragraph('Début', S_TH),");
        sb.l("  Paragraph('Places max.', S_TH),");
        sb.l("]]");
        for (TableauDto t : m.tableaux) {
            String code  = nvl(t.code(), "—");
            String desig = nvl(t.designation(), "");
            String cat   = desig.isBlank() ? code : code + "  —  " + desig;
            sb.f("_td.append([Paragraph(%s,S_TD),Paragraph(%s,S_TD),Paragraph(%s,S_TD),Paragraph(%s,S_TD),Paragraph(%s,S_TD)])",
                    py(code), py(cat), py(nvl(t.checkInEnd(),"—")), py(nvl(t.startTime(),"—")),
                    py(t.maxPlayers() != null ? String.valueOf(t.maxPlayers()) : "—"));
        }
        sb.l("_t = Table(_td, colWidths=[1.8*cm, None, 3.2*cm, 2.2*cm, 2.6*cm], hAlign='LEFT')");
        appendBaseTableStyle(sb, "_t");
        sb.l("story.append(_t)");
        sb.l("");
        return n + 1;
    }

    // ---- Art. Dotations ----
    private static int artDotations(Sb sb, int n, RegulationPdfModel m) {
        sb.f("story.append(Paragraph(%s, S_ART))",
                py("Article " + n + "  —  Récompenses"));

        String intro;
        if (m.totalDotation > 0) {
            intro = "Le tournoi est doté de " + m.totalDotation + "\u00a0€ au total.";
        } else {
            intro = "La dotation du tournoi sera précisée par le comité organisateur.";
        }
        // Règle de présence
        if (m.enforcePrizePresenceRule)
            intro += " Tout lauréat ou joueur classé dans une position donnant droit à une "
                   + "récompense doit être présent dans l'enceinte sportive jusqu'à la fin "
                   + "du tournoi pour la percevoir. Passé ce délai, la récompense sera acquise "
                   + "au comité organisateur.";

        sb.f("story.append(Paragraph(%s, S_BODY))", py(intro));
        sb.l("story.append(Spacer(1, 0.25*cm))");

        if (m.dotationsFormat == DotationsFormat.TABLE_RECAP) {
            appendDotationsRecap(sb, m);
        } else {
            appendDotationsList(sb, m);
        }
        return n + 1;
    }

    private static void appendDotationsRecap(Sb sb, RegulationPdfModel m) {
        sb.l("_dd = [[");
        sb.l("  Paragraph('Tableau', S_TH),");
        sb.l("  Paragraph('1er', S_TH),");
        sb.l("  Paragraph('Finaliste', S_TH),");
        sb.l("  Paragraph('½ Finalistes', S_TH),");
        sb.l("  Paragraph('Total', S_TH),");
        sb.l("]]");
        for (TableauDto t : m.tableaux) {
            if (t.prizeTiers() == null || t.prizeTiers().isEmpty()) continue;
            String code     = nvl(t.code(), "—");
            String p1       = prizeAt(t, 1);
            String p2       = prizeAt(t, 2);
            String p3       = prizeAt(t, 3);
            int total = t.prizeTiers().stream()
                    .filter(p -> p.rewardType() == PrizeRewardTypeDto.CASH && p.cashAmount() != null)
                    .mapToInt(p -> p.cashAmount() * Math.max(1, p.toRank() != null ? p.toRank() - p.fromRank() + 1 : 1))
                    .sum();
            String ts = total > 0 ? total + "\u00a0€" : "—";
            sb.f("_dd.append([Paragraph(%s,S_TD),Paragraph(%s,S_TD),Paragraph(%s,S_TD),Paragraph(%s,S_TD),Paragraph(%s,S_TD)])",
                    py(code), py(p1), py(p2), py(p3), py(ts));
        }
        sb.l("_dt = Table(_dd, colWidths=[2*cm, 2.5*cm, 2.8*cm, 3.2*cm, 2.2*cm], hAlign='LEFT')");
        appendBaseTableStyle(sb, "_dt");
        sb.l("story.append(_dt)");
        sb.l("");
    }

    private static void appendDotationsList(Sb sb, RegulationPdfModel m) {
        for (TableauDto t : m.tableaux) {
            if (t.prizeTiers() == null || t.prizeTiers().isEmpty()) continue;
            String lbl = "Tableau " + nvl(t.code(), "—")
                    + (t.designation() != null && !t.designation().isBlank()
                       ? "  —  " + t.designation() : "");
            sb.f("story.append(Paragraph(%s, S_BOLD))", py(lbl));
            for (PrizeTierDto p : t.prizeTiers()) {
                if (p.rewardType() != PrizeRewardTypeDto.CASH || p.cashAmount() == null) continue;
                String line = rankLabel(p.fromRank(), p.toRank()) + " : " + p.cashAmount() + "\u00a0€";
                sb.f("story.append(Paragraph(%s, S_BODY))", py("  •  " + line));
            }
            sb.l("story.append(Spacer(1, 0.15*cm))");
        }
        sb.l("");
    }

    // ---- Art. Officiels ----
    private static int artOfficiels(Sb sb, int n, RegulationPdfModel m) {
        sb.f("story.append(Paragraph(%s, S_ART))",
                py("Article " + n + "  —  Corps arbitral"));

        if (m.officials.isEmpty()) {
            sb.f("story.append(Paragraph(%s, S_BODY))",
                    py("Le juge-arbitre du tournoi sera désigné par le comité organisateur. "
                     + "Le juge-arbitre est seul habilité à trancher tout litige ; "
                     + "ses décisions sont définitives. "
                     + "Le tirage au sort a lieu sur place après la clôture du pointage de chaque tableau."));
            sb.l("");
            return n + 1;
        }

        // ── JA principal (grade le plus élevé / designatedMainJudge) ──
        List<TournamentOfficialAssignmentDto> juges = m.officials.stream()
                .filter(o -> "JUDGE".equalsIgnoreCase(o.officialRoleType())
                          || Boolean.TRUE.equals(o.designatedMainJudge())
                          || Boolean.TRUE.equals(o.assistantJudge()))
                .sorted((a, b) -> {
                    // designatedMainJudge en premier
                    boolean am = Boolean.TRUE.equals(a.designatedMainJudge());
                    boolean bm = Boolean.TRUE.equals(b.designatedMainJudge());
                    if (am != bm) return am ? -1 : 1;
                    // puis tri par grade décroissant (JA3 > JA2 > JA1)
                    String ag = a.judgeGrade() != null ? a.judgeGrade() : "";
                    String bg = b.judgeGrade() != null ? b.judgeGrade() : "";
                    return bg.compareToIgnoreCase(ag);
                }).toList();

        List<TournamentOfficialAssignmentDto> arbitres = m.officials.stream()
                .filter(o -> "REFEREE".equalsIgnoreCase(o.officialRoleType())
                          && !Boolean.TRUE.equals(o.designatedMainJudge())
                          && !Boolean.TRUE.equals(o.assistantJudge()))
                .toList();

        // ── Paragraphe JA ──
        if (!juges.isEmpty()) {
            TournamentOfficialAssignmentDto mainJa = juges.get(0);
            StringBuilder jaLine = new StringBuilder();
            jaLine.append("Le juge-arbitre du tournoi est ")
                  .append(officialName(mainJa));
            if (mainJa.judgeGrade() != null && !mainJa.judgeGrade().isBlank())
                jaLine.append(" (").append(mainJa.judgeGrade()).append(")");
            jaLine.append(".");

            if (juges.size() > 1) {
                jaLine.append(" Il est assisté de : ");
                jaLine.append(juges.subList(1, juges.size()).stream()
                        .map(a -> officialName(a)
                                + (a.judgeGrade() != null && !a.judgeGrade().isBlank()
                                   ? " (" + a.judgeGrade() + ")" : ""))
                        .collect(Collectors.joining(", ")));
                jaLine.append(".");
            }
            sb.f("story.append(Paragraph(%s, S_BODY))", py(jaLine.toString()));
        }

        // ── Paragraphe arbitres ──
        if (!arbitres.isEmpty()) {
            StringBuilder refLine = new StringBuilder();
            refLine.append(arbitres.size() == 1 ? "L'arbitre désigné est " : "Les arbitres désignés sont ");
            refLine.append(arbitres.stream()
                    .map(a -> officialName(a)
                            + (a.refereeGrade() != null && !a.refereeGrade().isBlank()
                               ? " (" + a.refereeGrade() + ")" : ""))
                    .collect(Collectors.joining(", ")));
            refLine.append(".");
            sb.f("story.append(Paragraph(%s, S_BODY))", py(refLine.toString()));
        }

        // ── Phrase de clôture ──
        sb.f("story.append(Paragraph(%s, S_BODY))",
                py("Le juge-arbitre est seul habilité à trancher tout litige prévu ou non "
                 + "au présent règlement ; ses décisions sont définitives et sans appel. "
                 + "Le tirage au sort a lieu sur place après la clôture du pointage de chaque tableau."));
        sb.l("");
        return n + 1;
    }

    // ---- Articles extra ----
    private static void appendExtraArticles(Sb sb, RegulationPdfModel m, int nextN) {
        for (ArticleOption art : m.extraArticles) {
            sb.f("story.append(Paragraph(%s, S_ART))",
                    py("Article " + nextN + "  —  " + art.title()));
            sb.f("story.append(Paragraph(%s, S_BODY))", py(art.body()));
            sb.l("");
            nextN++;
        }
    }

    // ── Helper article générique ──────────────────────────────────────────

    /** Ajoute un article standard (titre + paragraphe) et retourne n+1. */
    private static int art(Sb sb, int n, String title, String body) {
        sb.f("story.append(Paragraph(%s, S_ART))", py("Article " + n + "  —  " + title));
        sb.f("story.append(Paragraph(%s, S_BODY))", py(body));
        sb.l("");
        return n + 1;
    }

        // ── Helpers style tableau ─────────────────────────────────────────────

    private static void appendBaseTableStyle(Sb sb, String var) {
        sb.f("%s.setStyle(TableStyle([", var);
        sb.l("  ('BACKGROUND',     (0,0), (-1,0), BLUE),");
        sb.l("  ('FONTSIZE',       (0,0), (-1,-1), 9),");
        sb.l("  ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, GREY_ROW]),");
        sb.l("  ('GRID',          (0,0), (-1,-1), 0.4, GREY_LINE),");
        sb.l("  ('ALIGN',         (0,0), (-1,-1), 'CENTER'),");
        sb.l("  ('VALIGN',        (0,0), (-1,-1), 'MIDDLE'),");
        sb.l("  ('TOPPADDING',    (0,0), (-1,-1), 5),");
        sb.l("  ('BOTTOMPADDING', (0,0), (-1,-1), 5),");
        sb.l("]))");
    }

    // ── Calcul couleur de fond claire ────────────────────────────────────

    /**
     * Calcule une version très claire (85 % de luminosité) d'une couleur hex
     * pour les fonds de tableaux, en mélangeant avec le blanc.
     */
    private static String lightenHex(String hex) {
        try {
            String h = hex.startsWith("#") ? hex.substring(1) : hex;
            int r = Integer.parseInt(h.substring(0, 2), 16);
            int g = Integer.parseInt(h.substring(2, 4), 16);
            int b = Integer.parseInt(h.substring(4, 6), 16);
            // Mélange 15% couleur + 85% blanc
            r = (int)(r * 0.15 + 255 * 0.85);
            g = (int)(g * 0.15 + 255 * 0.85);
            b = (int)(b * 0.15 + 255 * 0.85);
            return String.format("#%02X%02X%02X", r, g, b);
        } catch (Exception e) {
            return "#EBF4FF";
        }
    }

        // ── Helpers texte ─────────────────────────────────────────────────────

    private static String prizeAt(TableauDto t, int rank) {
        if (t.prizeTiers() == null) return "—";
        return t.prizeTiers().stream()
                .filter(p -> p.rewardType() == PrizeRewardTypeDto.CASH && p.cashAmount() != null
                          && p.fromRank() != null && p.fromRank() <= rank
                          && (p.toRank() == null || p.toRank() >= rank))
                .findFirst()
                .map(p -> p.cashAmount() + "\u00a0€")
                .orElse("—");
    }

    private static String rankLabel(Integer from, Integer to) {
        if (from == null) return "Récompense";
        if (to == null || to.equals(from)) return ordinal(from);
        return ordinal(from) + " – " + ordinal(to);
    }

    private static String ordinal(int n) {
        return switch (n) {
            case 1 -> "1er";
            case 2 -> "Finaliste";
            case 3, 4 -> "½ Finalistes";
            default -> n + "e";
        };
    }

    private static String officialName(TournamentOfficialAssignmentDto o) {
        if (o.firstName() != null && o.lastName() != null)
            return o.firstName().trim() + " " + o.lastName().trim().toUpperCase(Locale.FRENCH);
        return o.lastName() != null ? o.lastName() : "";
    }

    private static String nvl(String v, String fb) { return v != null && !v.isBlank() ? v.trim() : fb; }
    private static String nvl(String v)             { return nvl(v, "—"); }
    private static String py(String s)              { return "'" + esc(s) + "'"; }
    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\r", "");
    }

    // ── Mini StringBuilder helper ─────────────────────────────────────────

    private static final class Sb {
        private final StringBuilder b = new StringBuilder(8192);
        void l(String line) { b.append(line).append('\n'); }
        void f(String fmt, Object... args) { b.append(String.format(fmt, args)).append('\n'); }
        @Override public String toString() { return b.toString(); }
    }
}