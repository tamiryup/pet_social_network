package com.tamir.followear.filters;

import com.tamir.followear.services.CsrfService;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
@NoArgsConstructor
public class CsrfFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsrfFilter.class);

    private CsrfService csrfService;

    public CsrfFilter(CsrfService csrfService) {
        this.csrfService = csrfService;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        if(request.getMethod().equals("POST")) {
            LOGGER.info("validating csrf");
            csrfService.validateCsrf(request);
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
