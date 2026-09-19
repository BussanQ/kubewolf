package com.bussanq.kubewolf.web.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Set;

@Slf4j
public class AuditFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            chain.doFilter(request, response);
        } finally {
            if (!Set.of("GET", "HEAD", "OPTIONS").contains(request.getMethod())) {
                var auth = SecurityContextHolder.getContext().getAuthentication();
                log.info("AUDIT user={} method={} path={} status={} remote={}",
                        auth == null ? "anonymous" : auth.getName(), request.getMethod(),
                        request.getRequestURI().replaceAll("[\r\n]", ""), response.getStatus(), request.getRemoteAddr());
            }
        }
    }
}
