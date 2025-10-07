package com.FairMatch.FairMatch.service;

import com.FairMatch.FairMatch.dto.LoginRequest;
import com.FairMatch.FairMatch.dto.SignupRequest;
import com.FairMatch.FairMatch.model.Auth;
import com.FairMatch.FairMatch.model.User;
import com.FairMatch.FairMatch.repository.AuthRepository;
import com.FairMatch.FairMatch.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
  private final AuthRepository authRepo;
  private final UserRepository userRepo;
  private final BCryptPasswordEncoder passwordEncoder;

  public AuthService(AuthRepository authRepo, UserRepository userRepo, BCryptPasswordEncoder passwordEncoder) {
    this.authRepo = authRepo;
    this.userRepo = userRepo;
    this.passwordEncoder = passwordEncoder;
  }

  public Auth signin(LoginRequest request) throws BadCredentialsException {
    Optional<Auth> userOpt = authRepo.findByUsername(request.getEmail());

    Auth user = userOpt
      .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new BadCredentialsException("Invalid username or password");
    }

    return user;
  }

  public Auth signup(SignupRequest request) throws DataIntegrityViolationException {
    if (userRepo.existsByEmail(request.getEmail()) ||
        authRepo.existsByUsername(request.getEmail())) {
      throw new DataIntegrityViolationException("Account already exists");
    }

    User user = User.builder()
      .name(request.getName())
      .email(request.getEmail())
      .phone(request.getPhone())
      .userType(request.getUserType())
      .build();

    user = userRepo.saveAndFlush(user);

    Auth auth = Auth.builder()
      .user(user)
      .username(request.getEmail())
      .password(passwordEncoder.encode(request.getPassword()))
      .role("USER")
      .build();

    return authRepo.saveAndFlush(auth);
  }
}
