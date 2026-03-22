package com.lovingapp.config.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.lovingapp.config.filter.RequestContextMdcFilter;
import com.lovingapp.config.filter.UserIdMdcFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	public static final String[] PUBLIC_INFRA_ENDPOINTS = {
			"/swagger-ui/**",
			"/v3/api-docs/**",
			"/api-docs/**",
			"/actuator/health/**",
			"/actuator/info",
			"/actuator/prometheus"
	};

	public static final String[] PUBLIC_API_ENDPOINTS = {
			"/v1/love-types/**",
			"/v1/rituals/**",
			"/v1/ritual-packs/**",
			"/v1/chat/sample-prompts"
	};

	private final JwtDecoderConfig jwtConfig;
	private final CorsConfig corsConfig;
	private final RequestContextMdcFilter requestContextFilter;
	private final UserIdMdcFilter userIdMdcFilter;

	private static final RequestMatcher PUBLIC_ENDPOINT_MATCHER = buildPublicMatcher();

	private static RequestMatcher buildPublicMatcher() {
		List<RequestMatcher> matchers = new ArrayList<>();
		PathPatternRequestMatcher.Builder builder = PathPatternRequestMatcher.withDefaults();

		Stream.concat(
				Arrays.stream(PUBLIC_INFRA_ENDPOINTS),
				Arrays.stream(PUBLIC_API_ENDPOINTS))
				.forEach(pattern -> matchers.add(builder.matcher(pattern)));

		return new OrRequestMatcher(matchers);
	}

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
				.oauth2ResourceServer(oauth2 -> oauth2
						.bearerTokenResolver(bearerTokenResolver())
						.jwt(jwt -> jwt.decoder(jwtConfig.jwtDecoder())))
				.addFilterBefore(requestContextFilter, SecurityContextHolderFilter.class)
				.addFilterAfter(userIdMdcFilter, BearerTokenAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public BearerTokenResolver bearerTokenResolver() {
		DefaultBearerTokenResolver delegate = new DefaultBearerTokenResolver();
		return request -> {
			if (PUBLIC_ENDPOINT_MATCHER.matches(request)) {
				return null; // skip JWT resolution for public endpoints
			}
			return delegate.resolve(request);
		};
	}
}