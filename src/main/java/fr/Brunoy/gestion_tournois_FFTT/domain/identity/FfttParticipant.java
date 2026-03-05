package fr.Brunoy.gestion_tournois_FFTT.domain.identity;

import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.*;

import java.util.Objects;

public final class FfttParticipant implements Participant {

    private final Player player;

    public FfttParticipant(Player player) {
        this.player = Objects.requireNonNull(player, "player");
    }

    public Player player() {
        return player;
    }

    @Override
    public String participantId() {
        return player.getLicenseNumber();
    }

    @Override
    public String fullName() {
        return player.getFullName();
    }

    @Override
    public Gender gender() {
        return player.getGender();
    }

    @Override
    public String nationalityCode() {
        return player.getNationality();
    }

    @Override
    public AgeCategory ageCategory() {
        return player.getAgeCategory();
    }

    @Override
    public int pointsFor(RankingPhase phase) {
        return player.pointsFor(phase);
    }

    @Override
    public MedicalCertificateStatus medicalCertificateStatus() {
        return player.getMedicalCertificateStatus();
    }

    @Override
    public boolean isFfttLicensed() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Participant p))
            return false;
        return participantId().equals(p.participantId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(participantId());
    }
}