package com.example.proyectosdn.controller;

import com.example.proyectosdn.entity.Atributo;
import com.example.proyectosdn.entity.AtributoPorDispositivo;
import com.example.proyectosdn.entity.Dispositivo;
import com.example.proyectosdn.entity.Usuario;
import com.example.proyectosdn.repository.AtributoPorDispositivoRepository;
import com.example.proyectosdn.repository.AtributoRepository;
import com.example.proyectosdn.repository.DispositivoRepository;
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
    private AtributoRepository atributoRepository;

    @Autowired
    private AtributoPorDispositivoRepository atributoPorDispositivoRepository;

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
            // Si hay un usuario seleccionado, buscar y asignar aaaah
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

        List<Atributo> atributosDisponibles = atributoRepository.findAll();
        atributosDisponibles.removeIf(atributo ->
                dispositivo.getAtributoPorDispositivos().stream()
                        .anyMatch(apd -> apd.getAtributo().getId().equals(atributo.getId())));

        model.addAttribute("dispositivo", dispositivo);
        model.addAttribute("atributosDisponibles", atributosDisponibles);
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

            // Eliminar primero las relaciones con atributos
            atributoPorDispositivoRepository.deleteAll(dispositivo.getAtributoPorDispositivos());
            dispositivoRepository.delete(dispositivo);

            redirectAttributes.addFlashAttribute("mensaje", "Dispositivo eliminado exitosamente");
        } catch (Exception e) {
            log.error("Error al eliminar dispositivo: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "No se pudo eliminar el dispositivo");
        }

        return "redirect:/dispositivos";
    }

    // Agregar atributo al dispositivo
    @PostMapping("/agregarAtributo")
    public String agregarAtributo(@RequestParam Integer idDispositivo,
                                  @RequestParam Integer idAtributo,
                                  @RequestParam String motivo,
                                  RedirectAttributes redirectAttributes) {
        log.info("Agregando atributo {} al dispositivo {}", idAtributo, idDispositivo);

        try {
            Dispositivo dispositivo = dispositivoRepository.findById(idDispositivo)
                    .orElseThrow(() -> new EntityNotFoundException("Dispositivo no encontrado"));

            Atributo atributo = atributoRepository.findById(idAtributo)
                    .orElseThrow(() -> new EntityNotFoundException("Atributo no encontrado"));

            AtributoPorDispositivo apd = new AtributoPorDispositivo();
            apd.setDispositivo(dispositivo);
            apd.setAtributo(atributo);
            apd.setMotivo(motivo);
            apd.setEstado(1);

            atributoPorDispositivoRepository.save(apd);
            redirectAttributes.addFlashAttribute("mensaje", "Atributo agregado exitosamente");

        } catch (Exception e) {
            log.error("Error al agregar atributo: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "No se pudo agregar el atributo");
        }

        return "redirect:/dispositivos/ver/" + idDispositivo;
    }

    // Eliminar atributo del dispositivo
    @PostMapping("/{idDispositivo}/eliminarAtributo/{idAtributo}")
    public String eliminarAtributo(@PathVariable Integer idDispositivo,
                                   @PathVariable Integer idAtributo,
                                   RedirectAttributes redirectAttributes) {
        log.info("Eliminando atributo {} del dispositivo {}", idAtributo, idDispositivo);

        try {
            AtributoPorDispositivo apd = atributoPorDispositivoRepository.findById(idAtributo)
                    .orElseThrow(() -> new EntityNotFoundException("Relación no encontrada"));

            atributoPorDispositivoRepository.delete(apd);
            redirectAttributes.addFlashAttribute("mensaje", "Atributo eliminado exitosamente");

        } catch (Exception e) {
            log.error("Error al eliminar atributo: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "No se pudo eliminar el atributo");
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