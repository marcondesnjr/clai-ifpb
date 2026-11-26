package br.ifpb.edu.clai.service;

import br.ifpb.edu.clai.model.PerfilUsuario;
import br.ifpb.edu.clai.model.Usuario;
import br.ifpb.edu.clai.repository.UsuarioRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.dao.DuplicateKeyException;
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

        try {
            return usuarioRepository.save(usuario);
        } 
        catch (DuplicateKeyException e) {
            throw new IllegalArgumentException("Este login já está cadastrado.", e);
        }
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

    public Usuario atualizar(
            String id,
            String nome,
            String login,
            PerfilUsuario perfil) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("O identificador do usuário é obrigatório.");
        }

        if (nome == null || nome.isBlank()
                || login == null || login.isBlank()
                || perfil == null) {
            throw new IllegalArgumentException("Nome, login e perfil são obrigatórios.");
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        String novoLogin = login.trim().toLowerCase(Locale.ROOT);

        if (!novoLogin.equals(usuario.getLogin())
                && usuarioRepository.existsByLogin(novoLogin)) {
            throw new IllegalArgumentException("Este login já está cadastrado.");
        }

        usuario.setNome(nome.trim());
        usuario.setLogin(novoLogin);
        usuario.setPerfil(perfil);

        try {
            return usuarioRepository.save(usuario);
        } catch (DuplicateKeyException e) {
            throw new IllegalArgumentException("Este login já está cadastrado.", e);
        }
    }

    public Usuario desativar(String id) {
        return alterarStatus(id, false);
    }

    public Usuario reativar(String id) {
        return alterarStatus(id, true);
    }

    private Usuario alterarStatus(String id, boolean ativo) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "O identificador do usuário é obrigatório.");
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuário não encontrado."));

        if (usuario.isAtivo() == ativo) {
            return usuario;
        }

        usuario.setAtivo(ativo);

        return usuarioRepository.save(usuario);
    }

    public Usuario alterarSenha(String id, String novaSenha) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "O identificador do usuário é obrigatório.");
        }

        if (novaSenha == null || novaSenha.isBlank()
                || novaSenha.length() < 8) {
            throw new IllegalArgumentException(
                    "A nova senha deve ter pelo menos 8 caracteres.");
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuário não encontrado."));

        usuario.setSenhaHash(
                passwordEncoder.encode(novaSenha)
        );

        return usuarioRepository.save(usuario);
    }
}