package controllers;

import entities.Demande;
import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.DemandeService;
import utils.SessionManager;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class NouvelleDemande {

    @FXML private TextField txtDestination;
    @FXML private DatePicker dateDebut;
    @FXML private DatePicker dateFin;
    @FXML private TextArea txtPreferences;
    @FXML private Label lblMessage;

    private DemandeService demandeService;
    private User currentUser;

    public void initialize() {
        demandeService = new DemandeService();
        currentUser = SessionManager.getCurrentUser();
    }

    @FXML
    private void handleSoumettre(ActionEvent event) {
        lblMessage.setText("");

        // Validation
        if (!validerFormulaire()) {
            return;
        }

        try {
            Demande demande = new Demande();
            demande.setDestination(txtDestination.getText().trim());
            demande.setDateDebut(dateDebut.getValue());
            demande.setDateFin(dateFin.getValue());
            demande.setPreferences(txtPreferences.getText().trim());
            demande.setStatut("en_attente");
            demande.setDateCreation(LocalDateTime.now());
            demande.setClient(currentUser);
            demande.setAgent(null); // Pas encore assigné

            demandeService.ajouter(demande);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Votre demande de voyage a été soumise avec succès !\nVous serez notifié lorsqu'un agent la prendra en charge.");
            alert.showAndWait();

            // Fermer la fenêtre
            handleAnnuler(event);

        } catch (SQLException e) {
            lblMessage.setText("Erreur lors de la soumission de la demande");
            e.printStackTrace();
        }
    }

    private boolean validerFormulaire() {
        if (txtDestination.getText() == null || txtDestination.getText().trim().isEmpty()) {
            lblMessage.setText("⚠ La destination est obligatoire");
            return false;
        }

        if (dateDebut.getValue() == null) {
            lblMessage.setText("⚠ La date de début est obligatoire");
            return false;
        }

        if (dateFin.getValue() == null) {
            lblMessage.setText("⚠ La date de fin est obligatoire");
            return false;
        }

        if (dateDebut.getValue().isBefore(LocalDate.now())) {
            lblMessage.setText("⚠ La date de début doit être dans le futur");
            return false;
        }

        if (dateFin.getValue().isBefore(dateDebut.getValue())) {
            lblMessage.setText("⚠ La date de fin doit être après la date de début");
            return false;
        }

        if (txtDestination.getText().trim().length() < 3) {
            lblMessage.setText("⚠ La destination doit contenir au moins 3 caractères");
            return false;
        }

        return true;
    }

    @FXML
    private void handleAnnuler(ActionEvent event) {
        Stage stage = (Stage) txtDestination.getScene().getWindow();
        stage.close();
    }
}

