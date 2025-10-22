package org.example.client.components;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.example.client.Calculation;
import org.example.client.theme.ThemedAlert;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import javafx.scene.control.Alert;

public class XMLParser {
    public XMLParser(){

    }

    public static void main(String[] args){
        URL url = XMLParser.class.getResource("/org/example/client/calcinstschema.xsd");

        System.out.println(url);
    }

    public static ImportData readInXML(File file) throws SAXException , JAXBException{
        try {
            JAXBContext context = JAXBContext.newInstance(CalcInst.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            //load schema for xml
            try (InputStream is = XMLParser.class.getResourceAsStream("/org/example/client/calcinstschema.xsd")) {
                if (is == null) {
                    throw new IOException("Schema file 'calcinstschema.xsd' not found in resources.");
                }

                SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = sf.newSchema(new javax.xml.transform.stream.StreamSource(is));
                unmarshaller.setSchema(schema);
            } catch (IOException e) {
                throw new JAXBException("Failed to load schema", e);
                //need to rethrow to stop refresh
            }

            // Set up event handler to stop on validation errors
            unmarshaller.setEventHandler(event -> {
                return false;
            });

            //try unmarshall
            CalcInst imp = null;
            try {
                imp = (CalcInst) unmarshaller.unmarshal(file);
            } catch (JAXBException e) {
                Throwable cause = e.getLinkedException();
                if (cause instanceof SAXParseException) {
                    Alert alert = new ThemedAlert(Alert.AlertType.INFORMATION);
                    alert.setHeaderText("Invalid XML file");
                    alert.setContentText("Invalid plane status - Takeoff/Landing");
                    alert.showAndWait();
                }
                throw e;
                //need to rethrow to stop refresh
            }

            //null if any unmarshalling issues
            if (imp == null) {
                System.err.println("Unmarshalling failed, 'imp' is null");
                return null;
            }

            //get vals from imported xml
            String name = imp.getName();
            String status = imp.getStatus();
            String runwayPair = imp.getRunwayPair();
            String LRrunway = imp.getLRrunway();
            Calculation calculation = imp.getCalculation();
            int eobjdist = imp.getEobjdist();
            int wobjdist = imp.getWobjdist();
            int centredist = imp.getCentredist();
            Obstacle obstacle = imp.getObstacle();
            Planes plane = imp.getPlane();

            //new object
            ImportData importData = new ImportData(name, status, runwayPair, LRrunway, calculation, eobjdist, wobjdist, centredist, obstacle, plane);

            if (importData != null) {
                return importData;
            } else {
                System.err.println("ImportData is null");
                return null;
            }

        } catch (JAXBException ex) {
            throw ex;
        }

    }

    public static boolean writeToXML(CalcInst calcInst, File file) {
        try {
            System.out.println("Starting XML export process");
            System.out.println("CalcInst status before marshalling: " + calcInst.getStatus());
            
            JAXBContext context = JAXBContext.newInstance(CalcInst.class);
            Marshaller marshaller = context.createMarshaller();
            
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            
            try (InputStream is = XMLParser.class.getResourceAsStream("/org/example/client/calcinstschema.xsd")) {
                if (is == null) {
                    System.err.println("Warning: Schema file 'calcinstschema.xsd' not found in resources, skipping validation");
                    // handle better?
                } else {
                    System.out.println("Schema file found, setting up validation");
                    SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                    Schema schema = sf.newSchema(new javax.xml.transform.stream.StreamSource(is));
                    marshaller.setSchema(schema);
                    
                    marshaller.setEventHandler(event -> {
                        System.err.println("Validation event: " + event.getMessage());
                        return true;
                    });
                }
            } catch (IOException e) {
                System.err.println("Error loading schema: " + e.getMessage());
            }
            
            System.out.println("Marshalling object to file: " + file.getAbsolutePath());
            marshaller.marshal(calcInst, file);
            System.out.println("Marshalling completed successfully");
            return true;
        } catch (Exception e) {
            System.err.println("Error writing XML: " + e.getMessage());
            if (e instanceof JAXBException && ((JAXBException)e).getLinkedException() != null) {
                System.err.println("Linked exception: " + ((JAXBException)e).getLinkedException().getMessage());
                ((JAXBException)e).getLinkedException().printStackTrace();
            }
            e.printStackTrace();
            return false;
        }
    }

}
