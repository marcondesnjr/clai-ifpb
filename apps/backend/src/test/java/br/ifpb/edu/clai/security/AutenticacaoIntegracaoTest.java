package br.ifpb.edu.clai.security;

import br.ifpb.edu.clai.model.PerfilUsuario;
import br.ifpb.edu.clai.model.Usuario;
import br.ifpb.edu.clai.repository.UsuarioRepository;
import br.ifpb.edu.clai.service.UsuarioService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;

@SpringBootTest(properties =
    "spring.mongodb.uri=mongodb://localhost:27017/clai_us27_integracao_teste")
@AutoConfigureMockMvc
class AutenticacaoIntegracaoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private String usuarioId;

    @AfterEach
    void limparUsuarioTeste() {
        if (usuarioId != null) {
            usuarioRepository.deleteById(usuarioId);
        }
    }

    @Test
    void usuarioAtivoDeveConseguirFazerLogin()
            throws Exception {

        String login = "teste-" + UUID.randomUUID();
        String senha = "SenhaSegura123";

        Usuario usuario = usuarioService.cadastrar(
            "Usuario de Teste",
            login,
            senha,
            PerfilUsuario.EDICAO
        );

        usuarioId = usuario.getId();

        mockMvc.perform(formLogin()
                .user(login)
                .password(senha))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername(login));
    }

    @Test
    void naoDeveAutenticarComSenhaIncorreta()
            throws Exception {

        String login = "teste-" + UUID.randomUUID();

        Usuario usuario = usuarioService.cadastrar(
            "Usuario de Teste",
            login,
            "SenhaCorreta123",
            PerfilUsuario.EDICAO
        );

        usuarioId = usuario.getId();

        mockMvc.perform(formLogin()
                .user(login)
                .password("SenhaErrada123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    void usuarioDesativadoNaoDeveConseguirFazerLogin()
            throws Exception {

        String login = "teste-" + UUID.randomUUID();
        String senha = "SenhaSegura123";

        Usuario usuario = usuarioService.cadastrar(
            "Usuario Desativado",
            login,
            senha,
            PerfilUsuario.CONSULTA
        );

        usuarioId = usuario.getId();

        usuarioService.desativar(usuarioId);

        mockMvc.perform(formLogin()
                .user(login)
                .password(senha))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }
}