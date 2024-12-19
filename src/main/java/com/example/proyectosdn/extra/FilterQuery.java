package com.example.proyectosdn.extra;

import com.example.proyectosdn.repository.SesionActivaRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class FilterQuery implements Filter {

    private final SesionActivaRepository sesionActivaRepository;

    public FilterQuery(SesionActivaRepository sesionActivaRepository) {
        this.sesionActivaRepository = sesionActivaRepository;
    }


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String ip=httpRequest.getRemoteAddr();
        Integer idSesion=sesionActivaRepository.idSesionActivaPorIp(ip);

        chain.doFilter(request, response); // Continuar con el flujo normal
    }
}
