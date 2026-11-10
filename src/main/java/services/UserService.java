package services;

import entities.Role;
import entities.User;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements IService<User> {

    private Connection cnx;

    public UserService() {
        cnx = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(User user) throws SQLException {
        String req = "INSERT INTO user (email, nom, password, roles) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);

        ps.setString(1, user.getEmail());
        ps.setString(2, user.getNom());
        ps.setString(3, user.getPassword());
        // Convertir le rôle en format JSON pour Symfony: ["ROLE_CLIENT"]
        ps.setString(4, "[\"" + user.getRoles().name() + "\"]");

        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            user.setId(rs.getInt(1));
        }

        System.out.println("✅ Utilisateur ajouté avec succès !");
    }

    @Override
    public void modifier(User user) throws SQLException {
        String req = "UPDATE user SET email=?, nom=?, password=?, roles=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);

        ps.setString(1, user.getEmail());
        ps.setString(2, user.getNom());
        ps.setString(3, user.getPassword());
        // Convertir le rôle en format JSON pour Symfony: ["ROLE_CLIENT"]
        ps.setString(4, "[\"" + user.getRoles().name() + "\"]");
        ps.setInt(5, user.getId());

        ps.executeUpdate();
        System.out.println("✅ Utilisateur modifié avec succès !");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM user WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();

        System.out.println("🗑️ Utilisateur supprimé avec succès !");
    }

    @Override
    public List<User> afficher() throws SQLException {
        List<User> users = new ArrayList<>();
        String req = "SELECT * FROM user";
        PreparedStatement ps = cnx.prepareStatement(req);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            users.add(mapResultSetToUser(rs));
        }

        return users;
    }

    public User findById(int id) throws SQLException {
        String req = "SELECT * FROM user WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return mapResultSetToUser(rs);
        }
        return null;
    }

    public User findByEmail(String email) throws SQLException {
        String req = "SELECT * FROM user WHERE email=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return mapResultSetToUser(rs);
        }
        return null;
    }

    // ---------- Méthodes spécifiques par rôle ----------
    public List<User> findAllClients() throws SQLException {
        return findByRole(Role.ROLE_CLIENT);
    }

    public List<User> findAllAdmins() throws SQLException {
        return findByRole(Role.ROLE_ADMIN);
    }

    public List<User> findAllUsers() throws SQLException {
        return findByRole(Role.ROLE_USER);
    }

    private List<User> findByRole(Role role) throws SQLException {
        List<User> users = new ArrayList<>();
        // Recherche dans le JSON avec LIKE pour compatibilité Symfony
        String req = "SELECT * FROM user WHERE roles LIKE ?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, "%\"" + role.name() + "\"%");
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            users.add(mapResultSetToUser(rs));
        }
        return users;
    }

    // ---------- Mapping ----------
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setEmail(rs.getString("email"));
        u.setNom(rs.getString("nom"));
        u.setPassword(rs.getString("password"));

        // Parser le JSON de Symfony: ["ROLE_ADMIN"] -> ROLE_ADMIN
        String rolesJson = rs.getString("roles");
        Role role = parseRoleFromJson(rolesJson);
        u.setRoles(role);

        return u;
    }

    /**
     * Parse le format JSON de Symfony pour extraire le rôle
     * Exemple: ["ROLE_ADMIN"] -> ROLE_ADMIN
     */
    private Role parseRoleFromJson(String rolesJson) {
        if (rolesJson == null || rolesJson.isEmpty()) {
            return Role.ROLE_USER;
        }

        try {
            // Enlever les crochets et guillemets: ["ROLE_ADMIN"] -> ROLE_ADMIN
            String cleanRole = rolesJson
                    .replace("[", "")
                    .replace("]", "")
                    .replace("\"", "")
                    .trim();

            // Si plusieurs rôles, prendre le premier (ou le plus important)
            if (cleanRole.contains(",")) {
                String[] roles = cleanRole.split(",");
                for (String r : roles) {
                    String trimmedRole = r.trim();
                    // Priorité: ADMIN > USER > CLIENT
                    if (trimmedRole.contains("ADMIN")) {
                        return Role.ROLE_ADMIN;
                    }
                }
                for (String r : roles) {
                    String trimmedRole = r.trim();
                    if (trimmedRole.contains("USER") && !trimmedRole.contains("CLIENT")) {
                        return Role.ROLE_USER;
                    }
                }
                cleanRole = roles[0].trim();
            }

            // Convertir en enum
            if (cleanRole.contains("ADMIN")) {
                return Role.ROLE_ADMIN;
            } else if (cleanRole.contains("CLIENT")) {
                return Role.ROLE_CLIENT;
            } else if (cleanRole.contains("USER")) {
                return Role.ROLE_USER;
            }

            return Role.ROLE_USER; // Par défaut
        } catch (Exception e) {
            System.err.println("Erreur parsing rôle JSON: " + rolesJson);
            return Role.ROLE_USER;
        }
    }
}
