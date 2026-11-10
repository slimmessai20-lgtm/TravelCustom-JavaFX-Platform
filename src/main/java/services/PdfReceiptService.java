package services;

import entities.Reservation;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * Génère un reçu PDF simple en mémoire.
 *
 * iText est utilisé via réflexion pour éviter les soucis JPMS.
 */
public class PdfReceiptService {

    public byte[] generateReceipt(Reservation reservation, String paymentId) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            Class<?> pdfWriterClz = Class.forName("com.itextpdf.kernel.pdf.PdfWriter");
            Class<?> pdfDocClz = Class.forName("com.itextpdf.kernel.pdf.PdfDocument");
            Class<?> docClz = Class.forName("com.itextpdf.layout.Document");
            Class<?> paragraphClz = Class.forName("com.itextpdf.layout.element.Paragraph");
            Class<?> tableClz = Class.forName("com.itextpdf.layout.element.Table");

            Object writer = pdfWriterClz.getConstructor(java.io.OutputStream.class).newInstance(out);
            Object pdf = pdfDocClz.getConstructor(pdfWriterClz).newInstance(writer);
            Object doc = docClz.getConstructor(pdfDocClz).newInstance(pdf);

            Object title = paragraphClz.getConstructor(String.class).newInstance("Reçu de paiement - Voyages");
            title = title.getClass().getMethod("setBold").invoke(title);
            title = title.getClass().getMethod("setFontSize", float.class).invoke(title, 16f);
            addToDocument(doc, title);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            Object table = tableClz.getConstructor(int.class).newInstance(2);
            addRow(table, "Réservation ID", String.valueOf(reservation.getId()));
            addRow(table, "Destination", reservation.getDemande() != null ? reservation.getDemande().getDestination() : "-");
            addRow(table, "Client", reservation.getDemande() != null && reservation.getDemande().getClient() != null ? reservation.getDemande().getClient().getNom() : "-");
            addRow(table, "Agent", reservation.getAgent() != null ? reservation.getAgent().getNom() : "-");
            addRow(table, "Date réservation", reservation.getDateReservation() != null ? reservation.getDateReservation().format(dtf) : "-");
            addRow(table, "Montant", reservation.getPrixTotal() != null ? reservation.getPrixTotal().toPlainString() + " EUR" : "-");
            addRow(table, "Paiement (Stripe)", paymentId != null ? paymentId : "-");

            addToDocument(doc, table);

            doc.getClass().getMethod("close").invoke(doc);
            return out.toByteArray();
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("iText n'est pas disponible (dépendance/module-path).", e);
        } catch (Exception e) {
            throw new RuntimeException("Impossible de générer le reçu PDF", e);
        }
    }

    private void addRow(Object table, String k, String v) throws Exception {
        // table.addCell(String)
        table.getClass().getMethod("addCell", String.class).invoke(table, k);
        table.getClass().getMethod("addCell", String.class).invoke(table, v);
    }

    /**
     * Ajout robuste d'un élément au Document iText.
     *
     * Selon la version d'iText, la signature expose souvent:
     * - Document#add(IBlockElement)
     * - Document#add(BlockElement<?>)
     * - (plus rarement) Document#add(IElement)
     */
    private void addToDocument(Object document, Object element) throws Exception {
        try {
            Class<?> iBlockEl = Class.forName("com.itextpdf.layout.element.IBlockElement");
            document.getClass().getMethod("add", iBlockEl).invoke(document, element);
            return;
        } catch (NoSuchMethodException ignored) {
            // fallback ci-dessous
        }

        try {
            Class<?> blockEl = Class.forName("com.itextpdf.layout.element.BlockElement");
            document.getClass().getMethod("add", blockEl).invoke(document, element);
            return;
        } catch (NoSuchMethodException ignored) {
            // fallback ci-dessous
        }

        // dernier recours
        Class<?> iEl = Class.forName("com.itextpdf.layout.element.IElement");
        document.getClass().getMethod("add", iEl).invoke(document, element);
    }
}
