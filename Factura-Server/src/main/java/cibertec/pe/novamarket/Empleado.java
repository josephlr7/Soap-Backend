package cibertec.pe.novamarket;

import jakarta.persistence.*;

@Entity
@Table(name = "empleados")
public class Empleado {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false, unique = true) public String codigoEmpleado;
    @Column(nullable = false, unique = true) public String usuario;
    @Column(nullable = false) public String passwordHash;
    @Column(nullable = false) public String nombres;
    @Column(nullable = false) public String rol = "CAJERO";
    public Boolean activo = true;
}
