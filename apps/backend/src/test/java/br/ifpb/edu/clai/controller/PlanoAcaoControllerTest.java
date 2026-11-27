package br.ifpb.edu.clai.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
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

import br.ifpb.edu.clai.exception.PlanoAcaoDuplicadoException;
import br.ifpb.edu.clai.model.Aluno;
import br.ifpb.edu.clai.model.PlanoAcao;
import br.ifpb.edu.clai.service.AlunoService;
import br.ifpb.edu.clai.service.PlanoAcaoService;

class PlanoAcaoControllerTest {

    private PlanoAcaoService planoService;
    private AlunoService alunoService;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        planoService = mock(PlanoAcaoService.class);
        alunoService = mock(AlunoService.class);
        mvc = MockMvcBuilders
                .standaloneSetup(new PlanoAcaoController(planoService, alunoService))
                .build();
    }

    private MockHttpServletRequestBuilder comDadosValidos(MockHttpServletRequestBuilder req) {
        return req.param("alunoId", "a1")
                .param("necessidadesEspecificas", "Apoio na leitura")
                .param("dificuldadesPercurso", "Concentração")
                .param("demandasDoCaso", "Acompanhamento")
                .param("propostasIntervencao", "Atendimentos semanais");
    }

    @Test
    void listarDeveExporPlanosENomesDosAlunos() throws Exception {
        when(planoService.listarTodos()).thenReturn(List.of(new PlanoAcao()));
        when(alunoService.listarTodos()).thenReturn(List.of());

        mvc.perform(get("/planos-acao"))
                .andExpect(status().isOk())
                .andExpect(view().name("planos-acao/lista-planos-acao"))
                .andExpect(model().attributeExists("planos", "nomesAlunos"));
    }

    @Test
    void visualizarDeveExporPlanoEAluno() throws Exception {
        PlanoAcao plano = new PlanoAcao();
        plano.setId("p1");
        plano.setAlunoId("a1");
        when(planoService.buscarPorId("p1")).thenReturn(plano);
        when(alunoService.buscarPorId("a1")).thenReturn(Optional.of(new Aluno()));

        mvc.perform(get("/planos-acao/p1"))
                .andExpect(status().isOk())
                .andExpect(view().name("planos-acao/detalhes-plano-acao"))
                .andExpect(model().attributeExists("plano", "aluno"));
    }

    @Test
    void novoDeveAbrirFormularioComListaDeAlunos() throws Exception {
        when(alunoService.listarTodos()).thenReturn(List.of());

        mvc.perform(get("/planos-acao/novo").param("alunoId", "a1"))
                .andExpect(status().isOk())
                .andExpect(view().name("planos-acao/formulario-plano-acao"))
                .andExpect(model().attributeExists("plano", "alunos"))
                .andExpect(model().attribute("plano",
                        Matchers.hasProperty("alunoId", Matchers.is("a1"))));
    }

    @Test
    void salvarValidoDeveCriarERedirecionar() throws Exception {
        mvc.perform(comDadosValidos(post("/planos-acao/salvar")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/planos-acao"));

        verify(planoService).criar(eq("a1"), any(PlanoAcao.class));
    }

    @Test
    void salvarSemCamposObrigatoriosDeveReexibirFormulario() throws Exception {
        when(alunoService.listarTodos()).thenReturn(List.of());

        mvc.perform(post("/planos-acao/salvar").param("alunoId", "a1"))
                .andExpect(status().isOk())
                .andExpect(view().name("planos-acao/formulario-plano-acao"))
                .andExpect(model().attributeHasFieldErrors("plano",
                        "necessidadesEspecificas", "dificuldadesPercurso",
                        "demandasDoCaso", "propostasIntervencao"));

        verify(planoService, never()).criar(any(), any());
    }

    @Test
    void salvarSemAlunoDeveReexibirFormulario() throws Exception {
        when(alunoService.listarTodos()).thenReturn(List.of());

        mvc.perform(post("/planos-acao/salvar")
                        .param("necessidadesEspecificas", "a")
                        .param("dificuldadesPercurso", "b")
                        .param("demandasDoCaso", "c")
                        .param("propostasIntervencao", "d"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("plano", "alunoId"));

        verify(planoService, never()).criar(any(), any());
    }

    @Test
    void salvarComAlunoJaTendoPlanoDeveMostrarErroNoFormulario() throws Exception {
        when(alunoService.listarTodos()).thenReturn(List.of());
        doThrow(new PlanoAcaoDuplicadoException("Já existe um Plano de Ação cadastrado para este aluno."))
                .when(planoService).criar(eq("a1"), any(PlanoAcao.class));

        mvc.perform(comDadosValidos(post("/planos-acao/salvar")))
                .andExpect(status().isOk())
                .andExpect(view().name("planos-acao/formulario-plano-acao"))
                .andExpect(model().attributeHasFieldErrors("plano", "alunoId"));
    }

    @Test
    void editarGetDeveAbrirFormularioComAluno() throws Exception {
        PlanoAcao existente = new PlanoAcao();
        existente.setId("p1");
        existente.setAlunoId("a1");
        when(planoService.buscarPorId("p1")).thenReturn(existente);
        when(alunoService.buscarPorId("a1")).thenReturn(Optional.of(new Aluno()));

        mvc.perform(get("/planos-acao/p1/editar"))
                .andExpect(status().isOk())
                .andExpect(view().name("planos-acao/formulario-plano-acao"))
                .andExpect(model().attributeExists("plano", "aluno"));
    }

    @Test
    void editarComErroDeveReexibirFormularioComAlunoOriginal() throws Exception {
        PlanoAcao existente = new PlanoAcao();
        existente.setId("p1");
        existente.setAlunoId("a1");
        when(planoService.buscarPorId("p1")).thenReturn(existente);
        when(alunoService.buscarPorId("a1")).thenReturn(Optional.of(new Aluno()));

        mvc.perform(post("/planos-acao/p1/editar"))
                .andExpect(status().isOk())
                .andExpect(view().name("planos-acao/formulario-plano-acao"))
                .andExpect(model().attribute("plano",
                        Matchers.hasProperty("alunoId", Matchers.is("a1"))));

        verify(planoService, never()).atualizar(any(), any());
    }

    @Test
    void editarValidoDeveAtualizarERedirecionar() throws Exception {
        mvc.perform(comDadosValidos(post("/planos-acao/p1/editar")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/planos-acao"));

        verify(planoService).atualizar(eq("p1"), any(PlanoAcao.class));
    }
}
