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

    public List<Pei> listarPorAluno(String alunoId) {
        return peiRepository.findByAlunoId(alunoId);
    }

    public Pei atualizar(String id, Pei dados) {
        Pei existente = buscarPorId(id);

        // o aluno vinculado nunca muda na edição
        dados.setId(existente.getId());
        dados.setAlunoId(existente.getAlunoId());

        return peiRepository.save(dados);
    }
}
