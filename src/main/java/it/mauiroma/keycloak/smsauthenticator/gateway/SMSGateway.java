package it.mauiroma.keycloak.smsauthenticator.gateway;

import org.jboss.logging.Logger;

public interface SMSGateway {
    static Logger logger = Logger.getLogger(SMSGateway.class);

    boolean sendSmsCode(SMS sms);
}
