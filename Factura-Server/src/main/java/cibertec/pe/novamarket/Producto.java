package cibertec.pe.novamarket;

import java.math.BigDecimal;
import jakarta.persistence.*;

@Entity
@Table(name = "productos")
public class Producto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false) public String nombre;
    @Column(nullable = false, unique = true) public String sku;
    @Column(nullable = false) public BigDecimal precio;
    @Column(nullable = false) public Integer stock;
    public String unidad;
    @ManyToOne public Categoria categoria;
}

