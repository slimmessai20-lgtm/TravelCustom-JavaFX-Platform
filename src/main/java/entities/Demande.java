package entities;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Entity
@Table(name = "demande")
public class Demande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 255, nullable = false)
    @NotBlank(message = "La destination est requise")
    @Size(min = 3, max = 255)
    private String destination;

    @Column(name = "date_debut", nullable = false)
    @NotNull(message = "La date de début est requise")
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    @NotNull(message = "La date de fin est requise")
    private LocalDate dateFin;

    @Column(columnDefinition = "TEXT")
    private String preferences;

    @Column(length = 50, nullable = false)
    private String statut = "en_attente"; // en_attente, en_cours, validee, refusee

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne
    @JoinColumn(name = "agent_id", nullable = true)
    private User agent;

    @OneToOne(mappedBy = "demande", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private Reservation reservation;

    public Demande() {
    }

    @PrePersist
    public void setDateCreationValue() {
        if (this.dateCreation == null) {
            this.dateCreation = LocalDateTime.now();
        }
    }

    public Integer getId() {
        return id;
    }

    public Demande setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getDestination() {
        return destination;
    }

    public Demande setDestination(String destination) {
        this.destination = destination;
        return this;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public Demande setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
        return this;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public Demande setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
        return this;
    }

    public String getPreferences() {
        return preferences;
    }

    public Demande setPreferences(String preferences) {
        this.preferences = preferences;
        return this;
    }

    public String getStatut() {
        return statut;
    }

    public Demande setStatut(String statut) {
        this.statut = statut;
        return this;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public Demande setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
        return this;
    }

    public User getClient() {
        return client;
    }

    public Demande setClient(User client) {
        this.client = client;
        return this;
    }

    public User getAgent() {
        return agent;
    }

    public Demande setAgent(User agent) {
        this.agent = agent;
        return this;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public Demande setReservation(Reservation reservation) {
        // unset the owning side if necessary
        if (reservation == null && this.reservation != null) {
            this.reservation.setDemande(null);
        }

        // set the owning side if necessary
        if (reservation != null && reservation.getDemande() != this) {
            reservation.setDemande(this);
        }

        this.reservation = reservation;
        return this;
    }

    public int getNombreJours() {
        if (this.dateDebut != null && this.dateFin != null) {
            return (int) ChronoUnit.DAYS.between(this.dateDebut, this.dateFin);
        }
        return 0;
    }

    public String getStatutBadgeClass() {
        return switch (this.statut) {
            case "en_attente" -> "bg-warning";
            case "en_cours"   -> "bg-info";
            case "validee"    -> "bg-success";
            case "refusee"    -> "bg-danger";
            default -> "bg-secondary";
        };
    }

    public String getStatutLibelle() {
        return switch (this.statut) {
            case "en_attente" -> "En attente";
            case "en_cours"   -> "En cours";
            case "validee"    -> "Validée";
            case "refusee"    -> "Refusée";
            default -> "Inconnu";
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Demande)) return false;
        Demande demande = (Demande) o;
        return Objects.equals(id, demande.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}