package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.SqliteClubRepository;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.SqliteOrganizerAccountRepository;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.security.HashUtils;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.security.PasswordPolicy;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.mail.EmailVerificationService;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;

public class OrganizerAuthService {

    private final SqliteOrganizerAccountRepository organizerRepo;
    private final SqliteClubRepository clubRepo;
    private final EmailVerificationService emailVerification;

    public OrganizerAuthService(SqliteOrganizerAccountRepository organizerRepo,
            SqliteClubRepository clubRepo,
            EmailVerificationService emailVerification) {
        this.organizerRepo = organizerRepo;
        this.clubRepo = clubRepo;
        this.emailVerification = emailVerification;
    }

    // ---------------- INSCRIPTION ----------------

    /**
     * Inscription : soit rejoindre un club existant (clubId), soit créer un nouveau
     * club (clubName).
     * - clubId non null => rejoint club existant
     * - sinon => crée un club minimal (clubName peut être null/blank mais c'est
     * mieux de le remplir)
     */
    public OrganizerAccount register(String email, String password, String existingClubIdOrNull,
            String newClubNameOrNull) {

        if (email == null || email.isBlank())
            throw new IllegalArgumentException("Email obligatoire.");
        if (password == null || password.isBlank())
            throw new IllegalArgumentException("Mot de passe obligatoire.");
        PasswordPolicy.validateOrThrow(password);

        String cleanEmail = email.trim().toLowerCase();

        String clubId;
        if (existingClubIdOrNull != null && !existingClubIdOrNull.isBlank()) {
            clubId = existingClubIdOrNull.trim();
        } else {
            // création club minimal
            String clubName = (newClubNameOrNull == null) ? null : newClubNameOrNull.trim();
            clubId = clubRepo.createClub(null, clubName);
        }

        String hash = HashUtils.hash(password);

        OrganizerAccount acc = organizerRepo.insert(clubId, cleanEmail, hash);

        // code vérification inscription
        emailVerification.sendVerificationCode(acc.getId(), acc.getEmail());

        return acc;
    }

    public boolean verifyEmail(String email, String code) {
        if (email == null || email.isBlank())
            return false;
        if (code == null || code.isBlank())
            return false;
        return emailVerification.verify(email.trim().toLowerCase(), code.trim());
    }

    public void resendVerificationCode(String email) {
        if (email == null || email.isBlank())
            throw new IllegalArgumentException("Email obligatoire.");

        var row = organizerRepo.findDbRowByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable."));

        if (row.emailVerified())
            throw new IllegalArgumentException("Email déjà vérifié.");

        emailVerification.sendVerificationCode(row.id(), row.email());
    }

    // ---------------- LOGIN (étape 1 : password OK => envoi OTP) ----------------

    /**
     * Étape 1 du login :
     * - vérifie password
     * - email doit être vérifié
     * - génère OTP et l'envoie par email
     * Retourne OrganizerAccount (mais la session ne doit être ouverte qu'après
     * verifyLoginOtp).
     */
    public OrganizerAccount loginStart(String email, String password) {

        if (email == null || email.isBlank())
            throw new IllegalArgumentException("Email obligatoire.");
        if (password == null || password.isBlank())
            throw new IllegalArgumentException("Mot de passe obligatoire.");

        String cleanEmail = email.trim().toLowerCase();

        var row = organizerRepo.findDbRowByEmail(cleanEmail)
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable."));

        // password
        if (!HashUtils.hash(password).equals(row.passwordHash())) {
            throw new IllegalArgumentException("Mot de passe incorrect.");
        }

        // email verified
        if (!row.emailVerified()) {
            throw new IllegalArgumentException("Email non vérifié. Vérifie ton email avec le code reçu.");
        }

        // générer OTP login (6 chiffres, 10 minutes)
        String otp = String.format("%06d", new Random().nextInt(1_000_000));
        String expires = Instant.now().plus(10, ChronoUnit.MINUTES).toString();

        organizerRepo.setLoginOtp(row.id(), otp, expires);

        // envoi mail (console en dev)
        emailVerification.sendLoginOtp(row.email(), otp);

        // On renvoie un OrganizerAccount minimal (clubName sera rempli plus tard via
        // join club)
        return OrganizerAccount.fromDb(row.id(), "", row.email(), row.passwordHash(), true);
    }

    /**
     * Étape 2 du login :
     * - vérifie OTP
     * - si OK => login final autorisé
     */
    public OrganizerAccount verifyLoginOtpAndFinish(String email, String otpCode) {
        if (email == null || email.isBlank())
            throw new IllegalArgumentException("Email obligatoire.");
        if (otpCode == null || otpCode.isBlank())
            throw new IllegalArgumentException("Code OTP obligatoire.");

        String cleanEmail = email.trim().toLowerCase();

        boolean ok = organizerRepo.verifyLoginOtp(cleanEmail, otpCode.trim());
        if (!ok) {
            throw new IllegalArgumentException("OTP invalide ou expiré.");
        }

        // Recharger compte
        return organizerRepo.findByEmail(cleanEmail)
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable."));
    }
}