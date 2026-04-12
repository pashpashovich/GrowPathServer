package by.bsuir.growpathserver.common.security;

import java.util.ArrayList;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import by.bsuir.growpathserver.common.util.JwtUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GrowPathResourceServerJwt {

    public static JwtAuthenticationConverter authenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> new ArrayList<>(JwtUtils.extractRoles(jwt)));
        return converter;
    }
}
