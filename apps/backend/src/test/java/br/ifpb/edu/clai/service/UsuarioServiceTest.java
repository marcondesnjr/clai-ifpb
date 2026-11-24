package br.ifpb.edu.clai.service;

import br.ifpb.edu.clai.model.PerfilUsuario;
import br.ifpb.edu.clai.model.Usuario;
import br.ifpb.edu.clai.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.dao.DuplicateKeyException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.Optional;

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

    @Test
    void deveListarTodosOsUsuarios() {
        Usuario primeiro = new Usuario();
        primeiro.setNome("Ana");

        Usuario segundo = new Usuario();
        segundo.setNome("Carlos");

        when(repository.findAll())
                .thenReturn(List.of(primeiro, segundo));

        List<Usuario> usuarios = service.listarTodos();

        assertEquals(2, usuarios.size());
        assertEquals("Ana", usuarios.get(0).getNome());
        assertEquals("Carlos", usuarios.get(1).getNome());

        verify(repository).findAll();
    }

    @Test
    void deveBuscarUsuarioPorId() {
        Usuario usuario = new Usuario();
        usuario.setId("123");
        usuario.setNome("Ana");

        when(repository.findById("123"))
                .thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = service.buscarPorId("123");

        assertTrue(resultado.isPresent());
        assertEquals("Ana", resultado.get().getNome());

        verify(repository).findById("123");
    }

    @Test
    void deveRetornarVazioParaIdInvalido() {
        Optional<Usuario> resultado = service.buscarPorId(" ");

        assertTrue(resultado.isEmpty());

        verify(repository, never()).findById(anyString());
    }

    @Test
    void deveBuscarUsuarioPorLoginNormalizado() {
        Usuario usuario = new Usuario();
        usuario.setLogin("ana");

        when(repository.findByLogin("ana"))
                .thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado =
                service.buscarPorLogin(" ANA ");

        assertTrue(resultado.isPresent());
        assertEquals("ana", resultado.get().getLogin());

        verify(repository).findByLogin("ana");
    }

    @Test
    void deveRetornarVazioParaLoginInvalido() {
        Optional<Usuario> resultado = service.buscarPorLogin("");

        assertTrue(resultado.isEmpty());

        verify(repository, never()).findByLogin(anyString());
    }
    @Test
    void naoDeveCadastrarUsuarioComSenhaCurta() {

        assertThrows(IllegalArgumentException.class, () ->
            service.cadastrar(
                "Ana",
                "ana",
                "1234567",
                PerfilUsuario.EDICAO
            )
        );

        verify(repository, never()).save(any());
    }

    @Test
    void deveTratarLoginDuplicadoDetectadoPeloBanco() {

        doThrow(new DuplicateKeyException(
                "E11000 duplicate key error"
        )).when(repository).save(any(Usuario.class));

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.cadastrar(
                        "Ana",
                        "ANA",
                        "senhaSegura123",
                        PerfilUsuario.EDICAO
                )
        );

        assertEquals("Este login já está cadastrado.",
                erro.getMessage()
        );

        verify(repository).save(any(Usuario.class));
    }

    @Test
    void deveAtualizarDadosDoUsuario() {
        Usuario usuario = new Usuario();
        usuario.setId("u1");
        usuario.setNome("Ana");
        usuario.setLogin("ana");
        usuario.setSenhaHash("hash-original");
        usuario.setPerfil(PerfilUsuario.CONSULTA);
        usuario.setAtivo(false);

        when(repository.findById("u1"))
                .thenReturn(Optional.of(usuario));

        Usuario atualizado = service.atualizar(
                "u1",
                "Ana Silva",
                "ANA.NOVA",
                PerfilUsuario.EDICAO
        );

        assertEquals("Ana Silva", atualizado.getNome());
        assertEquals("ana.nova", atualizado.getLogin());
        assertEquals(PerfilUsuario.EDICAO, atualizado.getPerfil());
        assertEquals("hash-original", atualizado.getSenhaHash());
        assertFalse(atualizado.isAtivo());

        verify(repository).save(usuario);
    }

    @Test
    void naoDeveAtualizarParaLoginDuplicado() {
        Usuario usuario = new Usuario();
        usuario.setId("u1");
        usuario.setLogin("ana");

        when(repository.findById("u1"))
                .thenReturn(Optional.of(usuario));

        when(repository.existsByLogin("carlos"))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                service.atualizar(
                        "u1",
                        "Ana Silva",
                        "carlos",
                        PerfilUsuario.EDICAO
                )
        );

        verify(repository, never()).save(any(Usuario.class));
    }
    @Test
    void deveDesativarUsuario() {
        Usuario usuario = new Usuario();
        usuario.setId("u1");
        usuario.setAtivo(true);

        when(repository.findById("u1"))
                .thenReturn(Optional.of(usuario));

        Usuario resultado = service.desativar("u1");

        assertFalse(resultado.isAtivo());
        verify(repository).save(usuario);
    }

    @Test
    void deveReativarUsuario() {
        Usuario usuario = new Usuario();
        usuario.setId("u1");
        usuario.setAtivo(false);

        when(repository.findById("u1"))
                .thenReturn(Optional.of(usuario));

        Usuario resultado = service.reativar("u1");

        assertTrue(resultado.isAtivo());
        verify(repository).save(usuario);
    }

    @Test
    void naoDeveSalvarQuandoUsuarioJaEstaInativo() {
        Usuario usuario = new Usuario();
        usuario.setId("u1");
        usuario.setAtivo(false);

        when(repository.findById("u1"))
                .thenReturn(Optional.of(usuario));

        Usuario resultado = service.desativar("u1");

        assertFalse(resultado.isAtivo());
        verify(repository, never()).save(any(Usuario.class));
    }

    @Test
    void naoDeveDesativarUsuarioInexistente() {
        when(repository.findById("inexistente"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.desativar("inexistente"));

        verify(repository, never()).save(any(Usuario.class));
    }
}