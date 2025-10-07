package com.FairMatch.FairMatch.controller;

import com.FairMatch.FairMatch.dto.LoginRequest;
import com.FairMatch.FairMatch.model.Auth;
import com.FairMatch.FairMatch.service.AuthService;
import com.FairMatch.FairMatch.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import jakarta.servlet.http.Cookie;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import com.FairMatch.FairMatch.config.SecurityConfig;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private JwtService jwtService;

    private Auth user;

    @BeforeEach
    void setUp() {
        user = new Auth();
        user.setUsername("user1");
        user.setPassword("hashed");
        user.setRole("USER");
    }

    @Test
    void login_success_setsCookieAndReturnsOk() throws Exception {
        when(authService.signin(any(LoginRequest.class))).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(jwtService.generateAuthCookie("jwt-token")).thenReturn(new Cookie("authToken", "jwt-token"));

        String body = "{\"email\":\"user1\",\"password\":\"password1\"}";
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("authToken"));
    }

    @Test
    void login_genericException_returnsInternalServerError() throws Exception {
        when(authService.signin(any(LoginRequest.class))).thenThrow(new RuntimeException("database error"));

        String body = "{\"email\":\"user1\",\"password\":\"wrong\"}";
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void login_invalidCredentials_returnsUnauthorized() throws Exception {
        when(authService.signin(any(LoginRequest.class))).thenThrow(new BadCredentialsException("bad creds"));

        String body = "{\"email\":\"user1\",\"password\":\"wrong\"}";
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_malformedRequest_returnsBadRequest() throws Exception {
        String badBody = "{\"email\":\"user1\"}";

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(badBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signup_success_setsCookieAndReturnsOk() throws Exception {
        Auth newUser = new Auth();
        newUser.setUsername("newuser@example.com");
        newUser.setPassword("hashed");
        newUser.setRole("USER");
        when(authService.signup(any())).thenReturn(newUser);
        when(jwtService.generateToken(newUser)).thenReturn("jwt-token");
        when(jwtService.generateAuthCookie("jwt-token")).thenReturn(new Cookie("authToken", "jwt-token"));

        String body = "{" +
                "\"email\":\"newuser@example.com\"," +
                "\"password\":\"password123\"," +
                "\"name\":\"New User\"," +
                "\"phone\":\"5551234567\"," +
                "\"userType\":\"JOB_SEEKER\"}";
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("authToken"));
    }

    @Test
    void signup_userAlreadyExists_returnsConflict() throws Exception {
        when(authService.signup(any())).thenThrow(new DataIntegrityViolationException("exists"));
        String body = "{" +
                "\"email\":\"existing@example.com\"," +
                "\"password\":\"password123\"," +
                "\"name\":\"Existing User\"," +
                "\"phone\":\"5551234567\"," +
                "\"userType\":\"JOB_SEEKER\"}";
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void signup_invalidRequest_returnsBadRequest() throws Exception {
        String badBody = "{" +
                "\"email\":\"bademail\"," +
                "\"password\":\"short\"}";
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(badBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signup_genericException_returnsInternalServerError() throws Exception {
        when(authService.signup(any())).thenThrow(new RuntimeException("db error"));
        String body = "{" +
                "\"email\":\"newuser2@example.com\"," +
                "\"password\":\"password123\"," +
                "\"name\":\"New User2\"," +
                "\"phone\":\"5551234567\"," +
                "\"userType\":\"JOB_SEEKER\"}";
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isInternalServerError());
    }
}
