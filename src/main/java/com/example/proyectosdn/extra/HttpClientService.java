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

    public HttpClientService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String obtenerMacPorIp(String ipDestino) {
        String url = "http://localhost:8082" + "/wm/device/";
        try {
            ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> devices = response.getBody();
                for (Map<String, Object> device : devices) {
                    List<String> ipv4Addresses = (List<String>) device.get("ipv4");
                    if (ipv4Addresses != null && ipv4Addresses.contains(ipDestino)) {
                        return (String) device.get("mac");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al consultar el API de Floodlight: " + e.getMessage());
        }
        return null;
    }
}
