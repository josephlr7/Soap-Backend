package cibertec.pe.novamarket;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "comprobantes")
public class Comprobante {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @OneToOne(optional = false) public Venta venta;
    public String tipoComprobante;
    public String serie;
    @Column(unique = true) public String numero;
    public String estado;
    public String hashFirma;
    public LocalDateTime fechaEmision = LocalDateTime.now();
}

