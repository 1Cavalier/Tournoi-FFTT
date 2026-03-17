package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto;

import java.util.List;

public record OfficialSelectablePlayerDto(

        String licenseNumber,
        String firstName,
        String lastName,
        String clubName,

        List<String> judgeGrades,
        List<String> refereeGrades) {
}