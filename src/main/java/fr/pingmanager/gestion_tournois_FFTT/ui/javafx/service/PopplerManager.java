package fr.pingmanager.gestion_tournois_FFTT.ui.javafx.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestionnaire des binaires Poppler nécessaires à la prévisualisation PDF.
 *
 * <h2>Stratégie selon l'OS</h2>
 * <ul>
 *   <li><b>Linux / macOS</b> — {@code pdftoppm} est typiquement dans le PATH système
 *       ({@code poppler-utils} via apt/brew). On cherche directement.</li>
 *   <li><b>Windows</b> — Poppler n'est pas fourni par le système. Au premier appel,
 *       un script Python télécharge et extrait les binaires dans {@code data/poppler/bin/}.
 *       Les appels suivants utilisent directement ce chemin local.</li>
 * </ul>
 *
 * <p>Toutes les méthodes sont thread-safe (synchronisées sur la classe).
 */
public final class PopplerManager {

    private static final Logger LOG = Logger.getLogger(PopplerManager.class.getName());

    /** URL du release Poppler Windows (binaires précompilés, ~15 Mo). */
    private static final String POPPLER_WIN_URL =
            "https://github.com/oschwartz10612/poppler-windows/releases/download/v24.08.0-0/Release-24.08.0-0.zip";

    /** Dossier local où Poppler sera extrait (relatif au répertoire de travail de l'appli). */
    private static final Path POPPLER_DIR = Paths.get("data", "poppler");

    /** Chemins système connus pour pdftoppm (Linux/macOS). */
    private static final List<String> SYSTEM_CANDIDATES = List.of(
            "pdftoppm",
            "/usr/bin/pdftoppm",
            "/usr/local/bin/pdftoppm",
            "/opt/homebrew/bin/pdftoppm"
    );

    /** Chemin résolu en cache (null = pas encore résolu). */
    private static volatile String resolvedPath = null;

    private PopplerManager() {}

    // -------------------------------------------------------------------------
    // POINT D'ENTRÉE PRINCIPAL
    // -------------------------------------------------------------------------

    /**
     * Retourne le chemin absolu vers {@code pdftoppm} utilisable par {@link ProcessBuilder},
     * ou {@code null} si introuvable même après tentative d'installation.
     *
     * <p>L'appel est bloquant lors du premier lancement sur Windows (téléchargement ~15 Mo).
     * Les appels suivants retournent immédiatement depuis le cache.
     */
    public static synchronized String resolve() {
        if (resolvedPath != null) return resolvedPath;

        // 1. Chercher dans le PATH système (Linux / macOS / Windows avec Poppler global)
        String sys = findInSystem();
        if (sys != null) {
            resolvedPath = sys;
            LOG.info("pdftoppm trouvé dans le système : " + sys);
            return resolvedPath;
        }

        // 2. Chercher dans le dossier local data/poppler/
        String local = findLocal();
        if (local != null) {
            resolvedPath = local;
            LOG.info("pdftoppm trouvé localement : " + local);
            return resolvedPath;
        }

        // 3. Sur Windows : télécharger + extraire via Python
        if (isWindows()) {
            LOG.info("Poppler absent — téléchargement en cours…");
            boolean ok = downloadAndExtract();
            if (ok) {
                local = findLocal();
                if (local != null) {
                    resolvedPath = local;
                    LOG.info("pdftoppm installé localement : " + local);
                    return resolvedPath;
                }
            }
        }

        LOG.warning("pdftoppm introuvable. La prévisualisation PDF sera désactivée.");
        return null;
    }

    /**
     * Retourne {@code true} si Poppler est disponible (ou vient d'être installé).
     * Non bloquant si déjà résolu.
     */
    public static boolean isAvailable() {
        return resolve() != null;
    }

    // -------------------------------------------------------------------------
    // RECHERCHE
    // -------------------------------------------------------------------------

