package cibertec.pe.novamarket;

import jakarta.persistence.*;

@Entity
@Table(name = "clientes")
public class Cliente {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(unique = true, length = 8) public String dni;
    public String nombres;
    public String apellidos;
    public String nombreCompleto;
    public String direccion;
}

