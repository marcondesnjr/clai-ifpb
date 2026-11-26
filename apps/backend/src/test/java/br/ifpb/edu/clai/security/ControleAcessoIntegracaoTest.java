package br.ifpb.edu.clai.security;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties =
    "spring.mongodb.uri=mongodb://localhost:27017/clai_us28_teste")
@AutoConfigureMockMvc
class ControleAcessoIntegracaoTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void consultaPodeVisualizarAlunos() throws Exception {

        mockMvc.perform(get("/alunos")
                .with(user("consultor").roles("CONSULTA")))
                .andExpect(status().isOk());
    }

    @Test
    void consultaNaoPodeAbrirCadastro() throws Exception {

        mockMvc.perform(get("/alunos/novo")
                .with(user("consultor").roles("CONSULTA")))
                .andExpect(status().isForbidden());
    }

    @Test
    void consultaNaoPodeSalvarAluno() throws Exception {

        mockMvc.perform(post("/alunos/salvar")
                .with(user("consultor").roles("CONSULTA"))
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void consultaNaoPodeAbrirEdicao() throws Exception {

        mockMvc.perform(get("/alunos/123/editar")
                .with(user("consultor").roles("CONSULTA")))
                .andExpect(status().isForbidden());
    }

    @Test
    void consultaNaoPodeExcluirAluno() throws Exception {

        mockMvc.perform(post("/alunos/123/excluir")
                .with(user("consultor").roles("CONSULTA"))
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void consultaNaoPodeCadastrarEstudo() throws Exception {

        mockMvc.perform(post("/estudos-de-caso/salvar")
                .with(user("consultor").roles("CONSULTA"))
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void edicaoPodeConsultarAlunos() throws Exception {

        mockMvc.perform(get("/alunos")
                .with(user("editor").roles("EDICAO")))
                .andExpect(status().isOk());
    }

    @Test
    void consultaNaoDeveVerBotaoCadastrar() throws Exception {

        mockMvc.perform(get("/alunos")
                .with(user("consultor").roles("CONSULTA")))
                .andExpect(status().isOk())
                .andExpect(content().string(
                    not(containsString("cadastrar-aluno-btn"))
                ));
    }

    @Test
    void edicaoDeveVerBotaoCadastrar() throws Exception {

        mockMvc.perform(get("/alunos")
                .with(user("editor").roles("EDICAO")))
                .andExpect(status().isOk())
                .andExpect(content().string(
                    containsString("cadastrar-aluno-btn")
                ));
    }

    @Test
    void administracaoContinuaBloqueada() throws Exception {

        mockMvc.perform(get("/usuarios")
                .with(user("editor").roles("EDICAO")))
                .andExpect(status().isForbidden());
    }
}