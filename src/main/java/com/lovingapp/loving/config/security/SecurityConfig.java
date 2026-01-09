package com.lovingapp.loving.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;

import com.lovingapp.loving.infra.security.DbCurrentUserContextFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	public static final String[] PUBLIC_INFRA_ENDPOINTS = {
			"/swagger-ui/**",
			"/v3/api-docs/**",
			"/api-docs/**"
	};

	public static final String[] PUBLIC_API_ENDPOINTS = {
			"/api/love-types/**",
			"/api/rituals/**",
			"/api/ritual-packs/**"
	};

	private final JwtDecoderConfig jwtConfig;
	private final CorsConfig corsConfig;
	private final DbCurrentUserContextFilter dbAuthUserContextFilter;
	private final RequestIdFilter requestIdFilter;
	private final UserIdMdcFilter userIdMdcFilter;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
				.csrf(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers(PUBLIC_INFRA_ENDPOINTS).permitAll()
						.requestMatchers(PUBLIC_API_ENDPOINTS).permitAll()
						.anyRequest().authenticated())
				// Supabase JWTs are validated here
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtConfig.jwtDecoder())))
				.addFilterBefore(requestIdFilter, SecurityContextHolderFilter.class)
				.addFilterAfter(dbAuthUserContextFilter, BearerTokenAuthenticationFilter.class)
				.addFilterAfter(userIdMdcFilter, BearerTokenAuthenticationFilter.class);

		return http.build();
	}
}