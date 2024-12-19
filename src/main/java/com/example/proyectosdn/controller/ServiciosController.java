package com.example.proyectosdn.controller;

import com.example.proyectosdn.entity.Servicio;
import com.example.proyectosdn.entity.ServicioPorDispositivo;
import com.example.proyectosdn.entity.Usuario;
import com.example.proyectosdn.repository.ServicioPorDispositivoRepository;
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
@RequestMapping("/sdn/servicios")
@Slf4j
public class ServiciosController {

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ServicioPorDispositivoRepository servicioPorDispositivoRepository;

    // Listar servicios
    @GetMapping("")
    public String listarServicios(Model model) {
        log.info("Listando todos los servicios");
        model.addAttribute("servicios", servicioRepository.findAll());
        model.addAttribute("active", "servicios");
        return "servicios/lista_servicios";
    }

    // Mostrar formulario de nuevo servicio
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        log.info("Mostrando formulario de nuevo servicio");
        model.addAttribute("servicio", new Servicio());
        model.addAttribute("usuarios", usuarioRepository.findAll());
        model.addAttribute("active", "servicios");
        return "servicios/formulario_servicios";
    }

    // Mostrar formulario de edición
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Integer id, Model model) {
        log.info("Mostrando formulario de edición para servicio ID: {}", id);

        try {
            Servicio servicio = servicioRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado"));

            model.addAttribute("servicio", servicio);
            model.addAttribute("usuarios", usuarioRepository.findAll());
            model.addAttribute("active", "servicios");
            return "servicios/formulario_servicios";
        } catch (EntityNotFoundException e) {
            log.error("Error al buscar servicio: {}", e.getMessage());
            return "redirect:/servicios";
        }
    }

    // Guardar servicio (crear/actualizar)
    @PostMapping("/guardar")
    public String guardarServicio(@Valid Servicio servicio,
                                  BindingResult result,
                                  @RequestParam(name = "usuarioCreadorId", required = false) Integer usuarioCreadorId,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        log.info("Guardando servicio: {}", servicio);

        if (result.hasErrors()) {
            model.addAttribute("usuarios", usuarioRepository.findAll());
            model.addAttribute("active", "servicios");
            return "servicios/formulario_servicios";
        }

        try {
            // Verificar nombre duplicado
            Optional<Servicio> servicioExistente = servicioRepository.findByNombre(servicio.getNombre());
            if (servicioExistente.isPresent() && !servicioExistente.get().getId().equals(servicio.getId())) {
                result.rejectValue("nombre", "error.servicio", "Ya existe un servicio con este nombre");
                model.addAttribute("usuarios", usuarioRepository.findAll());
                model.addAttribute("active", "servicios");
                return "servicios/formulario_servicios";
            }

            // Asignar usuario creador si se proporcionó uno
            if (usuarioCreadorId != null) {
                Usuario usuarioCreador = usuarioRepository.findById(usuarioCreadorId)
                        .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
                servicio.setUsuarioCreador(usuarioCreador);
            } else {
                servicio.setUsuarioCreador(null);
            }

            servicioRepository.save(servicio);
            redirectAttributes.addFlashAttribute("mensaje", "Servicio guardado exitosamente");

        } catch (Exception e) {
            log.error("Error al guardar servicio: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al guardar el servicio");
        }

        return "redirect:/servicios";
    }

    // Ver detalles del servicio
    @GetMapping("/ver/{id}")
    public String verServicio(@PathVariable Integer id, Model model) {
        log.info("Mostrando detalles del servicio ID: {}", id);

        try {
            Servicio servicio = servicioRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado"));

            model.addAttribute("servicio", servicio);
            model.addAttribute("active", "servicios");
            return "servicios/ver_servicio";
        } catch (EntityNotFoundException e) {
            log.error("Error al buscar servicio: {}", e.getMessage());
            return "redirect:/servicios";
        }
    }

    // Eliminar servicio
    @PostMapping("/eliminar/{id}")
    public String eliminarServicio(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        log.info("Eliminando servicio ID: {}", id);

        try {
            Servicio servicio = servicioRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado"));

            // Verificar si el servicio está siendo usado por algún dispositivo
            List<ServicioPorDispositivo> asociaciones = servicioPorDispositivoRepository
                    .findByServicioId(servicio.getId());

            if (!asociaciones.isEmpty()) {
                redirectAttributes.addFlashAttribute("error",
                        "No se puede eliminar el servicio porque está siendo usado por " +
                                asociaciones.size() + " dispositivo(s)");
                return "redirect:/servicios";
            }

            servicioRepository.delete(servicio);
            redirectAttributes.addFlashAttribute("mensaje", "Servicio eliminado exitosamente");

        } catch (EntityNotFoundException e) {
            log.error("Error al eliminar servicio: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "No se pudo eliminar el servicio");
        }

        return "redirect:/servicios";
    }

    // Manejador de excepciones para este controlador
    @ExceptionHandler(EntityNotFoundException.class)
    public String handleEntityNotFoundException(EntityNotFoundException ex,
                                                RedirectAttributes redirectAttributes) {
        log.error("Error de entidad no encontrada: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/servicios";
    }
}