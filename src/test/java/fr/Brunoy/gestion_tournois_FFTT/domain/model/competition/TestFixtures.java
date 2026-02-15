package fr.Brunoy.gestion_tournois_FFTT.domain.model.competition;

import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.entity.Tableau;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.entity.Tournament;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.GenderPolicy;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.TableauPointsRuleType;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.TournamentLevel;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value.FemaleExtraRuleType;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value.PrizeDistribution;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value.PrizeTier;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value.RegistrationFee;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value.TournamentRegistrationPolicy;
import fr.Brunoy.gestion_tournois_FFTT.domain.identity.model.Player;
import fr.Brunoy.gestion_tournois_FFTT.domain.organization.model.Club;
import fr.Brunoy.gestion_tournois_FFTT.domain.organization.model.Departement;
import fr.Brunoy.gestion_tournois_FFTT.domain.organization.model.Region;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.enums.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

public final class TestFixtures {

    private TestFixtures() {
    }

    public static Region idf() {
        return new Region("IDF", "Île-de-France");
    }

    public static Departement essonne91() {
        return new Departement("91", "Essonne", idf());
    }

    private static Club clubBrunoy() {
        return new Club(
                "08911132",
                "Brunoy CTT",
                essonne91(),
                "Brunoy",
                "157 route de Brie",
                null, // address2 optionnel
                null, // latitude optionnelle
                null // longitude optionnelle
        );
    }

    public static Player playerMale(int p1, int p2) {
        return new Player(
                "1234567",
                "Jean",
                "Dupont",
                Gender.MALE,
                "FR",
                clubBrunoy(),
                AgeCategory.SENIOR, // adapte si besoin
                LicenseType.COMPETITION, // adapte si besoin
                false,
                MedicalCertificateStatus.VALIDE,
                p1,
                p2);
    }

    public static Player playerFemale(int p1, int p2) {
        return new Player(
                "7654321",
                "Marie",
                "Durand",
                Gender.FEMALE,
                "FR",
                clubBrunoy(),
                AgeCategory.SENIOR,
                LicenseType.COMPETITION,
                false,
                MedicalCertificateStatus.VALIDE,
                p1,
                p2);
    }

    public static RegistrationFee fee10_12() {
        return new RegistrationFee(10, 12);
    }

    public static PrizeDistribution simplePrizes() {
        return new PrizeDistribution(List.of(
                new PrizeTier(1, 1, 50),
                new PrizeTier(2, 2, 30),
                new PrizeTier(3, 4, 15)));
    }

    public static Tableau tableauAllSeries(String code, LocalDate date, GenderPolicy genderPolicy, int maxPlayers) {
        return new Tableau(
                code,
                "Tableau " + code,
                date,
                genderPolicy,
                TableauPointsRuleType.TOUTES_SERIES,
                null,
                null,
                maxPlayers,
                fee10_12(),
                LocalTime.of(8, 30),
                LocalTime.of(9, 0),
                simplePrizes());
    }

    public static Tournament tournament1DayWithPolicy(LocalDate day) {
        TournamentRegistrationPolicy policy = new TournamentRegistrationPolicy(
                2, // max tableaux / jour
                6, // max total
                FemaleExtraRuleType.ANY_TABLEAU, // bonus féminin autorisé sur n’importe quel tableau
                null // pas de tableau spécifique
        );

        return new Tournament(
                "Tournoi Test",
                clubBrunoy(),
                TournamentLevel.DEPARTEMENTAL,
                RankingPhase.PHASE_2,
                Set.of(day),
                policy);
    }

    public static Tournament tournament1DayWithPolicy3PerDay(LocalDate day) {
        TournamentRegistrationPolicy policy = new TournamentRegistrationPolicy(
                3, // max tableaux / jour = 3
                10, // max total (large pour tests)
                FemaleExtraRuleType.ANY_TABLEAU, // bonus féminin autorisé
                null);

        return new Tournament(
                "Tournoi Test 3/jour",
                clubBrunoy(),
                TournamentLevel.DEPARTEMENTAL,
                RankingPhase.PHASE_2,
                Set.of(day),
                policy);
    }

    public static Tournament tournament1Day3PerDay_FemaleNone(LocalDate day) {
        TournamentRegistrationPolicy policy = new TournamentRegistrationPolicy(
                3, // max/jour
                10, // max total
                FemaleExtraRuleType.NONE,
                null);

        return new Tournament(
                "Tournoi 3/jour - No Female Extra",
                clubBrunoy(),
                TournamentLevel.DEPARTEMENTAL,
                RankingPhase.PHASE_2,
                Set.of(day),
                policy);
    }

    public static Tournament tournament1Day3PerDay_FemaleAny(LocalDate day) {
        TournamentRegistrationPolicy policy = new TournamentRegistrationPolicy(
                3,
                10,
                FemaleExtraRuleType.ANY_TABLEAU,
                null);

        return new Tournament(
                "Tournoi 3/jour - Female Extra Any",
                clubBrunoy(),
                TournamentLevel.DEPARTEMENTAL,
                RankingPhase.PHASE_2,
                Set.of(day),
                policy);
    }

    public static Tournament tournament1Day3PerDay_FemaleSpecificF(LocalDate day) {
        TournamentRegistrationPolicy policy = new TournamentRegistrationPolicy(
                3,
                10,
                FemaleExtraRuleType.SPECIFIC_TABLEAU_CODE,
                "F");

        return new Tournament(
                "Tournoi 3/jour - Female Extra on F",
                clubBrunoy(),
                TournamentLevel.DEPARTEMENTAL,
                RankingPhase.PHASE_2,
                Set.of(day),
                policy);
    }

}
