package br.ifpb.edu.clai.controller;

import br.ifpb.edu.clai.model.PerfilUsuario;
import br.ifpb.edu.clai.model.Usuario;
import br.ifpb.edu.clai.service.UsuarioService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "clai.usuarios.admin.enabled=true")
class UsuarioControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @Test
    void deveRenderizarListagemDeUsuarios() throws Exception {

        Usuario usuario = new Usuario();
        usuario.setNome("Ana");
        usuario.setLogin("ana");
        usuario.setPerfil(PerfilUsuario.EDICAO);
        usuario.setAtivo(true);

        when(usuarioService.listarTodos())
                .thenReturn(List.of(usuario));

        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarios/lista"))
                .andExpect(content().string(containsString("Ana")))
                .andExpect(content().string(containsString("Ativo")));
    }

    @Test
    void deveRenderizarFormularioDeCadastro() throws Exception {

        mockMvc.perform(get("/usuarios/novo"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarios/form"))
                .andExpect(content().string(
                        containsString("Cadastrar usuário")))
                .andExpect(content().string(
                        containsString("EDICAO")))
                .andExpect(content().string(
                        containsString("CONSULTA")));
    }
}