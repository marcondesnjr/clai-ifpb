package br.ifpb.edu.clai.model;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "peis")
public class Pei {

    @Id
    private String id;

    @NotBlank(message = "O aluno vinculado é obrigatório")
    private String alunoId;

    private DadosAcademicos dadosAcademicos;

    private TrajetoriaEscolar trajetoriaEscolar;

    private String necessidades;

    private String conhecimentos;

    private String capacidades;

    private String interesses;

    private String dificuldades;

    private List<AdaptacaoCurricular> adaptacoesCurriculares;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class DadosAcademicos {

        private String curso;

        private String periodoLetivo;

        private String turma;

        private String profissionalResponsavel;

        private LocalDate dataElaboracao;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class TrajetoriaEscolar {

        private String escolaridadeAnterior;

        private String historicoEscolar;

        private String experienciasRelevantes;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class AdaptacaoCurricular {

        private String componenteCurricular;

        private String adaptacao;

        private String justificativa;
    }
}
