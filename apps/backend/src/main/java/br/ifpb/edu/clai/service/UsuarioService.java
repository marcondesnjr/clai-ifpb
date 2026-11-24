package br.ifpb.edu.clai.service;

import br.ifpb.edu.clai.model.PerfilUsuario;
import br.ifpb.edu.clai.model.Usuario;
import br.ifpb.edu.clai.repository.UsuarioRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

import java.util.Locale;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario cadastrar(
            String nome,
            String login,
            String senha,
            PerfilUsuario perfil) {

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome é obrigatório.");
        }

        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("O login é obrigatório.");
        }

        if (senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("A senha é obrigatória.");
        }
        
        if (senha.length() < 8) {
             throw new IllegalArgumentException("A senha deve ter pelo menos 8 caracteres." );
        }

        if (perfil == null) {
            throw new IllegalArgumentException("O perfil é obrigatório.");
        }

        String loginNormalizado =
                login.trim().toLowerCase(Locale.ROOT);

        if (usuarioRepository.existsByLogin(loginNormalizado)) {
            throw new IllegalArgumentException("Este login já está cadastrado.");
        }

        Usuario usuario = new Usuario();

        usuario.setNome(nome.trim());
        usuario.setLogin(loginNormalizado);
        usuario.setSenhaHash(passwordEncoder.encode(senha));
        usuario.setPerfil(perfil);
        usuario.setAtivo(true);

        return usuarioRepository.save(usuario);
        }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> buscarPorId(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }

        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> buscarPorLogin(String login) {
        if (login == null || login.isBlank()) {
            return Optional.empty();
        }

        String loginNormalizado =
                login.trim().toLowerCase(Locale.ROOT);

        return usuarioRepository.findByLogin(loginNormalizado);
    }
}