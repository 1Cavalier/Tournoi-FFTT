package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class LoginOrganismeView extends VBox {

    public LoginOrganismeView(Navigator nav) {
        setPadding(new Insets(24));
        setSpacing(12);

        var title = new Label("Connexion Organisme");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        var email = new TextField();
        email.setPromptText("Email");

        var password = new PasswordField();
        password.setPromptText("Mot de passe");

        var info = new Label("V0 : aucun contrôle, c’est juste l’UI.");
        info.setStyle("-fx-opacity: 0.8;");

        var loginBtn = new Button("Se connecter");
        loginBtn.setOnAction(e -> {
            // V0 : pas de logique, pas de stockage
            info.setText("Connexion simulée ✅ (organisme)");
        });

        var backBtn = new Button("Retour");
        backBtn.setOnAction(e -> nav.showHome());

        getChildren().addAll(title, email, password, loginBtn, backBtn, info);
    }
}
