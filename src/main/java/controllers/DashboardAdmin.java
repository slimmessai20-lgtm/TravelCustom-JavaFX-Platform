package controllers;

import entities.Demande;
import entities.Reservation;
import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.DemandeService;
import services.ReservationService;
import services.UserService;
import utils.SessionManager;

import java.sql.SQLException;
import java.util.List;

public class DashboardAdmin {

    @FXML private Label lblNomAdmin;
    @FXML private Label lblTotalUtilisateurs;
    @FXML private Label lblTotalClients;
    @FXML private Label lblTotalAgents;
    @FXML private Label lblTotalDemandes;
    @FXML private Label lblDemandesEnAttente;
    @FXML private Label lblTotalReservations;

    private UserService userService;
    private DemandeService demandeService;
    private ReservationService reservationService;
    private User currentAdmin;

    public void initialize() {
        userService = new UserService();
        demandeService = new DemandeService();
        reservationService = new ReservationService();
        currentAdmin = SessionManager.getCurrentUser();

        if (currentAdmin != null) {
            lblNomAdmin.setText("Admin: " + currentAdmin.getNom());
        }

        loadStatistics();
    }

    private void loadStatistics() {
        try {
            // Statistiques utilisateurs
            List<User> allUsers = userService.afficher();
            lblTotalUtilisateurs.setText(String.valueOf(allUsers.size()));

            List<User> clients = userService.findAllClients();
            lblTotalClients.setText(String.valueOf(clients.size()));

            List<User> agents = userService.findAllUsers();
            lblTotalAgents.setText(String.valueOf(agents.size()));

            // Statistiques demandes
            List<Demande> allDemandes = demandeService.afficher();
            lblTotalDemandes.setText(String.valueOf(allDemandes.size()));

            long demandesEnAttente = allDemandes.stream()
                .filter(d -> d.getStatut().equalsIgnoreCase("en_attente"))
                .count();
            lblDemandesEnAttente.setText(String.valueOf(demandesEnAttente));

            // Statistiques réservations
            List<Reservation> allReservations = reservationService.afficher();
            lblTotalReservations.setText(String.valueOf(allReservations.size()));

        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les statistiques");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGestionUtilisateurs(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/voyages/voyages/GestionUtilisateurs.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Gestion des Utilisateurs");
            stage.setScene(new Scene(root, 900, 600));
            stage.showAndWait();

            loadStatistics();
        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir la gestion des utilisateurs");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGestionDemandes(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/voyages/voyages/GestionDemandes.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Gestion des Demandes");
            stage.setScene(new Scene(root, 1000, 600));
            stage.showAndWait();

            loadStatistics();
        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir la gestion des demandes");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGestionReservations(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/voyages/voyages/GestionReservations.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Gestion des Réservations");
            stage.setScene(new Scene(root, 1000, 600));
            stage.showAndWait();

            loadStatistics();
        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir la gestion des réservations");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleActualiser(ActionEvent event) {
        loadStatistics();
        showInfo("Actualisation", "Les statistiques ont été actualisées");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        SessionManager.clearSession();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/voyages/voyages/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) lblNomAdmin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
        } catch (Exception e) {
            showError("Erreur", "Impossible de retourner à la page de connexion");
            e.printStackTrace();
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
