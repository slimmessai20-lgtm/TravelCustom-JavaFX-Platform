package services;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class StripePaymentService {

    private final String secretKey;

    public StripePaymentService(String secretKey) {
        this.secretKey = secretKey;
        init();
    }

    private void init() {
        try {
            Class<?> stripe = Class.forName("com.stripe.Stripe");
            stripe.getField("apiKey").set(null, secretKey);
        } catch (Exception e) {
            throw new IllegalStateException("Stripe SDK non disponible (dépendance/module-path).", e);
        }
    }

    /**
     * Crée une session Stripe Checkout en EUR.
     * Retourne l'URL (hosted) à ouvrir dans le navigateur.
     *
     * IMPORTANT: success/cancel URLs doivent être publiques en prod. Ici on met des URLs placeholder.
     */
    public CheckoutSession createCheckoutSession(BigDecimal amountEur, String description) {
        try {
            long amountCents = amountEur.multiply(BigDecimal.valueOf(100)).longValueExact();

            // Construction params via réflexion (Stripe non JPMS friendly)
            Class<?> sessionCreateParams = Class.forName("com.stripe.param.checkout.SessionCreateParams");
            Class<?> lineItem = Class.forName("com.stripe.param.checkout.SessionCreateParams$LineItem");
            Class<?> priceData = Class.forName("com.stripe.param.checkout.SessionCreateParams$LineItem$PriceData");
            Class<?> productData = Class.forName("com.stripe.param.checkout.SessionCreateParams$LineItem$PriceData$ProductData");

            Object productBuilder = productData.getMethod("builder").invoke(null);
            productBuilder = productBuilder.getClass().getMethod("setName", String.class).invoke(productBuilder, description);
            Object product = productBuilder.getClass().getMethod("build").invoke(productBuilder);

            Object priceBuilder = priceData.getMethod("builder").invoke(null);
            priceBuilder = priceBuilder.getClass().getMethod("setCurrency", String.class).invoke(priceBuilder, "eur");
            priceBuilder = priceBuilder.getClass().getMethod("setUnitAmount", Long.class).invoke(priceBuilder, amountCents);
            priceBuilder = priceBuilder.getClass().getMethod("setProductData", productData).invoke(priceBuilder, product);
            Object price = priceBuilder.getClass().getMethod("build").invoke(priceBuilder);

            Object liBuilder = lineItem.getMethod("builder").invoke(null);
            liBuilder = liBuilder.getClass().getMethod("setQuantity", Long.class).invoke(liBuilder, 1L);
            liBuilder = liBuilder.getClass().getMethod("setPriceData", priceData).invoke(liBuilder, price);
            Object li = liBuilder.getClass().getMethod("build").invoke(liBuilder);

            Object paramsBuilder = sessionCreateParams.getMethod("builder").invoke(null);
            // setMode(SessionCreateParams.Mode.PAYMENT)
            Class<?> modeEnum = Class.forName("com.stripe.param.checkout.SessionCreateParams$Mode");
            Object paymentMode = Enum.valueOf((Class<Enum>) modeEnum, "PAYMENT");
            paramsBuilder = paramsBuilder.getClass().getMethod("setMode", modeEnum)
                    .invoke(paramsBuilder, paymentMode);

            // success/cancel (placeholder)
            paramsBuilder = paramsBuilder.getClass().getMethod("setSuccessUrl", String.class)
                    .invoke(paramsBuilder, "https://example.com/success");
            paramsBuilder = paramsBuilder.getClass().getMethod("setCancelUrl", String.class)
                    .invoke(paramsBuilder, "https://example.com/cancel");

            paramsBuilder = paramsBuilder.getClass().getMethod("addLineItem", lineItem).invoke(paramsBuilder, li);
            Object params = paramsBuilder.getClass().getMethod("build").invoke(paramsBuilder);

            Class<?> session = Class.forName("com.stripe.model.checkout.Session");
            Object s = session.getMethod("create", sessionCreateParams).invoke(null, params);

            String id = (String) s.getClass().getMethod("getId").invoke(s);
            String url = (String) s.getClass().getMethod("getUrl").invoke(s);
            return new CheckoutSession(id, url);
        } catch (Exception e) {
            throw new RuntimeException("Erreur Stripe lors de la création de la session Checkout", e);
        }
    }

    /**
     * Retourne le payment_status d'une Checkout Session (paid/unpaid/no_payment_required).
     */
    public String retrieveCheckoutPaymentStatus(String sessionId) {
        try {
            Class<?> session = Class.forName("com.stripe.model.checkout.Session");
            Object s = session.getMethod("retrieve", String.class).invoke(null, sessionId);
            return (String) s.getClass().getMethod("getPaymentStatus").invoke(s);
        } catch (Exception e) {
            throw new RuntimeException("Erreur Stripe lors de la récupération de la session Checkout", e);
        }
    }

    public record CheckoutSession(String id, String url) {}
}
