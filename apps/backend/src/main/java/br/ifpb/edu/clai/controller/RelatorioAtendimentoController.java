package br.ifpb.edu.clai.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import br.ifpb.edu.clai.exception.AlunoNaoEncontradoException;
import br.ifpb.edu.clai.model.Aluno;
import br.ifpb.edu.clai.model.RelatorioAtendimento;
import br.ifpb.edu.clai.service.AlunoService;
import br.ifpb.edu.clai.service.RelatorioAtendimentoService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/relatorios-atendimento")
public class RelatorioAtendimentoController {

    private final RelatorioAtendimentoService relatorioService;
    private final AlunoService alunoService;

    public RelatorioAtendimentoController(
            RelatorioAtendimentoService relatorioService,
            AlunoService alunoService) {
        this.relatorioService = relatorioService;
        this.alunoService = alunoService;
    }

    @GetMapping
    public String listar(Model model) {
        Map<String, String> nomesAlunos = new HashMap<>();

        for (Aluno aluno : alunoService.listarTodos()) {
            nomesAlunos.put(aluno.getId(), aluno.getNome());
        }

        List<RelatorioAtendimento> relatorios =
                relatorioService.listarTodos();

        model.addAttribute("relatorios", relatorios);
        model.addAttribute("nomesAlunos", nomesAlunos);

        return "relatorios-atendimento/lista";
    }

    @GetMapping("/{id}")
    public String visualizar(@PathVariable String id, Model model) {
        RelatorioAtendimento relatorio = relatorioService.buscarPorId(id);

        model.addAttribute("relatorio", relatorio);
        model.addAttribute("aluno", buscarAluno(relatorio.getAlunoId()));

        return "relatorios-atendimento/detalhes";
    }

    @GetMapping("/novo")
    public String exibirFormularioNovo(Model model) {
        model.addAttribute("relatorio", new RelatorioAtendimento());
        model.addAttribute("alunos", alunoService.listarTodos());

        return "relatorios-atendimento/formulario";
    }

    @PostMapping("/salvar")
    public String salvar(
            @Valid @ModelAttribute("relatorio") RelatorioAtendimento relatorio,
            BindingResult resultado,
            Model model) {

        if (resultado.hasErrors()) {
            model.addAttribute("alunos", alunoService.listarTodos());
            return "relatorios-atendimento/formulario";
        }

        relatorioService.registrar(relatorio.getAlunoId(), relatorio);

        return "redirect:/relatorios-atendimento";
    }

    @GetMapping("/{id}/editar")
    public String exibirFormularioEdicao(
            @PathVariable String id,
            Model model) {

        RelatorioAtendimento relatorio = relatorioService.buscarPorId(id);

        model.addAttribute("relatorio", relatorio);
        model.addAttribute("aluno", buscarAluno(relatorio.getAlunoId()));

        return "relatorios-atendimento/formulario";
    }

    @PostMapping("/{id}/editar")
    public String atualizar(
            @PathVariable String id,
            @Valid @ModelAttribute("relatorio") RelatorioAtendimento relatorio,
            BindingResult resultado,
            Model model) {

        relatorio.setId(id);

        if (resultado.hasErrors()) {
            String alunoIdOriginal =
                    relatorioService.buscarPorId(id).getAlunoId();

            relatorio.setAlunoId(alunoIdOriginal);
            model.addAttribute("aluno", buscarAluno(alunoIdOriginal));

            return "relatorios-atendimento/formulario";
        }

        relatorioService.atualizar(id, relatorio);

        return "redirect:/relatorios-atendimento";
    }

    private Aluno buscarAluno(String alunoId) {
        return alunoService.buscarPorId(alunoId)
                .orElseThrow(() -> new AlunoNaoEncontradoException(
                        "Aluno não encontrado para o ID: " + alunoId));
    }
}
