package br.ifpb.edu.clai.model;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class RelatorioObservacaoValidationTest {

    private static ValidatorFactory fabrica;
    private static Validator validador;

    @BeforeAll
    static void iniciar() {
        fabrica = Validation.buildDefaultValidatorFactory();
        validador = fabrica.getValidator();
    }

    @AfterAll
    static void encerrar() {
        fabrica.close();
    }

    private RelatorioObservacao relatorioValido() {
        RelatorioObservacao relatorio = new RelatorioObservacao();
        relatorio.setAlunoId("a1");
        relatorio.setAssistente("Carlos Lima");
        relatorio.setDataObservacao(LocalDate.now().minusDays(1));
        relatorio.setRelato("O aluno participou da aula normalmente.");
        return relatorio;
    }

    private boolean temErro(Set<ConstraintViolation<RelatorioObservacao>> erros, String mensagem) {
        return erros.stream().anyMatch(e -> e.getMessage().equals(mensagem));
    }

    @Test
    void relatorioValidoNaoDeveTerErros() {
        assertTrue(validador.validate(relatorioValido()).isEmpty());
    }

    @Test
    void deveExigirAlunoVinculado() {
        RelatorioObservacao relatorio = relatorioValido();
        relatorio.setAlunoId("");

        assertTrue(temErro(validador.validate(relatorio), "O aluno vinculado é obrigatório"));
    }

    @Test
    void deveExigirAssistente() {
        RelatorioObservacao relatorio = relatorioValido();
        relatorio.setAssistente(" ");

        assertTrue(temErro(validador.validate(relatorio),
                "O nome do assistente de aluno é obrigatório"));
    }

    @Test
    void deveExigirDataDaObservacao() {
        RelatorioObservacao relatorio = relatorioValido();
        relatorio.setDataObservacao(null);

        assertTrue(temErro(validador.validate(relatorio), "A data da observação é obrigatória"));
    }

    @Test
    void naoDevePermitirDataFutura() {
        RelatorioObservacao relatorio = relatorioValido();
        relatorio.setDataObservacao(LocalDate.now().plusDays(1));

        assertTrue(temErro(validador.validate(relatorio),
                "A data da observação não pode ser futura"));
    }

    @Test
    void deveExigirRelato() {
        RelatorioObservacao relatorio = relatorioValido();
        relatorio.setRelato(null);

        assertTrue(temErro(validador.validate(relatorio), "O relato da observação é obrigatório"));
    }

    @Test
    void deveLimitarTamanhoDoRelato() {
        RelatorioObservacao relatorio = relatorioValido();
        relatorio.setRelato("x".repeat(5001));

        assertTrue(temErro(validador.validate(relatorio),
                "O relato deve ter no máximo 5000 caracteres"));
    }
}
