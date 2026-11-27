package br.ifpb.edu.clai.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import br.ifpb.edu.clai.model.RelatorioObservacao;

public interface RelatorioObservacaoRepository extends MongoRepository<RelatorioObservacao, String> {

    List<RelatorioObservacao> findByAlunoId(String alunoId);
}
