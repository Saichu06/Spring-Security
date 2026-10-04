package com.amigo_security.spr_sec.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    private static final List<UserDetails> APPLICATION_USERS = Arrays.asList(
            new User(
                    "chuchu@gmail.com",
                    "password",
                    Collections.singleton(
                            new SimpleGrantedAuthority("ROLE_ADMIN")
                    )
            ),
            new User(
                    "chushi@gmail.com",
                    "password123",
                    Collections.singleton(
                            new SimpleGrantedAuthority("ROLE_ADMIN")
                    )
            )
    );

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .authenticationProvider(authenticationProvider())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .httpBasic(httpBasic -> {});

        return http.build();
    }

    private AuthenticationProvider authenticationProvider() {

        final DaoAuthenticationProvider authenticationProvider =
                new DaoAuthenticationProvider(userDetailsService());

        authenticationProvider.setPasswordEncoder(passwordEncoder());

        return authenticationProvider;
    }
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }


    private PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();

        // For real applications:
        // return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {

        return new UserDetailsService() {

            @Override
            public UserDetails loadUserByUsername(String email)
                    throws UsernameNotFoundException {

                return APPLICATION_USERS.stream()
                        .filter(user -> user.getUsername().equals(email))
                        .findFirst()
                        .orElseThrow(() ->
                                new UsernameNotFoundException(
                                        "User not found: " + email
                                )
                        );
            }
        };
    }
}