package com.example.proyectosdn.controller;

import com.example.proyectosdn.entity.Dispositivo;
import com.example.proyectosdn.entity.Servicio;
import com.example.proyectosdn.entity.ServicioPorDispositivo;
import com.example.proyectosdn.entity.Usuario;
import com.example.proyectosdn.repository.DispositivoRepository;
import com.example.proyectosdn.repository.ServicioRepository;
import com.example.proyectosdn.repository.UsuarioRepository;
import com.example.proyectosdn.service.UsuarioSesionService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/sdn/usuarios")
@Slf4j
public class UsuariosController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private DispositivoRepository dispositivoRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private UsuarioSesionService usuarioSesionService;

    // Listar usuarios
    @GetMapping("")
    public String listarUsuarios(Model model, HttpServletRequest request) {
        Usuario usuarioActual = usuarioSesionService.obtenerUsuarioActivo(request);

        log.info("Listando todos los usuarios");
        model.addAttribute("usuarios", usuarioRepository.obtenerUsuariosRegistrados());
        model.addAttribute("solicitudesUsuarios", usuarioRepository.obtenerSolicitudesUsuario());
        model.addAttribute("active", "usuarios");
        model.addAttribute("usuarioActual", usuarioActual);
        return "usuarios/lista_usuarios";
    }

    // Mostrar formulario de nuevo usuario
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model, HttpServletRequest request) {
        Usuario usuarioActual = usuarioSesionService.obtenerUsuarioActivo(request);

        log.info("Mostrando formulario de nuevo usuario");
        Usuario usuario = new Usuario();

        usuario.setAttribute("SHA256-Password");
        usuario.setOp(":=");

        model.addAttribute("usuario", usuario);
        model.addAttribute("active", "usuarios");
        model.addAttribute("usuarioActual", usuarioActual);
        return "usuarios/formulario_usuarios";
    }

    // Mostrar formulario de edición
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Integer id, Model model, HttpServletRequest request) {
        Usuario usuarioActual = usuarioSesionService.obtenerUsuarioActivo(request);

        log.info("Mostrando formulario de edición para usuario ID: {}", id);

        try {
            Usuario usuario = usuarioRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

            model.addAttribute("usuario", usuario);
            model.addAttribute("active", "usuarios");
            model.addAttribute("usuarioActual", usuarioActual);
            return "usuarios/formulario_usuarios";
        } catch (EntityNotFoundException e) {
            log.error("Error al buscar usuario: {}", e.getMessage());
            return "redirect:http://192.168.200.200:8080/sdn/usuarios";
        }
    }

    // Guardar usuario
    @PostMapping("/guardar")
    public String guardarUsuario(@Valid Usuario usuario,
                                 BindingResult result, Model model,
                                 RedirectAttributes redirectAttributes,
                                 HttpServletRequest request) {
        log.info("Guardando usuario: {}", usuario);

        Usuario usuarioActual = usuarioSesionService.obtenerUsuarioActivo(request);

        if (result.hasErrors()) {
            model.addAttribute("active", "usuarios");
            model.addAttribute("usuarioActual", usuarioActual);
            return "usuarios/formulario_usuarios";
        }

        try {
            // Verificar username único
            Optional<Usuario> usuarioExistente = usuarioRepository.findByUsername(usuario.getUsername());
            if (usuarioExistente.isPresent() && !usuarioExistente.get().getId().equals(usuario.getId())) {
                result.rejectValue("username", "error.usuario", "Este username ya está registrado");
                model.addAttribute("active", "usuarios");
                model.addAttribute("usuarioActual", usuarioActual);
                return "usuarios/formulario_usuarios";
            }

            // Verificar DNI único
            usuarioExistente = usuarioRepository.findByDni(usuario.getDni());
            if (usuarioExistente.isPresent() && !usuarioExistente.get().getId().equals(usuario.getId())) {
                result.rejectValue("dni", "error.usuario", "Este DNI ya está registrado");
                model.addAttribute("active", "usuarios");
                model.addAttribute("usuarioActual", usuarioActual);
                return "usuarios/formulario_usuarios";
            }
            usuarioRepository.registrarUsuario(usuario.getUsername(),usuario.getValue(),usuario.getNombres(),usuario.getApellidoPaterno(),usuario.getApellidoMaterno(),usuario.getRol(),usuario.getDni());
            redirectAttributes.addFlashAttribute("mensaje", "Usuario guardado exitosamente");

        } catch (Exception e) {
            log.error("Error al guardar usuario: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al guardar el usuario");
        }

        return "redirect:http://192.168.200.200:8080/sdn/login";
    }

    // Ver detalles del usuario
    @GetMapping("/ver/{id}")
    public String verUsuario(@PathVariable Integer id, Model model, HttpServletRequest request) {
        log.info("Mostrando detalles del usuario ID: {}", id);
        Usuario usuarioActual = usuarioSesionService.obtenerUsuarioActivo(request);

        try {
            Usuario usuario = usuarioRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

            model.addAttribute("usuario", usuario);
            model.addAttribute("usuarioActual", usuarioActual);
            return "usuarios/ver_usuario";
        } catch (EntityNotFoundException e) {
            log.error("Error al buscar usuario: {}", e.getMessage());
            return "redirect:http://192.168.200.200:8080/sdn/usuarios";
        }
    }

    // Eliminar usuario
    @PostMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Integer id, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        log.info("Eliminando usuario ID: {}", id);
        Usuario usuarioActual = usuarioSesionService.obtenerUsuarioActivo(request);

        try {
            Usuario usuario = usuarioRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

            // Verificar si tiene dispositivos
            List<Dispositivo> dispositivos = dispositivoRepository.findByUsuarioId(id);
            if (!dispositivos.isEmpty()) {
                redirectAttributes.addFlashAttribute("error",
                        "No se puede eliminar el usuario porque tiene " + dispositivos.size() +
                                " dispositivo(s) asociado(s)");
                return "redirect:http://192.168.200.200:8080/sdn/usuarios";
            }

            // Verificar si tiene servicios creados
            List<Servicio> servicios = servicioRepository.findByUsuarioCreadorId(id);
            if (!servicios.isEmpty()) {
                redirectAttributes.addFlashAttribute("error",
                        "No se puede eliminar el usuario porque ha creado " + servicios.size() +
                                " servicios(s)");
                return "redirect:http://192.168.200.200:8080/sdn/usuarios";
            }

            usuarioRepository.delete(usuario);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario eliminado exitosamente");

        } catch (Exception e) {
            log.error("Error al eliminar usuario: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "No se pudo eliminar el usuario");
        }

        return "redirect:http://192.168.200.200:8080/sdn/usuarios";
    }

    @PostMapping("/solicitud/{id}/aprobar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> aprobarSolicitud(
            @PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Verificar que la solicitud del usuario existe
            Usuario usuario = usuarioRepository.obtenerSolicitudUsuarioPorId(id);
            if(usuario!=null){
                usuarioRepository.aceptarUsuarioPorId(id);
                response.put("status", "success");
                response.put("message", "Solicitud aprobada exitosamente");
                return ResponseEntity.ok(response);
            }else{
                response.put("status", "error");
                response.put("message", "No hay una solicitud de usuario con el ID: "+ id);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            log.error("Error al aprobar solicitud: ", e);
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/solicitud/{id}/rechazar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> rechazarSolicitud(
            @PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Verificar que la solicitud del usuario existe
            Usuario usuario = usuarioRepository.obtenerSolicitudUsuarioPorId(id);
            if(usuario!=null){
                usuarioRepository.rechazarUsuarioPorId(id);
                response.put("status", "success");
                response.put("message", "Solicitud rechazada exitosamente");
                return ResponseEntity.ok(response);
            }else{
                response.put("status", "error");
                response.put("message", "No hay una solicitud de usuario con el ID: "+ id);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            log.error("Error al aprobar solicitud: ", e);
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Manejador de excepciones
    @ExceptionHandler(EntityNotFoundException.class)
    public String handleEntityNotFoundException(EntityNotFoundException ex,
                                                RedirectAttributes redirectAttributes) {
        log.error("Error de entidad no encontrada: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:http://192.168.200.200:8080/sdn/usuarios";
    }
}
