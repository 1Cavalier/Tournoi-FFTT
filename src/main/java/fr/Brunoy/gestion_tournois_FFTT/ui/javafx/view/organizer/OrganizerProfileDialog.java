package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view.organizer;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.ClubProfileRow;
import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model.OrganizerAccount;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;

public class OrganizerProfileDialog extends Stage {

    public OrganizerProfileDialog(Navigator nav) {
        setTitle("Modifier le profil de l'organisme");
        initModality(Modality.APPLICATION_MODAL);

        OrganizerAccount org = nav.getCurrentOrganizer();
        if (org == null) {
            close();
            return;
        }

        Optional<ClubProfileRow> existing = nav.clubProfileRepo().findByOrganizerId(org.getId());

        // Champs
        TextField clubNumber = new TextField();
        TextField clubName = new TextField();
        TextField depCode = new TextField();
        TextField city = new TextField();
        TextField address1 = new TextField();
        TextField address2 = new TextField();
        TextField firstName = new TextField();
        TextField lastName = new TextField();
        TextField latitude = new TextField();
        TextField longitude = new TextField();

        TextField logoPath = new TextField();
        logoPath.setEditable(false);

        Button chooseLogo = new Button("Choisir un logo...");
        chooseLogo.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Choisir un logo");
            fc.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.webp"),
                    new FileChooser.ExtensionFilter("Tous les fichiers", "*.*"));
            File f = fc.showOpenDialog(this);
            if (f != null)
                logoPath.setText(f.getAbsolutePath());
        });

        existing.ifPresent(p -> {
            clubNumber.setText(nvl(p.clubNumber()));
            clubName.setText(nvl(p.clubName()));
            depCode.setText(nvl(p.departementCode()));
            city.setText(nvl(p.city()));
            address1.setText(nvl(p.address1()));
            address2.setText(nvl(p.address2()));
            firstName.setText(nvl(p.contactFirstName()));
            lastName.setText(nvl(p.contactLastName()));
            logoPath.setText(nvl(p.logoPath()));
            latitude.setText(p.latitude() == null ? "" : String.valueOf(p.latitude()));
            longitude.setText(p.longitude() == null ? "" : String.valueOf(p.longitude()));
        });

        // Layout formulaire
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(18));

        int r = 0;
        grid.add(label("Nom club"), 0, r);
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

        Label hint = new Label("Les coordonnées GPS doivent être renseignées ensemble (ou laissées vides).");
        hint.setStyle("-fx-opacity:0.8;");
        VBox center = new VBox(10, grid, hint);
        center.setPadding(new Insets(0, 18, 10, 18));

        // Boutons
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

                // règle: lat/lon ensemble
                if ((lat == null) ^ (lon == null)) {
                    throw new IllegalArgumentException(
                            "Latitude et longitude doivent être renseignées ensemble (ou vides).");
                }

                ClubProfileRow p = new ClubProfileRow(
                        org.getId(),
                        clubNumber.getText(),
                        clubName.getText(),
                        depCode.getText(),
                        city.getText(),
                        address1.getText(),
                        address2.getText(),
                        lat,
                        lon,
                        firstName.getText(),
                        lastName.getText(),
                        logoPath.getText());

                nav.clubProfileRepo().upsert(p);
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

        setScene(new Scene(root, 720, 620));
    }

    private static Label label(String s) {
        Label l = new Label(s);
        l.setMinWidth(180);
        return l;
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    private static Double parseNullableDouble(String s) {
        if (s == null)
            return null;
        String t = s.trim();
        if (t.isEmpty())
            return null;
        return Double.parseDouble(t.replace(',', '.'));
    }
}
