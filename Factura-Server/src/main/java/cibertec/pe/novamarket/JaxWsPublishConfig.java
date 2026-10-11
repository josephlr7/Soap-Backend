package cibertec.pe.novamarket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.xml.ws.Endpoint;

@Configuration
public class JaxWsPublishConfig {
    @Value("${novamarket.soap-url}") private String soapUrl;

    @Bean
    CommandLineRunner publicarSoapNovaMarket(FacturacionNovaMarketImplement implement) {
        return args -> {
            Endpoint.publish(soapUrl, implement);
            System.out.println("SOAP NovaMarket publicado en: " + soapUrl + "?wsdl");
        };
    }
}


