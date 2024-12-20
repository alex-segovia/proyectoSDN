package com.example.proyectosdn.controller;

import com.example.proyectosdn.dto.Servicio2DTO;
import com.example.proyectosdn.dto.ServicioDTO;
import com.example.proyectosdn.dto.UsuarioDTO;
import com.example.proyectosdn.entity.PuertoPorServicio;
import com.example.proyectosdn.entity.Servicio;
import com.example.proyectosdn.entity.ServicioPorDispositivo;
import com.example.proyectosdn.entity.Usuario;
import com.example.proyectosdn.repository.ServicioPorDispositivoRepository;
import com.example.proyectosdn.repository.ServicioRepository;
import com.example.proyectosdn.repository.SesionActivaRepository;
import com.example.proyectosdn.repository.UsuarioRepository;
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
import java.util.stream.Collectors;

@Controller
@RequestMapping("/sdn/servicios")
@Slf4j
public class ServiciosController {
    @Autowired
    private SesionActivaRepository sesionActivaRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ServicioPorDispositivoRepository servicioPorDispositivoRepository;

    // Listar servicios
    @GetMapping("")
    public String listarServicios(Model model, HttpServletRequest httpRequest) {
        String ipAdd = httpRequest.getHeader("X-Real-IP");
        if (ipAdd == null || ipAdd.isEmpty()) {
            ipAdd = httpRequest.getHeader("X-Forwarded-For");
            if (ipAdd == null || ipAdd.isEmpty()) {
                ipAdd = httpRequest.getRemoteAddr();
            }
        }
        String rolUsuario = sesionActivaRepository.userRolPorIp(ipAdd)!=null?sesionActivaRepository.userRolPorIp(ipAdd):"asd";

        if(rolUsuario.equals("Superadmin")){
            log.info("Listando todos los servicios");
            List<Servicio> servicios = servicioRepository.findAll();
            List<ServicioDTO> dtos = servicios.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            model.addAttribute("serviciosDisponiblesSuperadmin", dtos);
        }else{
            log.info("Listando todos mis servicios");
            List<Servicio> servicios = servicioRepository.findByUsuarioCreadorId(sesionActivaRepository.userIdPorIp(ipAdd));
            List<ServicioDTO> dtos = servicios.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            model.addAttribute("servicios", dtos);
            log.info("Listando los servicios disponibles");
            List<Servicio> servicios2 = servicioRepository.listarServiciosDisponbiles(sesionActivaRepository.userIdPorIp(ipAdd));
            List<ServicioDTO> dtos2 = servicios2.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            model.addAttribute("serviciosDisponibles", dtos2);
        }
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

            model.addAttribute("servicio", convertToDTO2(servicio));
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

    private ServicioDTO convertToDTO(Servicio servicio) {
        ServicioDTO dto = new ServicioDTO();
        dto.setId(servicio.getId());
        dto.setNombre(servicio.getNombre());
        dto.setEstado(servicio.getEstado());

        if (servicio.getUsuarioCreador() != null) {
            UsuarioDTO usuarioDTO = new UsuarioDTO();
            usuarioDTO.setId(servicio.getUsuarioCreador().getId());
            usuarioDTO.setUsername(servicio.getUsuarioCreador().getUsername());
            usuarioDTO.setNombres(servicio.getUsuarioCreador().getNombres());
            usuarioDTO.setApellidoPaterno(servicio.getUsuarioCreador().getApellidoPaterno());
            dto.setUsuarioCreador(usuarioDTO);
        }

        dto.setPuertos(servicio.getPuertoPorServicios().stream()
                .map(pps -> pps.getPuerto().getNumeroPuerto())
                .collect(Collectors.toList()));

        dto.setCantidadDispositivos(servicio.getServicioPorDispositivos().size());

        return dto;
    }

    private Servicio2DTO convertToDTO2(Servicio servicio) {
        Servicio2DTO dto = new Servicio2DTO();
        dto.setId(servicio.getId());
        dto.setNombre(servicio.getNombre());
        dto.setEstado(servicio.getEstado());

        if (servicio.getUsuarioCreador() != null) {
            UsuarioDTO usuarioDTO = new UsuarioDTO();
            usuarioDTO.setId(servicio.getUsuarioCreador().getId());
            usuarioDTO.setUsername(servicio.getUsuarioCreador().getUsername());
            usuarioDTO.setNombres(servicio.getUsuarioCreador().getNombres());
            usuarioDTO.setApellidoPaterno(servicio.getUsuarioCreador().getApellidoPaterno());
            dto.setUsuarioCreador(usuarioDTO);
        }

        dto.setPuertos(servicio.getPuertoPorServicios().stream()
                .map(pps -> pps.getPuerto().getNumeroPuerto())
                .collect(Collectors.toList()));

        dto.setServicioPorDispositivo(servicio.getServicioPorDispositivos());

        return dto;
    }

    //Falta completar uwu: que se obtenga el id del dispositvo y se guarde en base de datos en la tabla servicio_por_dispositivo, con estado 0
    @PostMapping("/solicitar")
    public ResponseEntity<Map<String,Object>> solicitarServicio(@RequestParam("id") String id, HttpServletRequest httpRequest) {
        Map<String,Object>responseMap=new HashMap<>();
        try{
            String ipAdd = httpRequest.getHeader("X-Real-IP");
            if (ipAdd == null || ipAdd.isEmpty()) {
                ipAdd = httpRequest.getHeader("X-Forwarded-For");
                if (ipAdd == null || ipAdd.isEmpty()) {
                    ipAdd = httpRequest.getRemoteAddr();
                }
            }
            Integer idSesionActiva = sesionActivaRepository.idSesionActivaPorIp(ipAdd);
            if(idSesionActiva!=null){

                return ResponseEntity.ok(responseMap);
            }else{
                responseMap.put("status","error");
                responseMap.put("content","Este dispositivo no tiene una sesión activa");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Unexpected response type: " + e.getMessage());
            responseMap.put("status","error");
            responseMap.put("content",e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }
}