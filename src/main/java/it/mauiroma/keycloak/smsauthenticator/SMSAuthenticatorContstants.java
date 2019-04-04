package it.mauiroma.keycloak.smsauthenticator;

/**
 * Created by joris on 18/11/2016.
 */
public class SMSAuthenticatorContstants {
    public static final String ANSW_SMS_CODE = "smsCode";

    // Configurable fields
    public static final String CONF_PRP_USR_ATTR_MOBILE = "sms-auth.attr.mobile";
    public static final String CONF_PRP_SMS_CODE_TTL = "sms-auth.code.ttl";
    public static final String CONF_PRP_SMS_CODE_LENGTH = "sms-auth.code.length";
    public static final String CONF_PRP_SMS_TEXT = "sms-auth.msg.text";

    public static final String CONF_PRP_SMS_GATEWAY = "sms-auth.sms.gateway";
    public static final String CONF_PRP_SMS_URL = "sms-auth.sms.url";
    public static final String CONF_PRP_SMS_USERNAME = "sms-auth.sms.username";
    public static final String CONF_PRP_SMS_PASSWORD = "sms-auth.sms.password";

    public static final String USR_CRED_MDL_SMS_CODE = "sms-auth.code";
    public static final String USR_CRED_MDL_SMS_EXP_TIME = "sms-auth.exp-time";

    public static final String MOCK = "MOCK";
}