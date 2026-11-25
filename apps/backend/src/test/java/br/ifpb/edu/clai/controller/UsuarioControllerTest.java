package br.ifpb.edu.clai.controller;

import br.ifpb.edu.clai.dto.UsuarioCadastroForm;
import br.ifpb.edu.clai.model.PerfilUsuario;
import br.ifpb.edu.clai.model.Usuario;
import br.ifpb.edu.clai.service.UsuarioService;
import br.ifpb.edu.clai.dto.UsuarioListagemDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UsuarioControllerTest {

    private UsuarioService usuarioService;
    private UsuarioController controller;

    @BeforeEach
    void preparar() {
        usuarioService = mock(UsuarioService.class);
        controller = new UsuarioController(usuarioService);
    }

    @Test
    void deveListarUsuarios() {
        Usuario usuario = new Usuario();
        usuario.setNome("Ana");
        usuario.setLogin("ana");

        when(usuarioService.listarTodos())
                .thenReturn(List.of(usuario));

        Model model = new ExtendedModelMap();

        String pagina = controller.listarUsuarios(model);

        assertEquals("usuarios/lista", pagina);
        
        List<?> usuarios =
                (List<?>) model.asMap().get("usuarios");

        assertEquals(1, usuarios.size());

        UsuarioListagemDTO dto = assertInstanceOf(
                UsuarioListagemDTO.class,
                usuarios.get(0)
        );

        assertEquals("Ana", dto.nome());
        assertEquals("ana", dto.login());

        verify(usuarioService).listarTodos();
    }

    @Test
    void deveExibirFormularioComPerfis() {
        Model model = new ExtendedModelMap();

        String pagina = controller.exibirFormulario(model);

        assertEquals("usuarios/form", pagina);

        assertInstanceOf(
                UsuarioCadastroForm.class,
                model.asMap().get("form")
        );

        assertArrayEquals(
                PerfilUsuario.values(),
                (PerfilUsuario[]) model.asMap().get("perfis")
        );
    }

    @Test
    void deveCadastrarUsuarioValido() {
        UsuarioCadastroForm form = criarFormulario();

        BindingResult resultado =
                new BeanPropertyBindingResult(form, "form");

        Model model = new ExtendedModelMap();

        String pagina = controller.salvarUsuario(
                form,
                resultado,
                model
        );

        assertEquals("redirect:/usuarios", pagina);

        verify(usuarioService).cadastrar(
                "Ana",
                "ana",
                "senhaSegura123",
                PerfilUsuario.EDICAO
        );
    }

    @Test
    void deveRetornarAoFormularioQuandoHouverErro() {
        UsuarioCadastroForm form = criarFormulario();

        BindingResult resultado =
                new BeanPropertyBindingResult(form, "form");

        resultado.rejectValue(
                "nome",
                "nome.invalido",
                "O nome é obrigatório."
        );

        Model model = new ExtendedModelMap();

        String pagina = controller.salvarUsuario(
                form,
                resultado,
                model
        );

        assertEquals("usuarios/form", pagina);
        assertTrue(resultado.hasErrors());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void deveExibirErroQuandoLoginJaExiste() {
        UsuarioCadastroForm form = criarFormulario();

        BindingResult resultado =
                new BeanPropertyBindingResult(form, "form");

        Model model = new ExtendedModelMap();

        when(usuarioService.cadastrar(
                "Ana",
                "ana",
                "senhaSegura123",
                PerfilUsuario.EDICAO
        )).thenThrow(
                new IllegalArgumentException(
                        "Este login já está cadastrado."
                )
        );

        String pagina = controller.salvarUsuario(
                form,
                resultado,
                model
        );

        assertEquals("usuarios/form", pagina);
        assertTrue(resultado.hasGlobalErrors());

        assertEquals(
                "Este login já está cadastrado.",
                resultado.getGlobalError().getDefaultMessage()
        );
    }

    private UsuarioCadastroForm criarFormulario() {
        UsuarioCadastroForm form = new UsuarioCadastroForm();

        form.setNome("Ana");
        form.setLogin("ana");
        form.setSenha("senhaSegura123");
        form.setPerfil(PerfilUsuario.EDICAO);

        return form;
    }
}