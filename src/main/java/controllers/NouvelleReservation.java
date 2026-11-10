package controllers;

import entities.Demande;
import entities.Reservation;
import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.EmailService;
import services.ReservationService;
import utils.SessionManager;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class NouvelleReservation {

    @FXML private Label lblDemandeInfo;
    @FXML private TextField txtPrixTotal;
    @FXML private ComboBox<String> comboStatut;
    @FXML private Label lblMessage;

    private ReservationService reservationService;
    private Demande demande;
    private User currentAgent;
    private final EmailService emailService = new EmailService(
            System.getenv().getOrDefault("SMTP_EMAIL", "your.email@gmail.com"),
            System.getenv().getOrDefault("SMTP_PASSWORD", "your_app_password_here")
    );

    public void initialize() {
        reservationService = new ReservationService();
        currentAgent = SessionManager.getCurrentUser();

        // Initialiser les statuts
        comboStatut.getItems().addAll("confirmee", "annulee", "terminee");
        comboStatut.setValue("confirmee");
    }

    public void setDemande(Demande demande) {
        this.demande = demande;

        // Afficher les informations de la demande
        String info = String.format(
            "Client: %s\nDestination: %s\nDu %s au %s",
            demande.getClient().getNom(),
            demande.getDestination(),
            demande.getDateDebut(),
            demande.getDateFin()
        );
        lblDemandeInfo.setText(info);
    }

    @FXML
    private void handleCreer(ActionEvent event) {
        lblMessage.setText("");

        if (!validerFormulaire()) {
            return;
        }

        try {
            BigDecimal prix = new BigDecimal(txtPrixTotal.getText().trim());

            Reservation reservation = new Reservation();
            reservation.setPrixTotal(prix);
            reservation.setStatut(comboStatut.getValue());
            reservation.setDateReservation(LocalDateTime.now());
            reservation.setDemande(demande);
            reservation.setAgent(currentAgent);

            reservationService.ajouter(reservation);

            // Email client
            if (demande != null && demande.getClient() != null) {
                emailService.sendText(
                        demande.getClient().getEmail(),
                        "Réservation créée (#" + reservation.getId() + ")",
                        "Bonjour " + demande.getClient().getNom() + ",\n\nUne réservation a été créée pour votre demande (destination: " + demande.getDestination() + ")." +
                                "\nMontant: " + reservation.getPrixTotal().toPlainString() + " EUR" +
                                "\nStatut: " + reservation.getStatut() +
                                "\n\nVous pouvez procéder au paiement depuis votre tableau de bord (onglet Mes réservations)." +
                                "\n\nCordialement,\nVoyages"
                );
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("La réservation a été créée avec succès !");
            alert.showAndWait();

            handleAnnuler(event);

        } catch (NumberFormatException e) {
            lblMessage.setText("⚠ Le prix doit être un nombre valide");
        } catch (SQLException e) {
            lblMessage.setText("Erreur lors de la création de la réservation");
            e.printStackTrace();
        }
    }

    private boolean validerFormulaire() {
        if (txtPrixTotal.getText() == null || txtPrixTotal.getText().trim().isEmpty()) {
            lblMessage.setText("⚠ Le prix total est obligatoire");
            return false;
        }

        try {
            BigDecimal prix = new BigDecimal(txtPrixTotal.getText().trim());
            if (prix.compareTo(BigDecimal.ZERO) <= 0) {
                lblMessage.setText("⚠ Le prix doit être positif");
                return false;
            }
        } catch (NumberFormatException e) {
            lblMessage.setText("⚠ Le prix doit être un nombre valide");
            return false;
        }

        if (comboStatut.getValue() == null) {
            lblMessage.setText("⚠ Veuillez sélectionner un statut");
            return false;
        }

        return true;
    }

    @FXML
    private void handleAnnuler(ActionEvent event) {
        Stage stage = (Stage) txtPrixTotal.getScene().getWindow();
        stage.close();
    }
}
