package cl.reservas.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {
    @AfterEach
    void clearContext() { SecurityContextHolder.clearContext(); }

    @Test
    void doesNotAuthenticateDisabledUserWithStillValidJwt() throws Exception {
        JwtService jwt = mock(JwtService.class);
        UserDetailsService users = mock(UserDetailsService.class);
        when(jwt.extractSubject("valid-token")).thenReturn("blocked@example.com");
        when(users.loadUserByUsername("blocked@example.com")).thenReturn(User.withUsername("blocked@example.com")
                .password("hash").roles("CUSTOMER").disabled(true).build());
        var filter = new JwtAuthenticationFilter(jwt, users);
        var request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer valid-token");

        filter.doFilter(request, new MockHttpServletResponse(), mock(FilterChain.class));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
