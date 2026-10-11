package cibertec.pe.main;

import cibertec.pe.config.SoapClienteConfig;
import cibertec.pe.wsclient.IFacturacionNovaMarketService;

public class MainAnularComprobante {
    public static void main(String[] args) {
        String numero = args.length > 0 ? args[0] : "F001-000001";
        IFacturacionNovaMarketService service = SoapClienteConfig.getService();
        System.out.println(service.anularComprobante(numero));
    }
}
