package com.sumer.sumerstores.auth.config;
import com.sumer.sumerstores.services.KYCService;
import com.sumer.sumerstores.services.XDSLoginService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class KYCClientConfig {

    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("com.sumer.sumerstores.dto");
        return marshaller;
    }

    @Bean
    public KYCService kycService(XDSLoginService xdsLoginService, Jaxb2Marshaller marshaller) {
        return new KYCService(xdsLoginService, marshaller);
    }
}
