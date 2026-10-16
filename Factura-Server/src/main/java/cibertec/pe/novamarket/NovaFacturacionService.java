package cibertec.pe.novamarket;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;
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

    public DashboardResponse obtenerResumen() {
        DashboardResponse r = new DashboardResponse();
        List<Comprobante> comprobantes = comprobanteRepository.findAll();
        r.productos = productoRepository.count();
        r.comprobantes = comprobantes.size();
        r.comprobantesEmitidos = comprobantes.stream().filter(c -> "EMITIDO".equals(c.estado)).count();
        r.comprobantesAnulados = comprobantes.stream().filter(c -> "ANULADO".equals(c.estado)).count();
        r.clientes = clienteRepository.count();
        r.empresas = empresaRepository.count();
        r.ventasAcumuladas = comprobantes.stream().filter(c -> "EMITIDO".equals(c.estado))
                .map(c -> c.venta.total).reduce(BigDecimal.ZERO, BigDecimal::add);
        r.productosStockBajo = productoRepository.findAll().stream().filter(p -> p.stock <= 10).count();
        return r;
    }

    public DocumentoConsultaResponse consultarDni(String dni) {
        if (dni == null || !dni.matches("\\d{8}")) throw new IllegalArgumentException("El DNI debe tener 8 dígitos");
        return documentoCliente(resolverCliente(dni));
    }

    public DocumentoConsultaResponse consultarRuc(String ruc) {
        if (ruc == null || !ruc.matches("\\d{11}")) throw new IllegalArgumentException("El RUC debe tener 11 dígitos");
        return documentoEmpresa(resolverEmpresa(ruc));
    }

    @Transactional
    public ComprobanteResponse registrarVenta(VentaRequest request) {
        if (request.items == null || request.items.isEmpty()) throw new IllegalArgumentException("Agregue productos a la venta");
        String tipo = defaultText(request.tipoComprobante, "BOLETA").toUpperCase();
        if (!Set.of("FACTURA", "BOLETA").contains(tipo)) throw new IllegalArgumentException("Tipo de comprobante no válido");
        String metodoPago = defaultText(request.metodoPago, "TARJETA").toUpperCase();
        if (!Set.of("TARJETA", "EFECTIVO", "YAPE", "PLIN").contains(metodoPago)) throw new IllegalArgumentException("Método de pago no válido");
        String codigoEmpleado = defaultText(request.codigoCajero, "CJ-001");
        Empleado empleado = empleadoRepository.findByCodigoEmpleado(codigoEmpleado)
                .filter(e -> Boolean.TRUE.equals(e.activo))
                .orElseThrow(() -> new IllegalArgumentException("Empleado no autorizado para registrar ventas"));
        Cajero cajero = cajeroRepository.findByCodigoEmpleado(empleado.codigoEmpleado).orElseThrow();
        Cliente cliente = resolverCliente(request.dniCliente);
        Empresa empresa = "FACTURA".equals(tipo) ? resolverEmpresa(request.rucEmpresa) : null;
        if (empresa != null && ((empresa.estadoSunat != null && !empresa.estadoSunat.isBlank() && !"ACTIVO".equalsIgnoreCase(empresa.estadoSunat))
                || (empresa.condicionSunat != null && !empresa.condicionSunat.isBlank() && !"HABIDO".equalsIgnoreCase(empresa.condicionSunat)))) {
            throw new IllegalArgumentException("El RUC debe estar ACTIVO y HABIDO para emitir factura");
        }

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
            if (item.cantidad == null || item.cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
            Producto producto = productoRepository.findByIdForUpdate(item.productoId).orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + item.productoId));
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
        pago.metodoPago = metodoPago;
        pago.monto = venta.total;
        pago.estadoPago = "APROBADO_DEMO";
        pago.codigoOperacionInterna = "NOVA-" + firmaService.sha256(venta.id + "|" + venta.total).substring(0, 10).toUpperCase();
        pagoRepository.save(pago);

        Comprobante comp = new Comprobante();
        comp.venta = venta;
        comp.tipoComprobante = tipo;
        comp.serie = comp.tipoComprobante.equals("FACTURA") ? "F001" : "B001";
        comp.numero = comp.serie + "-" + String.format("%06d", venta.id);
        comp.estado = "EMITIDO";
        comp.hashFirma = firmaService.sha256(comp.numero + "|" + venta.total + "|" + pago.codigoOperacionInterna);
        comprobanteRepository.save(comp);
        return toResponse(comp, pago, "ACEPTADO", "Venta registrada desde caja NovaMarket");
    }

    public List<ComprobanteResponse> listarComprobantes() {
        return comprobanteRepository.findAllByOrderByFechaEmisionDesc().stream()
                .map(c -> toResponse(c, pagoRepository.findByVentaId(c.venta.id).orElse(null), "OK", "Comprobante registrado")).toList();
    }

    public ComprobanteResponse buscarComprobante(String numero) {
        Comprobante c = comprobanteRepository.findByNumero(numero).orElseThrow(() -> new IllegalArgumentException("Comprobante no encontrado"));
        Pago p = pagoRepository.findByVentaId(c.venta.id).orElse(null);
        return toResponse(c, p, "ENCONTRADO", "Comprobante encontrado");
    }

    @Transactional
    public String anularComprobante(String numero) {
        anularComprobanteDetalle(numero);
        return "Comprobante anulado: " + numero;
    }

    @Transactional
    public ComprobanteResponse anularComprobanteDetalle(String numero) {
        Comprobante c = comprobanteRepository.findByNumero(numero).orElseThrow(() -> new IllegalArgumentException("Comprobante no encontrado"));
        if ("ANULADO".equals(c.estado)) throw new IllegalArgumentException("El comprobante ya se encuentra anulado");
        c.estado = "ANULADO";
        c.venta.estado = "ANULADA";
        c.venta.detalles.forEach(detalle -> {
            Producto producto = productoRepository.findByIdForUpdate(detalle.producto.id).orElseThrow();
            producto.stock += detalle.cantidad;
            productoRepository.save(producto);
        });
        Pago pago = pagoRepository.findByVentaId(c.venta.id).orElse(null);
        if (pago != null) {
            pago.estadoPago = "ANULADO";
            pagoRepository.save(pago);
        }
        comprobanteRepository.save(c);
        return toResponse(c, pago, "ANULADO", "Comprobante anulado y stock repuesto");
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
            c.ubigeo = data.path("ubigeo_sunat").asText(data.path("ubigeo").asText(""));
            c.departamento = data.path("departamento").asText("");
            c.provincia = data.path("provincia").asText("");
            c.distrito = data.path("distrito").asText("");
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
            e.nombreComercial = data.path("nombre_comercial").asText("");
            e.ubigeo = data.path("ubigeo_sunat").asText(data.path("ubigeo").asText(""));
            e.departamento = data.path("departamento").asText("");
            e.provincia = data.path("provincia").asText("");
            e.distrito = data.path("distrito").asText("");
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
        r.codigoEstado = codigo; r.mensaje = mensaje; r.tipoComprobante = c.tipoComprobante; r.serie = c.serie; r.numero = c.numero; r.estado = c.estado;
        r.cliente = c.venta.cliente == null ? "CONSUMIDOR FINAL" : c.venta.cliente.nombreCompleto;
        r.empresa = c.venta.empresa == null ? "" : c.venta.empresa.razonSocial;
        r.subtotal = c.venta.subtotal; r.igv = c.venta.igv; r.total = c.venta.total;
        r.metodoPago = p == null ? "" : p.metodoPago; r.codigoOperacionInterna = p == null ? "" : p.codigoOperacionInterna; r.hashFirma = c.hashFirma;
        r.fechaEmision = c.fechaEmision == null ? "" : c.fechaEmision.toString();
        r.dniCliente = c.venta.cliente == null ? "" : c.venta.cliente.dni;
        r.direccionCliente = c.venta.cliente == null ? "" : c.venta.cliente.direccion;
        r.rucEmpresa = c.venta.empresa == null ? "" : c.venta.empresa.ruc;
        r.direccionEmpresa = c.venta.empresa == null ? "" : c.venta.empresa.direccion;
        r.estadoSunatEmpresa = c.venta.empresa == null ? "" : c.venta.empresa.estadoSunat;
        r.condicionSunatEmpresa = c.venta.empresa == null ? "" : c.venta.empresa.condicionSunat;
        r.codigoCajero = c.venta.cajero == null ? "" : c.venta.cajero.codigoEmpleado;
        r.nombreCajero = c.venta.cajero == null ? "" : c.venta.cajero.nombres;
        r.items = c.venta.detalles.stream().map(d -> {
            ComprobanteItemResponse item = new ComprobanteItemResponse();
            item.productoId = d.producto.id; item.sku = d.producto.sku; item.descripcion = d.producto.nombre;
            item.unidad = d.producto.unidad; item.cantidad = d.cantidad; item.precioUnitario = d.precioUnitario; item.importe = d.importe;
            return item;
        }).toList();
        r.cantidadItems = r.items.stream().mapToInt(i -> i.cantidad).sum();
        return r;
    }

    private DocumentoConsultaResponse documentoCliente(Cliente c) {
        DocumentoConsultaResponse r = new DocumentoConsultaResponse();
        r.tipoDocumento = "DNI"; r.numeroDocumento = c.dni; r.nombreCompleto = c.nombreCompleto;
        r.nombres = c.nombres; r.apellidos = c.apellidos; r.direccion = c.direccion; r.ubigeo = c.ubigeo;
        r.departamento = c.departamento; r.provincia = c.provincia; r.distrito = c.distrito;
        r.valido = c.nombreCompleto != null && !c.nombreCompleto.isBlank(); r.mensaje = r.valido ? "Identidad validada" : "DNI sin datos completos";
        return r;
    }

    private DocumentoConsultaResponse documentoEmpresa(Empresa e) {
        DocumentoConsultaResponse r = new DocumentoConsultaResponse();
        r.tipoDocumento = "RUC"; r.numeroDocumento = e.ruc; r.razonSocial = e.razonSocial; r.nombreComercial = e.nombreComercial;
        r.direccion = e.direccion; r.ubigeo = e.ubigeo; r.departamento = e.departamento; r.provincia = e.provincia; r.distrito = e.distrito;
        r.estado = e.estadoSunat; r.condicion = e.condicionSunat;
        r.valido = "ACTIVO".equalsIgnoreCase(e.estadoSunat) && "HABIDO".equalsIgnoreCase(e.condicionSunat);
        r.mensaje = r.valido ? "Contribuyente apto para facturación" : "Revise el estado y condición SUNAT";
        return r;
    }

    private String defaultText(String value, String fallback) { return value == null || value.isBlank() ? fallback : value; }
}

