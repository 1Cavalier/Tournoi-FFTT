package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.view;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.app.Navigator;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class LoginJoueurView extends VBox {

    public LoginJoueurView(Navigator nav) {
        setPadding(new Insets(24));
        setSpacing(12);

        var title = new Label("Connexion Joueur");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        var licenceOrEmail = new TextField();
        licenceOrEmail.setPromptText("N° licence ou email");

        var password = new PasswordField();
        password.setPromptText("Mot de passe");

        var info = new Label("V0 : aucun contrôle, c’est juste l’UI.");
        info.setStyle("-fx-opacity: 0.8;");

        var loginBtn = new Button("Se connecter");
        loginBtn.setOnAction(e -> info.setText("Connexion simulée ✅ (joueur)"));

        var backBtn = new Button("Retour");
        backBtn.setOnAction(e -> nav.showHome());

        getChildren().addAll(title, licenceOrEmail, password, loginBtn, backBtn, info);
    }
}
