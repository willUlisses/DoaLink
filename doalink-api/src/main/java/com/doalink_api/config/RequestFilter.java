package com.doalink_api.config;

import com.doalink_api.dtos.response.UsuarioAutenticado;
import com.doalink_api.service.CookieService;
import com.doalink_api.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.http.Cookie;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RequestFilter extends OncePerRequestFilter {

    private final JwtService jwtService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        Cookie cookie = WebUtils.getCookie(request, CookieService.ACCESS);
        if (cookie != null) {
            jwtService.validar(cookie.getValue()).ifPresent(jwt -> {
                var principal = new UsuarioAutenticado(
                        Long.valueOf(jwt.getSubject()), jwt.getClaim("email").asString());
                var authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_" + jwt.getClaim("role").asString()));
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(principal, null, authorities));
            });
        }
        chain.doFilter(request, response);   // sem token válido segue anônimo e o Security responde 401
    }

}
