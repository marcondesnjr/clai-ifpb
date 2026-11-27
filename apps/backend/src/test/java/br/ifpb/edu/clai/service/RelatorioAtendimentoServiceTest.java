package br.ifpb.edu.clai.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import br.ifpb.edu.clai.exception.AlunoNaoEncontradoException;
import br.ifpb.edu.clai.exception.RelatorioAtendimentoNaoEncontradoException;
import br.ifpb.edu.clai.model.Aluno;
import br.ifpb.edu.clai.model.RelatorioAtendimento;
import br.ifpb.edu.clai.repository.RelatorioAtendimentoRepository;
import org.bson.Document;

class RelatorioAtendimentoServiceTest {

    private RelatorioAtendimentoRepository repository;
    private AlunoService alunoService;
    private MongoTemplate mongoTemplate;
    private RelatorioAtendimentoService service;

    @BeforeEach
    void preparar() {
        repository = mock(RelatorioAtendimentoRepository.class);
        alunoService = mock(AlunoService.class);
        mongoTemplate = mock(MongoTemplate.class);

        service = new RelatorioAtendimentoService(
                repository, alunoService, mongoTemplate);

        when(repository.save(any(RelatorioAtendimento.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void deveRegistrarRelatorioVinculadoAoAluno() {
        when(alunoService.buscarPorId("a1"))
                .thenReturn(Optional.of(new Aluno()));

        Document contador = new Document("sequencia", 1L);
        when(mongoTemplate.findAndModify(
                any(Query.class),
                any(Update.class),
                any(FindAndModifyOptions.class),
                eq(Document.class),
                eq("contadores")))
                .thenReturn(contador);

        RelatorioAtendimento entrada = new RelatorioAtendimento();
        entrada.setId("id-invalido");

        RelatorioAtendimento salvo = service.registrar("a1", entrada);

        assertNull(salvo.getId());
        assertEquals("a1", salvo.getAlunoId());
        assertEquals("ATD-" + LocalDate.now().getYear() + "-000001",
                salvo.getCodigoAtendimento());

        verify(repository).save(entrada);
    }

    @Test
    void naoDeveRegistrarSemAluno() {
        assertThrows(IllegalArgumentException.class,
                () -> service.registrar(" ", new RelatorioAtendimento()));

        verify(repository, never()).save(any());
    }

    @Test
    void naoDeveRegistrarComDadosNulos() {
        assertThrows(IllegalArgumentException.class,
                () -> service.registrar("a1", null));

        verify(repository, never()).save(any());
    }

    @Test
    void naoDeveRegistrarParaAlunoInexistente() {
        when(alunoService.buscarPorId("x"))
                .thenReturn(Optional.empty());

        assertThrows(AlunoNaoEncontradoException.class,
                () -> service.registrar("x", new RelatorioAtendimento()));

        verify(repository, never()).save(any());
    }

    @Test
    void deveListarRelatoriosPorAluno() {
        when(repository.findByAlunoId("a1"))
                .thenReturn(List.of(new RelatorioAtendimento()));

        assertEquals(1, service.listarPorAluno("a1").size());

        verify(repository).findByAlunoId("a1");
    }

    @Test
    void deveLancarExcecaoQuandoRelatorioNaoExiste() {
        when(repository.findById("x"))
                .thenReturn(Optional.empty());

        assertThrows(RelatorioAtendimentoNaoEncontradoException.class,
                () -> service.buscarPorId("x"));
    }

    @Test
    void atualizarDevePreservarIdAlunoECodigoOriginais() {
        RelatorioAtendimento existente = new RelatorioAtendimento();
        existente.setId("r1");
        existente.setAlunoId("a1");
        existente.setCodigoAtendimento("ATD-2026-000001");

        when(repository.findById("r1"))
                .thenReturn(Optional.of(existente));

        RelatorioAtendimento dados = new RelatorioAtendimento();
        dados.setId("outro-id");
        dados.setAlunoId("outro-aluno");
        dados.setCodigoAtendimento("CODIGO-ALTERADO");
        dados.setTitulo("Título atualizado");

        RelatorioAtendimento salvo = service.atualizar("r1", dados);

        assertEquals("r1", salvo.getId());
        assertEquals("a1", salvo.getAlunoId());
        assertEquals("ATD-2026-000001", salvo.getCodigoAtendimento());
        assertEquals("Título atualizado", salvo.getTitulo());

        verify(repository).save(dados);
    }

    @Test
    void naoDeveAtualizarComDadosNulos() {
        assertThrows(IllegalArgumentException.class,
                () -> service.atualizar("r1", null));

        verify(repository, never()).findById(anyString());
        verify(repository, never()).save(any());
    }

    @Test
    void naoDeveAtualizarRelatorioInexistente() {
        when(repository.findById("x"))
                .thenReturn(Optional.empty());

        assertThrows(RelatorioAtendimentoNaoEncontradoException.class,
                () -> service.atualizar("x", new RelatorioAtendimento()));

        verify(repository, never()).save(any());
    }
}
