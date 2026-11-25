package br.ifpb.edu.clai.controller;

import br.ifpb.edu.clai.dto.UsuarioCadastroForm;
import br.ifpb.edu.clai.model.PerfilUsuario;
import br.ifpb.edu.clai.service.UsuarioService;

import jakarta.validation.Valid;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
        model.addAttribute("usuarios", usuarioService.listarTodos());
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
}