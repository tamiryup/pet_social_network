package com.tamir.followear.filters;

import com.tamir.followear.services.AuthService;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@NoArgsConstructor
public class AuthenticationFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsrfFilter.class);

    private AuthService authService;

    public AuthenticationFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        LOGGER.info("validating jwt tokens");
        authService.authenticateRequest(request, response);

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }

}
