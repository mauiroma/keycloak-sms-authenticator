package com.alliander.keycloak.authenticator;

import com.alliander.keycloak.authenticator.gateway.GatewayFactory;
import com.alliander.keycloak.authenticator.gateway.SMS;
import com.alliander.keycloak.authenticator.gateway.SMSGateway;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.*;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.Random;

/**
 * Created by joris on 11/11/2016.
 */
public class KeycloakSmsAuthenticator implements Authenticator {

    private static Logger logger = Logger.getLogger(KeycloakSmsAuthenticator.class);

    public static final String CREDENTIAL_TYPE = "sms_validation";

    private static enum CODE_STATUS {
        VALID,
        INVALID,
        EXPIRED
    }


    public void authenticate(AuthenticationFlowContext context) {
        logger.debug("authenticate called ... context = " + context);

        AuthenticatorConfigModel config = context.getAuthenticatorConfig();

        String mobileNumberAttribute = SMSAuthenticatorUtil.getConfigString(config, SMSAuthenticatorContstants.CONF_PRP_USR_ATTR_MOBILE);
        if(mobileNumberAttribute == null) {
            logger.error("Mobile number attribute is not configured for the SMS Authenticator.");
            Response challenge =  context.form()
                    .setError("Mobile number can not be determined.")
                    .createForm("sms-validation-error.ftl");
            context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR, challenge);
            return;
        }

        String mobileNumber = SMSAuthenticatorUtil.getAttributeValue(context.getUser(), mobileNumberAttribute);
        if(mobileNumber != null) {
            String gateway = SMSAuthenticatorUtil.getConfigString(config, SMSAuthenticatorContstants.CONF_PRP_SMS_GATEWAY);

            SMS sms = new SMS();
            sms.setCode(generateCode(context));
            sms.setMobileNumber(mobileNumber);
            sms.setMessage(SMSAuthenticatorUtil.getConfigString(config, SMSAuthenticatorContstants.CONF_PRP_SMS_TEXT));
            sms.setGatewayUrl(SMSAuthenticatorUtil.getConfigString(config, SMSAuthenticatorContstants.CONF_PRP_SMS_URL));
            sms.setGatewayUsr(SMSAuthenticatorUtil.getConfigString(config, SMSAuthenticatorContstants.CONF_PRP_SMS_USERNAME));
            sms.setGatewayPwd(SMSAuthenticatorUtil.getConfigString(config, SMSAuthenticatorContstants.CONF_PRP_SMS_PASSWORD));

            SMSGateway smsGateway = GatewayFactory.getSMSGateway(gateway);
            if (smsGateway.sendSmsCode(sms)) {
                Response challenge = context.form().createForm("sms-validation.ftl");
                context.challenge(challenge);
            } else {
                Response challenge =  context.form()
                        .setError("SMS could not be sent.")
                        .createForm("sms-validation-error.ftl");
                context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR, challenge);
                return;
            }
        } else {
            // The mobile number is NOT configured --> complain
            Response challenge =  context.form()
                    .setError("Missing mobile number")
                    .createForm("sms-validation-error.ftl");
            context.failureChallenge(AuthenticationFlowError.CLIENT_CREDENTIALS_SETUP_REQUIRED, challenge);
            return;
        }
    }


    public void action(AuthenticationFlowContext context) {
        logger.debug("action called ... context = " + context);
        CODE_STATUS status = validateCode(context);
        Response challenge = null;
        switch (status) {
            case EXPIRED:
                challenge =  context.form()
                        .setError("code is expired")
                        .createForm("sms-validation.ftl");
                context.failureChallenge(AuthenticationFlowError.EXPIRED_CODE, challenge);
                break;

            case INVALID:
                if(context.getExecution().getRequirement() == AuthenticationExecutionModel.Requirement.OPTIONAL ||
                        context.getExecution().getRequirement() == AuthenticationExecutionModel.Requirement.ALTERNATIVE) {
                    logger.debug("Calling context.attempted()");
                    context.attempted();
                } else if(context.getExecution().getRequirement() == AuthenticationExecutionModel.Requirement.REQUIRED) {
                    challenge =  context.form()
                            .setError("badCode")
                            .createForm("sms-validation.ftl");
                    context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
                } else {
                    // Something strange happened
                    logger.warn("Undefined execution ...");
                }
                break;

            case VALID:
                context.success();
                break;

        }
    }

    private CODE_STATUS validateCode(AuthenticationFlowContext context) {
        CODE_STATUS result = CODE_STATUS.INVALID;
        logger.debug("validateCode called ... ");
        try {
            MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
            String enteredCode = formData.getFirst(SMSAuthenticatorContstants.ANSW_SMS_CODE);
            String expectedCode = SMSAuthenticatorUtil.getCredentialValue(context,SMSAuthenticatorContstants.USR_CRED_MDL_SMS_CODE);
            String expTimeString = SMSAuthenticatorUtil.getCredentialValue(context,SMSAuthenticatorContstants.USR_CRED_MDL_SMS_EXP_TIME);

            logger.debug("Expected code = " + expectedCode + "    entered code = " + enteredCode);

            if(expectedCode != null) {
                result = enteredCode.equals(expectedCode) ? CODE_STATUS.VALID : CODE_STATUS.INVALID;
                long now = new Date().getTime();

                logger.debug("Valid code expires in " + (Long.parseLong(expTimeString) - now) + " ms");
                if(result == CODE_STATUS.VALID) {
                    if (Long.parseLong(expTimeString) < now) {
                        logger.debug("Code is expired !!");
                        result = CODE_STATUS.EXPIRED;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error when validate code", e);
        }
        logger.debug("result : " + result);
        return result;
    }

    public boolean requiresUser() {
        logger.debug("requiresUser called ... returning true");
        return true;
    }

    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        logger.debug("configuredFor called ... session=" + session + ", realm=" + realm + ", user=" + user);
        boolean result = true;
        logger.debug("... returning "  +result);
        return result;
    }

    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        logger.debug("setRequiredActions called ... session=" + session + ", realm=" + realm + ", user=" + user);
    }

    public void close() {
        logger.debug("close called ...");
    }


    private String generateCode(AuthenticationFlowContext context){
        long nrOfDigits = SMSAuthenticatorUtil.getConfigLong(context.getAuthenticatorConfig(), SMSAuthenticatorContstants.CONF_PRP_SMS_CODE_LENGTH, 8L);
        logger.debug("Using nrOfDigits " + nrOfDigits);

        long ttl = SMSAuthenticatorUtil.getConfigLong(context.getAuthenticatorConfig(), SMSAuthenticatorContstants.CONF_PRP_SMS_CODE_TTL, 10 * 60L); // 10 minutes in s
        logger.debug("Using ttl " + ttl + " (s)");

        Long expiringAt = new Date().getTime() + (ttl * 1000);

        double maxValue = Math.pow(10.0, nrOfDigits); // 10 ^ nrOfDigits;
        Random r = new Random();
        String code = Long.toString((long)(r.nextFloat() * maxValue));

        SMSAuthenticatorUtil.putCredentialValue(context,SMSAuthenticatorContstants.USR_CRED_MDL_SMS_CODE, code);
        SMSAuthenticatorUtil.putCredentialValue(context,SMSAuthenticatorContstants.USR_CRED_MDL_SMS_EXP_TIME, (expiringAt).toString());

        return code;
    }

}
