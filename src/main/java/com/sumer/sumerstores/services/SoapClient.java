package com.sumer.sumerstores.services;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SoapClient {

    private static final String SOAP_ENDPOINT_URL = "https://www.uat.xds.co.za/NewPortal/Account/Login?ReturnUrl=%2FNewPortal%2F"; // Correct SOAP endpoint
    private static final Logger LOGGER = Logger.getLogger(SoapClient.class.getName());

    public String sendSoapLoginRequest(String username, String password) throws Exception {
        LOGGER.info("Preparing SOAP request for login");

        // Constructing the SOAP message
        String soapMessage =
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                        "<soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                        "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
                        "xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\">" +
                        "<soap12:Body>" +
                        "<Login xmlns=\"https://www.uat.xds.co.za/NewPortal/\">" +
                        "<strUser>" + username + "</strUser>" +
                        "<strPwd>" + password + "</strPwd>" +
                        "</Login>" +
                        "</soap12:Body>" +
                        "</soap12:Envelope>";

        LOGGER.info("SOAP Message: " + soapMessage);

        // Setting up the connection
        URL url = new URL(SOAP_ENDPOINT_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/soap+xml; charset=utf-8");
        connection.setRequestProperty("Content-Length", String.valueOf(soapMessage.length()));
        connection.setDoOutput(true);

        // Uncomment to use actual credentials if required
        // connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString("username:password".getBytes()));

        // Sending the SOAP message
        try (OutputStream os = connection.getOutputStream()) {
            os.write(soapMessage.getBytes("UTF-8"));
            os.flush();
            LOGGER.info("SOAP request sent successfully");
        }

        // Getting the response
        int responseCode = connection.getResponseCode();
        LOGGER.info("Received HTTP response code: " + responseCode);

        StringBuilder responseBuilder = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                responseBuilder.append(inputLine);
            }
        } catch (Exception e) {
            if (responseCode != HttpURLConnection.HTTP_OK) {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    responseBuilder.append(errorLine);
                }
                LOGGER.severe("Error response: " + responseBuilder.toString());
            }
        }

        if (responseCode == HttpURLConnection.HTTP_OK) {
            LOGGER.info("SOAP Response: " + responseBuilder.toString());
            return responseBuilder.toString();
        } else {
            String errorMessage = "Failed : HTTP error code : " + responseCode;
            LOGGER.severe(errorMessage);
            throw new RuntimeException(errorMessage);
        }
    }

    private Document parseXmlFile(String in) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(in));
            return db.parse(is);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }

}