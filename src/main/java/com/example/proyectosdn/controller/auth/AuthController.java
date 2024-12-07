package com.example.proyectosdn.controller.auth;

import com.example.proyectosdn.entity.SesionActiva;
import com.example.proyectosdn.entity.Usuario;
import com.example.proyectosdn.extra.Utilities;
import com.example.proyectosdn.repository.SesionActivaRepository;
import com.example.proyectosdn.repository.UsuarioRepository;
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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;

@Controller
@RequestMapping("/sdn/autenticacion")
@Slf4j
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private String radiusServer = "127.0.0.1"; // Dirección del servidor RADIUS
    private String sharedSecret = "testing123"; // Secreto compartido
    private int authPort = 1812; // Puerto de autenticación (por defecto 1812)
    @Autowired
    private SesionActivaRepository sesionActivaRepository;

    @PostMapping("/autenticar")
    public ResponseEntity<HashMap<String,Object>> autenticar(@RequestParam("username") String username, @RequestParam("password") String password, HttpServletRequest httpRequest) {
        HashMap<String,Object>responseMap=new HashMap<>();
        try {
            String ipAdd=httpRequest.getRemoteAddr();
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
                responseMap.put("IP",ipAdd);
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
        model.addAttribute("usuario", new Usuario());
        return "login";
    }

    @PostMapping("/registro")
    public String registrarUsuario(@Valid Usuario usuario,
                                   BindingResult bindingResult,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Por favor, corrige los errores del formulario.");
            return "login";
        }

        try {
            // Verificar si el correo ya está registrado
            if (usuarioRepository.findByUsername(usuario.getUsername()).isPresent()) {
                model.addAttribute("error", "El correo ya está registrado.");
                return "login";
            }

            String hashedPassword = hashPassword(usuario.getValue());
            usuario.setOp("=");
            usuario.setAttribute("Cleartext-Password");
            usuario.setRol("ALUMNO");
            usuario.setValue(hashedPassword);
            usuario.setEstado(Integer.valueOf("1"));

            usuarioRepository.save(usuario);

            redirectAttributes.addFlashAttribute("success", "Usuario registrado exitosamente.");
            return "redirect:/sdn/autenticacion/";
        } catch (Exception e) {
            model.addAttribute("error", "Ocurrió un error al registrar al usuario: " + e.getMessage());
            return "login";
        }
    }


    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        String salt = "SALT123!";
        String saltedPassword = password + salt;

        byte[] encodedHash = digest.digest(saltedPassword.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(encodedHash);
    }
}
