package it.mauiroma.keycloak.smsauthenticator.gateway;

import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;


/**
 * SMS Message item contains alla paramenters separated by ";"
 * Sample:
 *  from=FROM ; to=TO ;
 * */
public class PostGateway implements SMSGateway {
    @Override
    public boolean sendSmsCode(SMS sms) {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            CloseableHttpResponse response = httpClient.execute(getHttpPost(sms));
            StatusLine sl = response.getStatusLine();
            response.close();
            if (sl.getStatusCode() != 200) {
                logger.error("SMS code for " + sms.getMobileNumber() + " could not be sent: " + sl.getStatusCode() + " - " + sl.getReasonPhrase());
            }
            return sl.getStatusCode() == 200;
        } catch (Exception e) {
            logger.error("Error when create post paramenter", e);
            return false;
        }
    }

    private HttpPost getHttpPost(SMS sms) throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(sms.getGatewayUrl());
        List<String> parameters = Arrays.asList(sms.getMessage().split(";"));
        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
        for (String parameter : parameters) {
            String[] keyValueParameter = parameter.split("=");
            nameValuePair.add(new BasicNameValuePair(keyValueParameter[0].trim(), keyValueParameter[1].trim()));
        }
        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair,"UTF-8"));


        String usernameColonPassword = sms.getGatewayUsr() + ":" + sms.getGatewayPwd();
        String basicAuthPayload = "Basic " + Base64.getEncoder().encodeToString(usernameColonPassword.getBytes());
        httpPost.setHeader(HttpHeaders.AUTHORIZATION, basicAuthPayload);

        return httpPost;
    }
}
