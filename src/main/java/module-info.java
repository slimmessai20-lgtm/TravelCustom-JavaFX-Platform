module org.voyages.voyages {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;

    requires java.sql;
    requires java.desktop;

    requires jakarta.persistence;
    requires jakarta.validation;
    requires jakarta.mail;

    // Les libs Stripe/iText restent en dépendances Maven mais on n'impose pas de 'requires' JPMS.

    opens org.voyages.voyages to javafx.fxml;
    exports org.voyages.voyages;

    opens controllers to javafx.fxml;
    exports controllers;

    exports entities;
    exports services;
    exports utils;
}