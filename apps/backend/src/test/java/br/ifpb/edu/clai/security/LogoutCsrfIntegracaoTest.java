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

import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties =
    "spring.mongodb.uri=mongodb://localhost:27017/clai_us27_logout_teste")
@AutoConfigureMockMvc
class LogoutCsrfIntegracaoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private String usuarioId;

    @AfterEach
    void limpar() {
        if (usuarioId != null) {
            usuarioRepository.deleteById(usuarioId);
        }
    }

    private MockHttpSession autenticarUsuario() throws Exception {

        String login = "teste-" + UUID.randomUUID();
        String senha = "SenhaSegura123";

        Usuario usuario = usuarioService.cadastrar(
            "Usuario de Teste",
            login,
            senha,
            PerfilUsuario.EDICAO
        );

        usuarioId = usuario.getId();

        MvcResult resultado = mockMvc.perform(
                formLogin()
                    .user(login)
                    .password(senha)
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(authenticated().withUsername(login))
            .andReturn();

        MockHttpSession sessao =
            (MockHttpSession) resultado.getRequest()
                .getSession(false);

        assertNotNull(sessao);

        return sessao;
    }

    @Test
    void logoutDeveInvalidarSessao() throws Exception {

        MockHttpSession sessao = autenticarUsuario();

        mockMvc.perform(get("/").session(sessao))
                .andExpect(status().isOk());

        mockMvc.perform(
                post("/logout")
                    .session(sessao)
                    .with(csrf())
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login?logout"));

        assertTrue(sessao.isInvalid());
    }

    @Test
    void logoutSemCsrfDeveSerBloqueado() throws Exception {

        MockHttpSession sessao = autenticarUsuario();

        mockMvc.perform(
                post("/logout").session(sessao)
            )
            .andExpect(status().isForbidden());

        assertFalse(sessao.isInvalid());

        mockMvc.perform(get("/").session(sessao))
                .andExpect(status().isOk());
    }

    @Test
    void postSemCsrfDeveSerBloqueado() throws Exception {

        MockHttpSession sessao = autenticarUsuario();

        mockMvc.perform(
                post("/alunos/salvar")
                    .session(sessao)
            )
            .andExpect(status().isForbidden());
    }

    @Test
    void gerenciamentoDeUsuariosContinuaBloqueado()
            throws Exception {

        MockHttpSession sessao = autenticarUsuario();

        mockMvc.perform(
                get("/usuarios/novo").session(sessao)
            )
            .andExpect(status().isForbidden());
    }
}