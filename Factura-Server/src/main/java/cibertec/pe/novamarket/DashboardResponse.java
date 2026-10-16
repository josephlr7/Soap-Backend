package cibertec.pe.novamarket;

import java.math.BigDecimal;

public class DashboardResponse {
    public long productos;
    public long comprobantes;
    public long comprobantesEmitidos;
    public long comprobantesAnulados;
    public long clientes;
    public long empresas;
    public BigDecimal ventasAcumuladas = BigDecimal.ZERO;
    public long productosStockBajo;
}
