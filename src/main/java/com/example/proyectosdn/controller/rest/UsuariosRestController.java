package com.example.proyectosdn.controller.rest;

import com.example.proyectosdn.controller.dto.DispositivoResumenDTO;
import com.example.proyectosdn.controller.dto.ServicioResumenDTO;
import com.example.proyectosdn.controller.dto.UsuarioDTO;
import com.example.proyectosdn.controller.dto.UsuarioRequest;
import com.example.proyectosdn.entity.Usuario;
import com.example.proyectosdn.repository.UsuarioRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
@Slf4j
public class UsuariosRestController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Listar todos los usuarios
    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> listarUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        List<UsuarioDTO> dtos = usuarios.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Obtener usuario por ID
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> obtenerUsuario(@PathVariable Integer id) {
        return usuarioRepository.findById(id)
                .map(this::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Crear usuario
    @PostMapping
    public ResponseEntity<?> crearUsuario(@Valid @RequestBody UsuarioRequest request) {
        // Verificar username duplicado
        if (usuarioRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body("Ya existe un usuario con el username " + request.getUsername());
        }

        // Verificar DNI duplicado
        if (usuarioRepository.findByDni(request.getDni()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body("Ya existe un usuario con el DNI " + request.getDni());
        }

        try {
            usuarioRepository.registrarUsuario(
                    request.getUsername(),
                    request.getPassword(),
                    request.getNombres(),
                    request.getApellidoPaterno(),
                    request.getApellidoMaterno(),
                    request.getRol(),
                    request.getDni()
            );

            Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("Error al crear usuario"));

            return ResponseEntity.ok(convertToDTO(usuario));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error al crear el usuario: " + e.getMessage());
        }
    }

    // Actualizar usuario
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarUsuario(
            @PathVariable Integer id,
            @Valid @RequestBody UsuarioRequest request) {

        return usuarioRepository.findById(id)
                .map(usuario -> {
                    // Verificar username duplicado
                    usuarioRepository.findByUsername(request.getUsername())
                            .ifPresent(u -> {
                                if (!u.getId().equals(id)) {
                                    throw new RuntimeException("Username ya existe");
                                }
                            });

                    // Verificar DNI duplicado
                    usuarioRepository.findByDni(request.getDni())
                            .ifPresent(u -> {
                                if (!u.getId().equals(id)) {
                                    throw new RuntimeException("DNI ya existe");
                                }
                            });

                    usuario.setUsername(request.getUsername());
                    usuario.setNombres(request.getNombres());
                    usuario.setApellidoPaterno(request.getApellidoPaterno());
                    usuario.setApellidoMaterno(request.getApellidoMaterno());
                    usuario.setRol(request.getRol());
                    usuario.setDni(request.getDni());
                    usuario.setEstado(request.getEstado());

                    try {
                        usuario = usuarioRepository.save(usuario);
                        return ResponseEntity.ok(convertToDTO(usuario));
                    } catch (Exception e) {
                        return ResponseEntity.badRequest()
                                .body("Error al actualizar el usuario: " + e.getMessage());
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Eliminar usuario
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Integer id) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    // Verificar si tiene dispositivos asociados
                    if (!usuario.getDispositivos().isEmpty()) {
                        return ResponseEntity.badRequest()
                                .body("No se puede eliminar el usuario porque tiene dispositivos asociados");
                    }

                    // Verificar si ha creado servicios
                    if (!usuario.getServicios().isEmpty()) {
                        return ResponseEntity.badRequest()
                                .body("No se puede eliminar el usuario porque ha creado servicios");
                    }

                    try {
                        usuarioRepository.delete(usuario);
                        return ResponseEntity.ok().build();
                    } catch (Exception e) {
                        return ResponseEntity.badRequest()
                                .body("Error al eliminar el usuario: " + e.getMessage());
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private UsuarioDTO convertToDTO(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        dto.setNombres(usuario.getNombres());
        dto.setApellidoPaterno(usuario.getApellidoPaterno());
        dto.setApellidoMaterno(usuario.getApellidoMaterno());
        dto.setRol(usuario.getRol());
        dto.setDni(usuario.getDni());
        dto.setEstado(usuario.getEstado());

        // Convertir dispositivos
        if (usuario.getDispositivos() != null) {
            List<DispositivoResumenDTO> dispositivosDTO = usuario.getDispositivos().stream()
                    .map(dispositivo -> {
                        DispositivoResumenDTO dispDTO = new DispositivoResumenDTO();
                        dispDTO.setId(dispositivo.getId());
                        dispDTO.setNombre(dispositivo.getNombre());
                        dispDTO.setMac(dispositivo.getMac());
                        dispDTO.setEstado(dispositivo.getEstado());
                        return dispDTO;
                    })
                    .collect(Collectors.toList());
            dto.setDispositivos(dispositivosDTO);
        }

        // Convertir servicios creados
        if (usuario.getServicios() != null) {
            List<ServicioResumenDTO> serviciosDTO = usuario.getServicios().stream()
                    .map(servicio -> {
                        ServicioResumenDTO servDTO = new ServicioResumenDTO();
                        servDTO.setId(servicio.getId());
                        servDTO.setNombre(servicio.getNombre());
                        servDTO.setEstado(servicio.getEstado());
                        return servDTO;
                    })
                    .collect(Collectors.toList());
            dto.setServiciosCreados(serviciosDTO);
        }

        return dto;
    }
}
