package com.alliander.keycloak.authenticator;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.common.util.Time;
import org.keycloak.credential.CredentialModel;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.UserModel;

import java.util.List;

/**
 * Created by joris on 18/11/2016.
 */
public class SMSAuthenticatorUtil {

    private static Logger logger = Logger.getLogger(SMSAuthenticatorUtil.class);

    public static String getAttributeValue(UserModel user, String attributeName) {
        String result = null;
        List<String> values = user.getAttribute(attributeName);
        if(values != null && values.size() > 0) {
            result = values.get(0);
        }

        return result;
    }


    public static void putCredentialValue(AuthenticationFlowContext context, String credentialName, String credentialValue){
        List<CredentialModel> creds = context.getSession().userCredentialManager().getStoredCredentialsByType(context.getRealm(), context.getUser(), credentialName);
        if (creds.isEmpty()) {
            CredentialModel secret = new CredentialModel();
            secret.setType(credentialName);
            secret.setValue(credentialValue);
            secret.setCreatedDate(Time.currentTimeMillis());
            context.getSession().userCredentialManager().createCredential(context.getRealm(), context.getUser(), secret);
        } else {
            creds.get(0).setValue(credentialValue);
            context.getSession().userCredentialManager().updateCredential(context.getRealm(), context.getUser(), creds.get(0));
        }

    }

    public static String getCredentialValue(AuthenticationFlowContext context, String credentialName) {
        String result = null;
        List codeCreds = context.getSession().userCredentialManager().getStoredCredentialsByType(context.getRealm(), context.getUser(), credentialName);
        CredentialModel credentialCode = (CredentialModel) codeCreds.get(0);
        if (credentialCode != null) {
            result = credentialCode.getValue();
        }
        logger.trace("CredentialName ["+credentialName+"] value ["+result+"]");
        return result;
    }

    public static String getConfigString(AuthenticatorConfigModel config, String configName) {
        return getConfigString(config, configName, null);
    }

    public static String getConfigString(AuthenticatorConfigModel config, String configName, String defaultValue) {

        String value = defaultValue;

        if (config.getConfig() != null) {
            // Get value
            value = config.getConfig().get(configName);
        }

        return value;
    }

    public static Long getConfigLong(AuthenticatorConfigModel config, String configName) {
        return getConfigLong(config, configName, null);
    }

    public static Long getConfigLong(AuthenticatorConfigModel config, String configName, Long defaultValue) {

        Long value = defaultValue;

        if (config.getConfig() != null) {
            // Get value
            Object obj = config.getConfig().get(configName);
            try {
                value = Long.valueOf((String) obj); // s --> ms
            } catch (NumberFormatException nfe) {
                logger.error("Can not convert " + obj + " to a number.");
            }
        }

        return value;
    }

    public static boolean getBooleanConfig(AuthenticatorConfigModel config, String configName) {
        return getBooleanConfig(config, configName, false);
    }

    public static boolean getBooleanConfig(AuthenticatorConfigModel config, String configName, boolean defaultValue) {

        boolean value = defaultValue;

        if (config.getConfig() != null) {
            // Get value
            Object obj = config.getConfig().get(configName);
            try {
                value = Boolean.valueOf((String) obj); // s --> ms
            } catch (NumberFormatException nfe) {
                logger.error("Can not convert " + obj + " to a boolean.");
            }
        }
        return value;
    }
}