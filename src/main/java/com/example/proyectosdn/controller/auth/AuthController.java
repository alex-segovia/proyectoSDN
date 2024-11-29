package com.example.proyectosdn.controller.auth;

import com.example.proyectosdn.entity.Usuario;
import com.example.proyectosdn.repository.UsuarioRepository;
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

@Controller
@RequestMapping("/autenticacion")
@Slf4j
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private String radiusServer = "127.0.0.1"; // Dirección del servidor RADIUS
    private String sharedSecret = "testing123"; // Secreto compartido
    private int authPort = 1812; // Puerto de autenticación (por defecto 1812)

    @PostMapping("/autenticar")
    public ResponseEntity<HashMap<String,Object>> authenticate(String username, String password) {
        HashMap<String,Object>responseMap=new HashMap<>();

        try {
            RadiusClient client = new RadiusClient(radiusServer, sharedSecret);
            client.setAuthPort(authPort);
            AccessRequest request = new AccessRequest(username, password);
            request.setAuthProtocol(AccessRequest.AUTH_PAP);

            // Envía la solicitud al servidor RADIUS
            RadiusPacket response = client.authenticate(request);

            // Verifica si la respuesta es de tipo "Access-Accept"
            if (response.getPacketType() == RadiusPacket.ACCESS_ACCEPT) {
                System.out.println("Authentication successful");
                responseMap.put("status","success");
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
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
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
