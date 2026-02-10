package fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.registration;

import java.util.Collections;
import java.util.List;

public final class RegistrationRecap {

    private final List<RegistrationLine> lines;
    private final int totalPrice;

    public RegistrationRecap(List<RegistrationLine> lines) {
        this.lines = List.copyOf(lines);
        this.totalPrice = this.lines.stream().mapToInt(RegistrationLine::price).sum();
    }

    public List<RegistrationLine> lines() {
        return Collections.unmodifiableList(lines);
    }

    public int totalPrice() {
        return totalPrice;
    }
}
