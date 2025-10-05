package com.FairMatch.FairMatch.service;

import com.FairMatch.FairMatch.model.Auth;
import com.FairMatch.FairMatch.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Service
public class JwtService {
    private final JwtUtil jwtUtil;

    @Autowired
    public JwtService(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public String generateToken(Auth user) {
        Map<String, Object> claims = Map.of(
            "role", user.getRole(),
            "userId", user.getUserId().toString()
        );
        return jwtUtil.generateToken(user.getUsername(), claims);
    }

    public Cookie generateAuthCookie(String token) {
        Cookie cookie = new Cookie("authToken", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60);
        return cookie;
    }

    public String getUsernameFromToken(String token) {
        return jwtUtil.getSubject(token);
    }
    public String getRoleFromToken(String token) {
        return jwtUtil.getRole(token);
    }
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }
}
