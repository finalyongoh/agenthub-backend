package com.yongoh.agenthub_backend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.yongoh.agenthub_backend.global.security.JsonAccessDeniedHandler;
import com.yongoh.agenthub_backend.global.security.JsonAuthenticationEntryPoint;
import com.yongoh.agenthub_backend.global.security.JwtAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	SecurityFilterChain securityFilterChain(
		HttpSecurity http,
		JwtAuthenticationFilter jwtAuthenticationFilter,
		JsonAuthenticationEntryPoint authenticationEntryPoint,
		JsonAccessDeniedHandler accessDeniedHandler
	)
		throws Exception {
		return http
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.exceptionHandling(exceptions -> exceptions
				.authenticationEntryPoint(authenticationEntryPoint)
				.accessDeniedHandler(accessDeniedHandler)
			)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(HttpMethod.POST, "/api/auth/signup", "/api/auth/login").permitAll()
				.requestMatchers("/api/admin/**").hasRole("ADMIN")
				.anyRequest().authenticated()
			)
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			.build();
	}
}
