package com.example.proyectosdn.controller;

import com.example.proyectosdn.entity.Atributo;
import com.example.proyectosdn.entity.AtributoPorDispositivo;
import com.example.proyectosdn.entity.Usuario;
import com.example.proyectosdn.repository.AtributoPorDispositivoRepository;
import com.example.proyectosdn.repository.AtributoRepository;
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
@RequestMapping("/sdn/atributos")
@Slf4j
public class AtributosController {

    @Autowired
    private AtributoRepository atributoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AtributoPorDispositivoRepository atributoPorDispositivoRepository;

    // Listar atributos
    @GetMapping("")
    public String listarAtributos(Model model) {
        log.info("Listando todos los atributos");
        model.addAttribute("atributos", atributoRepository.findAll());
        model.addAttribute("active", "atributos");
        return "atributos/lista_atributos";
    }

    // Mostrar formulario de nuevo atributo
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        log.info("Mostrando formulario de nuevo atributo");
        model.addAttribute("atributo", new Atributo());
        model.addAttribute("usuarios", usuarioRepository.findAll());
        model.addAttribute("active", "atributos");
        return "atributos/formulario_atributos";
    }

    // Mostrar formulario de edición
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Integer id, Model model) {
        log.info("Mostrando formulario de edición para atributo ID: {}", id);

        try {
            Atributo atributo = atributoRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Atributo no encontrado"));

            model.addAttribute("atributo", atributo);
            model.addAttribute("usuarios", usuarioRepository.findAll());
            model.addAttribute("active", "atributos");
            return "atributos/formulario_atributos";
        } catch (EntityNotFoundException e) {
            log.error("Error al buscar atributo: {}", e.getMessage());
            return "redirect:/atributos";
        }
    }

    // Guardar atributo (crear/actualizar)
    @PostMapping("/guardar")
    public String guardarAtributo(@Valid Atributo atributo,
                                  BindingResult result,
                                  @RequestParam(name = "usuarioCreadorId", required = false) Integer usuarioCreadorId,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        log.info("Guardando atributo: {}", atributo);

        if (result.hasErrors()) {
            model.addAttribute("usuarios", usuarioRepository.findAll());
            model.addAttribute("active", "atributos");
            return "atributos/formulario_atributos";
        }

        try {
            // Verificar nombre duplicado
            Optional<Atributo> atributoExistente = atributoRepository.findByNombre(atributo.getNombre());
            if (atributoExistente.isPresent() && !atributoExistente.get().getId().equals(atributo.getId())) {
                result.rejectValue("nombre", "error.atributo", "Ya existe un atributo con este nombre");
                model.addAttribute("usuarios", usuarioRepository.findAll());
                model.addAttribute("active", "atributos");
                return "atributos/formulario_atributos";
            }

            // Asignar usuario creador si se proporcionó uno
            if (usuarioCreadorId != null) {
                Usuario usuarioCreador = usuarioRepository.findById(usuarioCreadorId)
                        .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
                atributo.setUsuarioCreador(usuarioCreador);
            } else {
                atributo.setUsuarioCreador(null);
            }

            atributoRepository.save(atributo);
            redirectAttributes.addFlashAttribute("mensaje", "Atributo guardado exitosamente");

        } catch (Exception e) {
            log.error("Error al guardar atributo: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al guardar el atributo");
        }

        return "redirect:/atributos";
    }

    // Ver detalles del atributo
    @GetMapping("/ver/{id}")
    public String verAtributo(@PathVariable Integer id, Model model) {
        log.info("Mostrando detalles del atributo ID: {}", id);

        try {
            Atributo atributo = atributoRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Atributo no encontrado"));

            model.addAttribute("atributo", atributo);
            model.addAttribute("active", "atributos");
            return "atributos/ver_atributo";
        } catch (EntityNotFoundException e) {
            log.error("Error al buscar atributo: {}", e.getMessage());
            return "redirect:/atributos";
        }
    }

    // Eliminar atributo
    @PostMapping("/eliminar/{id}")
    public String eliminarAtributo(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        log.info("Eliminando atributo ID: {}", id);

        try {
            Atributo atributo = atributoRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Atributo no encontrado"));

            // Verificar si el atributo está siendo usado por algún dispositivo
            List<AtributoPorDispositivo> asociaciones = atributoPorDispositivoRepository
                    .findByAtributoId(atributo.getId());

            if (!asociaciones.isEmpty()) {
                redirectAttributes.addFlashAttribute("error",
                        "No se puede eliminar el atributo porque está siendo usado por " +
                                asociaciones.size() + " dispositivo(s)");
                return "redirect:/atributos";
            }

            atributoRepository.delete(atributo);
            redirectAttributes.addFlashAttribute("mensaje", "Atributo eliminado exitosamente");

        } catch (EntityNotFoundException e) {
            log.error("Error al eliminar atributo: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "No se pudo eliminar el atributo");
        }

        return "redirect:/atributos";
    }

    // Manejador de excepciones para este controlador
    @ExceptionHandler(EntityNotFoundException.class)
    public String handleEntityNotFoundException(EntityNotFoundException ex,
                                                RedirectAttributes redirectAttributes) {
        log.error("Error de entidad no encontrada: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/atributos";
    }
}