package fr.pingmanager.gestion_tournois_FFTT.infra.repo;

import java.util.List;
import java.util.Optional;

import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.TableauDto;

public interface TableauRepository {

    TableauDto insert(TableauDto tableau);

    Optional<TableauDto> findById(String id);

    List<TableauDto> findByTournamentId(String tournamentId);

    void update(TableauDto tableau);

    void delete(String id);
}