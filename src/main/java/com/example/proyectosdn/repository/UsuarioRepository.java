package com.example.proyectosdn.repository;

import com.example.proyectosdn.entity.Dispositivo;
import com.example.proyectosdn.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByDni(String dni);

    @Transactional
    @Modifying
    @Query(nativeQuery = true,value = "insert into usuario(username, attribute, op, value, nombres, apellido_paterno, apellido_materno, rol, dni, estado) values(?1,'SHA256-Password',':=',sha2(?2,256),?3,?4,?5,?6,?7,2)")
    void registrarUsuario(String username,String password,String nombres,String apellidoPaterno,String apellidoMaterno,String rol,String dni);

    @Query(nativeQuery = true,value = "select u.username from usuario u inner join dispositivo d on u.id = d.id_usuario where d.mac=?1")
    String obtenerUsernamePorDispositivo(String macDispositivo);
}