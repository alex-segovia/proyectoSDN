package com.example.proyectosdn.controller;

import com.example.proyectosdn.entity.Dispositivo;
import com.example.proyectosdn.entity.Servicio;
import com.example.proyectosdn.entity.ServicioPorDispositivo;
import com.example.proyectosdn.entity.Usuario;
import com.example.proyectosdn.repository.*;
import com.example.proyectosdn.service.UsuarioSesionService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
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

    @Autowired
    private UsuarioSesionService usuarioSesionService;

    // Listar dispositivos
    @GetMapping("")
    public String listarDispositivos(Model model, HttpServletRequest request) {
        Usuario usuarioActual = usuarioSesionService.obtenerUsuarioActivo(request);

        if(usuarioActual.getRol().equals("Superadmin")){
            log.info("Listando todos los dispositivos");
            model.addAttribute("dispositivos", dispositivoRepository.findAll());
        }else{
            log.info("Listando los dispositivos del usuario");
            model.addAttribute("dispositivos", dispositivoRepository.findByUsuarioId(usuarioActual.getId()));
        }

        model.addAttribute("active", "dispositivos");
        model.addAttribute("usuarioActual", usuarioActual);
        return "dispositivos/lista_dispositivos";
    }

    // Mostrar formulario de nuevo dispositivo
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model, HttpServletRequest request) {
        Usuario usuarioActual = usuarioSesionService.obtenerUsuarioActivo(request);

        log.info("Mostrando formulario de nuevo dispositivo para usuario: {}", usuarioActual.getUsername());

        model.addAttribute("dispositivo", new Dispositivo());
        // Si es Superadmin ve todos los usuarios, si no, solo se ve a sí mismo
        if (usuarioActual.getRol().equals("Superadmin")) {
            model.addAttribute("usuarios", usuarioRepository.findAll());
        } else {
            List<Usuario> usuarios = new ArrayList<>();
            usuarios.add(usuarioActual);
            model.addAttribute("usuarios", usuarios);
        }

        model.addAttribute("active", "dispositivos");
        model.addAttribute("usuarioActual", usuarioActual);
        return "dispositivos/formulario_dispositivos";
    }

    // Mostrar formulario de edición
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Integer id, Model model, HttpServletRequest request) {
        Usuario usuarioActual = usuarioSesionService.obtenerUsuarioActivo(request);

        log.info("Mostrando formulario de edición para dispositivo ID: {}", id);

        Dispositivo dispositivo = dispositivoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dispositivo no encontrado"));

        // Verificar permisos
        if (!usuarioActual.getRol().equals("Superadmin") &&
                !dispositivo.getUsuario().getId().equals(usuarioActual.getId())) {
            throw new RuntimeException("No tiene permisos para editar este dispositivo");
        }

        model.addAttribute("dispositivo", dispositivo);
        model.addAttribute("usuarios", usuarioRepository.findAll());
        model.addAttribute("active", "dispositivos");
        model.addAttribute("usuarioActual", usuarioActual);
        return "dispositivos/formulario_dispositivos";
    }

    // Guardar dispositivo (crear/actualizar)
    @PostMapping("/guardar")
    public String guardarDispositivo(@Valid Dispositivo dispositivo,
                                     BindingResult result,
                                     Model model,
                                     @RequestParam(name = "usuario.id", required = false) Integer usuarioId,
                                     @RequestParam(name = "autenticado", required = false) Integer autenticado,
                                     RedirectAttributes redirectAttributes,
                                     HttpServletRequest request) {
        Usuario usuarioActual = usuarioSesionService.obtenerUsuarioActivo(request);
        log.info("Guardando dispositivo: {}", dispositivo);

        try {
            // Asignar usuario
            if (usuarioId != null) {
                Usuario usuario = usuarioRepository.findById(usuarioId)
                        .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
                dispositivo.setUsuario(usuario);
            }

            // Asignar valores por defecto
            dispositivo.setAutenticado(autenticado != null ? autenticado : 0);
            if (dispositivo.getEstado() == null) {
                dispositivo.setEstado(1);
            }

            // Verificar MAC única
            Optional<Dispositivo> dispositivoExistente = dispositivoRepository.findByMac(dispositivo.getMac());
            if (dispositivoExistente.isPresent() &&
                    (dispositivo.getId() == null || !dispositivoExistente.get().getId().equals(dispositivo.getId()))) {
                result.rejectValue("mac", "error.dispositivo", "La dirección MAC ya está registrada");
                prepararModelo(model, usuarioActual);
                return "dispositivos/formulario_dispositivos";
            }

            dispositivoRepository.save(dispositivo);
            redirectAttributes.addFlashAttribute("mensaje", "Dispositivo guardado exitosamente");
            return "redirect:/sdn/dispositivos";

        } catch (Exception e) {
            log.error("Error al guardar dispositivo: ", e);
            prepararModelo(model, usuarioActual);
            model.addAttribute("error", "Error al guardar el dispositivo: " + e.getMessage());
            return "dispositivos/formulario_dispositivos";
        }
    }


    private void prepararModelo(Model model, Usuario usuarioActual) {
        model.addAttribute("usuarios", usuarioRepository.findAll());
        model.addAttribute("active", "dispositivos");
        model.addAttribute("usuarioActual", usuarioActual);
    }

    // Ver detalles del dispositivo
    @GetMapping("/ver/{id}")
    public String verDispositivo(@PathVariable Integer id, Model model, HttpServletRequest request) {
        Usuario usuarioActual = usuarioSesionService.obtenerUsuarioActivo(request);
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
        model.addAttribute("usuarioActual", usuarioActual);
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

        return "redirect:/sdn/dispositivos";
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

        return "redirect:/sdn/dispositivos/ver/" + idDispositivo;
    }

    // Manejador global de excepciones
    @ExceptionHandler(EntityNotFoundException.class)
    public String handleEntityNotFoundException(EntityNotFoundException ex,
                                                RedirectAttributes redirectAttributes) {
        log.error("Error de entidad no encontrada: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/sdn/dispositivos";
    }
}