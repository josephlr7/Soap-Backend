package cibertec.pe.novamarket;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ComprobanteResponse {
    public String codigoEstado;
    public String mensaje;
    public String tipoComprobante;
    public String serie;
    public String numero;
    public String estado;
    public String cliente;
    public String empresa;
    public BigDecimal subtotal;
    public BigDecimal igv;
    public BigDecimal total;
    public String metodoPago;
    public String codigoOperacionInterna;
    public String hashFirma;
    public String fechaEmision;
    public String dniCliente;
    public String direccionCliente;
    public String rucEmpresa;
    public String direccionEmpresa;
    public String estadoSunatEmpresa;
    public String condicionSunatEmpresa;
    public String codigoCajero;
    public String nombreCajero;
    public Integer cantidadItems;
    public List<ComprobanteItemResponse> items = new ArrayList<>();
}

