package br.ifpb.edu.clai.service;

import br.ifpb.edu.clai.model.PerfilUsuario;
import br.ifpb.edu.clai.model.Usuario;
import br.ifpb.edu.clai.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioDetailsServiceTest {

    private UsuarioRepository repository;
    private UsuarioDetailsService service;

    @BeforeEach
    void preparar() {
        repository = mock(UsuarioRepository.class);
        service = new UsuarioDetailsService(repository);
    }

    @Test
    void deveCarregarUsuarioAtivoComPerfilEdicao() {
        Usuario usuario = criarUsuario(true);

        when(repository.findByLogin("ana"))
                .thenReturn(Optional.of(usuario));

        UserDetails detalhes =
                service.loadUserByUsername(" ANA ");

        assertEquals("ana", detalhes.getUsername());
        assertEquals("{bcrypt}hash-exemplo", detalhes.getPassword());
        assertTrue(detalhes.isEnabled());

        assertTrue(detalhes.getAuthorities()
                .stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ROLE_EDICAO")));

        verify(repository).findByLogin("ana");
    }

    @Test
    void deveIdentificarUsuarioDesativado() {
        Usuario usuario = criarUsuario(false);

        when(repository.findByLogin("ana"))
                .thenReturn(Optional.of(usuario));

        UserDetails detalhes =
                service.loadUserByUsername("ana");

        assertFalse(detalhes.isEnabled());
    }

    @Test
    void deveRejeitarUsuarioInexistente() {
        when(repository.findByLogin("inexistente"))
                .thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername("inexistente")
        );
    }

    private Usuario criarUsuario(boolean ativo) {
        Usuario usuario = new Usuario();

        usuario.setNome("Ana");
        usuario.setLogin("ana");
        usuario.setSenhaHash("{bcrypt}hash-exemplo");
        usuario.setPerfil(PerfilUsuario.EDICAO);
        usuario.setAtivo(ativo);

        return usuario;
    }
}