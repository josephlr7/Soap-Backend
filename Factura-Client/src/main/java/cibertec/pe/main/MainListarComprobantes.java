package cibertec.pe.main;

import cibertec.pe.config.SoapClienteConfig;
import cibertec.pe.wsclient.ComprobanteResponse;
import cibertec.pe.wsclient.IFacturacionNovaMarketService;

public class MainListarComprobantes {
    public static void main(String[] args) {
        IFacturacionNovaMarketService service = SoapClienteConfig.getService();
        for (ComprobanteResponse c : service.listarComprobantes()) {
            System.out.println(c.getNumero() + " | " + c.getCliente() + " | S/ " + c.getTotal() + " | " + c.getEstado());
        }
    }
}
