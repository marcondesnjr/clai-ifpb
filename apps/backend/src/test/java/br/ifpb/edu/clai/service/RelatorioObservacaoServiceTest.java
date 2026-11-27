package br.ifpb.edu.clai.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.ifpb.edu.clai.exception.AlunoNaoEncontradoException;
import br.ifpb.edu.clai.exception.RelatorioObservacaoNaoEncontradoException;
import br.ifpb.edu.clai.model.Aluno;
import br.ifpb.edu.clai.model.RelatorioObservacao;
import br.ifpb.edu.clai.repository.RelatorioObservacaoRepository;

class RelatorioObservacaoServiceTest {

    private RelatorioObservacaoRepository repository;
    private AlunoService alunoService;
    private RelatorioObservacaoService service;

    @BeforeEach
    void preparar() {
        repository = mock(RelatorioObservacaoRepository.class);
        alunoService = mock(AlunoService.class);
        service = new RelatorioObservacaoService(repository, alunoService);
        when(repository.save(any(RelatorioObservacao.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void deveCriarRelatorioVinculadoAoAluno() {
        when(alunoService.buscarPorId("a1")).thenReturn(Optional.of(new Aluno()));
        RelatorioObservacao entrada = new RelatorioObservacao();
        entrada.setId("lixo");

        RelatorioObservacao salvo = service.criar("a1", entrada);

        assertNull(salvo.getId());
        assertEquals("a1", salvo.getAlunoId());
        verify(repository).save(entrada);
    }

    @Test
    void naoDeveCriarParaAlunoInexistente() {
        when(alunoService.buscarPorId("x")).thenReturn(Optional.empty());

        assertThrows(AlunoNaoEncontradoException.class,
                () -> service.criar("x", new RelatorioObservacao()));
        verify(repository, never()).save(any());
    }

    @Test
    void naoDeveCriarSemAluno() {
        assertThrows(IllegalArgumentException.class,
                () -> service.criar(" ", new RelatorioObservacao()));
        verify(repository, never()).save(any());
    }

    @Test
    void naoDeveCriarSemDados() {
        assertThrows(IllegalArgumentException.class,
                () -> service.criar("a1", null));
    }

    @Test
    void deveBuscarPorId() {
        RelatorioObservacao plano = new RelatorioObservacao();
        when(repository.findById("p1")).thenReturn(Optional.of(plano));

        assertSame(plano, service.buscarPorId("p1"));
    }

    @Test
    void deveLancarExcecaoQuandoRelatorioNaoExiste() {
        when(repository.findById("x")).thenReturn(Optional.empty());

        assertThrows(RelatorioObservacaoNaoEncontradoException.class, () -> service.buscarPorId("x"));
    }

    @Test
    void deveListarTodos() {
        when(repository.findAll()).thenReturn(List.of(new RelatorioObservacao(), new RelatorioObservacao()));

        assertEquals(2, service.listarTodos().size());
    }

    @Test
    void deveListarPorAluno() {
        when(repository.findByAlunoId("a1")).thenReturn(List.of(new RelatorioObservacao()));

        assertEquals(1, service.listarPorAluno("a1").size());
    }

    @Test
    void atualizarDevePreservarIdEAlunoOriginais() {
        RelatorioObservacao existente = new RelatorioObservacao();
        existente.setId("p1");
        existente.setAlunoId("a1");
        when(repository.findById("p1")).thenReturn(Optional.of(existente));

        RelatorioObservacao novos = new RelatorioObservacao();
        novos.setId("outro");
        novos.setAlunoId("a2");
        novos.setRelato("atualizado");

        RelatorioObservacao salvo = service.atualizar("p1", novos);

        assertEquals("p1", salvo.getId());
        assertEquals("a1", salvo.getAlunoId());
        assertEquals("atualizado", salvo.getRelato());
    }

    @Test
    void atualizarInexistenteDeveFalhar() {
        when(repository.findById("x")).thenReturn(Optional.empty());

        assertThrows(RelatorioObservacaoNaoEncontradoException.class,
                () -> service.atualizar("x", new RelatorioObservacao()));
        verify(repository, never()).save(any());
    }
}
