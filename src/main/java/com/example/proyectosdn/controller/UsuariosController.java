package com.example.proyectosdn.controller;

import com.example.proyectosdn.entity.Dispositivo;
import com.example.proyectosdn.entity.Servicio;
import com.example.proyectosdn.entity.Usuario;
import com.example.proyectosdn.repository.DispositivoRepository;
import com.example.proyectosdn.repository.ServicioRepository;
import com.example.proyectosdn.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
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

    // Listar usuarios
    @GetMapping("")
    public String listarUsuarios(Model model) {
        log.info("Listando todos los usuarios");
        model.addAttribute("usuarios", usuarioRepository.findAll());
        model.addAttribute("active", "usuarios");
        return "usuarios/lista_usuarios";
    }

    // Mostrar formulario de nuevo usuario
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        log.info("Mostrando formulario de nuevo usuario");
        Usuario usuario = new Usuario();
        // Valores por defecto para RADIUS
        usuario.setAttribute("Cleartext-Password");
        usuario.setOp("=");

        model.addAttribute("usuario", usuario);
        model.addAttribute("active", "usuarios");
        return "usuarios/formulario_usuarios";
    }

    // Mostrar formulario de edición
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Integer id, Model model) {
        log.info("Mostrando formulario de edición para usuario ID: {}", id);

        try {
            Usuario usuario = usuarioRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

            model.addAttribute("usuario", usuario);
            model.addAttribute("active", "usuarios");
            return "usuarios/formulario_usuarios";
        } catch (EntityNotFoundException e) {
            log.error("Error al buscar usuario: {}", e.getMessage());
            return "redirect:/usuarios";
        }
    }

    // Guardar usuario
    @PostMapping("/guardar")
    public String guardarUsuario(@Valid Usuario usuario,
                                 BindingResult result, Model model,
                                 RedirectAttributes redirectAttributes) {
        log.info("Guardando usuario: {}", usuario);

        if (result.hasErrors()) {
            model.addAttribute("active", "usuarios");
            return "usuarios/formulario_usuarios";
        }

        try {
            // Verificar username único
            Optional<Usuario> usuarioExistente = usuarioRepository.findByUsername(usuario.getUsername());
            if (usuarioExistente.isPresent() && !usuarioExistente.get().getId().equals(usuario.getId())) {
                result.rejectValue("username", "error.usuario", "Este username ya está registrado");
                model.addAttribute("active", "usuarios");
                return "usuarios/formulario_usuarios";
            }

            // Verificar DNI único
            usuarioExistente = usuarioRepository.findByDni(usuario.getDni());
            if (usuarioExistente.isPresent() && !usuarioExistente.get().getId().equals(usuario.getId())) {
                result.rejectValue("dni", "error.usuario", "Este DNI ya está registrado");
                model.addAttribute("active", "usuarios");
                return "usuarios/formulario_usuarios";
            }

            usuarioRepository.save(usuario);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario guardado exitosamente");

        } catch (Exception e) {
            log.error("Error al guardar usuario: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al guardar el usuario");
        }

        return "redirect:/usuarios";
    }

    // Ver detalles del usuario
    @GetMapping("/ver/{id}")
    public String verUsuario(@PathVariable Integer id, Model model) {
        log.info("Mostrando detalles del usuario ID: {}", id);

        try {
            Usuario usuario = usuarioRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

            model.addAttribute("usuario", usuario);
            model.addAttribute("active", "usuarios");
            return "usuarios/ver_usuario";
        } catch (EntityNotFoundException e) {
            log.error("Error al buscar usuario: {}", e.getMessage());
            return "redirect:/usuarios";
        }
    }

    // Eliminar usuario
    @PostMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        log.info("Eliminando usuario ID: {}", id);

        try {
            Usuario usuario = usuarioRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

            // Verificar si tiene dispositivos
            List<Dispositivo> dispositivos = dispositivoRepository.findByUsuarioId(id);
            if (!dispositivos.isEmpty()) {
                redirectAttributes.addFlashAttribute("error",
                        "No se puede eliminar el usuario porque tiene " + dispositivos.size() +
                                " dispositivo(s) asociado(s)");
                return "redirect:/usuarios";
            }

            // Verificar si tiene atributos creados
            List<Servicio> servicios = servicioRepository.findByUsuarioCreadorId(id);
            if (!servicios.isEmpty()) {
                redirectAttributes.addFlashAttribute("error",
                        "No se puede eliminar el usuario porque ha creado " + servicios.size() +
                                " atributo(s)");
                return "redirect:/usuarios";
            }

            usuarioRepository.delete(usuario);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario eliminado exitosamente");

        } catch (Exception e) {
            log.error("Error al eliminar usuario: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "No se pudo eliminar el usuario");
        }

        return "redirect:/usuarios";
    }

    // Manejador de excepciones
    @ExceptionHandler(EntityNotFoundException.class)
    public String handleEntityNotFoundException(EntityNotFoundException ex,
                                                RedirectAttributes redirectAttributes) {
        log.error("Error de entidad no encontrada: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/usuarios";
    }
}
