package fr.pingmanager.gestion_tournois_FFTT.infra.repo;

import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.classification.ClassificationBracket;

import java.util.Optional;

public interface ClassificationBracketRepository {

    void save(ClassificationBracket bracket, String tournamentId);

    void update(ClassificationBracket bracket);

    Optional<ClassificationBracket> findByTableau(String tournamentId, String tableauCode);

    void deleteByTableau(String tournamentId, String tableauCode);
}