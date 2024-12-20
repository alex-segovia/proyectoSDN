package com.example.proyectosdn.service;

import com.example.proyectosdn.entity.Usuario;
import com.example.proyectosdn.repository.SesionActivaRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UsuarioSesionService {

    @Autowired
    private SesionActivaRepository sesionActivaRepository;

    public String obtenerIpCliente(HttpServletRequest request) {
        String ipAdd = request.getHeader("X-Real-IP");
        if (ipAdd == null || ipAdd.isEmpty()) {
            ipAdd = request.getHeader("X-Forwarded-For");
            if (ipAdd == null || ipAdd.isEmpty()) {
                ipAdd = request.getRemoteAddr();
            }
        }
        return ipAdd;
    }

    public Usuario obtenerUsuarioActivo(HttpServletRequest request) {
        String ip = obtenerIpCliente(request);
        Usuario usuario = sesionActivaRepository.findUsuarioActivoByIp(ip);

        if (usuario == null) {
            log.error("No se encontró usuario activo para la IP: {}", ip);
            throw new RuntimeException("No se encontró sesión activa");
        }

        return usuario;
    }
}
