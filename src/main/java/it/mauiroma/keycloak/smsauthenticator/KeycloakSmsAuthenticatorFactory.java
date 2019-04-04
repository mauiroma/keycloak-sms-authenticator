package it.mauiroma.keycloak.smsauthenticator;

import it.mauiroma.keycloak.smsauthenticator.gateway.Gateways;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.ConfigurableAuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Created by joris on 11/11/2016.
 */
public class KeycloakSmsAuthenticatorFactory implements AuthenticatorFactory, ConfigurableAuthenticatorFactory {

    public static final String PROVIDER_ID = "sms-authentication";
    public static final String DISPLAY_NAME = "SMS Authentication";

//    public static final String PROVIDER_ID = "sms-authentication-simple";
//    public static final String DISPLAY_NAME = "SMS Authentication Simple";


    private static Logger logger = Logger.getLogger(KeycloakSmsAuthenticatorFactory.class);
    private static final KeycloakSmsAuthenticator SINGLETON = new KeycloakSmsAuthenticator();


    public static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.OPTIONAL,
            AuthenticationExecutionModel.Requirement.DISABLED};

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    static {
        ProviderConfigProperty property;

        // Mobile number attribute
        property = new ProviderConfigProperty();
        property.setName(SMSAuthenticatorContstants.CONF_PRP_USR_ATTR_MOBILE);
        property.setLabel("Mobile number attribute");
        property.setDefaultValue("mobile");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("The attribute in which the mobile number of a user is stored.");
        configProperties.add(property);

        // LENGTH
        property = new ProviderConfigProperty();
        property.setName(SMSAuthenticatorContstants.CONF_PRP_SMS_CODE_LENGTH);
        property.setLabel("Length of the SMS code");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Length of the SMS code.");
        property.setDefaultValue(8);
        configProperties.add(property);

        // TTL
        property = new ProviderConfigProperty();
        property.setName(SMSAuthenticatorContstants.CONF_PRP_SMS_CODE_TTL);
        property.setLabel("SMS code time to live");
        property.setDefaultValue(60);
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("The validity of the sent code in seconds.");
        configProperties.add(property);

        // SMS Text
        property = new ProviderConfigProperty();
        property.setName(SMSAuthenticatorContstants.CONF_PRP_SMS_TEXT);
        property.setLabel("Template of text to send to the user");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Use %sms-code% as placeholder for the generated SMS code. Use %phonenumber% as placeholder for the user mobile");
        configProperties.add(property);

        // GATEWAY TYPE
        property = new ProviderConfigProperty();
        property.setName(SMSAuthenticatorContstants.CONF_PRP_SMS_GATEWAY);
        property.setLabel("SMS gateway");
        property.setHelpText("Select SMS gateway");
        property.setType(ProviderConfigProperty.LIST_TYPE);
        property.setDefaultValue(Gateways.MOCK);
        property.setOptions(Stream.of(Gateways.values())
                .map(Enum::name)
                .collect(Collectors.toList()));
        configProperties.add(property);

        // GATEWAY URL
        property = new ProviderConfigProperty();
        property.setName(SMSAuthenticatorContstants.CONF_PRP_SMS_URL);
        property.setLabel("URL of SMS gateway");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Use {message} as a placeholder for the message and {phonenumber} as a placeholder for the mobile number when the SMS text is to be passed as a URL parameter.");
        configProperties.add(property);

        // GATEWAY USER
        property = new ProviderConfigProperty();
        property.setName(SMSAuthenticatorContstants.CONF_PRP_SMS_USERNAME);
        property.setLabel("Username to authenticate towards the SMS Gateway");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("");
        configProperties.add(property);

        // GATEWAY PASSWORD
        property = new ProviderConfigProperty();
        property.setName(SMSAuthenticatorContstants.CONF_PRP_SMS_PASSWORD);
        property.setLabel("Password to authenticate towards the SMS Gateway");
        property.setType(ProviderConfigProperty.PASSWORD);
        property.setHelpText("");
        configProperties.add(property);
    }

    public String getId() {
        logger.debug("getId called ... returning " + PROVIDER_ID);
        return PROVIDER_ID;
    }

    public Authenticator create(KeycloakSession session) {
        logger.debug("create called ... returning " + SINGLETON);
        return SINGLETON;
    }


    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        logger.debug("getRequirementChoices called ... returning " + REQUIREMENT_CHOICES);
        return REQUIREMENT_CHOICES;
    }

    public boolean isUserSetupAllowed() {
        logger.debug("isUserSetupAllowed called ... returning true");
        return true;
    }

    public boolean isConfigurable() {
        boolean result = true;
        logger.debug("isConfigurable called ... returning " + result);
        return result;
    }

    public String getHelpText() {
        logger.debug("getHelpText called ...");
        return "Validates an OTP sent by SMS.";
    }

    public String getDisplayType() {
        String result = DISPLAY_NAME;
        logger.debug("getDisplayType called ... returning " + result);
        return result;
    }

    public String getReferenceCategory() {
        logger.debug("getReferenceCategory called ... returning sms-auth-code");
        return "sms-auth-code";
    }

    public List<ProviderConfigProperty> getConfigProperties() {
        logger.debug("getConfigProperties called ... returning " + configProperties);
        return configProperties;
    }

    public void init(Config.Scope config) {
        logger.debug("init called ... config.scope = " + config);
    }

    public void postInit(KeycloakSessionFactory factory) {
        logger.debug("postInit called ... factory = " + factory);
    }

    public void close() {
        logger.debug("close called ...");
    }
}
