package com.filipe.api.controller;

import com.filipe.api.domain.usuario.Usuario;
import com.filipe.api.dto.auth.AccessTokenResponse;
import com.filipe.api.dto.auth.LoginRequest;
import com.filipe.api.dto.auth.LoginResponse;
import com.filipe.api.dto.auth.RefreshTokenRequest;
import com.filipe.api.security.TokenService;
import com.filipe.api.shared.config.RateLimiterConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final RateLimiterConfig rateLimiterConfig;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request, HttpServletRequest httpRequest) {
        String clientIp = resolveClientIp(httpRequest);

        try {
            var authToken = new UsernamePasswordAuthenticationToken(request.email(), request.password());
            Authentication authentication = authenticationManager.authenticate(authToken);

            Usuario usuario = (Usuario) authentication.getPrincipal();
            String accessToken = tokenService.gerarToken(usuario);
            String refreshToken = tokenService.gerarRefreshToken(usuario);

            return ResponseEntity.ok(new LoginResponse(accessToken, refreshToken));
        } catch (AuthenticationException ex) {
            var bucket = rateLimiterConfig.resolveBucket(clientIp);
            if (!bucket.tryConsume(1)) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                        "Muitas tentativas. Aguarde antes de tentar novamente.");
            }
            throw ex;
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponse> refresh(@RequestBody @Valid RefreshTokenRequest request) {
        String accessToken = tokenService.renovarAccessToken(request.refreshToken());
        return ResponseEntity.ok(new AccessTokenResponse(accessToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid RefreshTokenRequest request) {
        tokenService.revogarRefreshToken(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<Usuario> getMe(Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return ResponseEntity.ok(usuario);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
