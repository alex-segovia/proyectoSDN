package com.example.proyectosdn.controller.rest;

import com.example.proyectosdn.dto.DispositivoDTO;
import com.example.proyectosdn.dto.DispositivoRequest;
import com.example.proyectosdn.entity.Dispositivo;
import com.example.proyectosdn.entity.Usuario;
import com.example.proyectosdn.repository.DispositivoRepository;
import com.example.proyectosdn.repository.ServicioPorDispositivoRepository;
import com.example.proyectosdn.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dispositivos")
@Slf4j
public class DispositivosRestController {

    @Autowired
    private DispositivoRepository dispositivoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ServicioPorDispositivoRepository servicioPorDispositivoRepository;

    // Listar todos los dispositivos
    @GetMapping
    public ResponseEntity<List<DispositivoDTO>> listarDispositivos() {
        List<Dispositivo> dispositivos = dispositivoRepository.findAll();
        List<DispositivoDTO> dtos = dispositivos.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Obtener dispositivo por ID
    @GetMapping("/{id}")
    public ResponseEntity<DispositivoDTO> obtenerDispositivo(@PathVariable Integer id) {
        return dispositivoRepository.findById(id)
                .map(this::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Crear dispositivo
    @PostMapping
    public ResponseEntity<?> crearDispositivo(@RequestBody DispositivoRequest request) {
        // Verificar MAC duplicada
        Optional<Dispositivo> dispositivoExistente = dispositivoRepository.findByMac(request.getMac());
        if (dispositivoExistente.isPresent()) {
            return ResponseEntity.badRequest()
                    .body("Ya existe un dispositivo con la MAC " + request.getMac());
        }

        Dispositivo dispositivo = new Dispositivo();
        dispositivo.setNombre(request.getNombre());
        dispositivo.setMac(request.getMac());
        dispositivo.setAutenticado(request.getAutenticado());
        dispositivo.setEstado(request.getEstado());

        // Asignar usuario si existe
        if (request.getUsuarioId() != null) {
            Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            dispositivo.setUsuario(usuario);
        }

        dispositivo = dispositivoRepository.save(dispositivo);
        return ResponseEntity.ok(convertToDTO(dispositivo));
    }

    // Actualizar dispositivo
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarDispositivo(
            @PathVariable Integer id,
            @RequestBody DispositivoRequest request) {

        return dispositivoRepository.findById(id)
                .map(dispositivo -> {
                    // Verificar MAC duplicada si está cambiando
                    if (!dispositivo.getMac().equals(request.getMac())) {
                        Optional<Dispositivo> dispositivoExistente = dispositivoRepository.findByMac(request.getMac());
                        if (dispositivoExistente.isPresent()) {
                            return ResponseEntity.badRequest()
                                    .body("Ya existe un dispositivo con la MAC " + request.getMac());
                        }
                    }

                    dispositivo.setNombre(request.getNombre());
                    dispositivo.setMac(request.getMac());
                    dispositivo.setAutenticado(request.getAutenticado());
                    dispositivo.setEstado(request.getEstado());

                    // Actualizar usuario
                    if (request.getUsuarioId() != null) {
                        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                        dispositivo.setUsuario(usuario);
                    } else {
                        dispositivo.setUsuario(null);
                    }

                    dispositivo = dispositivoRepository.save(dispositivo);
                    return ResponseEntity.ok(convertToDTO(dispositivo));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Eliminar dispositivo
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarDispositivo(@PathVariable Integer id) {
        return dispositivoRepository.findById(id)
                .map(dispositivo -> {
                    // Verificar si tiene servicios asociados
                    if (!dispositivo.getServicioPorDispositivos().isEmpty()) {
                        return ResponseEntity.badRequest()
                                .body("No se puede eliminar el dispositivo porque tiene servicios asociados");
                    }

                    dispositivoRepository.delete(dispositivo);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private DispositivoDTO convertToDTO(Dispositivo dispositivo) {
        DispositivoDTO dto = new DispositivoDTO();
        dto.setId(dispositivo.getId());
        dto.setNombre(dispositivo.getNombre());
        dto.setMac(dispositivo.getMac());
        dto.setAutenticado(dispositivo.getAutenticado());
        dto.setEstado(dispositivo.getEstado());

        // Convertir usuario si existe
        if (dispositivo.getUsuario() != null) {
            DispositivoDTO.UsuarioResumenDTO usuarioDTO = new DispositivoDTO.UsuarioResumenDTO();
            usuarioDTO.setId(dispositivo.getUsuario().getId());
            usuarioDTO.setUsername(dispositivo.getUsuario().getUsername());
            usuarioDTO.setNombres(dispositivo.getUsuario().getNombres());
            usuarioDTO.setApellidoPaterno(dispositivo.getUsuario().getApellidoPaterno());
            dto.setUsuario(usuarioDTO);
        }

        // Convertir servicios asociados
        List<DispositivoDTO.ServicioResumenDTO> serviciosDTO = dispositivo.getServicioPorDispositivos().stream()
                .map(spd -> {
                    DispositivoDTO.ServicioResumenDTO servicioDTO = new DispositivoDTO.ServicioResumenDTO();
                    servicioDTO.setId(spd.getServicio().getId());
                    servicioDTO.setNombre(spd.getServicio().getNombre());
                    servicioDTO.setEstado(spd.getServicio().getEstado());
                    servicioDTO.setMotivo(spd.getMotivo());
                    return servicioDTO;
                })
                .collect(Collectors.toList());
        dto.setServicios(serviciosDTO);

        return dto;
    }
}
