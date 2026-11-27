package br.ifpb.edu.clai.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.ifpb.edu.clai.exception.AlunoNaoEncontradoException;
import br.ifpb.edu.clai.exception.PlanoAcaoDuplicadoException;
import br.ifpb.edu.clai.exception.PlanoAcaoNaoEncontradaException;
import br.ifpb.edu.clai.model.Aluno;
import br.ifpb.edu.clai.model.PlanoAcao;
import br.ifpb.edu.clai.repository.PlanoAcaoRepository;

class PlanoAcaoServiceTest {

    private PlanoAcaoRepository repository;
    private AlunoService alunoService;
    private PlanoAcaoService service;

    @BeforeEach
    void preparar() {
        repository = mock(PlanoAcaoRepository.class);
        alunoService = mock(AlunoService.class);
        service = new PlanoAcaoService(repository, alunoService);
        when(repository.save(any(PlanoAcao.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void deveCriarPlanoVinculadoAoAluno() {
        when(alunoService.buscarPorId("a1")).thenReturn(Optional.of(new Aluno()));
        PlanoAcao entrada = new PlanoAcao();
        entrada.setId("lixo");

        PlanoAcao salvo = service.criar("a1", entrada);

        assertNull(salvo.getId());
        assertEquals("a1", salvo.getAlunoId());
        verify(repository).save(entrada);
    }

    @Test
    void naoDeveCriarParaAlunoInexistente() {
        when(alunoService.buscarPorId("x")).thenReturn(Optional.empty());

        assertThrows(AlunoNaoEncontradoException.class,
                () -> service.criar("x", new PlanoAcao()));
        verify(repository, never()).save(any());
    }

    @Test
    void naoDeveCriarSemAluno() {
        assertThrows(IllegalArgumentException.class,
                () -> service.criar(" ", new PlanoAcao()));
        verify(repository, never()).save(any());
    }

    @Test
    void naoDeveCriarSemDados() {
        assertThrows(IllegalArgumentException.class,
                () -> service.criar("a1", null));
    }

    @Test
    void naoDeveCriarSegundoPlanoParaOMesmoAluno() {
        when(alunoService.buscarPorId("a1")).thenReturn(Optional.of(new Aluno()));
        when(repository.existsByAlunoId("a1")).thenReturn(true);

        assertThrows(PlanoAcaoDuplicadoException.class,
                () -> service.criar("a1", new PlanoAcao()));
        verify(repository, never()).save(any());
    }

    @Test
    void deveBuscarPorId() {
        PlanoAcao plano = new PlanoAcao();
        when(repository.findById("p1")).thenReturn(Optional.of(plano));

        assertSame(plano, service.buscarPorId("p1"));
    }

    @Test
    void deveLancarExcecaoQuandoPlanoNaoExiste() {
        when(repository.findById("x")).thenReturn(Optional.empty());

        assertThrows(PlanoAcaoNaoEncontradaException.class, () -> service.buscarPorId("x"));
    }

    @Test
    void deveListarTodos() {
        when(repository.findAll()).thenReturn(List.of(new PlanoAcao(), new PlanoAcao()));

        assertEquals(2, service.listarTodos().size());
    }

    @Test
    void deveListarPorAluno() {
        when(repository.findByAlunoId("a1")).thenReturn(List.of(new PlanoAcao()));

        assertEquals(1, service.listarPorAluno("a1").size());
    }

    @Test
    void atualizarDevePreservarIdEAlunoOriginais() {
        PlanoAcao existente = new PlanoAcao();
        existente.setId("p1");
        existente.setAlunoId("a1");
        when(repository.findById("p1")).thenReturn(Optional.of(existente));

        PlanoAcao novos = new PlanoAcao();
        novos.setId("outro");
        novos.setAlunoId("a2");
        novos.setDemandasDoCaso("atualizado");

        PlanoAcao salvo = service.atualizar("p1", novos);

        assertEquals("p1", salvo.getId());
        assertEquals("a1", salvo.getAlunoId());
        assertEquals("atualizado", salvo.getDemandasDoCaso());
    }

    @Test
    void atualizarInexistenteDeveFalhar() {
        when(repository.findById("x")).thenReturn(Optional.empty());

        assertThrows(PlanoAcaoNaoEncontradaException.class,
                () -> service.atualizar("x", new PlanoAcao()));
        verify(repository, never()).save(any());
    }
}
