package com.example.proyectosdn.controller;

import com.example.proyectosdn.dto.DispositivoDTO;
import com.example.proyectosdn.dto.Servicio2DTO;
import com.example.proyectosdn.dto.ServicioDTO;
import com.example.proyectosdn.dto.UsuarioDTO;
import com.example.proyectosdn.dto.vistas.DispositivoSelectDTO;
import com.example.proyectosdn.entity.*;
import com.example.proyectosdn.extra.HttpClientService;
import com.example.proyectosdn.repository.*;
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

import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private PuertoRepository puertoRepository;

    @Autowired
    private DispositivoRepository dispositivoRepository;

    @Autowired
    private PuertoPorServicioRepository puertoPorServicioRepository;

    @Autowired
    private UsuarioSesionService usuarioSesionService;
    @Autowired
    private HttpClientService httpClientService;

    @GetMapping("")
    public String listarServicios(Model model, HttpServletRequest httpRequest) {
        Usuario usuarioActual = usuarioSesionService.obtenerUsuarioActivo(httpRequest);

        if ("Alumno".equals(usuarioActual.getRol())) {
            List<Servicio> serviciosDisponibles = servicioRepository.findServiciosNoPertenecientesAUsuario(usuarioActual.getId());
            List<ServicioDTO> dtosDisponibles = serviciosDisponibles.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            model.addAttribute("serviciosDisponibles", dtosDisponibles);
        } else {
            List<Servicio> misServicios = servicioRepository.findByUsuarioCreadorId(usuarioActual.getId());
            List<ServicioDTO> dtosPropios = misServicios.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            model.addAttribute("servicios", dtosPropios);

            List<Servicio> serviciosDisponibles = servicioRepository.findServiciosNoPertenecientesAUsuario(usuarioActual.getId());
            List<ServicioDTO> dtosDisponibles = serviciosDisponibles.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            model.addAttribute("serviciosDisponibles", dtosDisponibles);
        }

        model.addAttribute("active", "servicios");
        model.addAttribute("rolUsuario", usuarioActual.getRol());
        model.addAttribute("usuarioActual", usuarioActual);
        return "servicios/lista_servicios";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model, HttpServletRequest request) {
        Usuario usuarioActual = usuarioSesionService.obtenerUsuarioActivo(request);
        if ("Alumno".equals(usuarioActual.getRol())) {
            return "redirect:http://192.168.200.200:8080/sdn/servicios";
        }

        model.addAttribute("servicio", new Servicio());
        model.addAttribute("puertos", new ArrayList<>());
        model.addAttribute("active", "servicios");
        model.addAttribute("usuarioActual", usuarioActual);
        return "servicios/formulario_solicitud_servicios";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Integer id, Model model, HttpServletRequest request) {
        Usuario usuarioActual = usuarioSesionService.obtenerUsuarioActivo(request);
        if ("Alumno".equals(usuarioActual.getRol())) {
            return "redirect:http://192.168.200.200:8080/sdn/servicios";
        }

        try {
            Servicio servicio = servicioRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado"));

            // Verificar que el usuario sea el creador del servicio
            if (!servicio.getUsuarioCreador().getId().equals(usuarioActual.getId())) {
                return "redirect:http://192.168.200.200:8080/sdn/servicios";
            }

            List<Integer> puertos = servicio.getPuertoPorServicios().stream()
                    .map(pps -> pps.getPuerto().getNumeroPuerto())
                    .collect(Collectors.toList());

            model.addAttribute("servicio", servicio);
            model.addAttribute("puertos", puertos);
            model.addAttribute("active", "servicios");
            model.addAttribute("usuarioActual", usuarioActual);
            return "servicios/formulario_solicitud_servicios";
        } catch (EntityNotFoundException e) {
            return "redirect:http://192.168.200.200:8080/sdn/servicios";
        }
    }

    @PostMapping("/guardar")
    public String guardarServicio(@Valid @ModelAttribute("servicio") Servicio servicio,
                                  BindingResult result,
                                  @RequestParam(value = "puertos", required = false) List<Integer> puertos,
                                  Model model,
                                  RedirectAttributes redirectAttributes,
                                  HttpServletRequest request) {
        Usuario usuarioActual = usuarioSesionService.obtenerUsuarioActivo(request);
        if ("Alumno".equals(usuarioActual.getRol())) {
            return "redirect:http://192.168.200.200:8080/sdn/servicios";
        }

        if (result.hasErrors()) {
            model.addAttribute("puertos", puertos);
            model.addAttribute("active", "servicios");
            model.addAttribute("usuarioActual", usuarioActual);
            return "servicios/formulario_solicitud_servicios";
        }

        try {
            // Verificar nombre duplicado
            Optional<Servicio> servicioExistente = servicioRepository.findByNombre(servicio.getNombre());
            if (servicioExistente.isPresent() && !servicioExistente.get().getId().equals(servicio.getId())) {
                result.rejectValue("nombre", "error.servicio", "Ya existe un servicio con este nombre");
                model.addAttribute("puertos", puertos);
                model.addAttribute("active", "servicios");
                model.addAttribute("usuarioActual", usuarioActual);
                return "servicios/formulario_solicitud_servicios";
            }

            // Asignar usuario creador
            servicio.setUsuarioCreador(usuarioActual);

            Integer idAux = servicio.getId();

            // Guardar servicio
            Servicio servicioGuardado = servicioRepository.save(servicio);

            // Eliminar puertos antiguos si es edición
            if (servicio.getId() != null) {
                puertoPorServicioRepository.deleteByServicioId(servicio.getId());
            }

            // Guardar nuevos puertos
            if (puertos != null){
                for (Integer numeroPuerto : puertos) {
                    Puerto puerto = puertoRepository.findByNumeroPuerto(numeroPuerto)
                            .orElseGet(() -> {
                                Puerto nuevoPuerto = new Puerto();
                                nuevoPuerto.setNumeroPuerto(numeroPuerto);
                                return puertoRepository.save(nuevoPuerto);
                            });

                    PuertoPorServicio pps = new PuertoPorServicio();
                    pps.setPuerto(puerto);
                    pps.setServicio(servicioGuardado);
                    puertoPorServicioRepository.save(pps);
                }
            }
            List<Dispositivo>dispositivosConServicio=dispositivoRepository.obtenerDispositivosPorServicio(servicioGuardado.getId());
            if(dispositivosConServicio!=null){
                for(Dispositivo dispositivo:dispositivosConServicio){
                    httpClientService.eliminarReglasPorMac(dispositivo.getMac());
                }
            }

            redirectAttributes.addFlashAttribute("mensaje",
                    idAux == null ? "Servicio creado exitosamente" : "Servicio actualizado exitosamente");
        } catch (Exception e) {
            log.error("Error al guardar servicio: ", e);
            redirectAttributes.addFlashAttribute("error", "Error al guardar el servicio");
        }

        return "redirect:http://192.168.200.200:8080/sdn/servicios";
    }

    @GetMapping("/ver/{id}")
    public String verServicio(@PathVariable Integer id, Model model, HttpServletRequest request) {
        try {
            Servicio servicio = servicioRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado"));

            Usuario usuarioActual = usuarioSesionService.obtenerUsuarioActivo(request);
            boolean esCreador = servicio.getUsuarioCreador().getId().equals(usuarioActual.getId());

            model.addAttribute("servicio", convertToDTO2(servicio));
            model.addAttribute("esCreador", esCreador);
            model.addAttribute("active", "servicios");
            model.addAttribute("usuarioActual", usuarioActual);
            return "servicios/ver_servicio";
        } catch (EntityNotFoundException e) {
            return "redirect:http://192.168.200.200:8080/sdn/servicios";
        }
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
            usuarioDTO.setApellidoMaterno(servicio.getUsuarioCreador().getApellidoMaterno());
            usuarioDTO.setDni(servicio.getUsuarioCreador().getDni());
            usuarioDTO.setRol(servicio.getUsuarioCreador().getRol());
            dto.setUsuarioCreador(usuarioDTO);
        }

        dto.setPuertos(servicio.getPuertoPorServicios().stream()
                .map(pps -> pps.getPuerto().getNumeroPuerto())
                .collect(Collectors.toList()));

        dto.setServicioPorDispositivo(servicio.getServicioPorDispositivos());
        dto.setSolicitudesPendientes(servicioPorDispositivoRepository.listarSolcitudesDeServicios(0,servicio.getId())); //PENDIENTES

        return dto;
    }

    // ENDPOINT ESPECÍFICOS
    @GetMapping("/mis-dispositivos")
    @ResponseBody
    public ResponseEntity<List<DispositivoSelectDTO>> getMisDispositivos(HttpServletRequest request) {
        try {
            Usuario usuario = usuarioSesionService.obtenerUsuarioActivo(request);
            List<Dispositivo> dispositivos = dispositivoRepository.findByUsuarioId(usuario.getId());

            List<DispositivoSelectDTO> dtos = dispositivos.stream()
                    .map(d -> new DispositivoSelectDTO(
                            d.getId(),
                            d.getNombre(),
                            d.getMac()
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("Error al obtener dispositivos: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/solicitar")
    @ResponseBody
    public ResponseEntity<Map<String,Object>> solicitarServicio(
            @RequestParam("id") Integer servicioId,
            @RequestParam("dispositivoId") Integer dispositivoId,
            @RequestParam("motivo") String motivo,
            HttpServletRequest request) {
        Map<String,Object> response = new HashMap<>();

        try {
            Usuario usuario = usuarioSesionService.obtenerUsuarioActivo(request);

            // Verificar que el servicio existe
            Servicio servicio = servicioRepository.findById(servicioId)
                    .orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado"));

            // Verificar que el dispositivo pertenece al usuario
            Dispositivo dispositivo = dispositivoRepository.findById(dispositivoId)
                    .orElseThrow(() -> new EntityNotFoundException("Dispositivo no encontrado"));

            if (!dispositivo.getUsuario().getId().equals(usuario.getId())) {
                throw new RuntimeException("El dispositivo no pertenece al usuario");
            }

            // Verificar si ya existe una solicitud
            Optional<ServicioPorDispositivo> solicitudExistente =
                    servicioPorDispositivoRepository.findByServicioIdAndDispositivoId(servicioId, dispositivoId);

            if (solicitudExistente.isPresent()) {
                throw new RuntimeException("Ya existe una solicitud para este servicio y dispositivo");
            }

            // Crear nueva solicitud
            ServicioPorDispositivo solicitud = new ServicioPorDispositivo();
            solicitud.setServicio(servicio);
            solicitud.setDispositivo(dispositivo);
            solicitud.setEstado(0); // Pendiente
            solicitud.setMotivo(motivo);

            servicioPorDispositivoRepository.save(solicitud);

            response.put("status", "success");
            response.put("message", "Solicitud enviada correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al solicitar servicio: ", e);
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/agregarDispositivoAServicio")
    @ResponseBody
    public ResponseEntity<Map<String,Object>> agregarDispositivoAServicio(
            @RequestParam("id") Integer servicioId,
            @RequestParam("dispositivoId") Integer dispositivoId,
            HttpServletRequest request) {
        Map<String,Object> response = new HashMap<>();

        try {
            Usuario usuario = usuarioSesionService.obtenerUsuarioActivo(request);

            // Verificar que el servicio existe
            Servicio servicio = servicioRepository.findById(servicioId)
                    .orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado"));

            // Verificar que el dispositivo pertenece al usuario
            Dispositivo dispositivo = dispositivoRepository.findById(dispositivoId)
                    .orElseThrow(() -> new EntityNotFoundException("Dispositivo no encontrado"));

            if (!dispositivo.getUsuario().getId().equals(usuario.getId())) {
                throw new RuntimeException("El dispositivo no pertenece al usuario");
            }

            // Verificar si ya existe una solicitud
            Optional<ServicioPorDispositivo> solicitudExistente =
                    servicioPorDispositivoRepository.findByServicioIdAndDispositivoId(servicioId, dispositivoId);

            if (solicitudExistente.isPresent()) {
                throw new RuntimeException("Ya existe la asociación para el dispositivo");
            }

            // Crear nueva solicitud
            ServicioPorDispositivo solicitud = new ServicioPorDispositivo();
            solicitud.setServicio(servicio);
            solicitud.setDispositivo(dispositivo);
            solicitud.setEstado(1); // Aceptado

            servicioPorDispositivoRepository.save(solicitud);

            response.put("status", "success");
            response.put("message", "Dispositivo añadido correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al añadir dispositivo: ", e);
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/solicitud/{id}/aprobar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> aprobarSolicitud(
            @PathVariable Integer id,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            Usuario usuario = usuarioSesionService.obtenerUsuarioActivo(request);

            ServicioPorDispositivo spd = servicioPorDispositivoRepository
                    .findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Solicitud no encontrada"));

            // Verificar que el usuario sea el creador del servicio
            if (!spd.getServicio().getUsuarioCreador().getId().equals(usuario.getId())) {
                throw new RuntimeException("No tienes permisos para aprobar esta solicitud");
            }

            spd.setEstado(1); // Aprobado
            servicioPorDispositivoRepository.save(spd);
            Set<Dispositivo>dispositivos=usuario.getDispositivos();
            for(Dispositivo dispositivo:dispositivos) {
                List<ServicioPorDispositivo>serviciosPorDispositivo=dispositivo.getServicioPorDispositivos();
                for(ServicioPorDispositivo servicio:serviciosPorDispositivo) {
                    if(servicio.getServicio().getId()==spd.getServicio().getId()) {
                        httpClientService.eliminarReglasPorMac(dispositivo.getMac());
                    }
                }
            }
            response.put("status", "success");
            response.put("message", "Solicitud aprobada exitosamente");
            return ResponseEntity.ok(response);
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
            @PathVariable Integer id,
            @RequestParam String motivo,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            Usuario usuario = usuarioSesionService.obtenerUsuarioActivo(request);

            ServicioPorDispositivo spd = servicioPorDispositivoRepository
                    .findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Solicitud no encontrada"));

            // Verificar que el usuario sea el creador del servicio
            if (!spd.getServicio().getUsuarioCreador().getId().equals(usuario.getId())) {
                throw new RuntimeException("No tienes permisos para rechazar esta solicitud");
            }

            spd.setEstado(2); // Rechazado
            spd.setMotivo(motivo);
            servicioPorDispositivoRepository.save(spd);

            response.put("status", "success");
            response.put("message", "Solicitud rechazada exitosamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al rechazar solicitud: ", e);
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/acceso/{id}/revocar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> revocarAcceso(
            @PathVariable Integer id,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            Usuario usuario = usuarioSesionService.obtenerUsuarioActivo(request);

            ServicioPorDispositivo spd = servicioPorDispositivoRepository
                    .findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Acceso no encontrado"));

            // Verificar que el usuario sea el creador del servicio
            if (!spd.getServicio().getUsuarioCreador().getId().equals(usuario.getId())) {
                throw new RuntimeException("No tienes permisos para revocar este acceso");
            }

            // Eliminar el acceso
            servicioPorDispositivoRepository.delete(spd);
            httpClientService.eliminarReglasPorMac(spd.getDispositivo().getMac());

            response.put("status", "success");
            response.put("message", "Acceso revocado exitosamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al revocar acceso: ", e);
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarServicio(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Optional<Servicio> servicioOptional = servicioRepository.findById(id);

        if (servicioOptional.isPresent()) {
            Servicio servicio = servicioOptional.get();

            // Validar si hay dispositivos asociados al servicio
            if (!servicio.getServicioPorDispositivos().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El servicio está asociado a dispositivos y no puede ser eliminado.");
                return "redirect:http://192.168.200.200:8080/sdn/servicios";
            }

            // Validar si hay puertos asociados al servicio
            if (!servicio.getPuertoPorServicios().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El servicio está asociado a puertos y no puede ser eliminado.");
                return "redirect:http://192.168.200.200:8080/sdn/servicios";
            }

            // Si no está asociado a dispositivos ni puertos, proceder con la eliminación
            servicioRepository.delete(servicio);
            redirectAttributes.addFlashAttribute("mensaje", "El servicio se eliminó correctamente.");
        } else {
            redirectAttributes.addFlashAttribute("error", "El servicio con ID " + id + " no existe.");
        }

        return "redirect:http://192.168.200.200:8080/sdn/servicios";
    }


}