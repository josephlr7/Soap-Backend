package cibertec.pe.novamarket;

import jakarta.persistence.*;

@Entity
@Table(name = "cajeros")
public class Cajero {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false) public String nombres;
    @Column(nullable = false, unique = true) public String codigoEmpleado;
    @Column(nullable = false) public String rol = "CAJERO";
    public Boolean activo = true;
}

