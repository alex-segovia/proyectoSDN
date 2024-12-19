package com.example.proyectosdn.controller.auth;

import com.example.proyectosdn.entity.Dispositivo;
import com.example.proyectosdn.entity.Servicio;
import com.example.proyectosdn.entity.SesionActiva;
import com.example.proyectosdn.entity.Usuario;
import com.example.proyectosdn.extra.HttpClientService;
import com.example.proyectosdn.extra.Utilities;
import com.example.proyectosdn.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.tinyradius.packet.AccessRequest;
import org.tinyradius.packet.RadiusPacket;
import org.tinyradius.util.RadiusClient;

import java.net.http.HttpResponse;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/sdn/auth")
@Slf4j
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private String radiusServer = "127.0.0.1"; // Dirección del servidor RADIUS
    private String sharedSecret = "testing123"; // Secreto compartido
    private int authPort = 1812; // Puerto de autenticación (por defecto 1812)
    @Autowired
    private SesionActivaRepository sesionActivaRepository;
    @Autowired
    private ServicioRepository servicioRepository;
    @Autowired
    private DispositivoRepository dispositivoRepository;
    @Autowired
    private ConexionRepository conexionRepository;
    @Autowired
    private HttpClientService httpClientService;

    @PostMapping("/registrarNuevaConexion")
    public ResponseEntity<Map<String,Object>> registrarNuevaConexion(@RequestParam("macOrigen")String macOrigen,@RequestParam("macDestino")String macDestino,@RequestParam("idVlan")Integer idVlan,@RequestParam("puerto")String puertoStr,@RequestParam("timeout")Integer timeout){
        System.out.println("Método: registrarNuevaConexion. Datos: macOrigen: "+macOrigen+", macDestino: "+macDestino+", idVlan: "+idVlan+", puertoStr: "+puertoStr+", timeout: "+timeout);
        Map<String,Object>responseMap=new HashMap<>();
        Integer puerto=puertoStr.equals("null")?null:Integer.parseInt(puertoStr);
        if(macOrigen!=null&&!macOrigen.isBlank()&&macDestino!=null&&!macDestino.isBlank()&&idVlan!=null&&idVlan!=0){
            conexionRepository.registrarConexion(macOrigen,macDestino,idVlan,puerto,Utilities.obtenerFechaHoraActual(),timeout);
            responseMap.put("status","success");
            System.out.println("Success");
            return ResponseEntity.ok(responseMap);
        }else {
            System.out.println("Error: Algún/os dato/s no es/son válido/s.");
            responseMap.put("status", "error");
            responseMap.put("content", "Algún/os dato/s no es/son válido/s.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
        }
    }

    @PostMapping("/registrarDispositivoInvitado")
    public ResponseEntity<Map<String,Object>> registrarDispositivo(@RequestParam("mac")String mac){
        System.out.println("Método: registrarDispositivo. Datos: mac: "+mac);
        Map<String,Object>responseMap=new HashMap<>();
        if(mac!=null&&!mac.isBlank()){
            Dispositivo dispositivo=new Dispositivo();
            dispositivo.setMac(mac);
            dispositivo.setAutenticado(1);
            dispositivo.setEstado(1);
            dispositivoRepository.save(dispositivo);
            responseMap.put("status","success");
            System.out.println("Success");
            return ResponseEntity.ok(responseMap);
        }else {
            responseMap.put("status", "error");
            responseMap.put("content", "El dispositivo no cuenta con dirección MAC");
            System.out.println("Error: El dispositivo no cuenta con dirección MAC");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
        }
    }

    @PostMapping("/obtenerDispositivo")
    public ResponseEntity<Map<String,Object>> obtenerDispositivo(@RequestParam("mac") String mac) {
        System.out.println("Método: obtenerDispositivo. Datos: mac: "+mac);
        Map<String,Object>responseMap=new HashMap<>();
        Dispositivo dispositivo= dispositivoRepository.obtenerDispositivo(mac);
        Map<String,Object> content=new HashMap<>();
        if(dispositivo==null){
            responseMap.put("status","error");
            System.out.println("Error: No se encontró el dispositivo");
        }else {
            responseMap.put("status","success");
            content.put("id",dispositivo.getId());
            content.put("nombre",dispositivo.getNombre());
            content.put("usuario",dispositivo.getUsuario());
            content.put("autenticado",dispositivo.getAutenticado());
            content.put("estado",dispositivo.getEstado());
            System.out.println("Success");

            responseMap.put("content",content);
        }
        return ResponseEntity.ok(responseMap);
    }

    @PostMapping("/obtenerVinculoTerminales")
    public ResponseEntity<Map<String,Object>> obtenerVinculoTerminales(@RequestParam("macOrigen") String macOrigen, @RequestParam("macDestino") String macDestino) {
        System.out.println("Método: obtenerVinculoTerminales. Datos: macOrigen: "+macOrigen+", macDestino: "+macDestino);
        Map<String,Object>responseMap=new HashMap<>();
        Servicio servicioEnComun=servicioRepository.obtenerServicioEnComun(macOrigen,macDestino);
        Map<String,Object> content=new HashMap<>();
        content.put("servicio",servicioEnComun);
        System.out.println("Success");

        responseMap.put("status","success");
        responseMap.put("content",content);
        return ResponseEntity.ok(responseMap);
    }

    @PostMapping("/verificarUsuarioEnSesion")
    public ResponseEntity<Map<String,Object>> verificarUsuarioEnSesion(@RequestParam("username") String username) {
        System.out.println("Método: verificarUsuarioEnSesion. Datos: username: "+username);
        Map<String,Object>responseMap=new HashMap<>();
        Integer idSesion=sesionActivaRepository.idSesionActivaPorUsuario(username);
        responseMap.put("status",(idSesion==null?"error":"success"));
        Map<String,Object> content=new HashMap<>();
        System.out.println("Success");

        content.put("idSesion",idSesion);
        responseMap.put("content",content);
        return ResponseEntity.ok(responseMap);
    }



    @PostMapping("/autenticar")
    public ResponseEntity<Map<String,Object>> autenticar(@RequestParam("username") String username, @RequestParam("password") String password, HttpServletRequest httpRequest) {
        Map<String,Object>responseMap=new HashMap<>();
        try {
            String ipAdd = httpRequest.getHeader("X-Real-IP");
            if (ipAdd == null || ipAdd.isEmpty()) {
                ipAdd = httpRequest.getHeader("X-Forwarded-For");
                if (ipAdd == null || ipAdd.isEmpty()) {
                    ipAdd = httpRequest.getRemoteAddr();
                }
            }
            System.out.println("Intento de autenticación de "+ipAdd+" con el username "+username+" y contraseña "+password);
            String mac=httpClientService.obtenerMacPorIp(ipAdd);
            String usernameNuevo=usuarioRepository.obtenerUsernamePorDispositivo(mac);
            if(usernameNuevo!=null){
                if(!usernameNuevo.equals(username)){
                    System.out.println("Asociación de dispositivo fallida.");
                    responseMap.put("status","error");
                    responseMap.put("content","El dispositivo desde el que te estás intentado autenticar ya está registrado por otro usuario.");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
                }
            }else{
                dispositivoRepository.actualizarIdUsuario(username,mac);
            }
            RadiusClient client = new RadiusClient(radiusServer, sharedSecret);
            client.setAuthPort(authPort);
            AccessRequest request = new AccessRequest(username, password);
            request.setAuthProtocol(AccessRequest.AUTH_PAP);

            RadiusPacket response = client.authenticate(request);

            if (response.getPacketType() == RadiusPacket.ACCESS_ACCEPT) {
                SesionActiva sesionActiva=sesionActivaRepository.obtenerUltimaSesionActiva(username);
                if(sesionActiva!=null) {
                    sesionActiva.setLastActivity(Utilities.obtenerFechaHoraActual());
                    sesionActivaRepository.save(sesionActiva);
                }else {
                    sesionActiva=new SesionActiva();
                    sesionActiva.setSessionStart(Utilities.obtenerFechaHoraActual());
                    sesionActiva.setLastActivity(Utilities.obtenerFechaHoraActual());
                    sesionActiva.setSessionTimeout(3600);
                    sesionActiva.setUsername(username);
                    sesionActiva.setActive(true);
                    sesionActivaRepository.save(sesionActiva);
                }
                System.out.println("Authentication successful");
                responseMap.put("status","success");
                responseMap.put("ip",ipAdd);
                responseMap.put("content",true);
                return ResponseEntity.ok(responseMap);
            } else if (response.getPacketType() == RadiusPacket.ACCESS_REJECT) {
                System.out.println("Authentication failed");
                responseMap.put("status","success");
                responseMap.put("content",false);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseMap);
            } else {
                System.out.println("Unexpected response type: " + response.getPacketType());
                responseMap.put("status","error");
                responseMap.put("content",response.getPacketType());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Unexpected response type: " + e.getMessage());
            responseMap.put("status","error");
            responseMap.put("content",e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    @PostMapping("/autenticarSSH")
    public ResponseEntity<Map<String,Object>> autenticarSSH(@RequestParam("username") String username, @RequestParam("password") String password, @RequestParam("ip") String ipAdd) {
        Map<String,Object>responseMap=new HashMap<>();
        try {
            System.out.println("Intento de autenticación de "+ipAdd+" con el username "+username+" y contraseña "+password);
            String mac=httpClientService.obtenerMacPorIp(ipAdd);
            String usernameNuevo=usuarioRepository.obtenerUsernamePorDispositivo(mac);
            if(usernameNuevo!=null){
                if(!usernameNuevo.equals(username)){
                    System.out.println("Asociación de dispositivo fallida.");
                    responseMap.put("status","error");
                    responseMap.put("content","El dispositivo desde el que te estás intentado autenticar ya está registrado por otro usuario.");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
                }
            }else{
                dispositivoRepository.actualizarIdUsuario(username,mac);
            }
            RadiusClient client = new RadiusClient(radiusServer, sharedSecret);
            client.setAuthPort(authPort);
            AccessRequest request = new AccessRequest(username, password);
            request.setAuthProtocol(AccessRequest.AUTH_PAP);

            RadiusPacket response = client.authenticate(request);

            if (response.getPacketType() == RadiusPacket.ACCESS_ACCEPT) {
                SesionActiva sesionActiva=sesionActivaRepository.obtenerUltimaSesionActiva(username);
                if(sesionActiva!=null) {
                    sesionActiva.setLastActivity(Utilities.obtenerFechaHoraActual());
                    sesionActivaRepository.save(sesionActiva);
                }else {
                    sesionActiva=new SesionActiva();
                    sesionActiva.setSessionStart(Utilities.obtenerFechaHoraActual());
                    sesionActiva.setLastActivity(Utilities.obtenerFechaHoraActual());
                    sesionActiva.setSessionTimeout(3600);
                    sesionActiva.setUsername(username);
                    sesionActiva.setActive(true);
                    sesionActivaRepository.save(sesionActiva);
                }

                System.out.println("Authentication successful");
                responseMap.put("status","success");
                responseMap.put("ip",ipAdd);
                responseMap.put("content",true);
                return ResponseEntity.ok(responseMap);
            } else if (response.getPacketType() == RadiusPacket.ACCESS_REJECT) {
                System.out.println("Authentication failed");
                responseMap.put("status","success");
                responseMap.put("content",false);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseMap);
            } else {
                System.out.println("Unexpected response type: " + response.getPacketType());
                responseMap.put("status","error");
                responseMap.put("content",response.getPacketType());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Unexpected response type: " + e.getMessage());
            responseMap.put("status","error");
            responseMap.put("content",e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }


    @GetMapping("/")
    public String mostrarLogin(Model model,
                               @RequestParam(required = false) String logout,
                               HttpSession session) {
        return "login";
    }

    @PostMapping("/registro")
    public String registrarUsuario(@Valid Usuario usuario,
                                   BindingResult bindingResult,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {

        return "";
    }
}
