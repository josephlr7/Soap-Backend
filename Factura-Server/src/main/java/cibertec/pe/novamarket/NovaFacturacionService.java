package cibertec.pe.novamarket;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class NovaFacturacionService {
    private final ClienteRepository clienteRepository;
    private final EmpresaRepository empresaRepository;
    private final ProductoRepository productoRepository;
    private final CajeroRepository cajeroRepository;
    private final EmpleadoRepository empleadoRepository;
    private final VentaRepository ventaRepository;
    private final ComprobanteRepository comprobanteRepository;
    private final PagoRepository pagoRepository;
    private final ApiPeruNovaService apiPeru;
    private final FirmaNovaService firmaService;

    public NovaFacturacionService(ClienteRepository clienteRepository, EmpresaRepository empresaRepository,
            ProductoRepository productoRepository, CajeroRepository cajeroRepository, EmpleadoRepository empleadoRepository, VentaRepository ventaRepository,
            ComprobanteRepository comprobanteRepository, PagoRepository pagoRepository, ApiPeruNovaService apiPeru,
            FirmaNovaService firmaService) {
        this.clienteRepository = clienteRepository;
        this.empresaRepository = empresaRepository;
        this.productoRepository = productoRepository;
        this.cajeroRepository = cajeroRepository;
        this.empleadoRepository = empleadoRepository;
        this.ventaRepository = ventaRepository;
        this.comprobanteRepository = comprobanteRepository;
        this.pagoRepository = pagoRepository;
        this.apiPeru = apiPeru;
        this.firmaService = firmaService;
    }

    public List<ProductoResponse> listarProductos() {
        return productoRepository.findAll().stream().map(this::productoResponse).toList();
    }

    @Transactional
    public ComprobanteResponse registrarVenta(VentaRequest request) {
        if (request.items == null || request.items.isEmpty()) throw new IllegalArgumentException("Agregue productos a la venta");
        String codigoEmpleado = defaultText(request.codigoCajero, "CJ-001");
        Empleado empleado = empleadoRepository.findByCodigoEmpleado(codigoEmpleado)
                .filter(e -> Boolean.TRUE.equals(e.activo))
                .orElseThrow(() -> new IllegalArgumentException("Empleado no autorizado para registrar ventas"));
        Cajero cajero = cajeroRepository.findByCodigoEmpleado(empleado.codigoEmpleado).orElseThrow();
        Cliente cliente = resolverCliente(request.dniCliente);
        Empresa empresa = "FACTURA".equalsIgnoreCase(request.tipoComprobante) ? resolverEmpresa(request.rucEmpresa) : null;

        Venta venta = new Venta();
        venta.cliente = cliente;
        venta.empresa = empresa;
        venta.cajero = cajero;
        venta.estado = "REGISTRADA";
        venta.subtotal = BigDecimal.ZERO;
        venta.igv = BigDecimal.ZERO;
        venta.total = BigDecimal.ZERO;
        venta = ventaRepository.save(venta);

        BigDecimal subtotal = BigDecimal.ZERO;
        for (ItemVentaRequest item : request.items) {
            Producto producto = productoRepository.findById(item.productoId).orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + item.productoId));
            if (producto.stock < item.cantidad) throw new IllegalArgumentException("Stock insuficiente: " + producto.nombre);
            producto.stock -= item.cantidad;
            productoRepository.save(producto);

            VentaDetalle detalle = new VentaDetalle();
            detalle.venta = venta;
            detalle.producto = producto;
            detalle.cantidad = item.cantidad;
            detalle.precioUnitario = producto.precio;
            detalle.importe = producto.precio.multiply(BigDecimal.valueOf(item.cantidad));
            venta.detalles.add(detalle);
            subtotal = subtotal.add(detalle.importe);
        }

        venta.subtotal = subtotal.divide(new BigDecimal("1.18"), 2, RoundingMode.HALF_UP);
        venta.igv = subtotal.subtract(venta.subtotal).setScale(2, RoundingMode.HALF_UP);
        venta.total = subtotal.setScale(2, RoundingMode.HALF_UP);
        venta = ventaRepository.save(venta);

        Pago pago = new Pago();
        pago.venta = venta;
        pago.metodoPago = defaultText(request.metodoPago, "TARJETA").toUpperCase();
        pago.monto = venta.total;
        pago.estadoPago = "APROBADO_DEMO";
        pago.codigoOperacionInterna = "NOVA-" + firmaService.sha256(venta.id + "|" + venta.total).substring(0, 10).toUpperCase();
        pagoRepository.save(pago);

        Comprobante comp = new Comprobante();
        comp.venta = venta;
        comp.tipoComprobante = defaultText(request.tipoComprobante, "BOLETA").toUpperCase();
        comp.serie = comp.tipoComprobante.equals("FACTURA") ? "F001" : "B001";
        comp.numero = comp.serie + "-" + String.format("%06d", venta.id);
        comp.estado = "EMITIDO";
        comp.hashFirma = firmaService.sha256(comp.numero + "|" + venta.total + "|" + pago.codigoOperacionInterna);
        comprobanteRepository.save(comp);
        return toResponse(comp, pago, "ACEPTADO", "Venta registrada desde caja NovaMarket");
    }

    public List<ComprobanteResponse> listarComprobantes() {
        return comprobanteRepository.findAll().stream().map(c -> toResponse(c, pagoRepository.findAll().stream().filter(p -> p.venta.id.equals(c.venta.id)).findFirst().orElse(null), "OK", "Comprobante registrado")).toList();
    }

    public ComprobanteResponse buscarComprobante(String numero) {
        Comprobante c = comprobanteRepository.findByNumero(numero).orElseThrow(() -> new IllegalArgumentException("Comprobante no encontrado"));
        Pago p = pagoRepository.findAll().stream().filter(pg -> pg.venta.id.equals(c.venta.id)).findFirst().orElse(null);
        return toResponse(c, p, "ENCONTRADO", "Comprobante encontrado");
    }

    @Transactional
    public String anularComprobante(String numero) {
        Comprobante c = comprobanteRepository.findByNumero(numero).orElseThrow(() -> new IllegalArgumentException("Comprobante no encontrado"));
        c.estado = "ANULADO";
        c.venta.estado = "ANULADA";
        comprobanteRepository.save(c);
        return "Comprobante anulado: " + numero;
    }

    private Cliente resolverCliente(String dni) {
        if (dni == null || dni.isBlank()) return null;
        return clienteRepository.findByDni(dni).orElseGet(() -> {
            JsonNode data = apiPeru.consultarDni(dni);
            Cliente c = new Cliente();
            c.dni = dni;
            c.nombres = data.path("nombres").asText("");
            c.apellidos = (data.path("apellido_paterno").asText("") + " " + data.path("apellido_materno").asText("")).trim();
            c.nombreCompleto = data.path("nombre_completo").asText((c.nombres + " " + c.apellidos).trim());
            c.direccion = data.path("direccion_completa").asText("");
            return clienteRepository.save(c);
        });
    }

    private Empresa resolverEmpresa(String ruc) {
        if (ruc == null || ruc.isBlank()) throw new IllegalArgumentException("La factura requiere RUC");
        return empresaRepository.findByRuc(ruc).orElseGet(() -> {
            JsonNode data = apiPeru.consultarRuc(ruc);
            Empresa e = new Empresa();
            e.ruc = ruc;
            e.razonSocial = data.path("nombre_o_razon_social").asText("");
            e.estadoSunat = data.path("estado").asText("");
            e.condicionSunat = data.path("condicion").asText("");
            e.direccion = data.path("direccion_completa").asText("");
            return empresaRepository.save(e);
        });
    }

    private ProductoResponse productoResponse(Producto p) {
        ProductoResponse r = new ProductoResponse();
        r.id = p.id; r.sku = p.sku; r.nombre = p.nombre; r.precio = p.precio; r.stock = p.stock; r.categoria = p.categoria == null ? "" : p.categoria.nombre;
        return r;
    }

    private ComprobanteResponse toResponse(Comprobante c, Pago p, String codigo, String mensaje) {
        ComprobanteResponse r = new ComprobanteResponse();
        r.codigoEstado = codigo; r.mensaje = mensaje; r.serie = c.serie; r.numero = c.numero; r.estado = c.estado;
        r.cliente = c.venta.cliente == null ? "CONSUMIDOR FINAL" : c.venta.cliente.nombreCompleto;
        r.empresa = c.venta.empresa == null ? "" : c.venta.empresa.razonSocial;
        r.subtotal = c.venta.subtotal; r.igv = c.venta.igv; r.total = c.venta.total;
        r.metodoPago = p == null ? "" : p.metodoPago; r.codigoOperacionInterna = p == null ? "" : p.codigoOperacionInterna; r.hashFirma = c.hashFirma;
        return r;
    }

    private String defaultText(String value, String fallback) { return value == null || value.isBlank() ? fallback : value; }
}

