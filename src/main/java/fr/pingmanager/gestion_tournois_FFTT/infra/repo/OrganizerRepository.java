package fr.pingmanager.gestion_tournois_FFTT.infra.repo;

import java.util.Optional;

import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.OrganizerDto;

/**
 * Contrat d'accès aux comptes organisateurs.
 */
public interface OrganizerRepository {

    OrganizerDto insert(String clubId, String firstName, String lastName, String email, String passwordHash);

    Optional<OrganizerDto> findByEmail(String email);

    Optional<OrganizerDto> findById(String organizerId);

    Optional<AuthOrganizerRow> findAuthByEmail(String email);

    void setEmailVerification(String organizerId, String code, String expiresAt);

    boolean verifyEmail(String email, String code);

    void setLoginOtp(String organizerId, String otpCode, String expiresAt);

    boolean verifyLoginOtp(String email, String otpCode);

    record AuthOrganizerRow(
            String id,
            String clubId,
            String email,
            String passwordHash,
            boolean emailVerified) {
    }
}