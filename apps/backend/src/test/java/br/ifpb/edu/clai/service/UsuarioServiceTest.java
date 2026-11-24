package br.ifpb.edu.clai.service;

import br.ifpb.edu.clai.model.PerfilUsuario;
import br.ifpb.edu.clai.model.Usuario;
import br.ifpb.edu.clai.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UsuarioServiceTest {

    private UsuarioRepository repository;
    private UsuarioService service;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void preparar() {
        repository = mock(UsuarioRepository.class);

        passwordEncoder =
                PasswordEncoderFactories.createDelegatingPasswordEncoder();

        service = new UsuarioService(repository, passwordEncoder);

        when(repository.save(any(Usuario.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void deveCadastrarUsuarioComSenhaProtegida() {
        Usuario usuario = service.cadastrar(
                "Kyldery",
                "KYLDERY",
                "senhaSegura123",
                PerfilUsuario.EDICAO
        );

        assertEquals("Kyldery", usuario.getNome());
        assertEquals("kyldery", usuario.getLogin());
        assertEquals(PerfilUsuario.EDICAO, usuario.getPerfil());
        assertTrue(usuario.isAtivo());

        assertNotEquals("senhaSegura123", usuario.getSenhaHash());

        assertTrue(passwordEncoder.matches(
                "senhaSegura123",
                usuario.getSenhaHash()
        ));

        verify(repository).save(any(Usuario.class));
    }

    @Test
    void naoDeveCadastrarLoginDuplicado() {
        when(repository.existsByLogin("kyldery"))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                service.cadastrar(
                        "Kyldery",
                        "KYLDERY",
                        "senha123",
                        PerfilUsuario.EDICAO
                )
        );

        verify(repository, never()).save(any());
    }

    @Test
    void naoDeveCadastrarSemNome() {
        assertThrows(IllegalArgumentException.class, () ->
                service.cadastrar(
                        "",
                        "usuario",
                        "senha123",
                        PerfilUsuario.CONSULTA
                )
        );
    }

    @Test
    void naoDeveCadastrarSemSenha() {
        assertThrows(IllegalArgumentException.class, () ->
                service.cadastrar(
                        "Usuario",
                        "usuario",
                        "",
                        PerfilUsuario.CONSULTA
                )
        );
    }

    @Test
    void naoDeveCadastrarSemPerfil() {
        assertThrows(IllegalArgumentException.class, () ->
                service.cadastrar(
                        "Usuario",
                        "usuario",
                        "senha123",
                        null
                )
        );
    }
}