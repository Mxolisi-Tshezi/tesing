/*
package com.sumer.sumerstores.xds.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.WebServiceTemplate;
import com.sumer.sumerstores.xds.generated.Login;
import com.sumer.sumerstores.xds.generated.LoginResponse;

@Service
@RequiredArgsConstructor
public class SoapClient {

    private final WebServiceTemplate webServiceTemplate;

    @Autowired
    public SoapClient(Jaxb2Marshaller marshaller) {
        this.webServiceTemplate = new WebServiceTemplate(marshaller);
    }

    public LoginResponse login(String username, String password) {
        // Create the Login request object and set the required parameters
        Login request = new Login();
        request.setStrUser(username);
        request.setStrPwd(password);

        // Send the request and receive the response
        LoginResponse response = (LoginResponse) webServiceTemplate
                .marshalSendAndReceive("https://www.uat.xds.co.za/xdsconnect/XDSConnectWS.asmx", request);

        return response;
    }
}
*/
