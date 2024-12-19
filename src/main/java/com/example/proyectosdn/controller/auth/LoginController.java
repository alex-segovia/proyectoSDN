package com.example.proyectosdn.controller.auth;

import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/sdn/login")
@Slf4j
public class LoginController {

    @GetMapping("")
    public String mostrarLogin(){
        return "login";
    }

}
