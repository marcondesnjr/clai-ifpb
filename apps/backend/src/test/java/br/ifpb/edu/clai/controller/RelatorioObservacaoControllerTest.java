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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import br.ifpb.edu.clai.model.Aluno;
import br.ifpb.edu.clai.model.RelatorioObservacao;
import br.ifpb.edu.clai.service.AlunoService;
import br.ifpb.edu.clai.service.RelatorioObservacaoService;

class RelatorioObservacaoControllerTest {

    private RelatorioObservacaoService relatorioService;
    private AlunoService alunoService;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        relatorioService = mock(RelatorioObservacaoService.class);
        alunoService = mock(AlunoService.class);
        mvc = MockMvcBuilders
                .standaloneSetup(new RelatorioObservacaoController(relatorioService, alunoService))
                .build();
    }

    private MockHttpServletRequestBuilder comDadosValidos(MockHttpServletRequestBuilder req) {
        return req.param("alunoId", "a1")
                .param("assistente", "Carlos Lima")
                .param("dataObservacao", "2026-01-10")
                .param("relato", "Participou da aula normalmente");
    }

    @Test
    void listarDeveExporRelatoriosENomesDosAlunos() throws Exception {
        when(relatorioService.listarTodos()).thenReturn(List.of(new RelatorioObservacao()));
        when(alunoService.listarTodos()).thenReturn(List.of());

        mvc.perform(get("/relatorios-observacao"))
                .andExpect(status().isOk())
                .andExpect(view().name("relatorios-observacao/lista-relatorios-observacao"))
                .andExpect(model().attributeExists("relatorios", "nomesAlunos"));
    }

    @Test
    void visualizarDeveExporRelatorioEAluno() throws Exception {
        RelatorioObservacao relatorio = new RelatorioObservacao();
        relatorio.setId("r1");
        relatorio.setAlunoId("a1");
        when(relatorioService.buscarPorId("r1")).thenReturn(relatorio);
        when(alunoService.buscarPorId("a1")).thenReturn(Optional.of(new Aluno()));

        mvc.perform(get("/relatorios-observacao/r1"))
                .andExpect(status().isOk())
                .andExpect(view().name("relatorios-observacao/detalhes-relatorio-observacao"))
                .andExpect(model().attributeExists("relatorio", "aluno"));
    }

    @Test
    void novoDeveAbrirFormularioComListaDeAlunos() throws Exception {
        when(alunoService.listarTodos()).thenReturn(List.of());

        mvc.perform(get("/relatorios-observacao/novo").param("alunoId", "a1"))
                .andExpect(status().isOk())
                .andExpect(view().name("relatorios-observacao/formulario-relatorio-observacao"))
                .andExpect(model().attributeExists("relatorio", "alunos"))
                .andExpect(model().attribute("relatorio",
                        Matchers.hasProperty("alunoId", Matchers.is("a1"))));
    }

    @Test
    void salvarValidoDeveCriarERedirecionar() throws Exception {
        mvc.perform(comDadosValidos(post("/relatorios-observacao/salvar")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/relatorios-observacao"));

        verify(relatorioService).criar(eq("a1"), any(RelatorioObservacao.class));
    }

    @Test
    void salvarSemCamposObrigatoriosDeveReexibirFormulario() throws Exception {
        when(alunoService.listarTodos()).thenReturn(List.of());

        mvc.perform(post("/relatorios-observacao/salvar").param("alunoId", "a1"))
                .andExpect(status().isOk())
                .andExpect(view().name("relatorios-observacao/formulario-relatorio-observacao"))
                .andExpect(model().attributeHasFieldErrors("relatorio",
                        "assistente", "dataObservacao", "relato"));

        verify(relatorioService, never()).criar(any(), any());
    }

    @Test
    void salvarComDataFuturaDeveReexibirFormulario() throws Exception {
        when(alunoService.listarTodos()).thenReturn(List.of());

        mvc.perform(post("/relatorios-observacao/salvar")
                        .param("alunoId", "a1")
                        .param("assistente", "Carlos Lima")
                        .param("dataObservacao", "2999-01-01")
                        .param("relato", "texto"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("relatorio", "dataObservacao"));

        verify(relatorioService, never()).criar(any(), any());
    }

    @Test
    void editarGetDeveAbrirFormularioComAluno() throws Exception {
        RelatorioObservacao existente = new RelatorioObservacao();
        existente.setId("r1");
        existente.setAlunoId("a1");
        when(relatorioService.buscarPorId("r1")).thenReturn(existente);
        when(alunoService.buscarPorId("a1")).thenReturn(Optional.of(new Aluno()));

        mvc.perform(get("/relatorios-observacao/r1/editar"))
                .andExpect(status().isOk())
                .andExpect(view().name("relatorios-observacao/formulario-relatorio-observacao"))
                .andExpect(model().attributeExists("relatorio", "aluno"));
    }

    @Test
    void editarComErroDeveReexibirFormularioComAlunoOriginal() throws Exception {
        RelatorioObservacao existente = new RelatorioObservacao();
        existente.setId("r1");
        existente.setAlunoId("a1");
        when(relatorioService.buscarPorId("r1")).thenReturn(existente);
        when(alunoService.buscarPorId("a1")).thenReturn(Optional.of(new Aluno()));

        mvc.perform(post("/relatorios-observacao/r1/editar"))
                .andExpect(status().isOk())
                .andExpect(view().name("relatorios-observacao/formulario-relatorio-observacao"))
                .andExpect(model().attribute("relatorio",
                        Matchers.hasProperty("alunoId", Matchers.is("a1"))));

        verify(relatorioService, never()).atualizar(any(), any());
    }

    @Test
    void editarValidoDeveAtualizarERedirecionar() throws Exception {
        mvc.perform(comDadosValidos(post("/relatorios-observacao/r1/editar")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/relatorios-observacao"));

        verify(relatorioService).atualizar(eq("r1"), any(RelatorioObservacao.class));
    }
}
