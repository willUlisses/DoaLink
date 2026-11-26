package com.doalink_api.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final RequestFilter requestFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/auth/login",
                                "/auth/refresh",
                                "/auth/logout",
                                "/auth/doadores/cadastro",
                                "/auth/transportadores/cadastro",
                                "/auth/receptores/cadastro",
                                "/auth/verificar-email",
                                "/auth/reenviar-codigo",
                                "/auth/recuperar-senha",
                                "/auth/redefinir-senha").permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/recuperar-senha/validar").permitAll()
                        .requestMatchers(HttpMethod.POST, "/ofertas").hasRole("DOADOR")
                        .requestMatchers("/ofertas/minhas").hasRole("DOADOR")
                        .requestMatchers(HttpMethod.GET, "/ofertas").hasRole("RECEPTOR")
                        .requestMatchers(HttpMethod.PATCH, "/ofertas/*/cancelar").hasRole("DOADOR")
                        .requestMatchers(HttpMethod.PATCH, "/ofertas/*/autorizar-transporte").hasRole("DOADOR")
                        .requestMatchers(HttpMethod.POST, "/campanhas").hasRole("RECEPTOR")
                        .requestMatchers("/campanhas/minhas").hasRole("RECEPTOR")
                        .requestMatchers(HttpMethod.POST, "/necessidades").hasRole("RECEPTOR")
                        .requestMatchers("/necessidades/minhas").hasRole("RECEPTOR")
                        .requestMatchers(HttpMethod.GET, "/necessidades").hasRole("DOADOR")
                        .requestMatchers(HttpMethod.GET, "/necessidades/*").hasAnyRole("DOADOR", "RECEPTOR")
                        .requestMatchers(HttpMethod.POST, "/reservas").hasRole("RECEPTOR")
                        .requestMatchers("/reservas/minhas/receptor").hasRole("RECEPTOR")
                        .requestMatchers("/reservas/minhas/doador").hasRole("DOADOR")
                        .requestMatchers(HttpMethod.PATCH, "/reservas/*/autorizar-transporte").hasRole("DOADOR")
                        .requestMatchers(HttpMethod.PATCH, "/reservas/*/cancelar").hasRole("RECEPTOR")
                        .requestMatchers(
                                "/reservas/aguardando-transporte",
                                "/reservas/minhas/transportador",
                                "/reservas/minhas/transportador/historico").hasRole("TRANSPORTADOR")
                        .requestMatchers(HttpMethod.PATCH,
                                "/reservas/*/aceitar-transporte",
                                "/reservas/*/confirmar-retirada",
                                "/reservas/*/confirmar-entrega").hasRole("TRANSPORTADOR")
                        .requestMatchers(HttpMethod.PATCH, "/reservas/*/concluir").hasAnyRole("TRANSPORTADOR", "RECEPTOR")
                        .anyRequest().authenticated()
                ).exceptionHandling(e -> e
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler((req, res, ex) -> res.setStatus(HttpServletResponse.SC_FORBIDDEN)))
                .addFilterBefore(requestFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfiguration)
            throws Exception {
        return authConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.origins}") List<String> origins) {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(origins);
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

}

