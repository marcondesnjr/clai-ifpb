package br.ifpb.edu.clai.security;

import br.ifpb.edu.clai.model.EstudoDeCaso;
import br.ifpb.edu.clai.service.AlunoService;
import br.ifpb.edu.clai.service.EstudoDeCasoService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties =
    "spring.mongodb.uri=mongodb://localhost:27017/clai_us28_teste")
@AutoConfigureMockMvc
class EdicaoAutorizadaIntegracaoTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AlunoService alunoService;

    @MockitoBean
    private EstudoDeCasoService estudoService;

    @Test
    void edicaoPodeExcluirAluno() throws Exception {

        mockMvc.perform(post("/alunos/aluno-teste/excluir")
                .with(user("editor").roles("EDICAO"))
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/alunos"));

        verify(alunoService).remover("aluno-teste");
    }

    @Test
    void consultaNaoPodeExcluirAluno() throws Exception {

        mockMvc.perform(post("/alunos/aluno-teste/excluir")
                .with(user("consultor").roles("CONSULTA"))
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(alunoService, never()).remover(anyString());
    }

    @Test
    void edicaoPodeCriarEstudoDeCaso() throws Exception {

        mockMvc.perform(post("/estudos-de-caso/salvar")
                .with(user("editor").roles("EDICAO"))
                .with(csrf())
                .param("alunoId", "aluno-teste"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/estudos-de-caso"));

        verify(estudoService).criar(
                eq("aluno-teste"),
                any(EstudoDeCaso.class)
        );
    }

    @Test
    void consultaNaoPodeCriarEstudoDeCaso()
            throws Exception {

        mockMvc.perform(post("/estudos-de-caso/salvar")
                .with(user("consultor").roles("CONSULTA"))
                .with(csrf())
                .param("alunoId", "aluno-teste"))
                .andExpect(status().isForbidden());

        verify(estudoService, never())
                .criar(anyString(), any(EstudoDeCaso.class));
    }
}