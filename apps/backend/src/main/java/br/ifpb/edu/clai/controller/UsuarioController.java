package br.ifpb.edu.clai.controller;

import br.ifpb.edu.clai.dto.UsuarioCadastroForm;
import br.ifpb.edu.clai.dto.UsuarioEdicaoForm;
import br.ifpb.edu.clai.dto.UsuarioSenhaForm;
import br.ifpb.edu.clai.model.PerfilUsuario;
import br.ifpb.edu.clai.model.Usuario;
import br.ifpb.edu.clai.service.UsuarioService;
import br.ifpb.edu.clai.dto.UsuarioListagemDTO;

import jakarta.validation.Valid;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/usuarios")
@ConditionalOnProperty(
        name = "clai.usuarios.admin.enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listarUsuarios(Model model) {

        model.addAttribute(
                "usuarios",
                usuarioService.listarTodos()
                        .stream()
                        .map(UsuarioListagemDTO::de)
                        .toList()
        );

        return "usuarios/lista";
    }

    @GetMapping("/novo")
    public String exibirFormulario(Model model) {
        model.addAttribute("form", new UsuarioCadastroForm());
        model.addAttribute("perfis", PerfilUsuario.values());

        return "usuarios/form";
    }

    @PostMapping("/salvar")
    public String salvarUsuario(
            @Valid @ModelAttribute("form") UsuarioCadastroForm form,
            BindingResult resultado,
            Model model) {

        if (!resultado.hasErrors()) {
            try {
                usuarioService.cadastrar(
                        form.getNome(),
                        form.getLogin(),
                        form.getSenha(),
                        form.getPerfil()
                );

                return "redirect:/usuarios";

            } catch (IllegalArgumentException e) {
                resultado.reject(
                        "cadastro.invalido",
                        e.getMessage()
                );
            }
        }

        model.addAttribute("perfis", PerfilUsuario.values());
        return "usuarios/form";
    }

    @GetMapping("/{id}/editar")
    public String exibirEdicao(
            @PathVariable String id,
            Model model) {

        Usuario usuario = usuarioService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário não encontrado."
                ));

        UsuarioEdicaoForm form = new UsuarioEdicaoForm();

        form.setNome(usuario.getNome());
        form.setLogin(usuario.getLogin());
        form.setPerfil(usuario.getPerfil());

        model.addAttribute("form", form);
        model.addAttribute("usuarioId", id);
        model.addAttribute("perfis", PerfilUsuario.values());

        return "usuarios/editar";
    }

    @PostMapping("/{id}/editar")
    public String atualizarUsuario(
            @PathVariable String id,
            @Valid @ModelAttribute("form") UsuarioEdicaoForm form,
            BindingResult resultado,
            Model model) {

        if (!resultado.hasErrors()) {
            try {
                usuarioService.atualizar(
                        id,
                        form.getNome(),
                        form.getLogin(),
                        form.getPerfil()
                );

                return "redirect:/usuarios";

            } catch (IllegalArgumentException e) {
                resultado.reject(
                        "edicao.invalida",
                        e.getMessage()
                );
            }
        }

        model.addAttribute("usuarioId", id);
        model.addAttribute("perfis", PerfilUsuario.values());

        return "usuarios/editar";
    }

    @PostMapping("/{id}/desativar")
    public String desativarUsuario(@PathVariable String id) {
        usuarioService.desativar(id);
        return "redirect:/usuarios";
    }

    @PostMapping("/{id}/reativar")
    public String reativarUsuario(@PathVariable String id) {
        usuarioService.reativar(id);
        return "redirect:/usuarios";
    }

    @GetMapping("/{id}/senha")
    public String exibirRedefinicaoSenha(
            @PathVariable String id,
            Model model) {

        if (usuarioService.buscarPorId(id).isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Usuário não encontrado."
            );
        }

        model.addAttribute("usuarioId", id);
        model.addAttribute("form", new UsuarioSenhaForm());

        return "usuarios/senha";
    }

    @PostMapping("/{id}/senha")
    public String redefinirSenha(
            @PathVariable String id,
            @Valid @ModelAttribute("form") UsuarioSenhaForm form,
            BindingResult resultado,
            Model model) {

        if (!resultado.hasErrors()) {
            try {
                usuarioService.alterarSenha(
                        id,
                        form.getNovaSenha()
                );

                return "redirect:/usuarios";

            } catch (IllegalArgumentException e) {
                resultado.reject(
                        "senha.invalida",
                        e.getMessage()
                );
            }
        }

        model.addAttribute("usuarioId", id);
        return "usuarios/senha";
    }
}