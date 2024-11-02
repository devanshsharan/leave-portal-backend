package com.example.leavePortal.config;

import com.example.leavePortal.model.Employee;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled=true)
@AllArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    private final JwtFilter jwtFilter;

    @Bean
    public AuthenticationProvider authProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(new BCryptPasswordEncoder(12));
        return provider;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .formLogin(formLoginCustomizer->formLoginCustomizer.disable())
                .httpBasic(httpBasicCustomizer->httpBasicCustomizer.disable())
                .csrf(customizer -> customizer.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/login","/deleteCookie","/refresh-token","/ws/**")
                        .permitAll()
                        .requestMatchers("/createEmployees", "/createProjects", "/createProjectEmployeeRoles")
                        .hasAnyAuthority(Employee.Role.ADMIN.name())
                        .requestMatchers("/updateStatus","/uncleared/**","/employees/**", "/listProject/**","/refresh","/totalLeave/**","/managerResponseList/**","/leaveRequestList/**")
                        .hasAnyAuthority(Employee.Role.ADMIN.name(), Employee.Role.EMPLOYEE.name(),Employee.Role.MANAGER.name() )
                        .requestMatchers("/respond","/manager/**","/managerEmployeeProjects/**")
                        .hasAnyAuthority(Employee.Role.ADMIN.name(), Employee.Role.MANAGER.name())
                        .requestMatchers("/apply", "/cancel")
                        .hasAnyAuthority(Employee.Role.MANAGER.name(), Employee.Role.EMPLOYEE.name())
                        .anyRequest().authenticated())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
         return config.getAuthenticationManager();
    }

}