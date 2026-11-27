package br.ifpb.edu.clai.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.ifpb.edu.clai.exception.AlunoNaoEncontradoException;
import br.ifpb.edu.clai.exception.PeiNaoEncontradoException;
import br.ifpb.edu.clai.model.Aluno;
import br.ifpb.edu.clai.model.Pei;
import br.ifpb.edu.clai.repository.PeiRepository;

public class PeiServiceTest {

    private PeiRepository repository;
    private AlunoService alunoService;
    private PeiService service;

    @BeforeEach
    void preparar() {
        repository = mock(PeiRepository.class);
        alunoService = mock(AlunoService.class);
        service = new PeiService(repository, alunoService);
        when(repository.save(any(Pei.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void deveCriarPeiVinculadoAoAluno() {
        when(alunoService.buscarPorId("a1")).thenReturn(Optional.of(new Aluno()));
        Pei entrada = new Pei();
        entrada.setId("lixo");

        Pei salvo = service.criar("a1", entrada);

        assertNull(salvo.getId());
        assertEquals("a1", salvo.getAlunoId());
        verify(repository).save(entrada);
    }

    @Test
    void naoDeveCriarParaAlunoInexistente() {
        when(alunoService.buscarPorId("x")).thenReturn(Optional.empty());

        assertThrows(AlunoNaoEncontradoException.class,
                () -> service.criar("x", new Pei()));
        verify(repository, never()).save(any());
    }

    @Test
    void naoDeveCriarSemAluno() {
        assertThrows(IllegalArgumentException.class,
                () -> service.criar(" ", new Pei()));
    }

    @Test
    void deveListarPorAluno() {
        when(repository.findByAlunoId("a1")).thenReturn(List.of(new Pei()));

        assertEquals(1, service.listarPorAluno("a1").size());
    }

    @Test
    void deveLancarExcecaoQuandoPeiNaoExiste() {
        when(repository.findById("x")).thenReturn(Optional.empty());

        assertThrows(PeiNaoEncontradoException.class,
                () -> service.buscarPorId("x"));
    }

    @Test
    void atualizarDevePreservarIdEAlunoOriginais() {
        Pei existente = new Pei();
        existente.setId("p1");
        existente.setAlunoId("a1");
        existente.setNecessidades("Necessidades anteriores");
        when(repository.findById("p1")).thenReturn(Optional.of(existente));

        Pei dados = new Pei();
        dados.setId("outro-id");
        dados.setAlunoId("OUTRO-ALUNO");
        dados.setNecessidades("Novas necessidades");

        Pei salvo = service.atualizar("p1", dados);

        assertEquals("p1", salvo.getId());
        assertEquals("a1", salvo.getAlunoId());
        assertEquals("Novas necessidades", salvo.getNecessidades());
        verify(repository).save(existente);
    }

    @Test
    void atualizarComDadosNulosDeveRejeitarESemSalvar() {
        assertThrows(IllegalArgumentException.class,
                () -> service.atualizar("p1", null));

        verify(repository, never()).findById(anyString());
        verify(repository, never()).save(any(Pei.class));
    }

    @Test
    void atualizarSomenteUmCampoDevePreservarDemaisInformacoes() {
        Pei existente = new Pei();
        existente.setId("p1");
        existente.setAlunoId("a1");
        existente.setNecessidades("Necessidades já cadastradas");
        existente.setConhecimentos("Conhecimentos já cadastrados");
        existente.setCapacidades("Capacidades já cadastradas");

        Pei.DadosAcademicos academicosExistentes = new Pei.DadosAcademicos();
        academicosExistentes.setCurso("Informática");
        academicosExistentes.setTurma("3A");
        existente.setDadosAcademicos(academicosExistentes);

        Pei.TrajetoriaEscolar trajetoriaExistente = new Pei.TrajetoriaEscolar();
        trajetoriaExistente.setHistoricoEscolar("Histórico anterior");
        existente.setTrajetoriaEscolar(trajetoriaExistente);

        when(repository.findById("p1")).thenReturn(Optional.of(existente));

        Pei dados = new Pei();
        dados.setInteresses("Novo interesse");

        Pei salvo = service.atualizar("p1", dados);

        assertEquals("Novo interesse", salvo.getInteresses());
        assertEquals("Necessidades já cadastradas", salvo.getNecessidades());
        assertEquals("Conhecimentos já cadastrados", salvo.getConhecimentos());
        assertEquals("Capacidades já cadastradas", salvo.getCapacidades());
        assertEquals("Informática", salvo.getDadosAcademicos().getCurso());
        assertEquals("3A", salvo.getDadosAcademicos().getTurma());
        assertEquals("Histórico anterior", salvo.getTrajetoriaEscolar().getHistoricoEscolar());
        verify(repository).save(existente);
    }

    @Test
    void atualizarDadosAcademicosParciaisDevePreservarSubcamposNaoInformados() {
        Pei existente = new Pei();
        existente.setId("p1");
        existente.setAlunoId("a1");

        Pei.DadosAcademicos academicosExistentes = new Pei.DadosAcademicos();
        academicosExistentes.setCurso("Informática");
        academicosExistentes.setPeriodoLetivo("2026.1");
        academicosExistentes.setTurma("3A");
        existente.setDadosAcademicos(academicosExistentes);
        when(repository.findById("p1")).thenReturn(Optional.of(existente));

        Pei dados = new Pei();
        Pei.DadosAcademicos academicosRecebidos = new Pei.DadosAcademicos();
        academicosRecebidos.setTurma("3B");
        dados.setDadosAcademicos(academicosRecebidos);

        Pei salvo = service.atualizar("p1", dados);

        assertEquals("Informática", salvo.getDadosAcademicos().getCurso());
        assertEquals("2026.1", salvo.getDadosAcademicos().getPeriodoLetivo());
        assertEquals("3B", salvo.getDadosAcademicos().getTurma());
    }

    @Test
    void atualizarInexistenteDeveFalhar() {
        when(repository.findById("x")).thenReturn(Optional.empty());

        assertThrows(PeiNaoEncontradoException.class,
                () -> service.atualizar("x", new Pei()));
    }
}
