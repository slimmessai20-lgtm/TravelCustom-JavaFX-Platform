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

import java.sql.SQLException;

public class Register {

    @FXML private TextField txtNom;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Label lblMessage;
    @FXML private Button btnRegister;
    @FXML private Hyperlink linkLogin;

    private UserService userService;

    public void initialize() {
        userService = new UserService();
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        lblMessage.setText("");

        // Validation
        if (!validerFormulaire()) {
            return;
        }

        try {
            // Vérifier si l'email existe déjà
            User existingUser = userService.findByEmail(txtEmail.getText().trim());
            if (existingUser != null) {
                lblMessage.setText("⚠ Cet email est déjà utilisé");
                return;
            }

            // Créer le nouvel utilisateur
            User newUser = new User();
            newUser.setNom(txtNom.getText().trim());
            newUser.setEmail(txtEmail.getText().trim());
            newUser.setPassword(txtPassword.getText()); // Sans hachage
            newUser.setRoles(Role.ROLE_CLIENT); // Par défaut, les nouveaux inscrits sont des clients

            userService.ajouter(newUser);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Inscription réussie");
            alert.setHeaderText(null);
            alert.setContentText("Votre compte a été créé avec succès !\nVous pouvez maintenant vous connecter.");
            alert.showAndWait();

            // Retourner à la page de connexion
            handleBackToLogin(event);

        } catch (SQLException e) {
            lblMessage.setText("Erreur lors de l'inscription");
            e.printStackTrace();
        }
    }

    private boolean validerFormulaire() {
        if (txtNom.getText() == null || txtNom.getText().trim().isEmpty()) {
            lblMessage.setText("⚠ Le nom est obligatoire");
            return false;
        }

        if (txtNom.getText().trim().length() < 2) {
            lblMessage.setText("⚠ Le nom doit contenir au moins 2 caractères");
            return false;
        }

        if (txtEmail.getText() == null || txtEmail.getText().trim().isEmpty()) {
            lblMessage.setText("⚠ L'email est obligatoire");
            return false;
        }

        if (!txtEmail.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            lblMessage.setText("⚠ L'email n'est pas valide");
            return false;
        }

        if (txtPassword.getText() == null || txtPassword.getText().isEmpty()) {
            lblMessage.setText("⚠ Le mot de passe est obligatoire");
            return false;
        }

        if (txtPassword.getText().length() < 6) {
            lblMessage.setText("⚠ Le mot de passe doit contenir au moins 6 caractères");
            return false;
        }

        if (txtConfirmPassword.getText() == null || txtConfirmPassword.getText().isEmpty()) {
            lblMessage.setText("⚠ Veuillez confirmer le mot de passe");
            return false;
        }

        if (!txtPassword.getText().equals(txtConfirmPassword.getText())) {
            lblMessage.setText("⚠ Les mots de passe ne correspondent pas");
            return false;
        }

        return true;
    }

    @FXML
    private void handleBackToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/voyages/voyages/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnRegister.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");

        } catch (Exception e) {
            lblMessage.setText("Erreur lors du retour à la connexion");
            e.printStackTrace();
        }
    }
}
