package com.sumer.sumerstores.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class SoapConfig {

    public Jaxb2Marshaller marshaller(){
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("https://www.uat.xds.co.za/xdsconnect/XDSConnectWS.asmx?WSDL");
        return marshaller;
    }
}
