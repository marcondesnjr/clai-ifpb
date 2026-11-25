package br.ifpb.edu.clai.dto;

import br.ifpb.edu.clai.model.PerfilUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioEdicaoForm {

    @NotBlank(message = "O nome é obrigatório.")
    private String nome;

    @NotBlank(message = "O login é obrigatório.")
    private String login;

    @NotNull(message = "O perfil é obrigatório.")
    private PerfilUsuario perfil;
}