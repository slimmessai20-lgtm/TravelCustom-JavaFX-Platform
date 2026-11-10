package controllers;

import entities.Reservation;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import services.EmailService;
import services.PdfReceiptService;
import services.StripePaymentService;

import java.awt.Desktop;
import java.math.BigDecimal;
import java.net.URI;

/**
 * Écran de paiement pour une réservation validée.
 *
 * Flux minimal (test): on crée un PaymentIntent et on affiche son id.
 * En prod, on ferait une redirection vers Checkout + webhook.
 */
public class PaiementReservation {

    @FXML private Label lblReservation;
    @FXML private Label lblMontant;
    @FXML private TextField txtPaymentIntentId;
    @FXML private Label lblMessage;

    private Reservation reservation;

    // TODO: idéalement via config/env, pas en dur.
        // Configuration Stripe et SMTP via variables d'environnement (ou valeurs securisees)
    private static final String STRIPE_API_KEY = System.getenv().getOrDefault(
            "STRIPE_API_KEY",
            "sk_test_your_stripe_test_key_here"
    );
    private final StripePaymentService stripe = new StripePaymentService(STRIPE_API_KEY);

    private static final String SMTP_EMAIL = System.getenv().getOrDefault(
            "SMTP_EMAIL",
            "your.email@gmail.com"
    );
    private static final String SMTP_PASSWORD = System.getenv().getOrDefault(
            "SMTP_PASSWORD",
            "your_app_password_here"
    );
    private final EmailService emailService = new EmailService(SMTP_EMAIL, SMTP_PASSWORD);

    private final PdfReceiptService pdfReceiptService = new PdfReceiptService();

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
        if (lblReservation != null && reservation != null) {
            lblReservation.setText("Réservation #" + reservation.getId() + " - " + reservation.getDemande().getDestination());
        }
        if (lblMontant != null && reservation != null) {
            lblMontant.setText(reservation.getPrixTotal().toPlainString() + " EUR");
        }
    }

    public void initialize() {
        // valeurs par défaut si setReservation appelé après load
        if (reservation != null) {
            setReservation(reservation);
        }
    }

    @FXML
    private void handleCreerPaiement(ActionEvent event) {
        lblMessage.setText("");
        if (reservation == null) {
            lblMessage.setText("Réservation introuvable.");
            return;
        }
        try {
            BigDecimal amount = reservation.getPrixTotal();
            StripePaymentService.CheckoutSession session = stripe.createCheckoutSession(
                    amount,
                    "Réservation #" + reservation.getId() + " - " + reservation.getDemande().getDestination()
            );

            txtPaymentIntentId.setText(session.id());
            lblMessage.setText("Session Checkout créée. Ouverture de la page de paiement...");

            if (Desktop.isDesktopSupported() && session.url() != null) {
                Desktop.getDesktop().browse(new URI(session.url()));
            } else {
                lblMessage.setText("Session créée: " + session.id() + "\nURL: " + session.url());
            }
        } catch (Exception e) {
            lblMessage.setText("Erreur paiement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleVerifierEtEnvoyerRecu(ActionEvent event) {
        lblMessage.setText("");
        if (reservation == null) {
            lblMessage.setText("Réservation introuvable.");
            return;
        }
        String sessionId = txtPaymentIntentId.getText();
        if (sessionId == null || sessionId.isBlank()) {
            lblMessage.setText("Veuillez saisir l'ID de session Checkout.");
            return;
        }

        try {
            String paymentStatus = stripe.retrieveCheckoutPaymentStatus(sessionId.trim());
            if (!"paid".equalsIgnoreCase(paymentStatus)) {
                lblMessage.setText("Paiement non confirmé. payment_status: " + paymentStatus);
                return;
            }

            byte[] pdf = pdfReceiptService.generateReceipt(reservation, sessionId.trim());
            String to = reservation.getDemande().getClient().getEmail();

            emailService.send(
                    to,
                    "Reçu de paiement - réservation #" + reservation.getId(),
                    "Bonjour " + reservation.getDemande().getClient().getNom() + ",\n\nVotre paiement a été confirmé (session " + sessionId.trim() + ").\nVeuillez trouver votre reçu en pièce jointe.\n\nCordialement,\nVoyages",
                    new EmailService.Attachment("recu-reservation-" + reservation.getId() + ".pdf", "application/pdf", pdf)
            );

            lblMessage.setText("Paiement vérifié (paid). Reçu envoyé par email.");
        } catch (Exception e) {
            lblMessage.setText("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleOuvrirPaiementsTest(ActionEvent event) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI("https://dashboard.stripe.com/test/payments"));
            }
        } catch (Exception e) {
            lblMessage.setText("Impossible d'ouvrir le navigateur.");
        }
    }
}
