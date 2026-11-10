package controllers;

import entities.Demande;
import entities.Reservation;
import entities.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import services.DemandeService;
import services.ReservationService;
import utils.SessionManager;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class DashboardClient {

    @FXML private Label lblNomClient;
    @FXML private TableView<Demande> tableViewDemandes;
    @FXML private TableColumn<Demande, Integer> colId;
    @FXML private TableColumn<Demande, String> colDestination;
    @FXML private TableColumn<Demande, LocalDate> colDateDebut;
    @FXML private TableColumn<Demande, LocalDate> colDateFin;
    @FXML private TableColumn<Demande, String> colStatut;
    @FXML private TableColumn<Demande, String> colAgent;
    @FXML private TableColumn<Demande, LocalDateTime> colDateCreation;

    @FXML private TableView<Reservation> tableViewReservations;
    @FXML private TableColumn<Reservation, Integer> colResId;
    @FXML private TableColumn<Reservation, String> colResDestination;
    @FXML private TableColumn<Reservation, String> colResAgent;
    @FXML private TableColumn<Reservation, String> colResStatut;
    @FXML private TableColumn<Reservation, LocalDateTime> colResDate;
    @FXML private TableColumn<Reservation, java.math.BigDecimal> colResPrix;

    private DemandeService demandeService;
    private ReservationService reservationService;
    private User currentUser;

    public void initialize() {
        demandeService = new DemandeService();
        reservationService = new ReservationService();
        currentUser = SessionManager.getCurrentUser();

        if (currentUser != null) {
            lblNomClient.setText("Bienvenue, " + currentUser.getNom());
        }

        setupTableColumns();
        setupReservationTableColumns();
        loadDemandes();
        loadReservations();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDestination.setCellValueFactory(new PropertyValueFactory<>("destination"));
        colDateDebut.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        colDateFin.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Colonne agent avec gestion du null
        colAgent.setCellValueFactory(cellData -> {
            User agent = cellData.getValue().getAgent();
            return new javafx.beans.property.SimpleStringProperty(
                agent != null ? agent.getNom() : "Non assigné"
            );
        });

        colDateCreation.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));

        // Style pour le statut
        colStatut.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item.toLowerCase()) {
                        case "en_attente":
                            setStyle("-fx-background-color: #FFF9C4; -fx-text-fill: #F57F17;");
                            break;
                        case "en_cours":
                            setStyle("-fx-background-color: #BBDEFB; -fx-text-fill: #1565C0;");
                            break;
                        case "validee":
                            setStyle("-fx-background-color: #C8E6C9; -fx-text-fill: #2E7D32;");
                            break;
                        case "refusee":
                            setStyle("-fx-background-color: #FFCDD2; -fx-text-fill: #C62828;");
                            break;
                    }
                }
            }
        });
    }

    private void setupReservationTableColumns() {
        if (tableViewReservations == null) {
            return; // si FXML pas encore mis à jour
        }
        colResId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colResPrix.setCellValueFactory(new PropertyValueFactory<>("prixTotal"));
        colResStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colResDate.setCellValueFactory(new PropertyValueFactory<>("dateReservation"));

        colResDestination.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDemande() != null ? cellData.getValue().getDemande().getDestination() : "-"));
        colResAgent.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getAgent() != null ? cellData.getValue().getAgent().getNom() : "-"));
    }

    private void loadDemandes() {
        try {
            if (currentUser != null) {
                List<Demande> demandes = demandeService.findByClientId(currentUser.getId());
                ObservableList<Demande> demandesList = FXCollections.observableArrayList(demandes);
                tableViewDemandes.setItems(demandesList);
            }
        } catch (SQLException e) {
            showError("Erreur lors du chargement des demandes", e.getMessage());
        }
    }

    private void loadReservations() {
        try {
            if (currentUser != null && tableViewReservations != null) {
                List<Reservation> reservations = reservationService.findByClientId(currentUser.getId());
                tableViewReservations.setItems(FXCollections.observableArrayList(reservations));
            }
        } catch (SQLException e) {
            showError("Erreur lors du chargement des réservations", e.getMessage());
        }
    }

    @FXML
    private void handleNouvelleDemande(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/voyages/voyages/NouvelleDemande.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Nouvelle Demande de Voyage");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Recharger les demandes après fermeture
            loadDemandes();
        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir le formulaire de demande");
        }
    }

    @FXML
    private void handlePayerReservation(ActionEvent event) {
        Reservation selected = tableViewReservations.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner une réservation");
            return;
        }

        // Simple règle: uniquement si confirmee
        if (!"confirmee".equalsIgnoreCase(selected.getStatut())) {
            showWarning("Paiement impossible", "Le paiement est disponible uniquement pour les réservations confirmées");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/voyages/voyages/PaiementReservation.fxml"));
            Parent root = loader.load();

            PaiementReservation controller = loader.getController();
            controller.setReservation(selected);

            Stage stage = new Stage();
            stage.setTitle("Paiement réservation");
            stage.setScene(new Scene(root, 520, 320));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir l'écran de paiement\n" + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    @FXML
    private void handleActualiser(ActionEvent event) {
        loadDemandes();
        loadReservations();
        showInfo("Actualisation", "Les listes ont été actualisées");
    }

    @FXML
    private void handleVoirDetails(ActionEvent event) {
        Demande selected = tableViewDemandes.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner une demande");
            return;
        }

        // Afficher les détails dans une boîte de dialogue
        String details = "ID: " + selected.getId() + "\n" +
                "Destination: " + selected.getDestination() + "\n" +
                "Date début: " + selected.getDateDebut() + "\n" +
                "Date fin: " + selected.getDateFin() + "\n" +
                "Préférences: " + (selected.getPreferences() != null ? selected.getPreferences() : "Aucune") + "\n" +
                "Statut: " + selected.getStatut() + "\n" +
                "Agent assigné: " + (selected.getAgent() != null ? selected.getAgent().getNom() : "Non assigné") + "\n" +
                "Date de création: " + selected.getDateCreation();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de la demande");
        alert.setHeaderText("Demande #" + selected.getId());
        alert.setContentText(details);
        alert.showAndWait();
    }

    @FXML
    private void handleSupprimerDemande(ActionEvent event) {
        Demande selected = tableViewDemandes.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner une demande à supprimer");
            return;
        }

        // Ne permettre la suppression que si la demande est en attente
        if (!selected.getStatut().equalsIgnoreCase("en_attente")) {
            showWarning("Suppression impossible", "Vous ne pouvez supprimer que les demandes en attente");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer la demande");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cette demande ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                demandeService.supprimer(selected.getId());
                loadDemandes();
                showInfo("Succès", "Demande supprimée avec succès");
            } catch (SQLException e) {
                showError("Erreur", "Impossible de supprimer la demande");
            }
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        SessionManager.clearSession();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/voyages/voyages/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) lblNomClient.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
        } catch (Exception e) {
            showError("Erreur", "Impossible de retourner à la page de connexion");
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
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
