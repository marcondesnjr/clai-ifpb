package br.ifpb.edu.clai.security;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.ifpb.edu.clai.model.PlanoAcao;
import br.ifpb.edu.clai.model.RelatorioObservacao;
import br.ifpb.edu.clai.service.AlunoService;
import br.ifpb.edu.clai.service.PeiService;
import br.ifpb.edu.clai.service.PlanoAcaoService;
import br.ifpb.edu.clai.service.RelatorioObservacaoService;

// Garante que PEI, Plano de Ação e Relatório de Observação seguem a regra
// "apenas o perfil EDICAO escreve; CONSULTA só lê" (US25/US28).
@SpringBootTest(properties =
    "spring.mongodb.uri=mongodb://localhost:27017/clai_planejamento_acesso_teste")
@AutoConfigureMockMvc
class PlanejamentoAcessoIntegracaoTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AlunoService alunoService;

    @MockitoBean
    private PeiService peiService;

    @MockitoBean
    private PlanoAcaoService planoAcaoService;

    @MockitoBean
    private RelatorioObservacaoService relatorioService;

    private static final String[] MODULOS = {
        "/peis", "/planos-acao", "/relatorios-observacao"
    };

    @Test
    void consultaPodeListarOsTresModulos() throws Exception {
        for (String modulo : MODULOS) {
            mockMvc.perform(get(modulo)
                    .with(user("consultor").roles("CONSULTA")))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void consultaNaoPodeAbrirCadastroNemEdicao() throws Exception {
        for (String modulo : MODULOS) {
            mockMvc.perform(get(modulo + "/novo")
                    .with(user("consultor").roles("CONSULTA")))
                    .andExpect(status().isForbidden());

            mockMvc.perform(get(modulo + "/123/editar")
                    .with(user("consultor").roles("CONSULTA")))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void consultaNaoPodeSalvarNemAtualizar() throws Exception {
        for (String modulo : MODULOS) {
            mockMvc.perform(post(modulo + "/salvar")
                    .with(user("consultor").roles("CONSULTA"))
                    .with(csrf())
                    .param("alunoId", "a1"))
                    .andExpect(status().isForbidden());

            mockMvc.perform(post(modulo + "/123/editar")
                    .with(user("consultor").roles("CONSULTA"))
                    .with(csrf())
                    .param("alunoId", "a1"))
                    .andExpect(status().isForbidden());
        }

        verify(peiService, never()).criar(anyString(), any());
        verify(planoAcaoService, never()).criar(anyString(), any(PlanoAcao.class));
        verify(relatorioService, never()).criar(anyString(), any(RelatorioObservacao.class));
    }

    @Test
    void edicaoPodeAbrirFormularioDeCadastro() throws Exception {
        for (String modulo : MODULOS) {
            mockMvc.perform(get(modulo + "/novo")
                    .with(user("editor").roles("EDICAO")))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void consultaNaoDeveVerBotaoCadastrar() throws Exception {
        for (String modulo : MODULOS) {
            mockMvc.perform(get(modulo)
                    .with(user("consultor").roles("CONSULTA")))
                    .andExpect(status().isOk())
                    .andExpect(content().string(
                        not(containsString(modulo + "/novo"))));
        }
    }

    @Test
    void edicaoDeveVerBotaoCadastrar() throws Exception {
        for (String modulo : MODULOS) {
            mockMvc.perform(get(modulo)
                    .with(user("editor").roles("EDICAO")))
                    .andExpect(status().isOk())
                    .andExpect(content().string(
                        containsString(modulo + "/novo")));
        }
    }

    @Test
    void usuarioNaoAutenticadoVaiParaLogin() throws Exception {
        for (String modulo : MODULOS) {
            mockMvc.perform(get(modulo))
                    .andExpect(status().is3xxRedirection());
        }
    }
}
