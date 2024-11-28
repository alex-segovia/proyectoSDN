package com.example.proyectosdn.controller.auth;

import com.example.proyectosdn.entity.Usuario;
import com.example.proyectosdn.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/autenticacion")
@Slf4j
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/")
    public String mostrarLogin(Model model,
                               @RequestParam(required = false) String logout,
                               HttpSession session) {
        return "login";
    }

    @PostMapping("/registro")
    public String registrarUsuario(@Valid Usuario usuario,
                                   BindingResult bindingResult,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {

        return "";
    }
}
