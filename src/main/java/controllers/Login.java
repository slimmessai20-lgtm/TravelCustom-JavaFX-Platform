package controllers;

import entities.Role;
import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.UserService;
import utils.SessionManager;

import java.sql.SQLException;

public class Login {

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblMessage;
    @FXML private Button btnLogin;
    @FXML private Hyperlink linkRegister;

    private UserService userService;

    public void initialize() {
        userService = new UserService();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        lblMessage.setText("");

        String email = txtEmail.getText();
        String password = txtPassword.getText();

        if (email == null || email.trim().isEmpty()) {
            lblMessage.setText("⚠ L'email est obligatoire");
            return;
        }

        if (password == null || password.isEmpty()) {
            lblMessage.setText("⚠ Le mot de passe est obligatoire");
            return;
        }

        try {
            User user = userService.findByEmail(email.trim());

            if (user == null) {
                lblMessage.setText("⚠ Email ou mot de passe incorrect");
                return;
            }

            // Vérification du mot de passe (sans hachage)
            if (!user.getPassword().equals(password)) {
                lblMessage.setText("⚠ Email ou mot de passe incorrect");
                return;
            }

            // Sauvegarder la session
            SessionManager.setCurrentUser(user);

            // Redirection selon le rôle
            redirectToDashboard(user);

        } catch (SQLException e) {
            lblMessage.setText("Erreur de connexion à la base de données");
            e.printStackTrace();
        }
    }

    private void redirectToDashboard(User user) {
        try {
            String fxmlFile = "";
            String title = "";

            // Déterminer le dashboard selon le rôle
            if (user.getRoles() == Role.ROLE_ADMIN) {
                fxmlFile = "/org/voyages/voyages/DashboardAdmin.fxml";
                title = "Tableau de bord - Administrateur";
            } else if (user.getRoles() == Role.ROLE_USER) {
                fxmlFile = "/org/voyages/voyages/DashboardAgent.fxml";
                title = "Tableau de bord - Agent";
            } else if (user.getRoles() == Role.ROLE_CLIENT) {
                fxmlFile = "/org/voyages/voyages/DashboardClient.fxml";
                title = "Tableau de bord - Client";
            } else {
                lblMessage.setText("⚠ Rôle utilisateur non reconnu");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.setMaximized(true);

        } catch (Exception e) {
            lblMessage.setText("Erreur lors de la redirection");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/voyages/voyages/Register.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Inscription");

        } catch (Exception e) {
            lblMessage.setText("Erreur lors de l'ouverture du formulaire d'inscription");
            e.printStackTrace();
        }
    }
}
