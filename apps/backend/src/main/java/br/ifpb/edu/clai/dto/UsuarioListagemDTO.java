package br.ifpb.edu.clai.dto;

import br.ifpb.edu.clai.model.PerfilUsuario;
import br.ifpb.edu.clai.model.Usuario;

public record UsuarioListagemDTO(
        String id,
        String nome,
        String login,
        PerfilUsuario perfil,
        boolean ativo
) {

    public static UsuarioListagemDTO de(Usuario usuario) {
        return new UsuarioListagemDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getLogin(),
                usuario.getPerfil(),
                usuario.isAtivo()
        );
    }
}