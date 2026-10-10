package de.ticketsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)) // Ermöglicht H2 Console
                .authorizeHttpRequests(auth -> auth
                        // Öffentlicher Zugriff auf Web-Oberfläche & statische Dateien
                        .requestMatchers("/", "/index.html", "/favicon.ico", "/css/**", "/js/**").permitAll()
                        // Öffentlicher Zugriff auf Swagger UI & API Docs
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**", "/v3/api-docs/**").permitAll()
                        // Zugriff auf H2-Datenbank-Konsole für lokale Entwicklung
                        .requestMatchers("/h2-console/**").permitAll()
                        // REST API Endpunkte für Portfolio-Präsentation und Tests freigegeben
                        .requestMatchers("/api/v1/**").permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
