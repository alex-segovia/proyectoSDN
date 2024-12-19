package com.example.proyectosdn.controller;

import com.example.proyectosdn.entity.Dispositivo;
import com.example.proyectosdn.entity.Servicio;
import com.example.proyectosdn.entity.ServicioPorDispositivo;
import com.example.proyectosdn.entity.Usuario;
import com.example.proyectosdn.repository.DispositivoRepository;
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
@RequestMapping("/sdn/dispositivos")
@Slf4j
public class DispositivosController {

    @Autowired
    private DispositivoRepository dispositivoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private ServicioPorDispositivoRepository servicioPorDispositivoRepository;

    // Listar dispositivos
    @GetMapping("")
    public String listarDispositivos(Model model) {

        log.info("Listando todos los dispositivos");
        model.addAttribute("dispositivos", dispositivoRepository.findAll());
        model.addAttribute("active", "dispositivos");
        return "dispositivos/lista_dispositivos";
    }

    // Mostrar formulario de nuevo dispositivo
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        log.info("Mostrando formulario de nuevo dispositivo");
        model.addAttribute("dispositivo", new Dispositivo());
        model.addAttribute("usuarios", usuarioRepository.findAll());
        model.addAttribute("active", "dispositivos");
        return "dispositivos/formulario_dispositivos";
    }

    // Mostrar formulario de edición
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Integer id, Model model) {
        log.info("Mostrando formulario de edición para dispositivo ID: {}", id);

        Dispositivo dispositivo = dispositivoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dispositivo no encontrado"));

        model.addAttribute("dispositivo", dispositivo);
        model.addAttribute("usuarios", usuarioRepository.findAll());
        model.addAttribute("active", "dispositivos");
        return "dispositivos/formulario_dispositivos";
    }

    // Guardar dispositivo (crear/actualizar)
    @PostMapping("/guardar")
    public String guardarDispositivo(@Valid Dispositivo dispositivo,
                                     BindingResult result,
                                     Model model,
                                     @RequestParam(name = "usuario.id", required = false) Integer usuarioId,
                                     RedirectAttributes redirectAttributes) {
        log.info("Guardando dispositivo: {}", dispositivo);

        if (result.hasErrors()) {
            model.addAttribute("usuarios", usuarioRepository.findAll());
            model.addAttribute("active", "dispositivos");
            return "dispositivos/formulario_dispositivos";
        }

        try {
            // Si hay un usuario seleccionado, buscar y asignar
            if (usuarioId != null) {
                Usuario usuario = usuarioRepository.findById(usuarioId)
                        .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
                dispositivo.setUsuario(usuario);
            } else {
                dispositivo.setUsuario(null);
            }

            // Verificar MAC única
            Optional<Dispositivo> dispositivoExistente = dispositivoRepository.findByMac(dispositivo.getMac());
            if (dispositivoExistente.isPresent() && !dispositivoExistente.get().getId().equals(dispositivo.getId())) {
                result.rejectValue("mac", "error.dispositivo", "La dirección MAC ya está registrada");
                model.addAttribute("usuarios", usuarioRepository.findAll());
                model.addAttribute("active", "dispositivos");
                return "dispositivos/formulario_dispositivos";
            }

            dispositivoRepository.save(dispositivo);
            redirectAttributes.addFlashAttribute("mensaje", "Dispositivo guardado exitosamente");
            return "redirect:/dispositivos";

        } catch (EntityNotFoundException e) {
            model.addAttribute("error", "Error al guardar el dispositivo: " + e.getMessage());
            model.addAttribute("usuarios", usuarioRepository.findAll());
            model.addAttribute("active", "dispositivos");
            return "dispositivos/formulario_dispositivos";
        }
    }

    // Ver detalles del dispositivo
    @GetMapping("/ver/{id}")
    public String verDispositivo(@PathVariable Integer id, Model model) {
        log.info("Mostrando detalles del dispositivo ID: {}", id);

        Dispositivo dispositivo = dispositivoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dispositivo no encontrado"));

        List<Servicio> serviciosDisponibles = servicioRepository.findAll();
        serviciosDisponibles.removeIf(servicio ->
                dispositivo.getServicioPorDispositivos().stream()
                        .anyMatch(apd -> apd.getServicio().getId().equals(servicio.getId())));

        model.addAttribute("dispositivo", dispositivo);
        model.addAttribute("serviciosDisponibles", serviciosDisponibles);
        model.addAttribute("active", "dispositivos");
        return "dispositivos/ver_dispositivo";
    }

    // Eliminar dispositivo
    @PostMapping("/eliminar/{id}")
    public String eliminarDispositivo(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        log.info("Eliminando dispositivo ID: {}", id);

        try {
            Dispositivo dispositivo = dispositivoRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Dispositivo no encontrado"));

            // Eliminar primero las relaciones con servicios
            servicioPorDispositivoRepository.deleteAll(dispositivo.getServicioPorDispositivos());
            dispositivoRepository.delete(dispositivo);

            redirectAttributes.addFlashAttribute("mensaje", "Dispositivo eliminado exitosamente");
        } catch (Exception e) {
            log.error("Error al eliminar dispositivo: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "No se pudo eliminar el dispositivo");
        }

        return "redirect:/dispositivos";
    }

    // Agregar servicio (antes era atributo) al dispositivo
    @PostMapping("/agregarServicio")
    public String agregarServicio(@RequestParam Integer idDispositivo,
                                  @RequestParam Integer idServicio,
                                  @RequestParam String motivo,
                                  RedirectAttributes redirectAttributes) {
        log.info("Agregando servicio {} al dispositivo {}", idServicio, idDispositivo);

        try {
            Dispositivo dispositivo = dispositivoRepository.findById(idDispositivo)
                    .orElseThrow(() -> new EntityNotFoundException("Dispositivo no encontrado"));

            Servicio servicio = servicioRepository.findById(idServicio)
                    .orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado"));

            ServicioPorDispositivo spd = new ServicioPorDispositivo();
            spd.setDispositivo(dispositivo);
            spd.setServicio(servicio);
            spd.setMotivo(motivo);
            spd.setEstado(1);

            servicioPorDispositivoRepository.save(spd);
            redirectAttributes.addFlashAttribute("mensaje", "Servicio agregado exitosamente");

        } catch (Exception e) {
            log.error("Error al agregar servicio: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "No se pudo agregar el servicio");
        }

        return "redirect:/dispositivos/ver/" + idDispositivo;
    }

    // Eliminar servicio del dispositivo
    @PostMapping("/{idDispositivo}/eliminarServicio/{idServicio}")
    public String eliminarServicio(@PathVariable Integer idDispositivo,
                                   @PathVariable Integer idServicio,
                                   RedirectAttributes redirectAttributes) {
        log.info("Eliminando servicio {} del dispositivo {}", idServicio, idDispositivo);

        try {
            ServicioPorDispositivo apd = servicioPorDispositivoRepository.findById(idServicio)
                    .orElseThrow(() -> new EntityNotFoundException("Relación no encontrada"));

            servicioPorDispositivoRepository.delete(apd);
            redirectAttributes.addFlashAttribute("mensaje", "Servicio eliminado exitosamente");

        } catch (Exception e) {
            log.error("Error al eliminar servicio: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "No se pudo eliminar el servicio");
        }

        return "redirect:/dispositivos/ver/" + idDispositivo;
    }

    // Manejador global de excepciones
    @ExceptionHandler(EntityNotFoundException.class)
    public String handleEntityNotFoundException(EntityNotFoundException ex,
                                                RedirectAttributes redirectAttributes) {
        log.error("Error de entidad no encontrada: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/dispositivos";
    }
}