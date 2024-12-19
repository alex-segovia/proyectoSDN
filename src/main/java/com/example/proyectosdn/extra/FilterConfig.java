package com.example.proyectosdn.extra;

import com.example.proyectosdn.extra.FilterQuery;
import com.example.proyectosdn.repository.SesionActivaRepository;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterQuery filterQuery(SesionActivaRepository sesionActivaRepository) {
        return new FilterQuery(sesionActivaRepository); // Proporciona manualmente el repositorio necesario
    }

    @Bean
    public FilterRegistrationBean<FilterQuery> queryValidationFilter(FilterQuery filterQuery) {
        FilterRegistrationBean<FilterQuery> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(filterQuery);
        registrationBean.addUrlPatterns("/sdn/dispositivos", "/sdn/servicios", "/sdn/usuarios");
        return registrationBean;
    }
}