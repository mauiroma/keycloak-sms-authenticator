package com.alliander.keycloak.authenticator.gateway;

public class MockGateway implements SMSGateway {
    @Override
    public boolean sendSmsCode(SMS sms) {
        logger.debug("Sending " + sms.getCode() + "  to mobileNumber " + sms.getMobileNumber());
        logger.debug(sms.getMessage());
        return true;
    }
}
