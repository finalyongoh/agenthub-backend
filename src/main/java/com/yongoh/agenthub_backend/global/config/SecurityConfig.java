package com.yongoh.agenthub_backend.global.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.yongoh.agenthub_backend.global.security.JsonAccessDeniedHandler;
import com.yongoh.agenthub_backend.global.security.JsonAuthenticationEntryPoint;
import com.yongoh.agenthub_backend.global.security.JwtAuthenticationFilter;
import com.yongoh.agenthub_backend.global.security.OAuth2LoginFailureHandler;
import com.yongoh.agenthub_backend.global.security.OAuth2LoginSuccessHandler;

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
		JsonAccessDeniedHandler accessDeniedHandler,
		OAuth2LoginSuccessHandler oauth2LoginSuccessHandler,
		OAuth2LoginFailureHandler oauth2LoginFailureHandler,
		OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService,
		ObjectProvider<ClientRegistrationRepository> clientRegistrationRepository
	)
		throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
			.exceptionHandling(exceptions -> exceptions
				.authenticationEntryPoint(authenticationEntryPoint)
				.accessDeniedHandler(accessDeniedHandler)
			)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(HttpMethod.POST, "/api/auth/signup", "/api/auth/login").permitAll()
				.requestMatchers("/api/v1/internal/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/health").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/trend-reports", "/api/trend-reports/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/images/**").permitAll()
				.requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
				.requestMatchers("/api/admin/**").hasRole("ADMIN")
				.anyRequest().authenticated()
			)
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		if (clientRegistrationRepository.getIfAvailable() != null) {
			http.oauth2Login(oauth -> oauth
				.userInfoEndpoint(userInfo -> userInfo.userService(oauth2UserService))
				.successHandler(oauth2LoginSuccessHandler)
				.failureHandler(oauth2LoginFailureHandler)
			);
		}

		return http.build();
	}
}
