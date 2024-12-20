package com.example.proyectosdn.controller.rest;

import com.example.proyectosdn.dto.PuertoDTO;
import com.example.proyectosdn.dto.PuertoRequest;
import com.example.proyectosdn.entity.Puerto;
import com.example.proyectosdn.entity.PuertoPorServicio;
import com.example.proyectosdn.repository.PuertoRepository;
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
class PuertosRestControllerTest {

    @Mock
    private PuertoRepository puertoRepository;

    @InjectMocks
    private PuertosRestController controller;

    private Puerto puerto;
    private PuertoRequest request;

    @BeforeEach
    void setUp() {
        puerto = new Puerto();
        puerto.setId(1);
        puerto.setNumeroPuerto(8080);
        puerto.setPuertoPorServicios(new ArrayList<>());

        request = new PuertoRequest();
        request.setNumeroPuerto(8080);
    }

    @Test
    void listarPuertos_DebeRetornarListaDePuertos() {
        // Arrange
        List<Puerto> puertos = Arrays.asList(puerto);
        when(puertoRepository.findAll()).thenReturn(puertos);

        // Act
        ResponseEntity<List<PuertoDTO>> response = controller.listarPuertos();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getNumeroPuerto()).isEqualTo(puerto.getNumeroPuerto());
        verify(puertoRepository).findAll();
    }

    @Test
    void obtenerPuerto_CuandoExiste_DebeRetornarPuerto() {
        // Arrange
        when(puertoRepository.findById(1)).thenReturn(Optional.of(puerto));

        // Act
        ResponseEntity<PuertoDTO> response = controller.obtenerPuerto(1);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(puerto.getId());
        assertThat(response.getBody().getNumeroPuerto()).isEqualTo(puerto.getNumeroPuerto());
        verify(puertoRepository).findById(1);
    }

    @Test
    void obtenerPuerto_CuandoNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(puertoRepository.findById(1)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<PuertoDTO> response = controller.obtenerPuerto(1);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(puertoRepository).findById(1);
    }

    @Test
    void crearPuerto_ConDatosValidos_DebeCrearPuerto() {
        // Arrange
        when(puertoRepository.findByNumeroPuerto(request.getNumeroPuerto())).thenReturn(Optional.empty());
        when(puertoRepository.save(any(Puerto.class))).thenReturn(puerto);

        // Act
        ResponseEntity<?> response = controller.crearPuerto(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(puertoRepository).findByNumeroPuerto(request.getNumeroPuerto());
        verify(puertoRepository).save(any(Puerto.class));
    }

    @Test
    void crearPuerto_ConNumeroDuplicado_DebeRetornarBadRequest() {
        // Arrange
        when(puertoRepository.findByNumeroPuerto(request.getNumeroPuerto())).thenReturn(Optional.of(puerto));

        // Act
        ResponseEntity<?> response = controller.crearPuerto(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(puertoRepository).findByNumeroPuerto(request.getNumeroPuerto());
        verify(puertoRepository, never()).save(any(Puerto.class));
    }

    @Test
    void actualizarPuerto_ConDatosValidos_DebeActualizarPuerto() {
        // Arrange
        when(puertoRepository.findById(1)).thenReturn(Optional.of(puerto));
        when(puertoRepository.findByNumeroPuerto(request.getNumeroPuerto())).thenReturn(Optional.empty());
        when(puertoRepository.save(any(Puerto.class))).thenReturn(puerto);

        // Act
        ResponseEntity<?> response = controller.actualizarPuerto(1, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(puertoRepository).findById(1);
        verify(puertoRepository).save(any(Puerto.class));
    }

    @Test
    void actualizarPuerto_ConNumeroDuplicado_DebeRetornarBadRequest() {
        // Arrange
        Puerto puertoDuplicado = new Puerto();
        puertoDuplicado.setId(2);
        puertoDuplicado.setNumeroPuerto(8080);

        when(puertoRepository.findById(1)).thenReturn(Optional.of(puerto));
        when(puertoRepository.findByNumeroPuerto(request.getNumeroPuerto())).thenReturn(Optional.of(puertoDuplicado));

        // Act
        ResponseEntity<?> response = controller.actualizarPuerto(1, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(puertoRepository).findById(1);
        verify(puertoRepository, never()).save(any(Puerto.class));
    }

    @Test
    void actualizarPuerto_CuandoNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(puertoRepository.findById(1)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = controller.actualizarPuerto(1, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(puertoRepository).findById(1);
        verify(puertoRepository, never()).save(any(Puerto.class));
    }

    @Test
    void eliminarPuerto_SinServiciosAsociados_DebeEliminarPuerto() {
        // Arrange
        when(puertoRepository.findById(1)).thenReturn(Optional.of(puerto));

        // Act
        ResponseEntity<?> response = controller.eliminarPuerto(1);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(puertoRepository).findById(1);
        verify(puertoRepository).delete(puerto);
    }

    @Test
    void eliminarPuerto_ConServiciosAsociados_DebeRetornarBadRequest() {
        // Arrange
        puerto.setPuertoPorServicios(Arrays.asList(new PuertoPorServicio()));
        when(puertoRepository.findById(1)).thenReturn(Optional.of(puerto));

        // Act
        ResponseEntity<?> response = controller.eliminarPuerto(1);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(puertoRepository).findById(1);
        verify(puertoRepository, never()).delete(any(Puerto.class));
    }
}