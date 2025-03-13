package com.sumer.sumerstores.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class XDSLoginService {

    private static final Logger logger = LoggerFactory.getLogger(XDSLoginService.class);
    private static final String LOGIN_URL = "https://www.uat.xds.co.za/NewPortal/Account/Login?ReturnUrl=%2FNewPortal%2F";
    private static final int CONNECT_TIMEOUT = 15000;
    private static final int READ_TIMEOUT = 15000;

    public String getAuthenticationTicket(String username, String password) throws Exception {
        String csrfToken = fetchCsrfToken();

        String urlParameters = "username=" + URLEncoder.encode(username, StandardCharsets.UTF_8) +
                "&password=" + URLEncoder.encode(password, StandardCharsets.UTF_8) +
                "&__RequestVerificationToken=" + URLEncoder.encode(csrfToken, StandardCharsets.UTF_8);

        HttpURLConnection connection = null;
        try {
            URL url = new URL(LOGIN_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Referer", LOGIN_URL);
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setInstanceFollowRedirects(true);

            // Write parameters to output stream
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = urlParameters.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                logger.info("Login successful, processing the response.");
                return readResponse(connection);
            } else {
                logger.error("Login failed. HTTP response code: {}", responseCode);
                String response = readResponse(connection);
                logger.error("Response content: {}", response);
                throw new RuntimeException("Login failed. HTTP response code: " + responseCode + ", Response: " + response);
            }

        } catch (IOException e) {
            logger.error("Error during login request: {}", e.getMessage());
            throw new RuntimeException("Failed to login to XDS service", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String fetchCsrfToken() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(LOGIN_URL).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String response = readResponse(connection);
            return extractCsrfToken(response);
        } else {
            throw new RuntimeException("Failed to fetch CSRF token. HTTP response code: " + responseCode);
        }
    }

    private String extractCsrfToken(String response) {
        // Regular expression to find the CSRF token in the response
        Pattern tokenPattern = Pattern.compile("name=\"__RequestVerificationToken\" value=\"([^\"]+)\"");
        Matcher matcher = tokenPattern.matcher(response);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new RuntimeException("CSRF token not found in response.");
        }
    }

    private String readResponse(HttpURLConnection connection) throws IOException {
        StringBuilder content = new StringBuilder();

        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
        }

        logger.info("Response content: {}", content.toString());
        return content.toString();
    }
}