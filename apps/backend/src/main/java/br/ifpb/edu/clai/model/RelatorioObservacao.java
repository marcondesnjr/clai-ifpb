package br.ifpb.edu.clai.model;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

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
@Document(collection = "relatorios_observacao")
public class RelatorioObservacao {

    @Id
    private String id;

    @NotBlank(message = "O aluno vinculado é obrigatório")
    private String alunoId;

    @NotBlank(message = "O nome do assistente de aluno é obrigatório")
    @Size(max = 255, message = "O nome do assistente deve ter no máximo 255 caracteres")
    private String assistente;

    @NotNull(message = "A data da observação é obrigatória")
    @PastOrPresent(message = "A data da observação não pode ser futura")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataObservacao;

    @Size(max = 255, message = "O contexto deve ter no máximo 255 caracteres")
    private String contexto;

    @NotBlank(message = "O relato da observação é obrigatório")
    @Size(max = 5000, message = "O relato deve ter no máximo 5000 caracteres")
    private String relato;

    @Size(max = 5000, message = "Os comportamentos observados devem ter no máximo 5000 caracteres")
    private String comportamentosObservados;

    @Size(max = 5000, message = "As observações adicionais devem ter no máximo 5000 caracteres")
    private String observacoesAdicionais;
}
