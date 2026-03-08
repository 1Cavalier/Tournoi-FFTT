package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer.components;

public final class OrganizerViewUtils {

    private OrganizerViewUtils() {
    }

    public static String nvl(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    public static String safe(String value) {
        if (value == null) {
            return "";
        }

        String v = value.trim();
        return v.isEmpty() ? "" : v;
    }

    public static String fullNameOrDash(String firstName, String lastName) {
        String first = safe(firstName);
        String last = safe(lastName);

        if (first.isEmpty() && last.isEmpty()) {
            return "—";
        }
        if (first.isEmpty()) {
            return last;
        }
        if (last.isEmpty()) {
            return first;
        }

        return first + " " + last;
    }
}