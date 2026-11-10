package services;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import entities.Reservation;
import entities.Demande;
import entities.User;
import utils.MyDatabase;

/**
 * Service pour la table reservation.
 * Implémente les opérations CRUD et quelques méthodes utilitaires.
 */
public class ReservationService implements IService<Reservation> {

    private Connection cnx;
    private DemandeService demandeService;
    private UserService userService;

    public ReservationService() {
        cnx = MyDatabase.getInstance().getConnection();
        demandeService = new DemandeService();
        userService = new UserService();
    }

    @Override
    public void ajouter(Reservation r) throws SQLException {
        String req = "INSERT INTO reservation (date_reservation, prix_total, statut, demande_id, agent_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);

        ps.setTimestamp(1, Timestamp.valueOf(r.getDateReservation() != null ? r.getDateReservation() : LocalDateTime.now()));
        ps.setBigDecimal(2, r.getPrixTotal());
        ps.setString(3, r.getStatut());
        ps.setInt(4, r.getDemande().getId());
        ps.setInt(5, r.getAgent().getId());

        ps.executeUpdate();
        ResultSet keys = ps.getGeneratedKeys();
        if (keys.next()) {
            r.setId(keys.getInt(1));
        }
        ps.close();
        System.out.println("Reservation ajoutée avec succès !");
    }

    @Override
    public void modifier(Reservation r) throws SQLException {
        String req = "UPDATE reservation SET date_reservation=?, prix_total=?, statut=?, demande_id=?, agent_id=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);

        ps.setTimestamp(1, Timestamp.valueOf(r.getDateReservation()));
        ps.setBigDecimal(2, r.getPrixTotal());
        ps.setString(3, r.getStatut());
        ps.setInt(4, r.getDemande().getId());
        ps.setInt(5, r.getAgent().getId());
        ps.setInt(6, r.getId());

        ps.executeUpdate();
        ps.close();
        System.out.println("Reservation modifiée avec succès !");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM reservation WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
        System.out.println("Reservation supprimée avec succès !");
    }

    @Override
    public List<Reservation> afficher() throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String req = "SELECT * FROM reservation";
        PreparedStatement ps = cnx.prepareStatement(req);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(mapResultSetToReservation(rs));
        }
        rs.close();
        ps.close();
        return list;
    }

    public Reservation findById(int id) throws SQLException {
        String req = "SELECT * FROM reservation WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            Reservation r = mapResultSetToReservation(rs);
            rs.close();
            ps.close();
            return r;
        }
        rs.close();
        ps.close();
        return null;
    }

    public List<Reservation> findByAgentId(int agentId) throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String req = "SELECT * FROM reservation WHERE agent_id = ?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, agentId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(mapResultSetToReservation(rs));
        }
        rs.close();
        ps.close();
        return list;
    }

    public List<Reservation> findByDemandeId(int demandeId) throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String req = "SELECT * FROM reservation WHERE demande_id = ?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, demandeId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(mapResultSetToReservation(rs));
        }
        rs.close();
        ps.close();
        return list;
    }

    public List<Reservation> findByClientId(int clientId) throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String req = "SELECT r.* FROM reservation r JOIN demande d ON r.demande_id = d.id WHERE d.client_id = ?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, clientId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(mapResultSetToReservation(rs));
        }
        rs.close();
        ps.close();
        return list;
    }

    private Reservation mapResultSetToReservation(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setId(rs.getInt("id"));

        Timestamp tr = rs.getTimestamp("date_reservation");
        if (tr != null) r.setDateReservation(tr.toLocalDateTime());

        BigDecimal prix = rs.getBigDecimal("prix_total");
        r.setPrixTotal(prix);

        r.setStatut(rs.getString("statut"));

        int demandeId = rs.getInt("demande_id");
        if (!rs.wasNull()) {
            Demande d = demandeService.findById(demandeId);
            r.setDemande(d);
        }

        int agentId = rs.getInt("agent_id");
        if (!rs.wasNull()) {
            User agent = userService.findById(agentId);
            r.setAgent(agent);
        }

        return r;
    }
}