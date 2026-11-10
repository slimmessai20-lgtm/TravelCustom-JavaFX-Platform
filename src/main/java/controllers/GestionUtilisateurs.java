package controllers;

import entities.Role;
import entities.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import services.UserService;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GestionUtilisateurs {

    @FXML private TextField txtRecherche;

    // Table tous
    @FXML private TableView<User> tableViewTous;
    @FXML private TableColumn<User, Integer> colTousId;
    @FXML private TableColumn<User, String> colTousNom;
    @FXML private TableColumn<User, String> colTousEmail;
    @FXML private TableColumn<User, Role> colTousRole;

    // Table clients
    @FXML private TableView<User> tableViewClients;
    @FXML private TableColumn<User, Integer> colClientId;
    @FXML private TableColumn<User, String> colClientNom;
    @FXML private TableColumn<User, String> colClientEmail;

    // Table agents
    @FXML private TableView<User> tableViewAgents;
    @FXML private TableColumn<User, Integer> colAgentId;
    @FXML private TableColumn<User, String> colAgentNom;
    @FXML private TableColumn<User, String> colAgentEmail;

    // Table admins
    @FXML private TableView<User> tableViewAdmins;
    @FXML private TableColumn<User, Integer> colAdminId;
    @FXML private TableColumn<User, String> colAdminNom;
    @FXML private TableColumn<User, String> colAdminEmail;

    private UserService userService;
    private ObservableList<User> allUsers;

    public void initialize() {
        userService = new UserService();

        setupTables();
        loadAllUsers();
    }

    private void setupTables() {
        // Table tous
        colTousId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTousNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colTousEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTousRole.setCellValueFactory(new PropertyValueFactory<>("roles"));

        // Table clients
        colClientId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colClientNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colClientEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Table agents
        colAgentId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAgentNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colAgentEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Table admins
        colAdminId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAdminNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colAdminEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
    }

    private void loadAllUsers() {
        try {
            List<User> users = userService.afficher();
            allUsers = FXCollections.observableArrayList(users);

            tableViewTous.setItems(allUsers);

            List<User> clients = userService.findAllClients();
            tableViewClients.setItems(FXCollections.observableArrayList(clients));

            List<User> agents = userService.findAllUsers();
            tableViewAgents.setItems(FXCollections.observableArrayList(agents));

            List<User> admins = userService.findAllAdmins();
            tableViewAdmins.setItems(FXCollections.observableArrayList(admins));

        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les utilisateurs");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNouvelUtilisateur(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/voyages/voyages/FormulaireUtilisateur.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Nouvel Utilisateur");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadAllUsers();
        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir le formulaire");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleModifier(ActionEvent event) {
        User selected = getSelectedUser();
        if (selected == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner un utilisateur");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/voyages/voyages/FormulaireUtilisateur.fxml"));
            Parent root = loader.load();

            FormulaireUtilisateur controller = loader.getController();
            controller.setUser(selected);

            Stage stage = new Stage();
            stage.setTitle("Modifier l'utilisateur");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadAllUsers();
        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir le formulaire");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSupprimer(ActionEvent event) {
        User selected = getSelectedUser();
        if (selected == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner un utilisateur");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer l'utilisateur");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer " + selected.getNom() + " ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                userService.supprimer(selected.getId());
                loadAllUsers();
                showInfo("Succès", "Utilisateur supprimé avec succès");
            } catch (SQLException e) {
                showError("Erreur", "Impossible de supprimer l'utilisateur");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleActualiser(ActionEvent event) {
        loadAllUsers();
        showInfo("Actualisation", "La liste a été actualisée");
    }

    @FXML
    private void handleRecherche(KeyEvent event) {
        String recherche = txtRecherche.getText().toLowerCase();
        if (recherche.isEmpty()) {
            tableViewTous.setItems(allUsers);
        } else {
            ObservableList<User> filtered = allUsers.stream()
                .filter(u -> u.getNom().toLowerCase().contains(recherche) ||
                            u.getEmail().toLowerCase().contains(recherche))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
            tableViewTous.setItems(filtered);
        }
    }

    private User getSelectedUser() {
        User selected = tableViewTous.getSelectionModel().getSelectedItem();
        if (selected == null) selected = tableViewClients.getSelectionModel().getSelectedItem();
        if (selected == null) selected = tableViewAgents.getSelectionModel().getSelectedItem();
        if (selected == null) selected = tableViewAdmins.getSelectionModel().getSelectedItem();
        return selected;
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

