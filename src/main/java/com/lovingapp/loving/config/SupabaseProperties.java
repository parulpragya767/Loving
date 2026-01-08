package com.lovingapp.loving.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "supabase")
public class SupabaseProperties {
    /** Base URL of the Supabase project, e.g. https://xyzcompany.supabase.co */
    private String url;

    /** Issuer for JWT validation */
    private String jwtIssuer;

    /** JWKS URI for JWT validation */
    private String jwtJwksUri;
}
