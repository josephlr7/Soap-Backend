package cibertec.pe.novamarket;

import java.util.List;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;

@WebService
public interface IFacturacionNovaMarketService {
    @WebMethod
    ComprobanteResponse registrarVenta(VentaRequest request);

    @WebMethod
    ComprobanteResponse buscarComprobante(String numero);

    @WebMethod
    List<ComprobanteResponse> listarComprobantes();

    @WebMethod
    String anularComprobante(String numero);
}

