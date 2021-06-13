package com.tamir.followear.filters;

import com.tamir.followear.services.AuthService;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.tamir.followear.helpers.HttpHelper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@NoArgsConstructor
public class AuthenticationFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationFilter.class);

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

        String lastPathPart = HttpHelper.getPathPartByIndex(request, -1);
        List<String> excludedPaths = Arrays.asList("user-feed", "post-info", "follow-slaves",
                "follow-masters", "inc-post-views");

        //exclude these two paths from the filter
        if(!excludedPaths.contains(lastPathPart)) {
            LOGGER.info("validating jwt tokens");
            authService.authenticateRequest(request, response);
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }

}
