package com.happiest.assignment.es.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.function.Function;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class JwtServiceTest {

    @Test
    public void testIsTokenExpired() {
        JwtService jwtService = new JwtService();
        String token = getToken();
        Claims claims = Jwts.claims();
        Date expectedExpirationDate = new Date(System.currentTimeMillis() + 100000);
        claims.setExpiration(expectedExpirationDate);
        claims.setSubject("user123");
        JwtService spyJwtService = spy(jwtService);
        doReturn(claims).when(spyJwtService).extractClaim(token, Claims::getExpiration);
        doReturn(claims).when(spyJwtService).extractAllClaims(token);
        Function<Claims, String> claimsResolver = Claims::getSubject;
        String extractedClaim = spyJwtService.extractClaim(token, claimsResolver);
        assertNotNull(extractedClaim);
        assertEquals("user123", extractedClaim);

        Date expirationDate = spyJwtService.extractExpiration(token);
        assertNotNull(expirationDate);

        boolean isExpired = spyJwtService.isTokenExpired(token);
        assertFalse(isExpired);
    }


    private String getToken() {
        String username = "user123";
        String secreteKey = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";
        String token = Jwts.builder().setSubject(username).signWith(SignatureAlgorithm.HS256, secreteKey).compact();
        return token;
    }


    @Test
    public void testValidateToken() {
        String username = "user123";
        JwtService jwtService = new JwtService();
        UserDetails userDetails = mock(UserDetails.class);
        String token = getToken();
        Claims claims = Jwts.claims();
        Date expectedExpirationDate = new Date(System.currentTimeMillis() + 100000);
        claims.setExpiration(expectedExpirationDate);
        claims.setSubject("user123");

        JwtService spyJwtService = spy(jwtService);
        doReturn(claims).when(spyJwtService).extractClaim(token, Claims::getExpiration);
        doReturn(claims).when(spyJwtService).extractAllClaims(token);
        Function<Claims, String> claimsResolver = Claims::getSubject;
        String extractedClaim = spyJwtService.extractClaim(token, claimsResolver);
        assertNotNull(extractedClaim);
        assertEquals("user123", extractedClaim);

        Date expirationDate = spyJwtService.extractExpiration(token);
        assertNotNull(expirationDate);

        when(spyJwtService.extractUsername(token)).thenReturn(username);
        boolean isExpired = spyJwtService.isTokenExpired(token);
        assertFalse(isExpired);
        when(userDetails.getUsername()).thenReturn(username);
        boolean isValid = spyJwtService.validateToken(token, userDetails);
        assertTrue(isValid);
        verify(userDetails, times(1)).getUsername();
    }
}
