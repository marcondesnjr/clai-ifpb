package br.ifpb.edu.clai.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "planos_acao")
public class PlanoAcao {

    @Id
    private String id;

    @NotBlank(message = "O aluno vinculado é obrigatório")
    private String alunoId;

    @NotBlank(message = "As necessidades específicas são obrigatórias")
    @Size(max = 5000, message = "As necessidades específicas devem ter no máximo 5000 caracteres")
    private String necessidadesEspecificas;

    @NotBlank(message = "As dificuldades no percurso educacional são obrigatórias")
    @Size(max = 5000, message = "As dificuldades devem ter no máximo 5000 caracteres")
    private String dificuldadesPercurso;

    @NotBlank(message = "As demandas do caso são obrigatórias")
    @Size(max = 5000, message = "As demandas do caso devem ter no máximo 5000 caracteres")
    private String demandasDoCaso;

    @NotBlank(message = "As propostas iniciais de intervenção são obrigatórias")
    @Size(max = 5000, message = "As propostas de intervenção devem ter no máximo 5000 caracteres")
    private String propostasIntervencao;
}
