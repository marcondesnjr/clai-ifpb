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
import br.ifpb.edu.clai.exception.PlanoAcaoDuplicadoException;
import br.ifpb.edu.clai.model.Aluno;
import br.ifpb.edu.clai.model.PlanoAcao;
import br.ifpb.edu.clai.service.AlunoService;
import br.ifpb.edu.clai.service.PlanoAcaoService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/planos-acao")
public class PlanoAcaoController {

    private static final String VIEW_LISTA = "planos-acao/lista-planos-acao";
    private static final String VIEW_DETALHES = "planos-acao/detalhes-plano-acao";
    private static final String VIEW_FORMULARIO = "planos-acao/formulario-plano-acao";

    private final PlanoAcaoService planoAcaoService;
    private final AlunoService alunoService;

    public PlanoAcaoController(PlanoAcaoService planoAcaoService, AlunoService alunoService) {
        this.planoAcaoService = planoAcaoService;
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

        model.addAttribute("planos", planoAcaoService.listarTodos());
        model.addAttribute("nomesAlunos", nomesAlunos);
        return VIEW_LISTA;
    }

    @GetMapping("/{id}")
    public String visualizar(@PathVariable String id, Model model) {
        PlanoAcao plano = planoAcaoService.buscarPorId(id);

        model.addAttribute("plano", plano);
        model.addAttribute("aluno", buscarAluno(plano.getAlunoId()));
        return VIEW_DETALHES;
    }

    @GetMapping("/novo")
    public String exibirFormularioNovo(
            @RequestParam(required = false) String alunoId,
            Model model) {

        PlanoAcao plano = new PlanoAcao();
        plano.setAlunoId(alunoId);

        model.addAttribute("plano", plano);
        model.addAttribute("alunos", alunoService.listarTodos());
        return VIEW_FORMULARIO;
    }

    @PostMapping("/salvar")
    public String salvar(
            @Valid @ModelAttribute("plano") PlanoAcao plano,
            BindingResult resultado,
            Model model) {

        if (!resultado.hasErrors()) {
            try {
                planoAcaoService.criar(plano.getAlunoId(), plano);
                return "redirect:/planos-acao";
            } catch (PlanoAcaoDuplicadoException e) {
                resultado.rejectValue("alunoId", "plano.duplicado", e.getMessage());
            }
        }

        model.addAttribute("alunos", alunoService.listarTodos());
        return VIEW_FORMULARIO;
    }

    @GetMapping("/{id}/editar")
    public String exibirFormularioEdicao(@PathVariable String id, Model model) {
        PlanoAcao plano = planoAcaoService.buscarPorId(id);

        model.addAttribute("plano", plano);
        model.addAttribute("aluno", buscarAluno(plano.getAlunoId()));
        return VIEW_FORMULARIO;
    }

    @PostMapping("/{id}/editar")
    public String atualizar(
            @PathVariable String id,
            @Valid @ModelAttribute("plano") PlanoAcao plano,
            BindingResult resultado,
            Model model) {

        plano.setId(id);

        if (resultado.hasErrors()) {
            // o aluno vinculado nunca muda na edição
            String alunoIdOriginal = planoAcaoService.buscarPorId(id).getAlunoId();

            plano.setAlunoId(alunoIdOriginal);
            model.addAttribute("aluno", buscarAluno(alunoIdOriginal));
            return VIEW_FORMULARIO;
        }

        planoAcaoService.atualizar(id, plano);
        return "redirect:/planos-acao";
    }

    private Aluno buscarAluno(String alunoId) {
        return alunoService.buscarPorId(alunoId)
                .orElseThrow(() -> new AlunoNaoEncontradoException(
                        "Aluno não encontrado para o ID: " + alunoId));
    }
}
