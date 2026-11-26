package br.ifpb.edu.clai.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class PermissaoModelAdvice {

    @ModelAttribute("podeEditar")
    public boolean podeEditar() {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities()
                .stream()
                .anyMatch(autoridade ->
                    "ROLE_EDICAO".equals(
                        autoridade.getAuthority()
                    )
                );
    }
}