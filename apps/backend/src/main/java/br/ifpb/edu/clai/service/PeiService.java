package br.ifpb.edu.clai.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.ifpb.edu.clai.exception.AlunoNaoEncontradoException;
import br.ifpb.edu.clai.exception.PeiNaoEncontradoException;
import br.ifpb.edu.clai.model.Pei;
import br.ifpb.edu.clai.repository.PeiRepository;

@Service
public class PeiService {

    private final PeiRepository peiRepository;
    private final AlunoService alunoService;

    public PeiService(PeiRepository peiRepository,
                      AlunoService alunoService) {
        this.peiRepository = peiRepository;
        this.alunoService = alunoService;
    }

    public Pei criar(String alunoId, Pei pei) {
        if (alunoId == null || alunoId.isBlank()) {
            throw new IllegalArgumentException("O aluno vinculado é obrigatório.");
        }

        if (pei == null) {
            throw new IllegalArgumentException("Os dados do PEI são obrigatórios.");
        }

        alunoService.buscarPorId(alunoId)
                .orElseThrow(() -> new AlunoNaoEncontradoException(
                        "Aluno não encontrado para o ID: " + alunoId));

        pei.setId(null);
        pei.setAlunoId(alunoId);

        return peiRepository.save(pei);
    }

    public Pei buscarPorId(String id) {
        return peiRepository.findById(id)
                .orElseThrow(() -> new PeiNaoEncontradoException(
                        "PEI não encontrado para o ID: " + id));
    }

    public List<Pei> listarTodos() {
        return peiRepository.findAll();
    }

    public List<Pei> listarPorAluno(String alunoId) {
        return peiRepository.findByAlunoId(alunoId);
    }

    public Pei atualizar(String id, Pei dados) {
        if (dados == null) {
            throw new IllegalArgumentException("Os dados do PEI para atualização são obrigatórios.");
        }

        Pei existente = buscarPorId(id);

        // Atualiza somente os campos enviados, preservando os que vierem como null.
        atualizarDadosAcademicos(existente, dados);
        atualizarTrajetoriaEscolar(existente, dados);

        if (dados.getNecessidades() != null) {
            existente.setNecessidades(dados.getNecessidades());
        }
        if (dados.getConhecimentos() != null) {
            existente.setConhecimentos(dados.getConhecimentos());
        }
        if (dados.getCapacidades() != null) {
            existente.setCapacidades(dados.getCapacidades());
        }
        if (dados.getInteresses() != null) {
            existente.setInteresses(dados.getInteresses());
        }
        if (dados.getDificuldades() != null) {
            existente.setDificuldades(dados.getDificuldades());
        }
        if (dados.getAdaptacoesCurriculares() != null) {
            // Uma lista vazia é considerada uma atualização explícita (limpa a lista).
            existente.setAdaptacoesCurriculares(dados.getAdaptacoesCurriculares());
        }

        // O ID do PEI e o aluno vinculado não são alterados pela requisição.
        return peiRepository.save(existente);
    }

    private void atualizarDadosAcademicos(Pei existente, Pei dados) {
        Pei.DadosAcademicos dadosRecebidos = dados.getDadosAcademicos();
        if (dadosRecebidos == null) {
            return;
        }

        Pei.DadosAcademicos atuais = existente.getDadosAcademicos();
        if (atuais == null) {
            atuais = new Pei.DadosAcademicos();
            existente.setDadosAcademicos(atuais);
        }

        if (dadosRecebidos.getCurso() != null) {
            atuais.setCurso(dadosRecebidos.getCurso());
        }
        if (dadosRecebidos.getPeriodoLetivo() != null) {
            atuais.setPeriodoLetivo(dadosRecebidos.getPeriodoLetivo());
        }
        if (dadosRecebidos.getTurma() != null) {
            atuais.setTurma(dadosRecebidos.getTurma());
        }
        if (dadosRecebidos.getProfissionalResponsavel() != null) {
            atuais.setProfissionalResponsavel(dadosRecebidos.getProfissionalResponsavel());
        }
        if (dadosRecebidos.getDataElaboracao() != null) {
            atuais.setDataElaboracao(dadosRecebidos.getDataElaboracao());
        }
    }

    private void atualizarTrajetoriaEscolar(Pei existente, Pei dados) {
        Pei.TrajetoriaEscolar dadosRecebidos = dados.getTrajetoriaEscolar();
        if (dadosRecebidos == null) {
            return;
        }

        Pei.TrajetoriaEscolar atuais = existente.getTrajetoriaEscolar();
        if (atuais == null) {
            atuais = new Pei.TrajetoriaEscolar();
            existente.setTrajetoriaEscolar(atuais);
        }

        if (dadosRecebidos.getEscolaridadeAnterior() != null) {
            atuais.setEscolaridadeAnterior(dadosRecebidos.getEscolaridadeAnterior());
        }
        if (dadosRecebidos.getHistoricoEscolar() != null) {
            atuais.setHistoricoEscolar(dadosRecebidos.getHistoricoEscolar());
        }
        if (dadosRecebidos.getExperienciasRelevantes() != null) {
            atuais.setExperienciasRelevantes(dadosRecebidos.getExperienciasRelevantes());
        }
    }
}
