package controllers;

import entities.Demande;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import services.DemandeService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GestionDemandes {

    @FXML private ComboBox<String> comboFiltre;
    @FXML private TableView<Demande> tableViewDemandes;
    @FXML private TableColumn<Demande, Integer> colId;
    @FXML private TableColumn<Demande, String> colDestination;
    @FXML private TableColumn<Demande, String> colClient;
    @FXML private TableColumn<Demande, String> colAgent;
    @FXML private TableColumn<Demande, LocalDate> colDateDebut;
    @FXML private TableColumn<Demande, LocalDate> colDateFin;
    @FXML private TableColumn<Demande, String> colStatut;
    @FXML private TableColumn<Demande, LocalDateTime> colDateCreation;

    private DemandeService demandeService;
    private ObservableList<Demande> allDemandes;

    public void initialize() {
        demandeService = new DemandeService();

        // Initialiser le filtre
        comboFiltre.getItems().addAll("Tous", "en_attente", "en_cours", "validee", "refusee");
        comboFiltre.setValue("Tous");

        setupTable();
        loadDemandes();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDestination.setCellValueFactory(new PropertyValueFactory<>("destination"));
        colClient.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getClient().getNom()));
        colAgent.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getAgent() != null ?
                cellData.getValue().getAgent().getNom() : "Non assigné"));
        colDateDebut.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        colDateFin.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colDateCreation.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));

        // Style pour le statut
        colStatut.setCellFactory(column -> new TableCell<Demande, String>() {
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
    }

    private void loadDemandes() {
        try {
            List<Demande> demandes = demandeService.afficher();
            allDemandes = FXCollections.observableArrayList(demandes);
            tableViewDemandes.setItems(allDemandes);
        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les demandes");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleActualiser(ActionEvent event) {
        loadDemandes();
        comboFiltre.setValue("Tous");
    }

    @FXML
    private void handleFiltrer(ActionEvent event) {
        String filtre = comboFiltre.getValue();
        if (filtre == null || filtre.equals("Tous")) {
            tableViewDemandes.setItems(allDemandes);
        } else {
            ObservableList<Demande> filtered = allDemandes.stream()
                .filter(d -> d.getStatut().equalsIgnoreCase(filtre))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
            tableViewDemandes.setItems(filtered);
        }
    }

    @FXML
    private void handleSupprimer(ActionEvent event) {
        Demande selected = tableViewDemandes.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner une demande");
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

