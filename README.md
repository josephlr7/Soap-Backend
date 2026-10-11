# Simulador de Facturacion Electronica con SOAP

Proyecto academico sin frontend. Contiene un servidor SOAP en Spring Boot y un cliente Java por consola.

## Estructura

```text
Factura-Server  -> Publica el servicio SOAP
Factura-Client  -> Consume el servicio SOAP desde consola
```

## Servicio SOAP

El contrato esta en:

```text
Factura-Server/src/main/resources/factura.xsd
```

Datos de entrada:

- RUC del emisor
- DNI del cliente, opcional
- Tipo de comprobante: FACTURA, BOLETA o NOTA_CREDITO
- Numero de comprobante
- Monto total

Datos de salida:

- Codigo de estado
- Mensaje
- Estado de validacion SUNAT
- Razon social del emisor
- Estado de validacion RENIEC
- Nombres del cliente
- Codigo de operacion interna demo
- Hash de firma digital simulada

Operaciones SOAP disponibles:

- `registrarFactura`
- `buscarFactura`
- `listarFacturas`
- `actualizarEstadoFactura`
- `eliminarFactura`

## Integracion SUNAT / RENIEC real

El proyecto queda preparado para consumir servicios reales mediante un proveedor
compatible con `apis.net.pe`. El token no se guarda en el codigo: debe pasarse
por variable de entorno.

Configuracion actual:

```yaml
integraciones:
  modo: REAL
  base-url: https://api.apiperu.dev
  token: ${APIS_NET_PE_TOKEN:}
  sunat-ruc-path: /ruc
  reniec-dni-path: /dni
```

Antes de levantar el servidor, configura el token:

```powershell
$env:APIS_NET_PE_TOKEN="TOKEN_DE_TU_COMPANERO"
.\mvnw.cmd -pl Factura-Server spring-boot:run
```

Importante: este proyecto solo consulta datos basicos para validacion. No emite
comprobantes reales ante SUNAT, no registra pagos reales y no reemplaza una
pasarela de pagos. RENIEC oficial no publica una API gratuita abierta para datos
de DNI; su servicio formal requiere convenio y costo por consulta.

Si quieres volver al modo academico sin consultar internet, cambia temporalmente:

```yaml
integraciones:
  modo: MOCK
```

## Ejecutar el servidor

Desde la raiz del proyecto:

```bash
.\mvnw.cmd -pl Factura-Server spring-boot:run
```

El servidor queda disponible en:

```text
http://localhost:8085/ws
```

El WSDL se puede revisar en:

```text
http://localhost:8085/ws/facturas.wsdl
```

## Ejecutar el cliente por consola

Con el servidor encendido, abre otra consola y ejecuta:

```bash
.\mvnw.cmd -pl Factura-Client spring-boot:run
```

Tambien puedes ejecutar el JAR con datos por argumentos:

```bash
java -jar Factura-Client/target/Factura-Client-0.0.1-SNAPSHOT.jar 20123456789 FACTURA F001-000001 150.50
```

Con DNI opcional:

```bash
java -jar Factura-Client/target/Factura-Client-0.0.1-SNAPSHOT.jar registrar 20123456789 FACTURA F001-000001 150.50 12345678
```

Buscar una factura:

```bash
java -jar Factura-Client/target/Factura-Client-0.0.1-SNAPSHOT.jar buscar F001-000001
```

Listar facturas:

```bash
java -jar Factura-Client/target/Factura-Client-0.0.1-SNAPSHOT.jar listar
```

Actualizar estado:

```bash
java -jar Factura-Client/target/Factura-Client-0.0.1-SNAPSHOT.jar estado F001-000001 OBSERVADA
```

Eliminar factura:

```bash
java -jar Factura-Client/target/Factura-Client-0.0.1-SNAPSHOT.jar eliminar F001-000001
```

Datos de prueba:

```text
RUC emisor: 20123456789
DNI cliente: 12345678
Tipo comprobante: FACTURA
Numero comprobante: F001-000001
Monto total: 150.50
```

Respuesta esperada:

```text
Codigo : ACEPTADO
Mensaje: Comprobante F001-000001 aceptado en modo demo. Operacion interna: OP-DEMO-XXXXXXXXXXXX
SUNAT  : SUNAT_SIMULADO
Emisor : EMPRESA DEMO S.A.C.
RENIEC : RENIEC_SIMULADO
Cliente: CLIENTE DEMO
OpDemo : OP-DEMO-XXXXXXXXXXXX
Hash   : valor SHA-256 generado por el servidor
```

En modo `REAL`, los campos `Emisor` y `Cliente` muestran los datos devueltos
por los servicios configurados. En modo `MOCK`, esos campos muestran datos demo
para evitar consultas reales durante pruebas academicas.
