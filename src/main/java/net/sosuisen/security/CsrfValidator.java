package net.sosuisen.security;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.mvc.MvcContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.ForbiddenException;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@NoArgsConstructor(force = true)
@RequiredArgsConstructor(onConstructor_ = @Inject)
@RequestScoped
public class CsrfValidator {
    private final HttpServletRequest req;
    private final MvcContext mvcContext;

    public void validateCsrfToken() {
        var csrf = req.getHeader("X-CSRF-Token");
        if (csrf == null || !csrf.equals(mvcContext.getCsrf().getToken())) {
            throw new ForbiddenException();
        }
    }
}
