package com.doalink_api.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CookieService {

    public static final String ACCESS = "access_token";
    public static final String REFRESH = "refresh_token";

    @Value("${app.jwt.access-minutes}") private long accessMinutes;
    @Value("${app.refresh.days}") private long refreshDays;
    @Value("${app.cookie.secure}") private boolean secure;

    public void adicionarHeaders(HttpServletResponse res, String access, String refresh) {
        res.addHeader(HttpHeaders.SET_COOKIE,
                build(ACCESS, access, "/", Duration.ofMinutes(accessMinutes)).toString());
        res.addHeader(HttpHeaders.SET_COOKIE,
                build(REFRESH, refresh, "/auth", Duration.ofDays(refreshDays)).toString());
    }

    public void limpar(HttpServletResponse res) {
        res.addHeader(HttpHeaders.SET_COOKIE, build(ACCESS, "", "/", Duration.ZERO).toString());
        res.addHeader(HttpHeaders.SET_COOKIE, build(REFRESH, "", "/auth", Duration.ZERO).toString());
    }

    private ResponseCookie build(String nome, String valor, String path, Duration maxAge) {
        return ResponseCookie.from(nome, valor)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path(path)
                .maxAge(maxAge)
                .build();
    }
}
