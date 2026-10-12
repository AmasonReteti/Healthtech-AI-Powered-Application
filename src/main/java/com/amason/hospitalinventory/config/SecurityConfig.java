package com.amason.hospitalinventory.config;

import com.amason.hospitalinventory.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;
    @org.springframework.beans.factory.annotation.Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // This tells Spring: "requests coming from our Angular dev server
    // are allowed to talk to us" - without this, browsers silently
    // block the request before it even reaches our code
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/login").permitAll()
                // Browsers send an OPTIONS "preflight" request before many 
                // real requests, just to check CORS permissions - this must 
                // be allowed through freely, or the browser blocks the 
                // REAL request before it's even sent
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()	
                
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/users").hasRole("ADMIN")
               .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/users/*/deactivate", "/api/users/*/reactivate").hasRole("ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/suppliers").hasRole("ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/departments").hasRole("ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/products").hasAnyRole("ADMIN", "STOREKEEPER")
               .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/stock-batches").hasAnyRole("ADMIN", "STOREKEEPER")
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/stock-movements").hasAnyRole("ADMIN", "STOREKEEPER")
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/reconciliation").hasAnyRole("ADMIN", "STOREKEEPER")
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/purchase-orders/*/submit").hasRole("ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/stock-movements/*/approve", "/api/stock-movements/*/reject").hasRole("AUDITOR")
                .anyRequest().authenticated()
            )

            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
