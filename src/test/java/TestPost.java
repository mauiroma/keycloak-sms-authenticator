import it.mauiroma.keycloak.smsauthenticator.gateway.PostGateway;
import it.mauiroma.keycloak.smsauthenticator.gateway.SMS;
import it.mauiroma.keycloak.smsauthenticator.gateway.SMSGateway;
import org.junit.Before;
import org.junit.Test;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public class TestPost {

    String gatewayUrl = "";
    String gatewayUsr = "";
    String gatewayPwd = "";
    String mobileNumber = "";


    @Before
    public void loadProperties(){
        Properties p = new Properties();
        try {
            p.load(new FileReader(getClass().getClassLoader().getResource("sms.properties").getFile()));
            p.list(System.out);
            gatewayUsr = (String)p.get("gatewayUsr");
            gatewayPwd = (String)p.get("gatewayPwd");
            mobileNumber = (String)p.get("mobileNumber");
            gatewayUrl = (String)p.get("gatewayUrl");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void sendSMS() {
        SMS sms = new SMS();
        sms.setCode("123");
        sms.setMobileNumber(mobileNumber);
        sms.setMessage("from=Mauiroma ; phone=%phonenumber% ; body=Codice di accesso:%sms-code%");
        sms.setGatewayUrl(gatewayUrl);
        sms.setGatewayUsr(gatewayUsr);
        sms.setGatewayPwd(gatewayPwd);
        SMSGateway gateway = new PostGateway();
        boolean response = gateway.sendSmsCode(sms);
        assertTrue(response);
    }

}
