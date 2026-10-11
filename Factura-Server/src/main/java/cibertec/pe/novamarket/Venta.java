package cibertec.pe.novamarket;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "ventas")
public class Venta {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    public LocalDateTime fecha = LocalDateTime.now();
    public BigDecimal subtotal;
    public BigDecimal igv;
    public BigDecimal total;
    public String estado;
    @ManyToOne public Cliente cliente;
    @ManyToOne public Empresa empresa;
    @ManyToOne public Cajero cajero;
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<VentaDetalle> detalles = new ArrayList<>();
}

