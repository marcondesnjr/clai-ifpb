package br.ifpb.edu.clai.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.ifpb.edu.clai.exception.AlunoNaoEncontradoException;
import br.ifpb.edu.clai.exception.RelatorioObservacaoNaoEncontradoException;
import br.ifpb.edu.clai.model.RelatorioObservacao;
import br.ifpb.edu.clai.repository.RelatorioObservacaoRepository;

@Service
public class RelatorioObservacaoService {

    private final RelatorioObservacaoRepository relatorioRepository;
    private final AlunoService alunoService;

    public RelatorioObservacaoService(RelatorioObservacaoRepository relatorioRepository,
                                      AlunoService alunoService) {
        this.relatorioRepository = relatorioRepository;
        this.alunoService = alunoService;
    }

    public RelatorioObservacao criar(String alunoId, RelatorioObservacao relatorio) {
        if (alunoId == null || alunoId.isBlank()) {
            throw new IllegalArgumentException("O aluno vinculado é obrigatório.");
        }

        if (relatorio == null) {
            throw new IllegalArgumentException("Os dados do Relatório de Observação são obrigatórios.");
        }

        alunoService.buscarPorId(alunoId)
                .orElseThrow(() -> new AlunoNaoEncontradoException(
                        "Aluno não encontrado para o ID: " + alunoId));

        relatorio.setId(null);
        relatorio.setAlunoId(alunoId);

        return relatorioRepository.save(relatorio);
    }

    public RelatorioObservacao buscarPorId(String id) {
        return relatorioRepository.findById(id)
                .orElseThrow(() -> new RelatorioObservacaoNaoEncontradoException(
                        "Relatório de Observação não encontrado para o ID: " + id));
    }

    public List<RelatorioObservacao> listarTodos() {
        return relatorioRepository.findAll();
    }

    public List<RelatorioObservacao> listarPorAluno(String alunoId) {
        return relatorioRepository.findByAlunoId(alunoId);
    }

    public RelatorioObservacao atualizar(String id, RelatorioObservacao dados) {
        RelatorioObservacao existente = buscarPorId(id);

        // o aluno vinculado nunca muda na edição
        dados.setId(existente.getId());
        dados.setAlunoId(existente.getAlunoId());

        return relatorioRepository.save(dados);
    }
}
