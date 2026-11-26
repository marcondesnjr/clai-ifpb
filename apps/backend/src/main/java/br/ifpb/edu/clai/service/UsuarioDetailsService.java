package br.ifpb.edu.clai.service;

import br.ifpb.edu.clai.model.Usuario;
import br.ifpb.edu.clai.repository.UsuarioRepository;

import java.util.Locale;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(
            UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String login)
            throws UsernameNotFoundException {

        if (login == null || login.isBlank()) {
            throw new UsernameNotFoundException(
                    "Usuário ou senha inválidos.");
        }

        String loginNormalizado =
                login.trim().toLowerCase(Locale.ROOT);

        Usuario usuario = usuarioRepository
                .findByLogin(loginNormalizado)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuário ou senha inválidos."));

        return User.withUsername(usuario.getLogin())
                .password(usuario.getSenhaHash())
                .roles(usuario.getPerfil().name())
                .disabled(!usuario.isAtivo())
                .build();
    }
}