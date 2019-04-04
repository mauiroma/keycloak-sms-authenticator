package it.mauiroma.keycloak.smsauthenticator.gateway;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class PostGateway implements SMSGateway {


    @Override
    public boolean sendSmsCode(SMS sms) {
        logger.debug("Sending " + sms.getCode() + "  to mobileNumber " + sms.getMobileNumber());
        logger.debug(sms.getMessage());
        BufferedReader httpResponseReader = null;
        try {


            String usernameColonPassword = sms.getGatewayUsr() + ":" + sms.getGatewayPwd();
            String basicAuthPayload = "Basic " + Base64.getEncoder().encodeToString(usernameColonPassword.getBytes());


            // Connect to the web server endpoint
            URL serverUrl = new URL(sms.getGatewayUrl());
            HttpURLConnection urlConnection = (HttpURLConnection) serverUrl.openConnection();

            // Set HTTP method as GET
            urlConnection.setRequestMethod("POST");

            // Include the HTTP Basic Authentication payload
            urlConnection.addRequestProperty("Authorization", basicAuthPayload);


            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Length", Integer.toString(sms.getMessage().length()));
            urlConnection.getOutputStream().write(sms.getMessage().getBytes("UTF8"));


            // Read response from web server, which will trigger HTTP Basic Authentication request to be sent.
            httpResponseReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String smsGatewayResponse = httpResponseReader.readLine();
            logger.debug("SMS Gateway Response [" + smsGatewayResponse + "]");
            return !smsGatewayResponse.toLowerCase().contains("SMS ERR".toLowerCase());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return true;
    }
}