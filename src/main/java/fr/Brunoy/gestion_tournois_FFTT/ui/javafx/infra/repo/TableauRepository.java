package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.TableauDto;

import java.util.List;
import java.util.Optional;

public interface TableauRepository {

    TableauDto insert(TableauDto tableau);

    Optional<TableauDto> findById(String id);

    List<TableauDto> findByTournamentId(String tournamentId);

    void update(TableauDto tableau);

    void delete(String id);
}