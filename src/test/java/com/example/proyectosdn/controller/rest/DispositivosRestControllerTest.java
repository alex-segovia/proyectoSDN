package com.example.proyectosdn.controller.rest;

import com.example.proyectosdn.dto.DispositivoDTO;
import com.example.proyectosdn.dto.DispositivoRequest;
import com.example.proyectosdn.entity.Dispositivo;
import com.example.proyectosdn.entity.ServicioPorDispositivo;
import com.example.proyectosdn.entity.Usuario;
import com.example.proyectosdn.repository.DispositivoRepository;
import com.example.proyectosdn.repository.ServicioPorDispositivoRepository;
import com.example.proyectosdn.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DispositivosRestControllerTest {

    @Mock
    private DispositivoRepository dispositivoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ServicioPorDispositivoRepository servicioPorDispositivoRepository;

    @InjectMocks
    private DispositivosRestController controller;

    private Dispositivo dispositivo;
    private Usuario usuario;
    private DispositivoRequest request;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1);
        usuario.setUsername("juanperez");
        usuario.setNombres("Juan");
        usuario.setApellidoPaterno("Pérez");

        dispositivo = new Dispositivo();
        dispositivo.setId(1);
        dispositivo.setNombre("Dispositivo Test");
        dispositivo.setMac("00:11:22:33:44:55");
        dispositivo.setAutenticado(1);
        dispositivo.setEstado(1);
        dispositivo.setUsuario(usuario);
        dispositivo.setServicioPorDispositivos(new ArrayList<>());

        request = new DispositivoRequest();
        request.setNombre("Dispositivo Test");
        request.setMac("00:11:22:33:44:55");
        request.setAutenticado(1);
        request.setEstado(1);
        request.setUsuarioId(1);
    }

    @Test
    void listarDispositivos_DebeRetornarListaDeDispositivos() {
        // Arrange
        List<Dispositivo> dispositivos = Arrays.asList(dispositivo);
        when(dispositivoRepository.findAll()).thenReturn(dispositivos);

        // Act
        ResponseEntity<List<DispositivoDTO>> response = controller.listarDispositivos();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getNombre()).isEqualTo(dispositivo.getNombre());
        verify(dispositivoRepository).findAll();
    }

    @Test
    void obtenerDispositivo_CuandoExiste_DebeRetornarDispositivo() {
        // Arrange
        when(dispositivoRepository.findById(1)).thenReturn(Optional.of(dispositivo));

        // Act
        ResponseEntity<DispositivoDTO> response = controller.obtenerDispositivo(1);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(dispositivo.getId());
        verify(dispositivoRepository).findById(1);
    }

    @Test
    void obtenerDispositivo_CuandoNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(dispositivoRepository.findById(1)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<DispositivoDTO> response = controller.obtenerDispositivo(1);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(dispositivoRepository).findById(1);
    }

    @Test
    void crearDispositivo_ConDatosValidos_DebeCrearDispositivo() {
        // Arrange
        when(dispositivoRepository.findByMac(request.getMac())).thenReturn(Optional.empty());
        when(usuarioRepository.findById(request.getUsuarioId())).thenReturn(Optional.of(usuario));
        when(dispositivoRepository.save(any(Dispositivo.class))).thenReturn(dispositivo);

        // Act
        ResponseEntity<?> response = controller.crearDispositivo(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(dispositivoRepository).findByMac(request.getMac());
        verify(usuarioRepository).findById(request.getUsuarioId());
        verify(dispositivoRepository).save(any(Dispositivo.class));
    }

    @Test
    void crearDispositivo_ConMacDuplicada_DebeRetornarBadRequest() {
        // Arrange
        when(dispositivoRepository.findByMac(request.getMac())).thenReturn(Optional.of(dispositivo));

        // Act
        ResponseEntity<?> response = controller.crearDispositivo(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(dispositivoRepository).findByMac(request.getMac());
        verify(dispositivoRepository, never()).save(any(Dispositivo.class));
    }

    @Test
    void actualizarDispositivo_ConDatosValidos_DebeActualizarDispositivo() {
        // Arrange
        when(dispositivoRepository.findById(1)).thenReturn(Optional.of(dispositivo));
        when(usuarioRepository.findById(request.getUsuarioId())).thenReturn(Optional.of(usuario));
        when(dispositivoRepository.save(any(Dispositivo.class))).thenReturn(dispositivo);

        // Act
        ResponseEntity<?> response = controller.actualizarDispositivo(1, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(dispositivoRepository).findById(1);
        verify(usuarioRepository).findById(request.getUsuarioId());
        verify(dispositivoRepository).save(any(Dispositivo.class));
    }

    @Test
    void actualizarDispositivo_CuandoNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(dispositivoRepository.findById(1)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = controller.actualizarDispositivo(1, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(dispositivoRepository).findById(1);
        verify(dispositivoRepository, never()).save(any(Dispositivo.class));
    }

    @Test
    void eliminarDispositivo_SinServiciosAsociados_DebeEliminarDispositivo() {
        // Arrange
        dispositivo.setServicioPorDispositivos(new ArrayList<>());
        when(dispositivoRepository.findById(1)).thenReturn(Optional.of(dispositivo));

        // Act
        ResponseEntity<?> response = controller.eliminarDispositivo(1);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(dispositivoRepository).findById(1);
        verify(dispositivoRepository).delete(dispositivo);
    }

    @Test
    void eliminarDispositivo_ConServiciosAsociados_DebeRetornarBadRequest() {
        // Arrange
        ServicioPorDispositivo spd = new ServicioPorDispositivo();
        dispositivo.setServicioPorDispositivos(Arrays.asList(spd));
        when(dispositivoRepository.findById(1)).thenReturn(Optional.of(dispositivo));

        // Act
        ResponseEntity<?> response = controller.eliminarDispositivo(1);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(dispositivoRepository).findById(1);
        verify(dispositivoRepository, never()).delete(any(Dispositivo.class));
    }
}