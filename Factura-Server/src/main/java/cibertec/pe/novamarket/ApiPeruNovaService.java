package cibertec.pe.novamarket;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ApiPeruNovaService {
    @Value("${novamarket.api-peru.base-url}") private String baseUrl;
    @Value("${novamarket.api-peru.token:}") private String token;
    private final ObjectMapper mapper;
    private final AuditoriaConsultaApiRepository auditoriaRepository;
    private final HttpClient client = HttpClient.newHttpClient();

    public ApiPeruNovaService(ObjectMapper mapper, AuditoriaConsultaApiRepository auditoriaRepository) {
        this.mapper = mapper;
        this.auditoriaRepository = auditoriaRepository;
    }

    public JsonNode consultarDni(String dni) { return consultar("/dni", "dni", dni, "DNI"); }
    public JsonNode consultarRuc(String ruc) { return consultar("/ruc", "ruc", ruc, "RUC"); }

    private JsonNode consultar(String path, String campo, String documento, String tipo) {
        if (token == null || token.isBlank()) {
            registrarAuditoria(tipo, documento, "SIN_TOKEN");
            throw new IllegalStateException("Configure APIPERU_TOKEN para consultas reales");
        }
        try {
            String body = "{\"" + campo + "\":\"" + documento + "\"}";
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            registrarAuditoria(tipo, documento, String.valueOf(response.statusCode()));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("ApiPeruDev HTTP " + response.statusCode() + ": " + response.body());
            }
            return mapper.readTree(response.body()).path("data");
        } catch (Exception ex) {
            registrarAuditoria(tipo, documento, "ERROR");
            throw new IllegalStateException("Error consultando ApiPeruDev: " + ex.getMessage(), ex);
        }
    }

    private void registrarAuditoria(String tipo, String documento, String resultado) {
        AuditoriaConsultaApi auditoria = new AuditoriaConsultaApi();
        auditoria.proveedor = "ApiPeruDev";
        auditoria.tipoConsulta = tipo;
        auditoria.documento = documento;
        auditoria.resultado = resultado;
        auditoriaRepository.save(auditoria);
    }
}

