package br.ifpb.edu.clai.model;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

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

    @Valid
    @NotNull(message = "Os dados acadêmicos são obrigatórios")
    private DadosAcademicos dadosAcademicos;

    @Valid
    private TrajetoriaEscolar trajetoriaEscolar;

    @Size(max = 5000, message = "As necessidades devem ter no máximo 5000 caracteres")
    private String necessidades;

    @Size(max = 5000, message = "Os conhecimentos devem ter no máximo 5000 caracteres")
    private String conhecimentos;

    @Size(max = 5000, message = "As capacidades devem ter no máximo 5000 caracteres")
    private String capacidades;

    @Size(max = 5000, message = "Os interesses devem ter no máximo 5000 caracteres")
    private String interesses;

    @Size(max = 5000, message = "As dificuldades devem ter no máximo 5000 caracteres")
    private String dificuldades;

    @Valid
    private List<AdaptacaoCurricular> adaptacoesCurriculares;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class DadosAcademicos {

        @Size(max = 255, message = "O curso deve ter no máximo 255 caracteres")
        private String curso;

        @Size(max = 50, message = "O período letivo deve ter no máximo 50 caracteres")
        private String periodoLetivo;

        @Size(max = 100, message = "A turma deve ter no máximo 100 caracteres")
        private String turma;

        @NotBlank(message = "O profissional responsável é obrigatório")
        @Size(max = 255, message = "O profissional responsável deve ter no máximo 255 caracteres")
        private String profissionalResponsavel;

        @NotNull(message = "A data de elaboração é obrigatória")
        @PastOrPresent(message = "A data de elaboração não pode ser futura")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate dataElaboracao;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class TrajetoriaEscolar {

        @Size(max = 5000, message = "A escolaridade anterior deve ter no máximo 5000 caracteres")
        private String escolaridadeAnterior;

        @Size(max = 5000, message = "O histórico escolar deve ter no máximo 5000 caracteres")
        private String historicoEscolar;

        @Size(max = 5000, message = "As experiências relevantes devem ter no máximo 5000 caracteres")
        private String experienciasRelevantes;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class AdaptacaoCurricular {

        @NotBlank(message = "O componente curricular da adaptação é obrigatório")
        @Size(max = 255, message = "O componente curricular deve ter no máximo 255 caracteres")
        private String componenteCurricular;

        @NotBlank(message = "A descrição da adaptação é obrigatória")
        @Size(max = 5000, message = "A adaptação deve ter no máximo 5000 caracteres")
        private String adaptacao;

        @Size(max = 5000, message = "A justificativa deve ter no máximo 5000 caracteres")
        private String justificativa;
    }
}
