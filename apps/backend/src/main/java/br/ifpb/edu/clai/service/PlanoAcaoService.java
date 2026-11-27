package br.ifpb.edu.clai.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.ifpb.edu.clai.exception.AlunoNaoEncontradoException;
import br.ifpb.edu.clai.exception.PlanoAcaoDuplicadoException;
import br.ifpb.edu.clai.exception.PlanoAcaoNaoEncontradaException;
import br.ifpb.edu.clai.model.PlanoAcao;
import br.ifpb.edu.clai.repository.PlanoAcaoRepository;

@Service
public class PlanoAcaoService {

    private final PlanoAcaoRepository planoAcaoRepository;
    private final AlunoService alunoService;

    public PlanoAcaoService(PlanoAcaoRepository planoAcaoRepository,
                            AlunoService alunoService) {
        this.planoAcaoRepository = planoAcaoRepository;
        this.alunoService = alunoService;
    }

    public PlanoAcao criar(String alunoId, PlanoAcao plano) {
        if (alunoId == null || alunoId.isBlank()) {
            throw new IllegalArgumentException("O aluno vinculado é obrigatório.");
        }

        if (plano == null) {
            throw new IllegalArgumentException("Os dados do Plano de Ação são obrigatórios.");
        }

        alunoService.buscarPorId(alunoId)
                .orElseThrow(() -> new AlunoNaoEncontradoException(
                        "Aluno não encontrado para o ID: " + alunoId));

        // um Plano de Ação por aluno (campo "único" da US07)
        if (planoAcaoRepository.existsByAlunoId(alunoId)) {
            throw new PlanoAcaoDuplicadoException(
                    "Já existe um Plano de Ação cadastrado para este aluno.");
        }

        plano.setId(null);
        plano.setAlunoId(alunoId);

        return planoAcaoRepository.save(plano);
    }

    public PlanoAcao buscarPorId(String id) {
        return planoAcaoRepository.findById(id)
                .orElseThrow(() -> new PlanoAcaoNaoEncontradaException(
                        "Plano de Ação não encontrado para o ID: " + id));
    }

    public List<PlanoAcao> listarTodos() {
        return planoAcaoRepository.findAll();
    }

    public List<PlanoAcao> listarPorAluno(String alunoId) {
        return planoAcaoRepository.findByAlunoId(alunoId);
    }

    public PlanoAcao atualizar(String id, PlanoAcao dados) {
        PlanoAcao existente = buscarPorId(id);

        // o aluno vinculado nunca muda na edição
        dados.setId(existente.getId());
        dados.setAlunoId(existente.getAlunoId());

        return planoAcaoRepository.save(dados);
    }
}
