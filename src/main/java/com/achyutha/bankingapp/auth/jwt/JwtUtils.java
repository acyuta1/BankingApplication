package com.achyutha.bankingapp.auth.jwt;

import com.achyutha.bankingapp.common.BankApplicationProperties;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Jwt utils.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUtils {

    private final BankApplicationProperties properties;

//    private final String jwtSecret;

//    private final Logger logger;

    /**
     * Generates a jwt token, once authenticated.
     * @param authentication The authentication with appropriate fields, i.e. the UserDetails.
     * @return The generated token.
     */
    public String generateJwtToken(Authentication authentication) {

        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject((userPrincipal.getUsername()))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + properties.getJwtExpirationMs()))
                .signWith(SignatureAlgorithm.HS512, properties.getJwtSecret())
                .compact();
    }

    /**
     * To obtain username from the token, based on the jwtSecret salt.
     * @param token The token.
     * @return The username.
     */
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser().setSigningKey(properties.getJwtSecret()).parseClaimsJws(token).getBody().getSubject();
    }

    /**
     * To validate a JwtToken.
     * @param authToken The token.
     * @return Boolean yes/no depending on the validation.
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(properties.getJwtSecret()).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }
}

