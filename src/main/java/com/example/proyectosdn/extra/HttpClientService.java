package com.example.proyectosdn.extra;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HttpClientService {
    private final RestTemplate restTemplate;

    // Constructor
    public HttpClientService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Método para obtener la dirección MAC asociada a una IP usando el API de Floodlight.
     *
     * @param ipDestino Dirección IP del destino.
     * @return Dirección MAC asociada, o null si no se encuentra.
     */
    public String obtenerMacPorIp(String ipDestino) {
        String url = "http://localhost:8082/wm/device/";
        try {
            // Realizar la solicitud al API de Floodlight
            ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Iterar sobre la lista de dispositivos
                List<Map<String, Object>> devices = response.getBody();
                for (Map<String, Object> device : devices) {
                    // Obtener la lista de direcciones IPv4 del dispositivo
                    List<String> ipv4Addresses = (List<String>) device.get("ipv4");
                    if (ipv4Addresses != null && ipv4Addresses.contains(ipDestino)) {
                        // Obtener la lista de direcciones MAC y devolver la primera (puedes cambiar esto según tus necesidades)
                        List<String> macAddresses = (List<String>) device.get("mac");
                        if (macAddresses != null && !macAddresses.isEmpty()) {
                            return macAddresses.get(0); // Devolver la primera MAC
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al consultar el API de Floodlight: " + e.getMessage());
        }
        return null; // Retornar null si no se encuentra la MAC
    }

    /**
     * Método para eliminar reglas en Floodlight basadas en la dirección MAC.
     *
     * @param macAddress Dirección MAC para la cual se eliminarán las reglas.
     * @return Respuesta del servidor Floodlight.
     */
    public String eliminarReglasPorMac(String macAddress) {
        String url = "http://localhost:8082/custom/deleteRulesByMac";
        try {
            // Configurar headers para la solicitud POST
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN); // Indicamos que enviamos texto plano

            // Crear la entidad HTTP con la dirección MAC
            HttpEntity<String> request = new HttpEntity<>(macAddress, headers);

            // Realizar la solicitud POST
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            System.out.println("Eliminando reglas que tengan la dirección MAC: " + macAddress);
            // Retornar la respuesta del servidor
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al eliminar reglas en Floodlight: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }
}
