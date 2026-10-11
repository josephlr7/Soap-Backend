package cibertec.pe.novamarket;

import java.util.ArrayList;
import java.util.List;

public class VentaRequest {
    public String tipoComprobante;
    public String dniCliente;
    public String rucEmpresa;
    public String metodoPago;
    public String codigoCajero;
    public List<ItemVentaRequest> items = new ArrayList<>();
}

