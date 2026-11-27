package br.ifpb.edu.clai.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class PeiValidationTest {

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

    private Pei peiValido() {
        Pei.DadosAcademicos dados = new Pei.DadosAcademicos();
        dados.setProfissionalResponsavel("Maria Souza");
        dados.setDataElaboracao(LocalDate.now().minusDays(1));

        Pei pei = new Pei();
        pei.setAlunoId("a1");
        pei.setDadosAcademicos(dados);
        return pei;
    }

    private boolean temErro(Set<ConstraintViolation<Pei>> erros, String mensagem) {
        return erros.stream().anyMatch(e -> e.getMessage().equals(mensagem));
    }

    @Test
    void peiValidoNaoDeveTerErros() {
        assertTrue(validador.validate(peiValido()).isEmpty());
    }

    @Test
    void deveExigirAlunoVinculado() {
        Pei pei = peiValido();
        pei.setAlunoId(" ");

        assertTrue(temErro(validador.validate(pei), "O aluno vinculado é obrigatório"));
    }

    @Test
    void deveExigirDadosAcademicos() {
        Pei pei = peiValido();
        pei.setDadosAcademicos(null);

        assertTrue(temErro(validador.validate(pei), "Os dados acadêmicos são obrigatórios"));
    }

    @Test
    void deveExigirProfissionalResponsavel() {
        Pei pei = peiValido();
        pei.getDadosAcademicos().setProfissionalResponsavel("");

        assertTrue(temErro(validador.validate(pei), "O profissional responsável é obrigatório"));
    }

    @Test
    void deveExigirDataDeElaboracao() {
        Pei pei = peiValido();
        pei.getDadosAcademicos().setDataElaboracao(null);

        assertTrue(temErro(validador.validate(pei), "A data de elaboração é obrigatória"));
    }

    @Test
    void naoDevePermitirDataDeElaboracaoFutura() {
        Pei pei = peiValido();
        pei.getDadosAcademicos().setDataElaboracao(LocalDate.now().plusDays(1));

        assertTrue(temErro(validador.validate(pei), "A data de elaboração não pode ser futura"));
    }

    @Test
    void adaptacaoDeveExigirComponenteEDescricao() {
        Pei pei = peiValido();
        pei.setAdaptacoesCurriculares(List.of(new Pei.AdaptacaoCurricular()));

        Set<ConstraintViolation<Pei>> erros = validador.validate(pei);

        assertEquals(2, erros.size());
        assertTrue(temErro(erros, "O componente curricular da adaptação é obrigatório"));
        assertTrue(temErro(erros, "A descrição da adaptação é obrigatória"));
    }

    @Test
    void deveLimitarTamanhoDosTextos() {
        Pei pei = peiValido();
        pei.setNecessidades("x".repeat(5001));

        assertTrue(temErro(validador.validate(pei),
                "As necessidades devem ter no máximo 5000 caracteres"));
    }
}
