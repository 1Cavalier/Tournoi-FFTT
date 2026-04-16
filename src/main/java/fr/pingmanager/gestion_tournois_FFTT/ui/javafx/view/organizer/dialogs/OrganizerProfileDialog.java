package fr.pingmanager.gestion_tournois_FFTT.ui.javafx.view.organizer.dialogs;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.Optional;

import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.app.AppRouter;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.ClubAccessDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.ClubDto;
import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.OrganizerDto;

/**
 * Édition du profil du club associé à l'organisateur connecté.
 *
 * Source de vérité : table club.
 * Récupération via organizer_account.club_id.
 */
public class OrganizerProfileDialog extends Stage {

    public OrganizerProfileDialog(AppRouter nav) {
        setTitle("Modifier le profil du club");
        initModality(Modality.APPLICATION_MODAL);

        OrganizerDto org = nav.requireOrganizer();
        Optional<ClubDto> clubOpt = nav.clubRepo().findByOrganizerId(org.getId());

        if (clubOpt.isEmpty()) {
            showError("Club introuvable", "Aucun club n'est associé à cet organisateur.");
            close();
            return;
        }

        ClubDto existing = clubOpt.get();

        TextField clubNumber = new TextField(nvl(existing.clubNumber()));
        TextField clubName = new TextField(nvl(existing.clubName()));
        TextField depCode = new TextField(nvl(existing.departementCode()));
        TextField city = new TextField(nvl(existing.city()));
        TextField address1 = new TextField(nvl(existing.address1()));
        TextField address2 = new TextField(nvl(existing.address2()));
        TextField firstName = new TextField(nvl(existing.contactFirstName()));
        TextField lastName = new TextField(nvl(existing.contactLastName()));
        TextField latitude = new TextField(existing.latitude() == null ? "" : String.valueOf(existing.latitude()));
        TextField longitude = new TextField(existing.longitude() == null ? "" : String.valueOf(existing.longitude()));

        TextField logoPath = new TextField(nvl(existing.logoPath()));
        logoPath.setEditable(false);

        Button chooseLogo = new Button("Choisir un logo...");
        chooseLogo.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Choisir un logo");
            fc.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.webp"),
                    new FileChooser.ExtensionFilter("Tous les fichiers", "*.*"));
            File f = fc.showOpenDialog(this);
            if (f != null) {
                logoPath.setText(f.getAbsolutePath());
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(18));

        int r = 0;
        grid.add(label("Nom du club"), 0, r);
        grid.add(clubName, 1, r++);

        grid.add(label("N° club FFTT"), 0, r);
        grid.add(clubNumber, 1, r++);

        grid.add(label("Département (code)"), 0, r);
        grid.add(depCode, 1, r++);

        grid.add(label("Ville"), 0, r);
        grid.add(city, 1, r++);

        grid.add(label("Adresse 1"), 0, r);
        grid.add(address1, 1, r++);

        grid.add(label("Adresse 2"), 0, r);
        grid.add(address2, 1, r++);

        grid.add(label("Latitude (optionnel)"), 0, r);
        grid.add(latitude, 1, r++);

        grid.add(label("Longitude (optionnel)"), 0, r);
        grid.add(longitude, 1, r++);

        grid.add(label("Prénom responsable"), 0, r);
        grid.add(firstName, 1, r++);

        grid.add(label("Nom responsable"), 0, r);
        grid.add(lastName, 1, r++);

        HBox logoRow = new HBox(10, logoPath, chooseLogo);
        HBox.setHgrow(logoPath, Priority.ALWAYS);
        grid.add(label("Logo"), 0, r);
        grid.add(logoRow, 1, r++);

        Label hint = new Label("Les coordonnées GPS doivent être renseignées ensemble, ou laissées vides.");
        hint.setStyle("-fx-opacity: 0.8;");

        // ---------------------------------------------------------------------
        // TABLEAU DES ACCÈS MULTI-COMPTES DU CLUB
        // ---------------------------------------------------------------------

        Label accessTitle = new Label("Comptes ayant accès au club");
        accessTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        TableView<ClubAccessDto> accessTable = new TableView<>();
        accessTable.setPlaceholder(new Label("Aucun accès supplémentaire trouvé pour ce club."));
        accessTable.setPrefHeight(220);

        TableColumn<ClubAccessDto, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(nvl(cell.getValue().email())));

        TableColumn<ClubAccessDto, String> firstNameCol = new TableColumn<>("Prénom");
        firstNameCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(nvl(cell.getValue().firstName())));

        TableColumn<ClubAccessDto, String> lastNameCol = new TableColumn<>("Nom");
        lastNameCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(nvl(cell.getValue().lastName())));

        accessTable.getColumns().add(emailCol);
        accessTable.getColumns().add(firstNameCol);
        accessTable.getColumns().add(lastNameCol);

        Runnable reloadAccessTable = () -> {
            List<ClubAccessDto> accesses = nav.clubAccessRepo().findByClubId(existing.id());
            accessTable.setItems(FXCollections.observableArrayList(accesses));
        };
        reloadAccessTable.run();

        Button refreshAccess = new Button("Rafraîchir");
        refreshAccess.setOnAction(e -> reloadAccessTable.run());

        HBox accessHeader = new HBox(10, accessTitle, new Region(), refreshAccess);
        HBox.setHgrow(accessHeader.getChildren().get(1), Priority.ALWAYS);

        VBox accessSection = new VBox(10,
                new Separator(),
                accessHeader,
                accessTable);
        accessSection.setPadding(new Insets(10, 18, 0, 18));

        VBox center = new VBox(10, grid, hint, accessSection);
        center.setPadding(new Insets(0, 18, 10, 18));

        Button cancel = new Button("Annuler");
        Button save = new Button("Enregistrer");
        save.setDefaultButton(true);

        Label error = new Label();
        error.setStyle("-fx-text-fill:#b00020;");

        cancel.setOnAction(e -> close());

        save.setOnAction(e -> {
            try {
                Double lat = parseNullableDouble(latitude.getText());
                Double lon = parseNullableDouble(longitude.getText());

                if ((lat == null) ^ (lon == null)) {
                    throw new IllegalArgumentException(
                            "Latitude et longitude doivent être renseignées ensemble, ou laissées vides.");
                }

                ClubDto updated = new ClubDto(
                        existing.id(),
                        blankToNull(clubNumber.getText()),
                        blankToNull(clubName.getText()),
                        blankToNull(depCode.getText()),
                        blankToNull(city.getText()),
                        blankToNull(address1.getText()),
                        blankToNull(address2.getText()),
                        lat,
                        lon,
                        blankToNull(firstName.getText()),
                        blankToNull(lastName.getText()),
                        existing.officialContactEmail(),
                        blankToNull(logoPath.getText()),
                        existing.updatedAt());

                nav.clubRepo().updateClubProfile(updated);
                reloadAccessTable.run();
                close();

            } catch (Exception ex) {
                error.setText(ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage());
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox bottom = new HBox(10, error, spacer, cancel, save);
        bottom.setPadding(new Insets(12, 18, 18, 18));

        BorderPane root = new BorderPane();
        root.setCenter(center);
        root.setBottom(bottom);

        setScene(new Scene(root, 860, 860));
    }

    private static Label label(String text) {
        Label l = new Label(text);
        l.setMinWidth(180);
        return l;
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    private static String blankToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static Double parseNullableDouble(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        if (t.isEmpty()) {
            return null;
        }
        return Double.parseDouble(t.replace(',', '.'));
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}