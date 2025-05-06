package com.computer.demoComputer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.session.security.web.authentication.SpringSessionRememberMeServices;

import com.computer.demoComputer.service.CustomUserDetailsService;
import com.computer.demoComputer.service.UserService;

import jakarta.servlet.DispatcherType;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserService userService) {
        return new CustomUserDetailsService(userService);
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider
            (PasswordEncoder passwordEncoder, UserDetailsService userDetailsService) {

        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setHideUserNotFoundExceptions(false);

        return daoAuthenticationProvider;
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler(UserService userService) {
        return new CustomSuccessHandler(userService);
    }

    @Bean
    public SpringSessionRememberMeServices rememberMeServices() {
        SpringSessionRememberMeServices rememberMeServices =
                new SpringSessionRememberMeServices();
        // optionally customize
        rememberMeServices.setAlwaysRemember(true);
        return rememberMeServices;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, 
                                            AuthenticationSuccessHandler authenticationSuccessHandler) 
                                            throws Exception {
        httpSecurity
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.INCLUDE).permitAll()
                        .requestMatchers("/", "/login", "/register", "/product/**", "/products/**",
                                "/client/**", "/css/**", "/js/**", "/uploads/images/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .failureUrl("/login?error")
                        .successHandler(authenticationSuccessHandler).permitAll()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                        .invalidSessionUrl("/logout?expired").maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                )
                .logout(logout -> logout
                        .deleteCookies("JSESSIONID").invalidateHttpSession(true)
                )
                .rememberMe(remember -> remember
                        .rememberMeServices(rememberMeServices())
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/access-deny")
                );
        return httpSecurity.build();
    }
}
