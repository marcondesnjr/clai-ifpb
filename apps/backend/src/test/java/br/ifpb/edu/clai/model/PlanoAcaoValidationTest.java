package br.ifpb.edu.clai.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class PlanoAcaoValidationTest {

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

    private PlanoAcao planoValido() {
        PlanoAcao plano = new PlanoAcao();
        plano.setAlunoId("a1");
        plano.setNecessidadesEspecificas("Necessita de apoio na leitura");
        plano.setDificuldadesPercurso("Dificuldade de concentração");
        plano.setDemandasDoCaso("Acompanhamento psicopedagógico");
        plano.setPropostasIntervencao("Atendimentos semanais");
        return plano;
    }

    private boolean temErro(Set<ConstraintViolation<PlanoAcao>> erros, String mensagem) {
        return erros.stream().anyMatch(e -> e.getMessage().equals(mensagem));
    }

    @Test
    void planoValidoNaoDeveTerErros() {
        assertTrue(validador.validate(planoValido()).isEmpty());
    }

    @Test
    void deveExigirAlunoVinculado() {
        PlanoAcao plano = planoValido();
        plano.setAlunoId(null);

        assertTrue(temErro(validador.validate(plano), "O aluno vinculado é obrigatório"));
    }

    @Test
    void deveExigirNecessidadesEspecificas() {
        PlanoAcao plano = planoValido();
        plano.setNecessidadesEspecificas(" ");

        assertTrue(temErro(validador.validate(plano), "As necessidades específicas são obrigatórias"));
    }

    @Test
    void deveExigirDificuldadesNoPercurso() {
        PlanoAcao plano = planoValido();
        plano.setDificuldadesPercurso("");

        assertTrue(temErro(validador.validate(plano),
                "As dificuldades no percurso educacional são obrigatórias"));
    }

    @Test
    void deveExigirDemandasDoCaso() {
        PlanoAcao plano = planoValido();
        plano.setDemandasDoCaso(null);

        assertTrue(temErro(validador.validate(plano), "As demandas do caso são obrigatórias"));
    }

    @Test
    void deveExigirPropostasDeIntervencao() {
        PlanoAcao plano = planoValido();
        plano.setPropostasIntervencao(null);

        assertTrue(temErro(validador.validate(plano),
                "As propostas iniciais de intervenção são obrigatórias"));
    }

    @Test
    void deveLimitarTamanhoDosTextos() {
        PlanoAcao plano = planoValido();
        plano.setDemandasDoCaso("x".repeat(5001));

        Set<ConstraintViolation<PlanoAcao>> erros = validador.validate(plano);

        assertEquals(1, erros.size());
        assertTrue(temErro(erros, "As demandas do caso devem ter no máximo 5000 caracteres"));
    }
}
