package cibertec.pe.novamarket;

import java.math.BigDecimal;
import jakarta.persistence.*;

@Entity
@Table(name = "venta_detalles")
public class VentaDetalle {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @ManyToOne(optional = false) public Venta venta;
    @ManyToOne(optional = false) public Producto producto;
    public Integer cantidad;
    public BigDecimal precioUnitario;
    public BigDecimal importe;
}

