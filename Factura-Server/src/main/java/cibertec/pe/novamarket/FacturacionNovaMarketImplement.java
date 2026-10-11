package cibertec.pe.novamarket;

import java.util.List;
import org.springframework.stereotype.Component;
import jakarta.jws.WebService;

@WebService(endpointInterface = "cibertec.pe.novamarket.IFacturacionNovaMarketService")
@Component
public class FacturacionNovaMarketImplement implements IFacturacionNovaMarketService {
    private final NovaFacturacionService service;

    public FacturacionNovaMarketImplement(NovaFacturacionService service) {
        this.service = service;
    }

    @Override
    public ComprobanteResponse registrarVenta(VentaRequest request) {
        return service.registrarVenta(request);
    }

    @Override
    public ComprobanteResponse buscarComprobante(String numero) {
        return service.buscarComprobante(numero);
    }

    @Override
    public List<ComprobanteResponse> listarComprobantes() {
        return service.listarComprobantes();
    }

    @Override
    public String anularComprobante(String numero) {
        return service.anularComprobante(numero);
    }
}

