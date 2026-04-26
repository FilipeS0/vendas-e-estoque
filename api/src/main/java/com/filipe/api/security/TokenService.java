package com.filipe.api.security;

import com.filipe.api.domain.token.RefreshToken;
import com.filipe.api.domain.token.RefreshTokenRepository;
import com.filipe.api.domain.usuario.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    public String gerarToken(Usuario usuario) {
        Instant now = Instant.now();
        String scope = usuario.getAuthorities().stream()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .collect(java.util.stream.Collectors.joining(" "));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("pdv-api")
                .issuedAt(now)
                .expiresAt(now.plus(8, ChronoUnit.HOURS))
                .subject(usuario.getEmail())
                .claim("id", usuario.getId().toString())
                .claim("scope", scope)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    public String gerarRefreshToken(Usuario usuario) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .usuario(usuario)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revogado(false)
                .build();

        return refreshTokenRepository.save(refreshToken).getToken();
    }

    public String renovarAccessToken(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByTokenAndRevogadoFalse(refreshToken)
                .orElseThrow(() -> new BadCredentialsException("Refresh token inválido."));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("Refresh token expirado.");
        }

        return gerarToken(token.getUsuario());
    }

    public void revogarRefreshToken(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByTokenAndRevogadoFalse(refreshToken)
                .orElseThrow(() -> new BadCredentialsException("Refresh token inválido ou já revogado."));

        token.setRevogado(true);
        refreshTokenRepository.save(token);
    }
}