package by.bsuir.growpathserver.trainee.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CurrentApplicationUserResolverImplTest {

    @Mock
    private UserRepository userRepository;

    private CurrentApplicationUserResolverImpl resolver;

    @BeforeEach
    void setUp() {
        resolver = new CurrentApplicationUserResolverImpl(userRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void resolvesByKeycloakUserId() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("kc-sub-1");
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        UserEntity u = new UserEntity();
        u.setId(42L);
        when(userRepository.findByKeycloakUserId("kc-sub-1")).thenReturn(java.util.Optional.of(u));

        assertEquals(42L, resolver.resolveCurrentUserDatabaseId().orElseThrow());
    }

    @Test
    void resolvesByEmailIgnoreCaseWhenSubUnknown() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("unknown");
        when(jwt.getClaims()).thenReturn(Map.of("sub", "unknown", "email", "Admin@Example.com"));
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        when(userRepository.findByKeycloakUserId("unknown")).thenReturn(java.util.Optional.empty());
        UserEntity u = new UserEntity();
        u.setId(7L);
        when(userRepository.findByEmailIgnoreCase("Admin@Example.com")).thenReturn(java.util.Optional.of(u));

        assertEquals(7L, resolver.resolveCurrentUserDatabaseId().orElseThrow());
    }

    @Test
    void resolvesJwtWhenPrincipalIsJwtButNotJwtAuthenticationToken() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("x");
        when(jwt.getClaims()).thenReturn(Map.of("sub", "x", "email", "a@b.c"));
        var auth = new TestingAuthenticationToken(jwt, "", AuthorityUtils.NO_AUTHORITIES);
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(userRepository.findByKeycloakUserId("x")).thenReturn(java.util.Optional.empty());
        UserEntity u = new UserEntity();
        u.setId(1L);
        when(userRepository.findByEmailIgnoreCase("a@b.c")).thenReturn(java.util.Optional.of(u));

        assertEquals(1L, resolver.resolveCurrentUserDatabaseId().orElseThrow());
    }

    @Test
    void returnsEmptyWhenNotJwt() {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("user", "", Collections.emptyList()));
        assertTrue(resolver.resolveCurrentUserDatabaseId().isEmpty());
    }
}
