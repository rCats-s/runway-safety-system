module client {
  requires javafx.controls;
  requires javafx.fxml;
  requires javafx.swing;

  requires org.controlsfx.controls;
  requires com.dlsc.formsfx;

  requires java.xml;
    requires jakarta.xml.bind;

    opens org.example.client to jakarta.xml.bind, javafx.fxml, org.glassfish.jaxb.runtime;
    opens org.example.client.components to jakarta.xml.bind, javafx.fxml;
  exports org.example.client;
  exports org.example.client.components;
    exports org.example.client.theme;
    opens org.example.client.theme to jakarta.xml.bind, javafx.fxml, org.glassfish.jaxb.runtime;
    requires shared;
  requires java.sql;
    requires java.desktop;
    requires itextpdf;


}