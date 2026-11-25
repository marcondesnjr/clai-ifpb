package br.ifpb.edu.clai.repository;

import br.ifpb.edu.clai.model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UsuarioRepository extends MongoRepository<Usuario, String> {

    Optional<Usuario> findByLogin(String login);

    boolean existsByLogin(String login);
}