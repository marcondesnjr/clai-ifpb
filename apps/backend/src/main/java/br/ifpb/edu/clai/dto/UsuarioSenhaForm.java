package br.ifpb.edu.clai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioSenhaForm {

    @NotBlank(message = "A nova senha é obrigatória.")
    @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres.")
    private String novaSenha;
}