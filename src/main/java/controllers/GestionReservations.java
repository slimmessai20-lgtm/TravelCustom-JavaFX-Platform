package controllers;

import entities.Reservation;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import services.ReservationService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GestionReservations {

    @FXML private ComboBox<String> comboFiltre;
    @FXML private TableView<Reservation> tableViewReservations;
    @FXML private TableColumn<Reservation, Integer> colId;
    @FXML private TableColumn<Reservation, String> colClient;
    @FXML private TableColumn<Reservation, String> colDestination;
    @FXML private TableColumn<Reservation, String> colAgent;
    @FXML private TableColumn<Reservation, BigDecimal> colPrix;
    @FXML private TableColumn<Reservation, String> colStatut;
    @FXML private TableColumn<Reservation, LocalDateTime> colDateRes;

    private ReservationService reservationService;
    private ObservableList<Reservation> allReservations;

    public void initialize() {
        reservationService = new ReservationService();

        // Initialiser le filtre
        comboFiltre.getItems().addAll("Tous", "confirmee", "annulee", "terminee");
        comboFiltre.setValue("Tous");

        setupTable();
        loadReservations();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colClient.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getDemande().getClient().getNom()));
        colDestination.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getDemande().getDestination()));
        colAgent.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getAgent().getNom()));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prixTotal"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colDateRes.setCellValueFactory(new PropertyValueFactory<>("dateReservation"));

        // Style pour le statut
        colStatut.setCellFactory(column -> new TableCell<Reservation, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item.toLowerCase()) {
                        case "confirmee":
                            setStyle("-fx-background-color: #C8E6C9;");
                            break;
                        case "annulee":
                            setStyle("-fx-background-color: #FFCDD2;");
                            break;
                        case "terminee":
                            setStyle("-fx-background-color: #E0E0E0;");
                            break;
                    }
                }
            }
        });
    }

    private void loadReservations() {
        try {
            List<Reservation> reservations = reservationService.afficher();
            allReservations = FXCollections.observableArrayList(reservations);
            tableViewReservations.setItems(allReservations);
        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les réservations");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleActualiser(ActionEvent event) {
        loadReservations();
        comboFiltre.setValue("Tous");
    }

    @FXML
    private void handleFiltrer(ActionEvent event) {
        String filtre = comboFiltre.getValue();
        if (filtre == null || filtre.equals("Tous")) {
            tableViewReservations.setItems(allReservations);
        } else {
            ObservableList<Reservation> filtered = allReservations.stream()
                .filter(r -> r.getStatut().equalsIgnoreCase(filtre))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
            tableViewReservations.setItems(filtered);
        }
    }

    @FXML
    private void handleSupprimer(ActionEvent event) {
        Reservation selected = tableViewReservations.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner une réservation");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer la réservation");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cette réservation ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                reservationService.supprimer(selected.getId());
                loadReservations();
                showInfo("Succès", "Réservation supprimée avec succès");
            } catch (SQLException e) {
                showError("Erreur", "Impossible de supprimer la réservation");
                e.printStackTrace();
            }
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

