package controllers;

import entities.Demande;
import entities.Reservation;
import entities.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import services.DemandeService;
import services.EmailService;
import services.ReservationService;
import utils.SessionManager;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class DashboardAgent {

    @FXML private Label lblNomAgent;
    @FXML private Label lblTotalDemandes;
    @FXML private Label lblDemandesEnCours;
    @FXML private Label lblReservations;

    // Table demandes en attente
    @FXML private TableView<Demande> tableViewAttente;
    @FXML private TableColumn<Demande, Integer> colAttenteId;
    @FXML private TableColumn<Demande, String> colAttenteDestination;
    @FXML private TableColumn<Demande, String> colAttenteClient;
    @FXML private TableColumn<Demande, LocalDate> colAttenteDateDebut;
    @FXML private TableColumn<Demande, LocalDate> colAttenteDateFin;
    @FXML private TableColumn<Demande, String> colAttentePreferences;
    @FXML private TableColumn<Demande, LocalDateTime> colAttenteDateCreation;

    // Table mes demandes
    @FXML private TableView<Demande> tableViewMesDemandes;
    @FXML private TableColumn<Demande, Integer> colMesId;
    @FXML private TableColumn<Demande, String> colMesDestination;
    @FXML private TableColumn<Demande, String> colMesClient;
    @FXML private TableColumn<Demande, LocalDate> colMesDateDebut;
    @FXML private TableColumn<Demande, LocalDate> colMesDateFin;
    @FXML private TableColumn<Demande, String> colMesStatut;
    @FXML private TableColumn<Demande, String> colMesReservation;

    // Table réservations
    @FXML private TableView<Reservation> tableViewReservations;
    @FXML private TableColumn<Reservation, Integer> colResId;
    @FXML private TableColumn<Reservation, String> colResClient;
    @FXML private TableColumn<Reservation, String> colResDestination;
    @FXML private TableColumn<Reservation, BigDecimal> colResPrix;
    @FXML private TableColumn<Reservation, String> colResStatut;
    @FXML private TableColumn<Reservation, LocalDateTime> colResDateRes;

    private DemandeService demandeService;
    private ReservationService reservationService;
    private User currentAgent;
    private final EmailService emailService = new EmailService(
            System.getenv().getOrDefault("SMTP_EMAIL", "your.email@gmail.com"),
            System.getenv().getOrDefault("SMTP_PASSWORD", "your_app_password_here")
    );

    public void initialize() {
        demandeService = new DemandeService();
        reservationService = new ReservationService();
        currentAgent = SessionManager.getCurrentUser();

        if (currentAgent != null) {
            lblNomAgent.setText("Agent: " + currentAgent.getNom());
        }

        setupTables();
        loadAllData();
        updateStatistics();
    }

    private void setupTables() {
        // Table attente
        colAttenteId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAttenteDestination.setCellValueFactory(new PropertyValueFactory<>("destination"));
        colAttenteClient.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getClient().getNom()));
        colAttenteDateDebut.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        colAttenteDateFin.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        colAttentePreferences.setCellValueFactory(new PropertyValueFactory<>("preferences"));
        colAttenteDateCreation.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));

        // Table mes demandes
        colMesId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colMesDestination.setCellValueFactory(new PropertyValueFactory<>("destination"));
        colMesClient.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getClient().getNom()));
        colMesDateDebut.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        colMesDateFin.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        colMesStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colMesReservation.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getReservation() != null ? "Oui" : "Non"));

        // Style pour le statut
        colMesStatut.setCellFactory(column -> new TableCell<Demande, String>() {
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
                            setStyle("-fx-background-color: #FFF9C4;");
                            break;
                        case "en_cours":
                            setStyle("-fx-background-color: #BBDEFB;");
                            break;
                        case "validee":
                            setStyle("-fx-background-color: #C8E6C9;");
                            break;
                        case "refusee":
                            setStyle("-fx-background-color: #FFCDD2;");
                            break;
                    }
                }
            }
        });

        // Table réservations
        colResId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colResClient.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getDemande().getClient().getNom()));
        colResDestination.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getDemande().getDestination()));
        colResPrix.setCellValueFactory(new PropertyValueFactory<>("prixTotal"));
        colResStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colResDateRes.setCellValueFactory(new PropertyValueFactory<>("dateReservation"));
    }

    private void loadAllData() {
        loadDemandesAttente();
        loadMesDemandes();
        loadMesReservations();
    }

    private void loadDemandesAttente() {
        try {
            List<Demande> demandes = demandeService.afficher();
            List<Demande> demandesEnAttente = demandes.stream()
                .filter(d -> d.getStatut().equalsIgnoreCase("en_attente") && d.getAgent() == null)
                .toList();
            tableViewAttente.setItems(FXCollections.observableArrayList(demandesEnAttente));
        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les demandes en attente");
        }
    }

    private void loadMesDemandes() {
        try {
            if (currentAgent != null) {
                List<Demande> demandes = demandeService.findByAgentId(currentAgent.getId());
                tableViewMesDemandes.setItems(FXCollections.observableArrayList(demandes));
            }
        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger vos demandes");
        }
    }

    private void loadMesReservations() {
        try {
            if (currentAgent != null) {
                List<Reservation> reservations = reservationService.findByAgentId(currentAgent.getId());
                tableViewReservations.setItems(FXCollections.observableArrayList(reservations));
            }
        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger vos réservations");
        }
    }

    private void updateStatistics() {
        try {
            if (currentAgent != null) {
                List<Demande> mesDemandes = demandeService.findByAgentId(currentAgent.getId());
                lblTotalDemandes.setText(String.valueOf(mesDemandes.size()));

                long enCours = mesDemandes.stream()
                    .filter(d -> d.getStatut().equalsIgnoreCase("en_cours"))
                    .count();
                lblDemandesEnCours.setText(String.valueOf(enCours));

                List<Reservation> mesReservations = reservationService.findByAgentId(currentAgent.getId());
                lblReservations.setText(String.valueOf(mesReservations.size()));
            }
        } catch (SQLException e) {
            // Log silently
        }
    }

    @FXML
    private void handleActualiserAttente(ActionEvent event) {
        loadDemandesAttente();
    }

    @FXML
    private void handleActualiserMesDemandes(ActionEvent event) {
        loadMesDemandes();
        updateStatistics();
    }

    @FXML
    private void handleActualiserReservations(ActionEvent event) {
        loadMesReservations();
        updateStatistics();
    }

    @FXML
    private void handlePrendreEnCharge(ActionEvent event) {
        Demande selected = tableViewAttente.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner une demande");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Prendre en charge cette demande");
        confirmation.setContentText("Voulez-vous prendre en charge la demande #" + selected.getId() + " ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                selected.setAgent(currentAgent);
                selected.setStatut("en_cours");
                demandeService.modifier(selected);

                // Email client
                if (selected.getClient() != null) {
                    emailService.sendText(
                            selected.getClient().getEmail(),
                            "Votre demande est prise en charge (#" + selected.getId() + ")",
                            "Bonjour " + selected.getClient().getNom() + ",\n\nVotre demande de voyage pour " + selected.getDestination() + " est maintenant en cours de traitement." +
                                    "\nAgent: " + (currentAgent != null ? currentAgent.getNom() : "-") +
                                    "\n\nCordialement,\nVoyages"
                    );
                }

                loadDemandesAttente();
                loadMesDemandes();
                updateStatistics();

                showInfo("Succès", "Demande prise en charge avec succès");
            } catch (SQLException e) {
                showError("Erreur", "Impossible de prendre en charge la demande");
            }
        }
    }

    @FXML
    private void handleModifierStatut(ActionEvent event) {
        Demande selected = tableViewMesDemandes.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner une demande");
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>("en_cours",
            "en_attente", "en_cours", "validee", "refusee");
        dialog.setTitle("Modifier le statut");
        dialog.setHeaderText("Demande #" + selected.getId());
        dialog.setContentText("Nouveau statut:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                String newStatut = result.get();
                selected.setStatut(newStatut);
                demandeService.modifier(selected);

                // Email client
                if (selected.getClient() != null) {
                    emailService.sendText(
                            selected.getClient().getEmail(),
                            "Mise à jour du statut de votre demande (#" + selected.getId() + ")",
                            "Bonjour " + selected.getClient().getNom() + ",\n\nLe statut de votre demande pour " + selected.getDestination() + " a changé: " + newStatut + "." +
                                    "\n\nCordialement,\nVoyages"
                    );
                }

                loadMesDemandes();
                updateStatistics();
                showInfo("Succès", "Statut modifié avec succès");
            } catch (SQLException e) {
                showError("Erreur", "Impossible de modifier le statut");
            }
        }
    }

    @FXML
    private void handleCreerReservation(ActionEvent event) {
        Demande selected = tableViewMesDemandes.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner une demande");
            return;
        }

        if (selected.getReservation() != null) {
            showWarning("Réservation existante", "Cette demande a déjà une réservation");
            return;
        }

        if (!selected.getStatut().equalsIgnoreCase("validee")) {
            showWarning("Statut invalide", "La demande doit être validée avant de créer une réservation");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/voyages/voyages/NouvelleReservation.fxml"));
            Parent root = loader.load();

            NouvelleReservation controller = loader.getController();
            controller.setDemande(selected);

            Stage stage = new Stage();
            stage.setTitle("Nouvelle Réservation");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadMesDemandes();
            loadMesReservations();
            updateStatistics();
        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir le formulaire de réservation");
        }
    }

    @FXML
    private void handleModifierStatutReservation(ActionEvent event) {
        Reservation selected = tableViewReservations.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner une réservation");
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>("confirmee",
            "confirmee", "annulee", "terminee");
        dialog.setTitle("Modifier le statut");
        dialog.setHeaderText("Réservation #" + selected.getId());
        dialog.setContentText("Nouveau statut:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                String newStatut = result.get();
                selected.setStatut(newStatut);
                reservationService.modifier(selected);

                // Email client
                if (selected.getDemande() != null && selected.getDemande().getClient() != null) {
                    emailService.sendText(
                            selected.getDemande().getClient().getEmail(),
                            "Statut de votre réservation (#" + selected.getId() + ")",
                            "Bonjour " + selected.getDemande().getClient().getNom() + ",\n\nLe statut de votre réservation pour " + selected.getDemande().getDestination() + " est maintenant: " + newStatut + "." +
                                    "\n\nCordialement,\nVoyages"
                    );
                }

                loadMesReservations();
                showInfo("Succès", "Statut de la réservation modifié");
            } catch (SQLException e) {
                showError("Erreur", "Impossible de modifier le statut");
            }
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        SessionManager.clearSession();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/voyages/voyages/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) lblNomAgent.getScene().getWindow();
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
