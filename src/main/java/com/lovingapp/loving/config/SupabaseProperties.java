package com.lovingapp.loving.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "supabase")
public class SupabaseProperties {
    /** Base URL of the Supabase project, e.g. https://xyzcompany.supabase.co */
    private String url;

    /** Publishable (anon) key */
    private String anonKey;

    /** Service role (secret) key */
    private String serviceRoleKey;

    /** Optional issuer for JWT validation (if enforcing issuer checks) */
    private String jwtIssuer;

    /** Optional JWKS URI for JWT validation (if enforcing issuer checks) */
    private String jwtJwksUri;
}
