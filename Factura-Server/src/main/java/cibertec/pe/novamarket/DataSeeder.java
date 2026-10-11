package cibertec.pe.novamarket;

import java.math.BigDecimal;
import java.security.SecureRandom;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final CajeroRepository cajeroRepository;
    private final EmpleadoRepository empleadoRepository;
    @Value("${novamarket.admin.password:}") private String adminPassword;

    public DataSeeder(CategoriaRepository categoriaRepository, ProductoRepository productoRepository, CajeroRepository cajeroRepository,
            EmpleadoRepository empleadoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.cajeroRepository = cajeroRepository;
        this.empleadoRepository = empleadoRepository;
    }

    @Override public void run(String... args) {
        if (empleadoRepository.findByCodigoEmpleado("CJ-001").isEmpty()) {
            String password = adminPassword == null || adminPassword.isBlank() ? generarPasswordTemporal() : adminPassword;
            Empleado e = new Empleado();
            e.codigoEmpleado = "CJ-001";
            e.usuario = "cajero";
            e.passwordHash = new BCryptPasswordEncoder().encode(password);
            e.nombres = "Cajero Nova";
            e.rol = "CAJERO";
            empleadoRepository.save(e);
            System.out.println("Empleado inicial creado. Usuario: cajero | Password temporal: " + password);
        }
        if (cajeroRepository.findByCodigoEmpleado("CJ-001").isEmpty()) {
            Cajero c = new Cajero(); c.codigoEmpleado = "CJ-001"; c.nombres = "Cajero Nova"; cajeroRepository.save(c);
        }
        if (productoRepository.count() > 0) return;
        Categoria abarrotes = categoria("Abarrotes", "Productos de despensa");
        Categoria bebidas = categoria("Bebidas", "Bebidas familiares");
        producto("NM-ARROZ-001", "Arroz extra 5kg", new BigDecimal("22.90"), 80, abarrotes);
        producto("NM-ACEITE-001", "Aceite vegetal 900ml", new BigDecimal("9.50"), 120, abarrotes);
        producto("NM-LECHE-001", "Leche evaporada pack x6", new BigDecimal("23.40"), 55, abarrotes);
        producto("NM-AGUA-001", "Agua sin gas 2.5L", new BigDecimal("3.90"), 140, bebidas);
        producto("NM-GASEOSA-001", "Gaseosa familiar 3L", new BigDecimal("10.90"), 70, bebidas);
    }
    private Categoria categoria(String nombre, String desc) { Categoria c = new Categoria(); c.nombre = nombre; c.descripcion = desc; return categoriaRepository.save(c); }
    private void producto(String sku, String nombre, BigDecimal precio, int stock, Categoria cat) { Producto p = new Producto(); p.sku = sku; p.nombre = nombre; p.precio = precio; p.stock = stock; p.unidad = "UND"; p.categoria = cat; productoRepository.save(p); }
    private String generarPasswordTemporal() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 12; i++) password.append(chars.charAt(random.nextInt(chars.length())));
        return password.toString();
    }
}

