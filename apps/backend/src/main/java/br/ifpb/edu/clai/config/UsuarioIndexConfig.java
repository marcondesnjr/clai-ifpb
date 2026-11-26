package br.ifpb.edu.clai.config;

import br.ifpb.edu.clai.model.Usuario;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

@Configuration
public class UsuarioIndexConfig {

    @Bean
    public ApplicationRunner garantirIndiceLogin(
            MongoTemplate mongoTemplate) {

        return args -> mongoTemplate
                .indexOps(Usuario.class)
                .ensureIndex(
                        new Index()
                                .on("login", Sort.Direction.ASC)
                                .unique()
                );
    }
}