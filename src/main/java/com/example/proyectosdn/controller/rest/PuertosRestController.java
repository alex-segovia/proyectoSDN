package com.example.proyectosdn.controller.rest;

import com.example.proyectosdn.dto.PuertoDTO;
import com.example.proyectosdn.dto.PuertoRequest;
import com.example.proyectosdn.entity.Puerto;
import com.example.proyectosdn.repository.PuertoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/puertos")
@Slf4j
public class PuertosRestController {

    @Autowired
    private PuertoRepository puertoRepository;

    // Listar todos los puertos
    @GetMapping
    public ResponseEntity<List<PuertoDTO>> listarPuertos() {
        List<Puerto> puertos = puertoRepository.findAll();
        List<PuertoDTO> dtos = puertos.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Obtener un puerto por ID
    @GetMapping("/{id}")
    public ResponseEntity<PuertoDTO> obtenerPuerto(@PathVariable Integer id) {
        return puertoRepository.findById(id)
                .map(this::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Crear puerto
    @PostMapping
    public ResponseEntity<?> crearPuerto(@RequestBody PuertoRequest request) {
        // Verificar puerto duplicado
        Optional<Puerto> puertoDuplicado = puertoRepository.findByNumeroPuerto(request.getNumeroPuerto());
        if (puertoDuplicado.isPresent()) {
            return ResponseEntity.badRequest()
                    .body("Ya existe un puerto con el número " + request.getNumeroPuerto());
        }

        Puerto puerto = new Puerto();
        puerto.setNumeroPuerto(request.getNumeroPuerto());

        puerto = puertoRepository.save(puerto);
        return ResponseEntity.ok(convertToDTO(puerto));
    }

    // Actualizar puerto
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarPuerto(
            @PathVariable Integer id,
            @RequestBody PuertoRequest request) {

        return puertoRepository.findById(id)
                .map(puerto -> {
                    // Verificar puerto duplicado
                    Optional<Puerto> puertoDuplicado = puertoRepository.findByNumeroPuerto(request.getNumeroPuerto());
                    if (puertoDuplicado.isPresent() && !puertoDuplicado.get().getId().equals(id)) {
                        return ResponseEntity.badRequest()
                                .body("Ya existe un puerto con el número " + request.getNumeroPuerto());
                    }

                    puerto.setNumeroPuerto(request.getNumeroPuerto());
                    puerto = puertoRepository.save(puerto);
                    return ResponseEntity.ok(convertToDTO(puerto));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Eliminar puerto
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarPuerto(@PathVariable Integer id) {
        return puertoRepository.findById(id)
                .map(puerto -> {
                    // Verificar si el puerto está siendo usado en algún servicio
                    if (!puerto.getPuertoPorServicios().isEmpty()) {
                        return ResponseEntity.badRequest()
                                .body("No se puede eliminar el puerto porque está siendo usado por servicios...");
                    }

                    puertoRepository.delete(puerto);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private PuertoDTO convertToDTO(Puerto puerto) {
        PuertoDTO dto = new PuertoDTO();
        dto.setId(puerto.getId());
        dto.setNumeroPuerto(puerto.getNumeroPuerto());

        // Agregar información de servicios
        dto.setCantidadServicios(puerto.getPuertoPorServicios().size());

        // Convertir la lista de servicios
        List<PuertoDTO.ServicioResumenDTO> serviciosDTO = puerto.getPuertoPorServicios().stream()
                .map(pps -> {
                    PuertoDTO.ServicioResumenDTO servicioDTO = new PuertoDTO.ServicioResumenDTO();
                    servicioDTO.setId(pps.getServicio().getId());
                    servicioDTO.setNombre(pps.getServicio().getNombre());
                    servicioDTO.setEstado(pps.getServicio().getEstado());
                    return servicioDTO;
                })
                .collect(Collectors.toList());

        dto.setServicios(serviciosDTO);

        return dto;
    }
}