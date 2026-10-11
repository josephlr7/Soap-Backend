package cibertec.pe.novamarket;

import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/caja")
@CrossOrigin(originPatterns = {
        "http://localhost:*",
        "http://127.0.0.1:*",
        "http://192.168.*.*:*",
        "http://10.*.*.*:*",
        "http://172.*.*.*:*"
})
public class CajaController {
    private final NovaFacturacionService service;
    public CajaController(NovaFacturacionService service) { this.service = service; }

    @GetMapping("/productos") public List<ProductoResponse> productos() { return service.listarProductos(); }
    @PostMapping("/ventas") public ComprobanteResponse registrarVenta(@RequestBody VentaRequest request) { return service.registrarVenta(request); }
    @GetMapping("/comprobantes") public List<ComprobanteResponse> comprobantes() { return service.listarComprobantes(); }
    @GetMapping("/comprobantes/{numero}") public ComprobanteResponse buscar(@PathVariable String numero) { return service.buscarComprobante(numero); }
    @PutMapping("/comprobantes/{numero}/anular") public String anular(@PathVariable String numero) { return service.anularComprobante(numero); }
}

