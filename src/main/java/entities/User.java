package entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    // ---------- Champs ----------
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 180, unique = true, nullable = false)
    @NotBlank(message = "L'email est requis")
    @Email(message = "L'email n'est pas valide")
    private String email;

    @Column(length = 100, nullable = false)
    @NotBlank(message = "Le nom est requis")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre {min} et {max} caractères")
    private String nom;

    /**
     * Un seul rôle par utilisateur (ROLE_USER, ROLE_CLIENT, ROLE_ADMIN)
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Role roles = Role.ROLE_USER; // rôle par défaut

    /**
     * Mot de passe hashé (ou en clair temporairement)
     */
    @Column(nullable = false)
    private String password;

    /**
     * Mot de passe en clair (non persisté)
     */
    @Transient
    @Size(min = 6, message = "Le mot de passe doit contenir au moins {min} caractères")
    private String plainPassword;

    // Relations
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Demande> demandes = new ArrayList<>();

    @OneToMany(mappedBy = "agent")
    private List<Demande> demandesTraitees = new ArrayList<>();

    @OneToMany(mappedBy = "agent")
    private List<Reservation> reservations = new ArrayList<>();

    // ---------- Constructeurs ----------
    public User() {}

    // ---------- Getters / Setters ----------
    public Integer getId() { return id; }

    public User setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getEmail() { return email; }

    public User setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getNom() { return nom; }

    public User setNom(String nom) {
        this.nom = nom;
        return this;
    }

    public Role getRoles() { return roles; }

    public User setRoles(Role roles) {
        this.roles = roles;
        return this;
    }

    public String getPassword() { return password; }

    public User setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getPlainPassword() { return plainPassword; }

    public User setPlainPassword(String plainPassword) {
        this.plainPassword = plainPassword;
        return this;
    }

    public void eraseCredentials() {
        this.plainPassword = null;
    }

    // ---------- Utilitaires de rôle ----------
    public boolean isAdmin() { return roles == Role.ROLE_ADMIN; }

    public boolean isClient() { return roles == Role.ROLE_CLIENT; }

    public boolean isUser() { return roles == Role.ROLE_USER; }

    // ---------- equals / hashCode ----------
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(getId(), user.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
