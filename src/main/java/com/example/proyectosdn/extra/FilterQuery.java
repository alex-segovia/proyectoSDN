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
        String url = httpRequest.getRequestURI();
        String ipAdd = httpRequest.getHeader("X-Real-IP");
        if (ipAdd == null || ipAdd.isEmpty()) {
            ipAdd = httpRequest.getHeader("X-Forwarded-For");
            if (ipAdd == null || ipAdd.isEmpty()) {
                ipAdd = httpRequest.getRemoteAddr();
            }
        };
        System.out.println("El IP actualmente conectado es: "+ipAdd);
        Integer idSesion=sesionActivaRepository.idSesionActivaPorIp(ipAdd);
        System.out.println("El ID de sesión actual es: "+idSesion);



        if(idSesion!=null){
            if(url.startsWith("/sdn/login")){
                httpResponse.sendRedirect("http://192.168.200.200:8080/sdn/dispositivos"); // Redirigir si no cumple la condición
            }
            chain.doFilter(request, response); // Continuar con el flujo normal
        }else {
            httpResponse.sendRedirect("http://192.168.200.200:8080/sdn/login"); // Redirigir si no cumple la condición
        }
    }
}
