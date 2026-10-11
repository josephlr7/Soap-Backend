package cibertec.pe.main;

import cibertec.pe.config.SoapClienteConfig;
import cibertec.pe.wsclient.ComprobanteResponse;
import cibertec.pe.wsclient.IFacturacionNovaMarketService;

public class MainBuscarComprobante {
    public static void main(String[] args) {
        String numero = args.length > 0 ? args[0] : "F001-000001";
        IFacturacionNovaMarketService service = SoapClienteConfig.getService();
        ComprobanteResponse c = service.buscarComprobante(numero);
        System.out.println("Comprobante: " + c.getNumero());
        System.out.println("Cliente: " + c.getCliente());
        System.out.println("Empresa: " + c.getEmpresa());
        System.out.println("Total: " + c.getTotal());
        System.out.println("Estado: " + c.getEstado());
    }
}
