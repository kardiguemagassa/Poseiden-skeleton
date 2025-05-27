package com.nnk.springboot.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * Main Spring Security configuration.
 * This class defines access rules, protected URLs,
 * login/logout behavior, role management and configuration
 * components necessary for security (encoder, user service, etc.).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final AuthenticationSuccessHandler authenticationSuccessHandler;

    /**
     * Constructor injecting the dependencies necessary for the security configuration.
     *
     * @param userDetailsService the service to load a user's details
     * @param authenticationSuccessHandler custom handler to redirect after authentication success
     */
    public SecurityConfig(UserDetailsService userDetailsService,
                          AuthenticationSuccessHandler authenticationSuccessHandler) {
        this.userDetailsService = userDetailsService;
        this.authenticationSuccessHandler = authenticationSuccessHandler;
    }

    /**
     * Sets the main Spring Security security filter.
     * Configure endpoint access rules, login form,
     * exception handling, and user service.
     *
     * @param http the HTTP security configuration object
     * @return the constructed instance of {@link SecurityFilterChain}
     * @throws Exception on configuration error
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/app/login",
                                "/css/**",
                                "/js/**",
                                "/error/**",
                                "/images/**"
                        ).permitAll()
                        .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/app/login")
                        .loginProcessingUrl("/login")
                        .successHandler(authenticationSuccessHandler) // Rediriger utilisateur selon son role après une connexion réussie.
                        .failureUrl("/app/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/app/logout")
                        .logoutSuccessUrl("/app/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/app/access-denied")
                )
                .userDetailsService(userDetailsService); // charger un utilisateur depuis la BDD

        return http.build();
    }

    /**
     * Provides a password encoder based on the BCrypt algorithm.
     *
     * @return an instance of {@link BCryptPasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}
