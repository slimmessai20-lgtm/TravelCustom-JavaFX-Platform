package entities;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "reservation")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "date_reservation", nullable = false)
    private LocalDateTime dateReservation;

    @Column(name = "prix_total", precision = 10, scale = 2, nullable = false)
    @NotNull(message = "Le prix total est requis")
    @Positive(message = "Le prix doit être positif")
    private BigDecimal prixTotal;

    @Column(length = 50, nullable = false)
    private String statut = "confirmee"; // confirmee, annulee, terminee

    @OneToOne
    @JoinColumn(name = "demande_id", nullable = false)
    private Demande demande;

    @ManyToOne
    @JoinColumn(name = "agent_id", nullable = false)
    private User agent;

    public Reservation() {
    }

    @PrePersist
    public void setDateReservationValue() {
        if (this.dateReservation == null) {
            this.dateReservation = LocalDateTime.now();
        }
    }

    public Integer getId() {
        return id;
    }

    public Reservation setId(Integer id) {
        this.id = id;
        return this;
    }

    public LocalDateTime getDateReservation() {
        return dateReservation;
    }

    public Reservation setDateReservation(LocalDateTime dateReservation) {
        this.dateReservation = dateReservation;
        return this;
    }

    public BigDecimal getPrixTotal() {
        return prixTotal;
    }

    public Reservation setPrixTotal(BigDecimal prixTotal) {
        this.prixTotal = prixTotal;
        return this;
    }

    public String getStatut() {
        return statut;
    }

    public Reservation setStatut(String statut) {
        this.statut = statut;
        return this;
    }

    public Demande getDemande() {
        return demande;
    }

    public Reservation setDemande(Demande demande) {
        this.demande = demande;
        if (demande != null && demande.getReservation() != this) {
            demande.setReservation(this);
        }
        return this;
    }

    public User getAgent() {
        return agent;
    }

    public Reservation setAgent(User agent) {
        this.agent = agent;
        return this;
    }

    public String getStatutBadgeClass() {
        return switch (this.statut) {
            case "confirmee" -> "bg-success";
            case "annulee"  -> "bg-danger";
            case "terminee" -> "bg-secondary";
            default -> "bg-warning";
        };
    }

    public String getStatutLibelle() {
        return switch (this.statut) {
            case "confirmee" -> "Confirmée";
            case "annulee"  -> "Annulée";
            case "terminee" -> "Terminée";
            default -> "Inconnu";
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Reservation that = (Reservation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}