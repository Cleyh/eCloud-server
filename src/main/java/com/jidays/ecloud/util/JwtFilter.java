package com.jidays.ecloud.util;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@WebFilter(urlPatterns = "/userFile/*")
public class JwtFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if(request.getMethod().equalsIgnoreCase("OPTIONS")){
            response.setStatus(HttpServletResponse.SC_OK);
            chain.doFilter(req, res);
            return;
        }
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null
//                || !authHeader.startsWith("Bearer ")
        ) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Missing or invalid Authorization header.");
            return;
        }

        try {
            if (TokenComp.isValidToken(authHeader) == null) {
                throw new Exception("Invalid token");
            }
            request.setAttribute("user_id", TokenComp.getIDFromToken(authHeader));
            request.setAttribute("email", TokenComp.getEmailFromToken(authHeader));
            request.setAttribute("role", TokenComp.getRoleFromToken(authHeader));
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid token.");
            return;
        }

        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
