package br.ifpb.edu.clai.dto;

import br.ifpb.edu.clai.model.PerfilUsuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioCadastroForm {

    @NotBlank(message = "O nome é obrigatório.")
    private String nome;

    @NotBlank(message = "O login é obrigatório.")
    private String login;

    @NotBlank(message = "A senha é obrigatória.")
    @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres.")
    private String senha;

    @NotNull(message = "O perfil é obrigatório.")
    private PerfilUsuario perfil;
}