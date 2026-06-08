package com.lisu.onlinestore.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class BasicAuthenticationFilter extends HttpFilter {
    /* 
    // Filter commented out for the current HW as per requirements, 
    // but kept for future security tasks.
    @Override
    protected void doFilter(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {
        String url = request.getRequestURI();
        if (PublicAvaliableEndpoints.getPublicEndpoints().contains(url)) {
            chain.doFilter(request, response);
            return;
        }
        String header = request.getHeader("Authorization");

        chain.doFilter(request, response);
    }
    */
}
