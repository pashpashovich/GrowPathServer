package by.bsuir.growpathserver.common.util;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JwtUtils {

    /**
     * Realm role names from Keycloak that map to GrowPath application roles (case-insensitive).
     * Keycloak also assigns composite/system roles (e.g. default-roles-*, offline_access, uma_authorization); those are omitted.
     */
    private static final Set<String> APPLICATION_REALM_ROLES = Set.of(
            "INTERN", "MENTOR", "HR_MANAGER", "ADMIN"
    );

    private static boolean isApplicationRealmRole(String realmRole) {
        if (realmRole == null || realmRole.isBlank()) {
            return false;
        }
        return APPLICATION_REALM_ROLES.contains(realmRole.toUpperCase(Locale.ROOT));
    }

    public static Collection<GrantedAuthority> extractRoles(Jwt jwt) {
        @SuppressWarnings("unchecked")
        var realmAccess = (java.util.Map<String, Object>) jwt.getClaims().get("realm_access");
        if (realmAccess == null) {
            return List.of();
        }

        @SuppressWarnings("unchecked")
        var roles = (List<String>) realmAccess.get("roles");
        if (roles == null) {
            return List.of();
        }

        return roles.stream()
                .filter(JwtUtils::isApplicationRealmRole)
                .map(realmRole -> new SimpleGrantedAuthority("ROLE_" + realmRole.toUpperCase(Locale.ROOT)))
                .collect(Collectors.toList());
    }

    public static boolean hasRole(Jwt jwt, String role) {
        if (role == null) {
            return false;
        }
        String expected = "ROLE_" + role.toUpperCase(Locale.ROOT);
        return extractRoles(jwt).stream()
                .anyMatch(authority -> authority.getAuthority().equals(expected));
    }

    public static String getUsername(Jwt jwt) {
        Object claim = jwt.getClaims().get("preferred_username");
        return claim instanceof String ? (String) claim : null;
    }

    public static String getEmail(Jwt jwt) {
        Object claim = jwt.getClaims().get("email");
        return claim instanceof String ? (String) claim : null;
    }

    public static String getFirstName(Jwt jwt) {
        Object claim = jwt.getClaims().get("given_name");
        return claim instanceof String ? (String) claim : null;
    }

    public static String getLastName(Jwt jwt) {
        Object claim = jwt.getClaims().get("family_name");
        return claim instanceof String ? (String) claim : null;
    }

    public static String getUserId(Jwt jwt) {
        return jwt.getSubject();
    }
}
