package cibertec.pe.novamarket;

import java.math.BigDecimal;
import jakarta.persistence.*;

@Entity
@Table(name = "pagos")
public class Pago {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @OneToOne(optional = false) public Venta venta;
    public String metodoPago;
    public String codigoOperacionInterna;
    public BigDecimal monto;
    public String estadoPago;
}

