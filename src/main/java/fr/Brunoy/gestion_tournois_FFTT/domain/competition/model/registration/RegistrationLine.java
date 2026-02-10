package fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.registration;

import java.time.LocalDate;

public final class RegistrationLine {
    private final String tableauCode;
    private final String designation;
    private final LocalDate date;
    private final int price;

    public RegistrationLine(String tableauCode, String designation, LocalDate date, int price) {
        this.tableauCode = tableauCode;
        this.designation = designation;
        this.date = date;
        this.price = price;
    }

    public String tableauCode() {
        return tableauCode;
    }

    public String designation() {
        return designation;
    }

    public LocalDate date() {
        return date;
    }

    public int price() {
        return price;
    }
}
