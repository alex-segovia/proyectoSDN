package com.example.proyectosdn.controller.rest;

import com.example.proyectosdn.controller.dto.ServicioDTO;
import com.example.proyectosdn.controller.dto.ServicioRequest;
import com.example.proyectosdn.controller.dto.UsuarioDTO;
import com.example.proyectosdn.entity.PuertoPorServicio;
import com.example.proyectosdn.entity.Servicio;
import com.example.proyectosdn.repository.PuertoPorServicioRepository;
import com.example.proyectosdn.repository.PuertoRepository;
import com.example.proyectosdn.repository.ServicioRepository;
import com.example.proyectosdn.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/servicios")
@Slf4j
public class ServiciosRestController {

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private PuertoRepository puertoRepository;

    @Autowired
    private PuertoPorServicioRepository puertoPorServicioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Listar todos los servicios
    @GetMapping
    public ResponseEntity<List<ServicioDTO>> listarServicios() {
        List<Servicio> servicios = servicioRepository.findAll();
        List<ServicioDTO> dtos = servicios.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Obtener un servicio por ID
    @GetMapping("/{id}")
    public ResponseEntity<ServicioDTO> obtenerServicio(@PathVariable Integer id) {
        return servicioRepository.findById(id)
                .map(this::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Crear servicio
    @PostMapping
    public ResponseEntity<ServicioDTO> crearServicio(@RequestBody ServicioRequest request) {
        Servicio servicio = new Servicio();
        servicio.setNombre(request.getNombre());
        servicio.setEstado(request.getEstado());

        // Asignar usuario creador si existe
        if (request.getUsuarioCreadorId() != null) {
            usuarioRepository.findById(request.getUsuarioCreadorId())
                    .ifPresent(servicio::setUsuarioCreador);
        }

        // Guardar servicio
        servicio = servicioRepository.save(servicio);

        // Asignar puertos si existen
        if (request.getPuertos() != null && !request.getPuertos().isEmpty()) {
            for (Integer numeroPuerto : request.getPuertos()) {
                PuertoPorServicio pps = new PuertoPorServicio();
                pps.setServicio(servicio);
                pps.setPuerto(puertoRepository.findByNumeroPuerto(numeroPuerto)
                        .orElseThrow(() -> new RuntimeException("Puerto no encontrado: " + numeroPuerto)));
                puertoPorServicioRepository.save(pps);
            }
        }

        return ResponseEntity.ok(convertToDTO(servicio));
    }

    // Actualizar servicio
    @PutMapping("/{id}")
    public ResponseEntity<ServicioDTO> actualizarServicio(
            @PathVariable Integer id,
            @RequestBody ServicioRequest request) {

        return servicioRepository.findById(id)
                .map(servicio -> {
                    servicio.setNombre(request.getNombre());
                    servicio.setEstado(request.getEstado());

                    if (request.getUsuarioCreadorId() != null) {
                        usuarioRepository.findById(request.getUsuarioCreadorId())
                                .ifPresent(servicio::setUsuarioCreador);
                    }

                    // Actualizar puertos
                    puertoPorServicioRepository.deleteAll(
                            puertoPorServicioRepository.findByServicioId(servicio.getId())
                    );

                    if (request.getPuertos() != null) {
                        for (Integer numeroPuerto : request.getPuertos()) {
                            PuertoPorServicio pps = new PuertoPorServicio();
                            pps.setServicio(servicio);
                            pps.setPuerto(puertoRepository.findByNumeroPuerto(numeroPuerto)
                                    .orElseThrow(() -> new RuntimeException("Puerto no encontrado: " + numeroPuerto)));
                            puertoPorServicioRepository.save(pps);
                        }
                    }

                    return ResponseEntity.ok(convertToDTO(servicioRepository.save(servicio)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Eliminar servicio
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarServicio(@PathVariable Integer id) {
        return servicioRepository.findById(id)
                .map(servicio -> {
                    // Verificar si tiene dispositivos asociados
                    if (!servicio.getServicioPorDispositivos().isEmpty()) {
                        return ResponseEntity.badRequest()
                                .body("No se puede eliminar el servicio porque tiene dispositivos asociados");
                    }

                    // Eliminar relaciones con puertos
                    puertoPorServicioRepository.deleteAll(
                            puertoPorServicioRepository.findByServicioId(servicio.getId())
                    );

                    servicioRepository.delete(servicio);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
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
}