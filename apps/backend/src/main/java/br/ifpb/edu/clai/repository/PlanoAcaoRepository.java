package br.ifpb.edu.clai.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import br.ifpb.edu.clai.model.PlanoAcao;

public interface PlanoAcaoRepository extends MongoRepository<PlanoAcao, String> {

    List<PlanoAcao> findByAlunoId(String alunoId);

    boolean existsByAlunoId(String alunoId);
}
