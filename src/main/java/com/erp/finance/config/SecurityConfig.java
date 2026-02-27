package com.erp.finance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .httpBasic(httpBasic -> httpBasic.realmName("ERP Finance"))
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            )
            .authorizeHttpRequests(auth -> auth
                // H2 Console access
                .requestMatchers("/h2-console/**").permitAll()
                // Read-only endpoints - all roles can access
                .requestMatchers("GET", "/api/accounts/**").hasAnyRole("SYSTEM_ADMIN", "ACCOUNTANT", "ACCOUNT_EXECUTIVE")
                .requestMatchers("GET", "/api/journals/**").hasAnyRole("SYSTEM_ADMIN", "ACCOUNTANT", "ACCOUNT_EXECUTIVE")
                .requestMatchers("GET", "/api/gl/**").hasAnyRole("SYSTEM_ADMIN", "ACCOUNTANT", "ACCOUNT_EXECUTIVE")
                .requestMatchers("GET", "/api/auth/**").hasAnyRole("SYSTEM_ADMIN", "ACCOUNTANT", "ACCOUNT_EXECUTIVE")
                .requestMatchers("GET", "/api/ap/**").hasAnyRole("SYSTEM_ADMIN", "ACCOUNTANT", "ACCOUNT_EXECUTIVE")
                .requestMatchers("GET", "/api/ar/**").hasAnyRole("SYSTEM_ADMIN", "ACCOUNTANT", "ACCOUNT_EXECUTIVE")
                .requestMatchers("GET", "/api/fixed-assets/**").hasAnyRole("SYSTEM_ADMIN", "ACCOUNTANT", "ACCOUNT_EXECUTIVE")
                .requestMatchers("GET", "/api/reports/**").hasAnyRole("SYSTEM_ADMIN", "ACCOUNTANT", "ACCOUNT_EXECUTIVE")
                // User management - only SYSTEM_ADMIN can access
                .requestMatchers("/api/users/**").hasRole("SYSTEM_ADMIN")
                // Write endpoints - only ACCOUNTANT can access
                .requestMatchers("POST", "/api/accounts/**").hasRole("ACCOUNTANT")
                .requestMatchers("PUT", "/api/accounts/**").hasRole("ACCOUNTANT")
                .requestMatchers("DELETE", "/api/accounts/**").hasRole("ACCOUNTANT")
                .requestMatchers("POST", "/api/journals/**").hasRole("ACCOUNTANT")
                .requestMatchers("PUT", "/api/journals/**").hasRole("ACCOUNTANT")
                .requestMatchers("DELETE", "/api/journals/**").hasRole("ACCOUNTANT")
                .requestMatchers("POST", "/api/ap/**").hasRole("ACCOUNTANT")
                .requestMatchers("PUT", "/api/ap/**").hasRole("ACCOUNTANT")
                .requestMatchers("DELETE", "/api/ap/**").hasRole("ACCOUNTANT")
                .requestMatchers("POST", "/api/ar/**").hasRole("ACCOUNTANT")
                .requestMatchers("PUT", "/api/ar/**").hasRole("ACCOUNTANT")
                .requestMatchers("DELETE", "/api/ar/**").hasRole("ACCOUNTANT")
                .requestMatchers("POST", "/api/fixed-assets/**").hasRole("ACCOUNTANT")
                .requestMatchers("PUT", "/api/fixed-assets/**").hasRole("ACCOUNTANT")
                .requestMatchers("DELETE", "/api/fixed-assets/**").hasRole("ACCOUNTANT")
                .requestMatchers("POST", "/api/budgets/**").hasRole("ACCOUNTANT")
                .requestMatchers("PUT", "/api/budgets/**").hasRole("ACCOUNTANT")
                .requestMatchers("DELETE", "/api/budgets/**").hasRole("ACCOUNTANT")
                // Any other request requires authentication
                .anyRequest().authenticated()
            );
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails systemAdmin = User.builder()
            .username("sysadmin")
            .password(passwordEncoder().encode("sysadmin123"))
            .roles("SYSTEM_ADMIN")
            .build();

        UserDetails accountant = User.builder()
            .username("admin")
            .password(passwordEncoder().encode("admin123"))
            .roles("ACCOUNTANT")
            .build();

        UserDetails accountExecutive = User.builder()
            .username("viewer")
            .password(passwordEncoder().encode("viewer123"))
            .roles("ACCOUNT_EXECUTIVE")
            .build();

        return new InMemoryUserDetailsManager(systemAdmin, accountant, accountExecutive);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
