package br.ifpb.edu.clai.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import br.ifpb.edu.clai.model.Pei;

public interface PeiRepository extends MongoRepository<Pei, String> {

    List<Pei> findByAlunoId(String alunoId);
}