    private static String findInSystem() {
        List<String> candidates;
        if (isWindows()) {
            // Chemins globaux Windows courants (winget, choco, manuel)
            candidates = List.of(
                    "pdftoppm",
                    "C:\\Program Files\\poppler\\bin\\pdftoppm.exe",
                    "C:\\Program Files (x86)\\poppler\\bin\\pdftoppm.exe",
                    "C:\\poppler\\bin\\pdftoppm.exe",
                    "C:\\tools\\poppler\\bin\\pdftoppm.exe"
            );
        } else {
            candidates = SYSTEM_CANDIDATES;
        }
        return candidates.stream().filter(PopplerManager::isExecutable).findFirst().orElse(null);
    }

    private static String findLocal() {
        // Chercher pdftoppm(.exe) dans data/poppler/ et ses sous-dossiers (jusqu'à 3 niveaux)
        if (!Files.isDirectory(POPPLER_DIR)) return null;
        try {
            return Files.walk(POPPLER_DIR, 4)
                    .filter(p -> {
                        String name = p.getFileName().toString();
                        return name.equals("pdftoppm") || name.equals("pdftoppm.exe");
                    })
                    .map(Path::toAbsolutePath)
                    .map(Path::toString)
                    .filter(PopplerManager::isExecutable)
                    .findFirst()
                    .orElse(null);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Erreur lors de la recherche locale de pdftoppm", e);
            return null;
        }
    }

    private static boolean isExecutable(String candidate) {
        try {
            Process p = new ProcessBuilder(candidate, "-v")
                    .redirectErrorStream(true)
                    .start();
            p.waitFor(3, TimeUnit.SECONDS);
            p.destroy();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // TÉLÉCHARGEMENT ET EXTRACTION (Windows)
    // -------------------------------------------------------------------------

    /**
     * Télécharge le ZIP Poppler Windows et l'extrait dans {@code data/poppler/}
     * via un script Python temporaire.
     *
     * @return {@code true} si l'opération a réussi
     */
    private static boolean downloadAndExtract() {
        try {
            Files.createDirectories(POPPLER_DIR);

            String script = buildDownloadScript(
                    POPPLER_WIN_URL,
                    POPPLER_DIR.toAbsolutePath().toString());

            Path pyFile = Files.createTempFile("poppler_install_", ".py");
            try {
                Files.writeString(pyFile, script, StandardCharsets.UTF_8);
                Process proc = new ProcessBuilder("python3", pyFile.toAbsolutePath().toString())
                        .inheritIO()
                        .start();
                // Laisser jusqu'à 3 minutes pour le téléchargement
                boolean done = proc.waitFor(3, TimeUnit.MINUTES);
                return done && proc.exitValue() == 0;
            } finally {
                Files.deleteIfExists(pyFile);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Échec de l'installation de Poppler", e);
            return false;
        }
    }

    private static String buildDownloadScript(String url, String destDir) {
        // Utilise uniquement la bibliothèque standard Python — pas de dépendances tierces.
        String safeUrl  = url.replace("'", "\\'");
        String safeDest = destDir.replace("\\", "\\\\").replace("'", "\\'");

        return "# -*- coding: utf-8 -*-\n"
             + "import urllib.request, zipfile, os, sys, shutil\n"
             + "\n"
             + "URL  = '" + safeUrl  + "'\n"
             + "DEST = '" + safeDest + "'\n"
             + "ZIP  = os.path.join(DEST, 'poppler.zip')\n"
             + "\n"
             + "print('Téléchargement de Poppler…', flush=True)\n"
             + "try:\n"
             + "    urllib.request.urlretrieve(URL, ZIP)\n"
             + "except Exception as e:\n"
             + "    print('Erreur de téléchargement :', e)\n"
             + "    sys.exit(1)\n"
             + "\n"
             + "print('Extraction…', flush=True)\n"
             + "try:\n"
             + "    with zipfile.ZipFile(ZIP, 'r') as z:\n"
             + "        z.extractall(DEST)\n"
             + "    os.remove(ZIP)\n"
             + "    print('Poppler installé dans', DEST)\n"
             + "except Exception as e:\n"
             + "    print('Erreur d extraction :', e)\n"
             + "    sys.exit(1)\n";
    }

    // -------------------------------------------------------------------------
    // UTILITAIRES
    // -------------------------------------------------------------------------

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }
}