package controllers;

import entities.Role;
import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.UserService;

import java.sql.SQLException;

public class FormulaireUtilisateur {

    @FXML private Label lblTitre;
    @FXML private TextField txtNom;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<Role> comboRole;
    @FXML private Label lblMessage;

    private UserService userService;
    private User userToEdit;
    private boolean isEditMode = false;

    public void initialize() {
        userService = new UserService();

        // Initialiser les rôles
        comboRole.getItems().addAll(Role.ROLE_CLIENT, Role.ROLE_USER, Role.ROLE_ADMIN);
        comboRole.setValue(Role.ROLE_CLIENT);
    }

    public void setUser(User user) {
        this.userToEdit = user;
        this.isEditMode = true;

        lblTitre.setText("Modifier l'utilisateur");
        txtNom.setText(user.getNom());
        txtEmail.setText(user.getEmail());
        txtPassword.setPromptText("Laisser vide pour ne pas changer");
        comboRole.setValue(user.getRoles());
    }

    @FXML
    private void handleEnregistrer(ActionEvent event) {
        lblMessage.setText("");

        if (!validerFormulaire()) {
            return;
        }

        try {
            if (isEditMode) {
                // Modification
                userToEdit.setNom(txtNom.getText().trim());
                userToEdit.setEmail(txtEmail.getText().trim());

                // Mettre à jour le mot de passe seulement s'il est fourni
                if (!txtPassword.getText().isEmpty()) {
                    userToEdit.setPassword(txtPassword.getText());
                }

                userToEdit.setRoles(comboRole.getValue());

                userService.modifier(userToEdit);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("Utilisateur modifié avec succès !");
                alert.showAndWait();

            } else {
                // Création
                User newUser = new User();
                newUser.setNom(txtNom.getText().trim());
                newUser.setEmail(txtEmail.getText().trim());
                newUser.setPassword(txtPassword.getText());
                newUser.setRoles(comboRole.getValue());

                userService.ajouter(newUser);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("Utilisateur créé avec succès !");
                alert.showAndWait();
            }

            handleAnnuler(event);

        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate")) {
                lblMessage.setText("⚠ Cet email existe déjà");
            } else {
                lblMessage.setText("Erreur lors de l'enregistrement");
            }
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

        // Validation du mot de passe (seulement en mode création ou si le champ est rempli)
        if (!isEditMode || !txtPassword.getText().isEmpty()) {
            if (txtPassword.getText().isEmpty()) {
                lblMessage.setText("⚠ Le mot de passe est obligatoire");
                return false;
            }

            if (txtPassword.getText().length() < 6) {
                lblMessage.setText("⚠ Le mot de passe doit contenir au moins 6 caractères");
                return false;
            }
        }

        if (comboRole.getValue() == null) {
            lblMessage.setText("⚠ Veuillez sélectionner un rôle");
            return false;
        }

        return true;
    }

    @FXML
    private void handleAnnuler(ActionEvent event) {
        Stage stage = (Stage) txtNom.getScene().getWindow();
        stage.close();
    }
}

