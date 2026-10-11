package cibertec.pe.config;

import cibertec.pe.wsclient.FacturacionNovaMarketImplementService;
import cibertec.pe.wsclient.IFacturacionNovaMarketService;
import jakarta.xml.ws.BindingProvider;

public class SoapClienteConfig {
    private static final String DEFAULT_SOAP_URL = "http://localhost:8090/ws/NovaMarketFacturacion";

    public static IFacturacionNovaMarketService getService() {
        FacturacionNovaMarketImplementService service = new FacturacionNovaMarketImplementService();
        IFacturacionNovaMarketService port = service.getFacturacionNovaMarketImplementPort();
        String endpoint = System.getenv().getOrDefault("NOVAMARKET_SOAP_URL", DEFAULT_SOAP_URL);
        ((BindingProvider) port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);
        return port;
    }
}
