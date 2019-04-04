package com.alliander.keycloak.authenticator.gateway;

public class GatewayFactory {

    public static SMSGateway getSMSGateway(String gateway){
        Gateways g = Gateways.valueOf(gateway);
        SMSGateway smsGateway;
        switch (g) {
            case POST:
                smsGateway = new PostGateway();
                break;
            default:
                smsGateway = new MockGateway();
        }
        return smsGateway;
    }
}