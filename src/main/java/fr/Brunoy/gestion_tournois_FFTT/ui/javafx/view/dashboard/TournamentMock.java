package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.dashboard;

public class TournamentMock {

    private final String name;
    private final String club;
    private final String level;
    private final String phase;
    private final String date;
    private final TournamentStatus status;

    public TournamentMock(String name, String club, String level, String phase, String date, TournamentStatus status) {
        this.name = name;
        this.club = club;
        this.level = level;
        this.phase = phase;
        this.date = date;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getClub() {
        return club;
    }

    public String getLevel() {
        return level;
    }

    public String getPhase() {
        return phase;
    }

    public String getDate() {
        return date;
    }

    public TournamentStatus getStatus() {
        return status;
    }
}
