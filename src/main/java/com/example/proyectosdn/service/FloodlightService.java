package com.example.proyectosdn.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Slf4j
public class FloodlightService {
    // URL base del controlador Floodlight
    private final String FLOODLIGHT_BASE_URL = "http://10.20.12.232:8080"; // cambiar según el vnrt...
    private final RestTemplate restTemplate;

    public FloodlightService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        log.info("FloodlightService iniciado con URL base: {}", FLOODLIGHT_BASE_URL);
    }

    /**
     * Encuentra el punto de conexión de un dispositivo por su MAC o IP.
     * @param identifier MAC o IP del dispositivo a buscar
     * @param byIp true si la búsqueda es por IP, false si es por MAC
     * @return Map con DPID del switch y puerto donde está conectado el dispositivo
     */
    public Map<String, Object> getDeviceAttachmentPoint(String identifier, boolean byIp) {
        String url = FLOODLIGHT_BASE_URL + "/wm/device/all/json";
        log.info("Buscando dispositivo por {}: {}", byIp ? "IP" : "MAC", identifier);

        try {
            log.debug("Realizando petición GET a {}", url);
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.debug("Respuesta exitosa, procesando dispositivos");

                for (Map<String, Object> device : response.getBody()) {
                    // Busca en la lista de MACs o IPs según el parámetro byIp
                    List<?> searchField = (List<?>) device.get(byIp ? "ipv4" : "mac");
                    if (searchField != null && searchField.contains(identifier)) {
                        log.info("Dispositivo encontrado");

                        List<Map<String, Object>> attachmentPoints =
                                (List<Map<String, Object>>) device.get("attachmentPoint");

                        if (attachmentPoints != null && !attachmentPoints.isEmpty()) {
                            Map<String, Object> attachment = attachmentPoints.get(0);
                            String switchDPID = (String) attachment.get("switchDPID");
                            Integer port = (Integer) attachment.get("port");

                            log.info("Punto de conexión encontrado - Switch: {}, Puerto: {}",
                                    switchDPID, port);

                            Map<String, Object> result = new HashMap<>();
                            result.put("switchDPID", switchDPID);
                            result.put("port", port);
                            return result;
                        }
                    }
                }
                log.warn("No se encontró el dispositivo con {}: {}", byIp ? "IP" : "MAC", identifier);
            }
        } catch (Exception e) {
            log.error("Error al obtener punto de conexión del dispositivo: {}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * Obtiene la ruta entre dos puntos en la red sdn.
     * @param dpidSrc DPID del switch origen
     * @param portSrc Puerto origen
     * @param dpidDst DPID del switch destino
     * @param portDst Puerto destino
     * @return Lista de switches y puertos que forman la ruta
     */
    public List<Map<String, Object>> getRoute(String dpidSrc, int portSrc, String dpidDst, int portDst) {
        String url = String.format("%s/wm/topology/route/%s/%d/%s/%d/json",
                FLOODLIGHT_BASE_URL, dpidSrc, portSrc, dpidDst, portDst);

        log.info("Buscando ruta - Origen: {}:{} -> Destino: {}:{}",
                dpidSrc, portSrc, dpidDst, portDst);

        try {
            log.debug("Realizando petición GET a {}", url);
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                List<Map<String, Object>> route = response.getBody();
                log.info("Ruta encontrada con {} saltos", route != null ? route.size() : 0);
                return route;
            }
        } catch (Exception e) {
            log.error("Error al obtener ruta: {}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * Instala una regla de flujo en un switch OpenFlow.
     * @param dpid ID del switch
     * @param match Criterios de coincidencia para el flujo
     * @param actions Acciones a ejecutar cuando hay coincidencia
     * @param priority Prioridad del flujo
     * @return true si la instalación fue exitosa
     */
    public boolean addStaticFlow(String dpid, Map<String, String> match,
                                 List<Map<String, String>> actions, int priority) {
        String flowUrl = FLOODLIGHT_BASE_URL + "/wm/staticflowpusher/json";

        // Genera un nombre único para el flujo
        String flowName = "flow_" + priority + "_" + dpid + "_" + System.currentTimeMillis();
        log.info("Instalando flujo '{}' en switch {}", flowName, dpid);

        // Construye la regla de flujo
        Map<String, Object> flowRule = new HashMap<>(match);
        flowRule.put("switch", dpid);
        flowRule.put("name", flowName);
        flowRule.put("cookie", "0");
        flowRule.put("priority", String.valueOf(priority));
        flowRule.put("active", "true");

        // Convierte las acciones al formato de Floodlight
        String actionsStr = actions.stream()
                .map(action -> action.get("type") + "=" + action.get("port"))
                .collect(Collectors.joining(","));
        flowRule.put("actions", actionsStr);

        log.debug("Regla de flujo configurada: {}", flowRule);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(flowUrl, flowRule, String.class);
            boolean success = response.getStatusCode().is2xxSuccessful();
            log.info("Instalación de flujo {}: {}", success ? "exitosa" : "fallida", flowName);
            return success;
        } catch (Exception e) {
            log.error("Error al instalar flujo {}: {}", flowName, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Lo usábamos para crear flujos necesarios para una conexión bidireccional,
     * incluyendo reglas para TCP y ARP.
     * @param srcMac MAC origen
     * @param srcIp IP origen
     * @param dstIp IP destino
     * @param servicePort Puerto del servicio
     * @param route Ruta entre origen y destino
     * @return true si todos los flujos se instalaron correctamente
     */
    public boolean createPathFlows(String srcMac, String srcIp, String dstIp,
                                   int servicePort, List<Map<String, Object>> route) {
        log.info("Creando flujos para conexión - Origen: {}({}) -> Destino: {}:{}",
                srcMac, srcIp, dstIp, servicePort);

        try {
            // Itera sobre la ruta instalando flujos en cada switch
            for (int i = 0; i < route.size() - 1; i += 2) {
                Map<String, Object> currentSwitch = route.get(i);
                int inPort = ((Map<String, Integer>) currentSwitch.get("port")).get("portNumber");
                int outPort = ((Map<String, Integer>) route.get(i + 1).get("port")).get("portNumber");
                String dpid = (String) currentSwitch.get("switch");

                log.debug("Configurando switch {} - Puerto entrada: {}, Puerto salida: {}",
                        dpid, inPort, outPort);

                // Configura flujo TCP de ida
                Map<String, String> matchForward = new HashMap<>();
                matchForward.put("in_port", String.valueOf(inPort));
                matchForward.put("eth_type", "0x0800");  // IPv4
                matchForward.put("ipv4_src", srcIp);
                matchForward.put("ipv4_dst", dstIp);
                matchForward.put("ip_proto", "6");       // TCP
                matchForward.put("tcp_dst", String.valueOf(servicePort));

                // Configura flujo TCP de vuelta
                Map<String, String> matchBackward = new HashMap<>();
                matchBackward.put("in_port", String.valueOf(outPort));
                matchBackward.put("eth_type", "0x0800");
                matchBackward.put("ipv4_src", dstIp);
                matchBackward.put("ipv4_dst", srcIp);
                matchBackward.put("ip_proto", "6");
                matchBackward.put("tcp_src", String.valueOf(servicePort));

                // Configura flujo ARP
                Map<String, String> matchArpReq = new HashMap<>();
                matchArpReq.put("in_port", String.valueOf(inPort));
                matchArpReq.put("eth_type", "0x0806");  // ARP
                matchArpReq.put("eth_src", srcMac);

                List<Map<String, String>> forwardAction = List.of(
                        Map.of("type", "output", "port", String.valueOf(outPort)));
                List<Map<String, String>> backwardAction = List.of(
                        Map.of("type", "output", "port", String.valueOf(inPort)));

                // Instala todos los flujos necesarios
                if (!addStaticFlow(dpid, matchForward, forwardAction, 32768) ||
                        !addStaticFlow(dpid, matchBackward, backwardAction, 32768) ||
                        !addStaticFlow(dpid, matchArpReq, forwardAction, 32700)) {
                    log.error("Fallo al instalar flujos en switch {}", dpid);
                    return false;
                }

                log.info("Flujos instalados correctamente en switch {}", dpid);
            }
            log.info("Todos los flujos fueron instalados exitosamente");
            return true;
        } catch (Exception e) {
            log.error("Error al crear flujos en la ruta: {}", e.getMessage(), e);
            return false;
        }
    }
}