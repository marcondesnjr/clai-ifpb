package br.ifpb.edu.clai.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;
import java.util.Optional;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import br.ifpb.edu.clai.model.Aluno;
import br.ifpb.edu.clai.model.Pei;
import br.ifpb.edu.clai.service.AlunoService;
import br.ifpb.edu.clai.service.PeiService;

class PeiControllerTest {

    private PeiService peiService;
    private AlunoService alunoService;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        peiService = mock(PeiService.class);
        alunoService = mock(AlunoService.class);
        mvc = MockMvcBuilders
                .standaloneSetup(new PeiController(peiService, alunoService))
                .build();
    }

    @Test
    void listarDeveExporPeisENomesDosAlunos() throws Exception {
        when(peiService.listarTodos()).thenReturn(List.of(new Pei()));
        when(alunoService.listarTodos()).thenReturn(List.of());

        mvc.perform(get("/peis"))
                .andExpect(status().isOk())
                .andExpect(view().name("peis/lista-peis"))
                .andExpect(model().attributeExists("peis", "nomesAlunos"));
    }

    @Test
    void visualizarDeveExporPeiEAluno() throws Exception {
        Pei pei = new Pei();
        pei.setId("p1");
        pei.setAlunoId("a1");
        when(peiService.buscarPorId("p1")).thenReturn(pei);
        when(alunoService.buscarPorId("a1")).thenReturn(Optional.of(new Aluno()));

        mvc.perform(get("/peis/p1"))
                .andExpect(status().isOk())
                .andExpect(view().name("peis/detalhes-pei"))
                .andExpect(model().attributeExists("pei", "aluno"));
    }

    @Test
    void novoDeveAbrirFormularioComAninhadosInicializados() throws Exception {
        when(alunoService.listarTodos()).thenReturn(List.of());

        mvc.perform(get("/peis/novo"))
                .andExpect(status().isOk())
                .andExpect(view().name("peis/formulario-pei"))
                .andExpect(model().attributeExists("pei", "alunos"))
                .andExpect(model().attribute("pei",
                        Matchers.hasProperty("dadosAcademicos", Matchers.notNullValue())));
    }

    @Test
    void novoComAlunoIdDeveJaVincularOAluno() throws Exception {
        when(alunoService.listarTodos()).thenReturn(List.of());

        mvc.perform(get("/peis/novo").param("alunoId", "a1"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("pei",
                        Matchers.hasProperty("alunoId", Matchers.is("a1"))));
    }

    @Test
    void salvarValidoDeveCriarERedirecionar() throws Exception {
        mvc.perform(post("/peis/salvar")
                        .param("alunoId", "a1")
                        .param("dadosAcademicos.profissionalResponsavel", "Maria Souza")
                        .param("dadosAcademicos.dataElaboracao", "2026-01-10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/peis"));

        verify(peiService).criar(eq("a1"), any(Pei.class));
    }

    @Test
    void salvarSemAlunoDeveReexibirFormularioSemChamarOServico() throws Exception {
        when(alunoService.listarTodos()).thenReturn(List.of());

        mvc.perform(post("/peis/salvar")
                        .param("dadosAcademicos.profissionalResponsavel", "Maria Souza")
                        .param("dadosAcademicos.dataElaboracao", "2026-01-10"))
                .andExpect(status().isOk())
                .andExpect(view().name("peis/formulario-pei"))
                .andExpect(model().attributeHasFieldErrors("pei", "alunoId"));

        verify(peiService, never()).criar(any(), any());
    }

    @Test
    void salvarSemProfissionalDeveReexibirFormulario() throws Exception {
        when(alunoService.listarTodos()).thenReturn(List.of());

        mvc.perform(post("/peis/salvar")
                        .param("alunoId", "a1")
                        .param("dadosAcademicos.dataElaboracao", "2026-01-10"))
                .andExpect(status().isOk())
                .andExpect(view().name("peis/formulario-pei"))
                .andExpect(model().attributeHasFieldErrors("pei",
                        "dadosAcademicos.profissionalResponsavel"));

        verify(peiService, never()).criar(any(), any());
    }

    @Test
    void editarGetDeveAbrirFormularioComAluno() throws Exception {
        Pei existente = new Pei();
        existente.setId("p1");
        existente.setAlunoId("a1");
        when(peiService.buscarPorId("p1")).thenReturn(existente);
        when(alunoService.buscarPorId("a1")).thenReturn(Optional.of(new Aluno()));

        mvc.perform(get("/peis/p1/editar"))
                .andExpect(status().isOk())
                .andExpect(view().name("peis/formulario-pei"))
                .andExpect(model().attributeExists("pei", "aluno"));
    }

    @Test
    void editarComErroDeveReexibirFormularioComAlunoOriginal() throws Exception {
        Pei existente = new Pei();
        existente.setId("p1");
        existente.setAlunoId("a1");
        when(peiService.buscarPorId("p1")).thenReturn(existente);
        when(alunoService.buscarPorId("a1")).thenReturn(Optional.of(new Aluno()));

        mvc.perform(post("/peis/p1/editar"))
                .andExpect(status().isOk())
                .andExpect(view().name("peis/formulario-pei"))
                .andExpect(model().attribute("pei",
                        Matchers.hasProperty("alunoId", Matchers.is("a1"))));

        verify(peiService, never()).atualizar(any(), any());
    }

    @Test
    void editarValidoDeveAtualizarERedirecionar() throws Exception {
        mvc.perform(post("/peis/p1/editar")
                        .param("alunoId", "a1")
                        .param("dadosAcademicos.profissionalResponsavel", "Maria Souza")
                        .param("dadosAcademicos.dataElaboracao", "2026-01-10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/peis"));

        verify(peiService).atualizar(eq("p1"), any(Pei.class));
    }
}
