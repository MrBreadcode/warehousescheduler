package com.rohoza.warehousescheduler.config;

import com.rohoza.warehousescheduler.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Autowired
    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/admin/**",
                                "/api/managers/**",
                                "/api/workers/**",
                                "/api/users/**",
                                "/api/locations/**").hasRole("ADMIN")
                        .requestMatchers("/api/tasks/**",
                                "/api/preferences/**").hasRole("MANAGER")
                        .requestMatchers("/api/schedule/**",
                                "/api/personal/**").hasRole("WORKER")
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler((request, response, authentication) -> {
                            String role = authentication.getAuthorities().stream()
                                    .map(grantedAuthority -> grantedAuthority.getAuthority())
                                    .filter(roleName -> roleName.equals("ROLE_ADMIN") || roleName.equals("ROLE_MANAGER") || roleName.equals("ROLE_WORKER"))
                                    .findFirst()
                                    .orElse("ROLE_GUEST");
                            if ("ROLE_ADMIN".equals(role)) {
                                response.sendRedirect("/api/workers");
                            } else if ("ROLE_MANAGER".equals(role)) {
                                response.sendRedirect("/api/tasks");
                            } else if ("ROLE_WORKER".equals(role)) {
                                response.sendRedirect("/api/schedule");
                            } else {
                                response.sendRedirect("/");
                            }
                        })
                        .permitAll()
                )
                .logout(LogoutConfigurer::permitAll
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
