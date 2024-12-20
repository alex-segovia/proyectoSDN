package com.example.proyectosdn.controller.rest;

import com.example.proyectosdn.dto.ServicioDTO;
import com.example.proyectosdn.dto.ServicioRequest;
import com.example.proyectosdn.entity.*;
import com.example.proyectosdn.repository.PuertoPorServicioRepository;
import com.example.proyectosdn.repository.PuertoRepository;
import com.example.proyectosdn.repository.ServicioRepository;
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
class ServiciosRestControllerTest {

    @Mock
    private ServicioRepository servicioRepository;

    @Mock
    private PuertoRepository puertoRepository;

    @Mock
    private PuertoPorServicioRepository puertoPorServicioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ServiciosRestController controller;

    private Servicio servicio;
    private Usuario usuario;
    private Puerto puerto;
    private ServicioRequest request;
    private PuertoPorServicio puertoPorServicio;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1);
        usuario.setUsername("juanperez");
        usuario.setNombres("Juan");
        usuario.setApellidoPaterno("Pérez");

        puerto = new Puerto();
        puerto.setId(1);
        puerto.setNumeroPuerto(8080);

        servicio = new Servicio();
        servicio.setId(1);
        servicio.setNombre("Servicio Test");
        servicio.setEstado(1);
        servicio.setUsuarioCreador(usuario);
        servicio.setServicioPorDispositivos(new ArrayList<>());

        puertoPorServicio = new PuertoPorServicio();
        puertoPorServicio.setServicio(servicio);
        puertoPorServicio.setPuerto(puerto);

        List<PuertoPorServicio> puertoPorServicios = new ArrayList<>();
        puertoPorServicios.add(puertoPorServicio);
        servicio.setPuertoPorServicios(puertoPorServicios);

        request = new ServicioRequest();
        request.setNombre("Servicio Test");
        request.setEstado(1);
        request.setUsuarioCreadorId(1);
        request.setPuertos(Arrays.asList(8080));
    }

    @Test
    void listarServicios_DebeRetornarListaDeServicios() {
        // Arrange
        List<Servicio> servicios = Arrays.asList(servicio);
        when(servicioRepository.findAll()).thenReturn(servicios);

        // Act
        ResponseEntity<List<ServicioDTO>> response = controller.listarServicios();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getNombre()).isEqualTo(servicio.getNombre());
        verify(servicioRepository).findAll();
    }

    @Test
    void obtenerServicio_CuandoExiste_DebeRetornarServicio() {
        // Arrange
        when(servicioRepository.findById(1)).thenReturn(Optional.of(servicio));

        // Act
        ResponseEntity<ServicioDTO> response = controller.obtenerServicio(1);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(servicio.getId());
        assertThat(response.getBody().getNombre()).isEqualTo(servicio.getNombre());
        verify(servicioRepository).findById(1);
    }

    @Test
    void obtenerServicio_CuandoNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(servicioRepository.findById(1)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<ServicioDTO> response = controller.obtenerServicio(1);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(servicioRepository).findById(1);
    }

    @Test
    void crearServicio_ConDatosValidos_DebeCrearServicio() {
        // Arrange
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(puertoRepository.findByNumeroPuerto(8080)).thenReturn(Optional.of(puerto));
        when(servicioRepository.save(any(Servicio.class))).thenReturn(servicio);
        when(puertoPorServicioRepository.save(any(PuertoPorServicio.class))).thenReturn(puertoPorServicio);

        // Act
        ResponseEntity<ServicioDTO> response = controller.crearServicio(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getNombre()).isEqualTo(servicio.getNombre());
        verify(usuarioRepository).findById(1);
        verify(puertoRepository).findByNumeroPuerto(8080);
        verify(servicioRepository).save(any(Servicio.class));
        verify(puertoPorServicioRepository).save(any(PuertoPorServicio.class));
    }

    @Test
    void actualizarServicio_ConDatosValidos_DebeActualizarServicio() {
        // Arrange
        List<PuertoPorServicio> puertoPorServicios = Arrays.asList(puertoPorServicio);
        when(servicioRepository.findById(1)).thenReturn(Optional.of(servicio));
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(puertoRepository.findByNumeroPuerto(8080)).thenReturn(Optional.of(puerto));
        when(servicioRepository.save(any(Servicio.class))).thenReturn(servicio);
        when(puertoPorServicioRepository.findByServicioId(1)).thenReturn(puertoPorServicios);
        when(puertoPorServicioRepository.save(any(PuertoPorServicio.class))).thenReturn(puertoPorServicio);

        // Act
        ResponseEntity<ServicioDTO> response = controller.actualizarServicio(1, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(servicioRepository).findById(1);
        verify(usuarioRepository).findById(1);
        verify(puertoRepository).findByNumeroPuerto(8080);
        verify(servicioRepository).save(any(Servicio.class));
        verify(puertoPorServicioRepository).deleteAll(puertoPorServicios);
        verify(puertoPorServicioRepository).save(any(PuertoPorServicio.class));
    }

    @Test
    void actualizarServicio_CuandoNoExiste_DebeRetornarNotFound() {
        // Arrange
        when(servicioRepository.findById(1)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<ServicioDTO> response = controller.actualizarServicio(1, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(servicioRepository).findById(1);
        verify(servicioRepository, never()).save(any(Servicio.class));
    }

    @Test
    void eliminarServicio_SinDispositivosAsociados_DebeEliminarServicio() {
        // Arrange
        List<PuertoPorServicio> puertoPorServicios = Arrays.asList(puertoPorServicio);
        when(servicioRepository.findById(1)).thenReturn(Optional.of(servicio));
        when(puertoPorServicioRepository.findByServicioId(1)).thenReturn(puertoPorServicios);

        // Act
        ResponseEntity<?> response = controller.eliminarServicio(1);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(servicioRepository).findById(1);
        verify(puertoPorServicioRepository).deleteAll(puertoPorServicios);
        verify(servicioRepository).delete(servicio);
    }

    @Test
    void eliminarServicio_ConDispositivosAsociados_DebeRetornarBadRequest() {
        // Arrange
        servicio.setServicioPorDispositivos(Arrays.asList(new ServicioPorDispositivo()));
        when(servicioRepository.findById(1)).thenReturn(Optional.of(servicio));

        // Act
        ResponseEntity<?> response = controller.eliminarServicio(1);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(servicioRepository).findById(1);
        verify(servicioRepository, never()).delete(any(Servicio.class));
    }
}