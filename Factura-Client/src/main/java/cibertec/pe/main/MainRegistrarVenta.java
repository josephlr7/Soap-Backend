package cibertec.pe.main;

import cibertec.pe.config.SoapClienteConfig;
import cibertec.pe.wsclient.ComprobanteResponse;
import cibertec.pe.wsclient.IFacturacionNovaMarketService;
import cibertec.pe.wsclient.ItemVentaRequest;
import cibertec.pe.wsclient.VentaRequest;

public class MainRegistrarVenta {
    public static void main(String[] args) {
        IFacturacionNovaMarketService service = SoapClienteConfig.getService();

        VentaRequest request = new VentaRequest();
        request.setTipoComprobante("FACTURA");
        request.setDniCliente("60142383");
        request.setRucEmpresa("20100070970");
        request.setMetodoPago("TARJETA");
        request.setCodigoCajero("CJ-001");

        ItemVentaRequest item1 = new ItemVentaRequest();
        item1.setProductoId(1L);
        item1.setCantidad(2);
        request.getItems().add(item1);

        ItemVentaRequest item2 = new ItemVentaRequest();
        item2.setProductoId(2L);
        item2.setCantidad(1);
        request.getItems().add(item2);

        ComprobanteResponse response = service.registrarVenta(request);
        imprimir(response);
    }

    private static void imprimir(ComprobanteResponse response) {
        System.out.println("Codigo: " + response.getCodigoEstado());
        System.out.println("Mensaje: " + response.getMensaje());
        System.out.println("Comprobante: " + response.getNumero());
        System.out.println("Cliente: " + response.getCliente());
        System.out.println("Empresa: " + response.getEmpresa());
        System.out.println("Total: " + response.getTotal());
        System.out.println("Operacion: " + response.getCodigoOperacionInterna());
    }
}
