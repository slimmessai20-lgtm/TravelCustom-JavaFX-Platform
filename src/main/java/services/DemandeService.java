package services;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import entities.Demande;
import entities.User;
import utils.MyDatabase;


public class DemandeService implements IService<Demande> {

    private Connection cnx;
    private UserService userService;

    public DemandeService() {
        cnx = MyDatabase.getInstance().getConnection();
        userService = new UserService();
    }

    @Override
    public void ajouter(Demande d) throws SQLException {
        String req = "INSERT INTO demande (destination, date_debut, date_fin, preferences, statut, date_creation, client_id, agent_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);

        ps.setString(1, d.getDestination());
        ps.setDate(2, Date.valueOf(d.getDateDebut()));
        ps.setDate(3, Date.valueOf(d.getDateFin()));
        ps.setString(4, d.getPreferences());
        ps.setString(5, d.getStatut());
        ps.setTimestamp(6, Timestamp.valueOf(d.getDateCreation()));
        ps.setInt(7, d.getClient().getId());
        if (d.getAgent() != null) ps.setInt(8, d.getAgent().getId());
        else ps.setNull(8, Types.INTEGER);

        ps.executeUpdate();
        ResultSet keys = ps.getGeneratedKeys();
        if (keys.next()) {
            d.setId(keys.getInt(1));
        }
        ps.close();
        System.out.println("Demande ajoutée avec succès !");
    }

    @Override
    public void modifier(Demande d) throws SQLException {
        String req = "UPDATE demande SET destination=?, date_debut=?, date_fin=?, preferences=?, statut=?, date_creation=?, client_id=?, agent_id=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);

        ps.setString(1, d.getDestination());
        ps.setDate(2, Date.valueOf(d.getDateDebut()));
        ps.setDate(3, Date.valueOf(d.getDateFin()));
        ps.setString(4, d.getPreferences());
        ps.setString(5, d.getStatut());
        ps.setTimestamp(6, Timestamp.valueOf(d.getDateCreation()));
        ps.setInt(7, d.getClient().getId());
        if (d.getAgent() != null) ps.setInt(8, d.getAgent().getId());
        else ps.setNull(8, Types.INTEGER);
        ps.setInt(9, d.getId());

        ps.executeUpdate();
        ps.close();
        System.out.println("Demande modifiée avec succès !");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM demande WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
        System.out.println("Demande supprimée avec succès !");
    }

    @Override
    public List<Demande> afficher() throws SQLException {
        List<Demande> list = new ArrayList<>();
        String req = "SELECT * FROM demande";
        PreparedStatement ps = cnx.prepareStatement(req);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(mapResultSetToDemande(rs));
        }
        rs.close();
        ps.close();
        return list;
    }

    public Demande findById(int id) throws SQLException {
        String req = "SELECT * FROM demande WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            Demande d = mapResultSetToDemande(rs);
            rs.close();
            ps.close();
            return d;
        }
        rs.close();
        ps.close();
        return null;
    }

    public List<Demande> findByClientId(int clientId) throws SQLException {
        List<Demande> list = new ArrayList<>();
        String req = "SELECT * FROM demande WHERE client_id = ?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, clientId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(mapResultSetToDemande(rs));
        }
        rs.close();
        ps.close();
        return list;
    }

    public List<Demande> findByAgentId(int agentId) throws SQLException {
        List<Demande> list = new ArrayList<>();
        String req = "SELECT * FROM demande WHERE agent_id = ?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, agentId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(mapResultSetToDemande(rs));
        }
        rs.close();
        ps.close();
        return list;
    }

    private Demande mapResultSetToDemande(ResultSet rs) throws SQLException {
        Demande d = new Demande();
        d.setId(rs.getInt("id"));
        d.setDestination(rs.getString("destination"));

        Date db = rs.getDate("date_debut");
        if (db != null) d.setDateDebut(db.toLocalDate());

        Date df = rs.getDate("date_fin");
        if (df != null) d.setDateFin(df.toLocalDate());

        d.setPreferences(rs.getString("preferences"));
        d.setStatut(rs.getString("statut"));

        Timestamp ts = rs.getTimestamp("date_creation");
        if (ts != null) d.setDateCreation(ts.toLocalDateTime());

        int clientId = rs.getInt("client_id");
        if (!rs.wasNull()) {
            User client = userService.findById(clientId);
            d.setClient(client);
        }

        int agentId = rs.getInt("agent_id");
        if (!rs.wasNull()) {
            User agent = userService.findById(agentId);
            d.setAgent(agent);
        }

        // reservation is one-to-one; you can lazily load it via ReservationService if needed
        return d;
    }
}