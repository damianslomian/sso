package de.websso.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class OAuth2Configuration {

    @Bean
    SecurityFilterChain app(HttpSecurity http,
            @Value("${oauth2.registration-id}") String regId,
            ClientRegistrationRepository clientRegistrationRepository) throws Exception {
        OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}/api");
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/login", "/error", "/oauth2/**",
                                "/favicon.ico", "/javax.faces.resource/**", "/resources/**",
                                "/assets/**", "/css/**", "/js/**", "/img/**",
                                "/logout" // ← permitAll
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(o -> o
                        .loginPage("/oauth2/authorization/" + regId)
                        .userInfoEndpoint(u -> u.oidcUserService(oidcUserService()))
                )
                .logout(l -> l
                        .logoutUrl("/perform_logout")
                        .logoutSuccessHandler(oidcLogoutSuccessHandler)
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                        "/javax.faces.resource/**", "/resources/**", "/assets/**", "/api/**"
                ));

        return http.build();
    }

    @Bean
    OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        OidcUserService delegate = new OidcUserService();
        return req -> {
            OidcUser user = delegate.loadUser(req);
            Map<String, Object> claims = user.getClaims();
            ArrayList<GrantedAuthority> authorities = new ArrayList<>(user.getAuthorities());

            Collection<String> roles = (Collection<String>) claims.getOrDefault("roles", List.of());
            roles.forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_" + r.toUpperCase())));

            Collection<String> groups = (Collection<String>) claims.getOrDefault("groups", List.of());
            groups.forEach(g -> authorities.add(new SimpleGrantedAuthority("GROUP_" + g)));

            Map<String, Map<String, Collection<String>>> ra = (Map<String, Map<String, Collection<String>>>) claims.get("resource_access");
            if (ra != null) {
                ra.values().forEach(v -> v.getOrDefault("roles", List.of())
                        .forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_" + r.toUpperCase()))));
            }

            return new DefaultOidcUser(authorities, user.getIdToken(), user.getUserInfo());
        };
    }
}
