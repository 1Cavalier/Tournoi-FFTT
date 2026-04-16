package fr.pingmanager.gestion_tournois_FFTT.ui.javafx.view.organizer.components;

public final class OrganizerViewUtils {

    private OrganizerViewUtils() {
    }

    public static String nvl(String value) {
        return isBlank(value) ? "—" : value.trim();
    }

    public static String safe(String value) {
        return isBlank(value) ? "" : value.trim();
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

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}