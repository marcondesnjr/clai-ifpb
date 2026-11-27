package br.ifpb.edu.clai.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import br.ifpb.edu.clai.exception.AlunoNaoEncontradoException;
import br.ifpb.edu.clai.model.Aluno;
import br.ifpb.edu.clai.model.RelatorioObservacao;
import br.ifpb.edu.clai.service.AlunoService;
import br.ifpb.edu.clai.service.RelatorioObservacaoService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/relatorios-observacao")
public class RelatorioObservacaoController {

    private static final String VIEW_LISTA = "relatorios-observacao/lista-relatorios-observacao";
    private static final String VIEW_DETALHES = "relatorios-observacao/detalhes-relatorio-observacao";
    private static final String VIEW_FORMULARIO = "relatorios-observacao/formulario-relatorio-observacao";

    private final RelatorioObservacaoService relatorioService;
    private final AlunoService alunoService;

    public RelatorioObservacaoController(RelatorioObservacaoService relatorioService, AlunoService alunoService) {
        this.relatorioService = relatorioService;
        this.alunoService = alunoService;
    }

    // campos de texto em branco chegam como null (assim o "Não informado" aparece nas telas)
    @InitBinder
    public void configurarBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping
    public String listar(Model model) {
        Map<String, String> nomesAlunos = new HashMap<>();
        for (Aluno aluno : alunoService.listarTodos()) {
            nomesAlunos.put(aluno.getId(), aluno.getNome());
        }

        model.addAttribute("relatorios", relatorioService.listarTodos());
        model.addAttribute("nomesAlunos", nomesAlunos);
        return VIEW_LISTA;
    }

    @GetMapping("/{id}")
    public String visualizar(@PathVariable String id, Model model) {
        RelatorioObservacao relatorio = relatorioService.buscarPorId(id);

        model.addAttribute("relatorio", relatorio);
        model.addAttribute("aluno", buscarAluno(relatorio.getAlunoId()));
        return VIEW_DETALHES;
    }

    @GetMapping("/novo")
    public String exibirFormularioNovo(
            @RequestParam(required = false) String alunoId,
            Model model) {

        RelatorioObservacao relatorio = new RelatorioObservacao();
        relatorio.setAlunoId(alunoId);

        model.addAttribute("relatorio", relatorio);
        model.addAttribute("alunos", alunoService.listarTodos());
        return VIEW_FORMULARIO;
    }

    @PostMapping("/salvar")
    public String salvar(
            @Valid @ModelAttribute("relatorio") RelatorioObservacao relatorio,
            BindingResult resultado,
            Model model) {

        if (resultado.hasErrors()) {
            model.addAttribute("alunos", alunoService.listarTodos());
            return VIEW_FORMULARIO;
        }

        relatorioService.criar(relatorio.getAlunoId(), relatorio);
        return "redirect:/relatorios-observacao";
    }

    @GetMapping("/{id}/editar")
    public String exibirFormularioEdicao(@PathVariable String id, Model model) {
        RelatorioObservacao relatorio = relatorioService.buscarPorId(id);

        model.addAttribute("relatorio", relatorio);
        model.addAttribute("aluno", buscarAluno(relatorio.getAlunoId()));
        return VIEW_FORMULARIO;
    }

    @PostMapping("/{id}/editar")
    public String atualizar(
            @PathVariable String id,
            @Valid @ModelAttribute("relatorio") RelatorioObservacao relatorio,
            BindingResult resultado,
            Model model) {

        relatorio.setId(id);

        if (resultado.hasErrors()) {
            // o aluno vinculado nunca muda na edição
            String alunoIdOriginal = relatorioService.buscarPorId(id).getAlunoId();

            relatorio.setAlunoId(alunoIdOriginal);
            model.addAttribute("aluno", buscarAluno(alunoIdOriginal));
            return VIEW_FORMULARIO;
        }

        relatorioService.atualizar(id, relatorio);
        return "redirect:/relatorios-observacao";
    }

    private Aluno buscarAluno(String alunoId) {
        return alunoService.buscarPorId(alunoId)
                .orElseThrow(() -> new AlunoNaoEncontradoException(
                        "Aluno não encontrado para o ID: " + alunoId));
    }
}
