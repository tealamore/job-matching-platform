package com.FairMatch.FairMatch.service;

import com.FairMatch.FairMatch.dto.LoginRequest;
import com.FairMatch.FairMatch.dto.SignupRequest;
import com.FairMatch.FairMatch.model.Auth;
import com.FairMatch.FairMatch.model.User;
import com.FairMatch.FairMatch.model.UserType;
import com.FairMatch.FairMatch.repository.AuthRepository;
import com.FairMatch.FairMatch.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceTest {
  private AuthRepository authRepo;
  private BCryptPasswordEncoder passwordEncoder;
  private UserRepository userRepository;
  private AuthService authService;

  @BeforeEach
  void setUp() {
    authRepo = mock(AuthRepository.class);
    passwordEncoder = mock(BCryptPasswordEncoder.class);
    userRepository = mock(UserRepository.class);
    authService = new AuthService(authRepo, userRepository, passwordEncoder);
  }

  @Test
  void signin_successful() {
    LoginRequest request = new LoginRequest();
    request.setEmail("user@example.com");
    request.setPassword("password");

    Auth user = new Auth();
    user.setUsername("user@example.com");
    user.setPassword("hashed");

    when(authRepo.findByUsername("user@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("password", "hashed")).thenReturn(true);

    Auth result = authService.signin(request);

    assertEquals(user, result);
  }

  @Test
  void signin_invalidUsername_throws() {
    LoginRequest request = new LoginRequest();
    request.setEmail("notfound@example.com");
    request.setPassword("password");

    when(authRepo.findByUsername("notfound@example.com")).thenReturn(Optional.empty());

    assertThrows(BadCredentialsException.class, () -> authService.signin(request));
  }

  @Test
  void signin_invalidPassword_throws() {
    LoginRequest request = new LoginRequest();
    request.setEmail("user@example.com");
    request.setPassword("wrong");

    Auth user = new Auth();
    user.setUsername("user@example.com");
    user.setPassword("hashed");

    when(authRepo.findByUsername("user@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

    assertThrows(BadCredentialsException.class, () -> authService.signin(request));
  }

  @Test
  void signup_userAlreadyExists_inUserRepo_throws() {
    String email = "user@example.com";
    when(userRepository.existsByEmail(email)).thenReturn(true);
    when(authRepo.existsByUsername(email)).thenReturn(false);

    SignupRequest signupRequest = SignupRequest.builder()
      .name("Test User")
      .email(email)
      .phone("1234567890")
      .password("password")
      .userType(UserType.JOB_SEEKER)
      .build();

    assertThrows(DataIntegrityViolationException.class, () -> {
      authService.signup(signupRequest);
    });
  }

  @Test
  void signup_userAlreadyExists_inAuthRepo_throws() {
    String email = "user@example.com";
    when(userRepository.existsByEmail(email)).thenReturn(false);
    when(authRepo.existsByUsername(email)).thenReturn(true);

    SignupRequest signupRequest = SignupRequest.builder()
      .name("Test User")
      .email(email)
      .phone("1234567890")
      .password("password")
      .userType(UserType.JOB_SEEKER)
      .build();

    assertThrows(DataIntegrityViolationException.class, () -> {
      authService.signup(signupRequest);
    });
  }

  @Test
  void signup_successful_createsUserAndAuth() {
    String email = "newuser@example.com";
    String password = "password123";
    String hashedPassword = "hashedPassword123";
    String name = "New User";
    String phone = "5551234567";
    UserType userType = UserType.JOB_SEEKER;
    UUID userId = UUID.randomUUID();

    SignupRequest signupRequest = SignupRequest.builder()
      .name(name)
      .email(email)
      .phone(phone)
      .password(password)
      .userType(userType)
      .build();

    when(userRepository.existsByEmail(email)).thenReturn(false);
    when(authRepo.existsByUsername(email)).thenReturn(false);
    when(passwordEncoder.encode(password)).thenReturn(hashedPassword);

    User savedUser = User.builder()
        .id(userId)
        .name(name)
        .email(email)
        .phone(phone)
        .userType(userType)
        .build();

    when(userRepository.saveAndFlush(any())).thenReturn(savedUser);

    Auth savedAuth = Auth.builder()
        .username(email)
        .password(hashedPassword)
        .build();

    when(authRepo.saveAndFlush(any())).thenReturn(savedAuth);

    Auth result = authService.signup(signupRequest);

    assertEquals(savedAuth.getUsername(), result.getUsername());
    assertEquals(savedAuth.getPassword(), result.getPassword());
  }
}
