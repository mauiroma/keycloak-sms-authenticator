package it.mauiroma.keycloak.smsauthenticator.gateway;

public class SMS {

    private String mobileNumber;
    private String code;
    private String gatewayUrl;
    private String gatewayUsr;
    private String gatewayPwd;
    private String message;

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getGatewayUrl() {
        return gatewayUrl;
    }

    public void setGatewayUrl(String gatewayUrl) {
        this.gatewayUrl = gatewayUrl;
    }

    public String getGatewayUsr() {
        return gatewayUsr;
    }

    public void setGatewayUsr(String gatewayUsr) {
        this.gatewayUsr = gatewayUsr;
    }

    public String getGatewayPwd() {
        return gatewayPwd;
    }

    public void setGatewayPwd(String gatewayPwd) {
        this.gatewayPwd = gatewayPwd;
    }

    public String getMessage() {
        return message.replaceAll("%sms-code%", code).replaceAll("%phonenumber%", mobileNumber);
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
