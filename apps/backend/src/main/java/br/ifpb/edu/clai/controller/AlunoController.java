package br.ifpb.edu.clai.controller;

import br.ifpb.edu.clai.model.Aluno;
import br.ifpb.edu.clai.service.AlunoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import br.ifpb.edu.clai.exception.AlunoNaoEncontradoException;
import br.ifpb.edu.clai.exception.MatriculaDuplicadaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.Map;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/alunos")
public class AlunoController {

    private final AlunoService alunoService;

    public AlunoController(AlunoService alunoService) {
        this.alunoService = alunoService;
    }

    @GetMapping
    public String listarAlunos(Model model) {
        model.addAttribute("alunos", alunoService.listarTodos());
        model.addAttribute("aluno", new Aluno());
        return "alunos/alunos";
    }

    @GetMapping("/novo")
    public String exibirFormularioNovoAluno(Model model) {
        model.addAttribute("aluno", new Aluno());
        return "alunos/form";
    }

    @GetMapping("/{id}/editar")
    public String exibirFormularioEdicao(
            @PathVariable String id,
            Model model) {

        Aluno aluno = alunoService.buscarPorId(id)
                .orElseThrow(() -> new AlunoNaoEncontradoException(
                        "Aluno não encontrado para o ID: " + id));

        model.addAttribute("aluno", aluno);

        return "alunos/form";
    }


    @PostMapping("/cadastro-integrado")
    @ResponseBody
    public ResponseEntity<?> cadastrarAlunoIntegrado(
            @Valid @ModelAttribute("aluno") Aluno aluno,
            BindingResult resultado) {

        if (resultado.hasErrors()) {
            List<Map<String, String>> erros = resultado.getFieldErrors()
                    .stream()
                    .map(erro -> Map.of(
                            "campo", erro.getField(),
                            "mensagem", erro.getDefaultMessage() != null
                                    ? erro.getDefaultMessage()
                                    : "Valor inválido."
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.badRequest().body(Map.of(
                    "mensagem", "Verifique os campos informados.",
                    "erros", erros
            ));
        }

        try {
            Aluno salvo = alunoService.salvar(aluno);

            Map<String, Object> dados = new LinkedHashMap<>();
            dados.put("id", salvo.getId());
            dados.put("nome", salvo.getNome());
            dados.put("cpf", salvo.getCpf());
            dados.put("matricula", salvo.getMatricula().getNumeroMatricula());
            dados.put("curso", salvo.getMatricula().getCurso());
            dados.put("periodoIngresso", salvo.getMatricula().getPeriodoIngresso());
            dados.put("periodoLetivo", salvo.getMatricula().getPeriodoLetivo());

            return ResponseEntity.status(HttpStatus.CREATED).body(dados);

        } catch (MatriculaDuplicadaException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "mensagem", e.getMessage()
            ));
        }
    }

    @PostMapping("/salvar")
    public String salvarAluno(
            @Valid @ModelAttribute("aluno") Aluno aluno,
            BindingResult resultado) {

        aluno.setId(null);

        if (resultado.hasErrors()) {
            return "alunos/form";
        }

        alunoService.salvar(aluno);
        return "redirect:/alunos";
    }

    @PostMapping("/{id}/editar")
    public String atualizarAluno(
            @PathVariable String id,
            @Valid @ModelAttribute("aluno") Aluno aluno,
            BindingResult resultado) {

        aluno.setId(id);

        if (resultado.hasErrors()) {
            return "alunos/form";
        }

        alunoService.atualizar(id, aluno);
        return "redirect:/alunos";
    }

    @PostMapping("/{id}/excluir")
    public String excluirAluno(@PathVariable String id) {
        alunoService.remover(id);
        return "redirect:/alunos";
    }

    @GetMapping("/{id}")
    public String exibirDetalhesAluno(
            @PathVariable String id,
            Model model) {

        Aluno aluno = alunoService.buscarPorId(id)
                .orElseThrow(() -> new AlunoNaoEncontradoException(
                        "Aluno não encontrado para o ID: " + id));

        model.addAttribute("aluno", aluno);
        return "alunos/detalhes";
    }
}