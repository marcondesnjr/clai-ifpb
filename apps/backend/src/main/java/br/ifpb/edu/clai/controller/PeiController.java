package br.ifpb.edu.clai.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import br.ifpb.edu.clai.exception.AlunoNaoEncontradoException;
import br.ifpb.edu.clai.model.Aluno;
import br.ifpb.edu.clai.model.Pei;
import br.ifpb.edu.clai.service.AlunoService;
import br.ifpb.edu.clai.service.PeiService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/peis")
public class PeiController {

    private static final String VIEW_LISTA = "peis/lista-peis";
    private static final String VIEW_DETALHES = "peis/detalhes-pei";
    private static final String VIEW_FORMULARIO = "peis/formulario-pei";

    private final PeiService peiService;
    private final AlunoService alunoService;

    public PeiController(PeiService peiService, AlunoService alunoService) {
        this.peiService = peiService;
        this.alunoService = alunoService;
    }

    // remove espaços das pontas, mas mantém "" (o PeiService só atualiza campos != null,
    // então enviar "" é o que permite apagar um campo na edição)
    @InitBinder
    public void configurarBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(false));
    }

    @GetMapping
    public String listar(Model model) {
        Map<String, String> nomesAlunos = new HashMap<>();
        for (Aluno aluno : alunoService.listarTodos()) {
            nomesAlunos.put(aluno.getId(), aluno.getNome());
        }

        model.addAttribute("peis", peiService.listarTodos());
        model.addAttribute("nomesAlunos", nomesAlunos);
        return VIEW_LISTA;
    }

    @GetMapping("/{id}")
    public String visualizar(@PathVariable String id, Model model) {
        Pei pei = peiService.buscarPorId(id);

        model.addAttribute("pei", pei);
        model.addAttribute("aluno", buscarAluno(pei.getAlunoId()));
        return VIEW_DETALHES;
    }

    @GetMapping("/novo")
    public String exibirFormularioNovo(
            @RequestParam(required = false) String alunoId,
            Model model) {

        Pei pei = new Pei();
        pei.setAlunoId(alunoId);
        inicializarAninhados(pei);

        model.addAttribute("pei", pei);
        model.addAttribute("alunos", alunoService.listarTodos());
        return VIEW_FORMULARIO;
    }

    @PostMapping("/salvar")
    public String salvar(
            @Valid @ModelAttribute("pei") Pei pei,
            BindingResult resultado,
            Model model) {

        if (resultado.hasErrors()) {
            inicializarAninhados(pei);
            model.addAttribute("alunos", alunoService.listarTodos());
            return VIEW_FORMULARIO;
        }

        peiService.criar(pei.getAlunoId(), pei);
        return "redirect:/peis";
    }

    @GetMapping("/{id}/editar")
    public String exibirFormularioEdicao(@PathVariable String id, Model model) {
        Pei pei = peiService.buscarPorId(id);
        inicializarAninhados(pei);

        model.addAttribute("pei", pei);
        model.addAttribute("aluno", buscarAluno(pei.getAlunoId()));
        return VIEW_FORMULARIO;
    }

    @PostMapping("/{id}/editar")
    public String atualizar(
            @PathVariable String id,
            @Valid @ModelAttribute("pei") Pei pei,
            BindingResult resultado,
            Model model) {

        pei.setId(id);

        if (resultado.hasErrors()) {
            // o aluno vinculado nunca muda na edição
            String alunoIdOriginal = peiService.buscarPorId(id).getAlunoId();

            pei.setAlunoId(alunoIdOriginal);
            inicializarAninhados(pei);

            model.addAttribute("aluno", buscarAluno(alunoIdOriginal));
            return VIEW_FORMULARIO;
        }

        peiService.atualizar(id, pei);
        return "redirect:/peis";
    }

    private Aluno buscarAluno(String alunoId) {
        return alunoService.buscarPorId(alunoId)
                .orElseThrow(() -> new AlunoNaoEncontradoException(
                        "Aluno não encontrado para o ID: " + alunoId));
    }

    // Os objetos aninhados vêm nulos; sem isso o Thymeleaf quebra em *{dadosAcademicos.curso}
    private void inicializarAninhados(Pei pei) {
        if (pei.getDadosAcademicos() == null)
            pei.setDadosAcademicos(new Pei.DadosAcademicos());
        if (pei.getTrajetoriaEscolar() == null)
            pei.setTrajetoriaEscolar(new Pei.TrajetoriaEscolar());
        if (pei.getAdaptacoesCurriculares() == null)
            pei.setAdaptacoesCurriculares(new ArrayList<>());
    }
}
