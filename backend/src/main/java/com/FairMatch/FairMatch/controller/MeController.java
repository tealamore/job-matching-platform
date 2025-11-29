package com.FairMatch.FairMatch.controller;

import com.FairMatch.FairMatch.dto.request.UpdateDesiredTitlesRequest;
import com.FairMatch.FairMatch.dto.request.UpdateMeRequest;
import com.FairMatch.FairMatch.dto.request.UpdateSkillsRequest;
import com.FairMatch.FairMatch.dto.response.AuthResponse;
import com.FairMatch.FairMatch.dto.response.UserResponse;
import com.FairMatch.FairMatch.model.Auth;
import com.FairMatch.FairMatch.model.Jobs;
import com.FairMatch.FairMatch.model.UserType;
import com.FairMatch.FairMatch.service.JwtService;
import com.FairMatch.FairMatch.service.MeService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/me")
@Validated
public class MeController {
  private final JwtService jwtService;
  private final MeService meService;

  public MeController(JwtService jwtService, MeService meService) {
    this.jwtService = jwtService;
    this.meService = meService;
  }

  @GetMapping
  public UserResponse getMe(HttpServletRequest request) {
    String username = jwtService.getUsernameFromCookies(request.getCookies());

    return meService.getMe(username);
  }

  @GetMapping("/jobs")
  public List<Jobs> getMyJobs(HttpServletRequest request) {
    String username = jwtService.getUsernameFromCookies(request.getCookies());

    return meService.getMyJobs(username);
  }

  @PostMapping("/")
  public ResponseEntity<AuthResponse> updateMe(HttpServletRequest request,
                                               @Valid @RequestBody UpdateMeRequest updateMeRequest,
                                               HttpServletResponse response) {
    String username = jwtService.getUsernameFromCookies(request.getCookies());

    Auth user = meService.updateMe(username, updateMeRequest);

    String token = jwtService.generateToken(user);
    Cookie cookie = jwtService.generateAuthCookie(token);
    response.addCookie(cookie);

    AuthResponse authResponse = AuthResponse.builder()
      .userType(UserType.valueOf(user.getRole()))
      .build();
    return ResponseEntity.ok()
      .body(authResponse);
  }

  @PostMapping("/title")
  public void updateJobTitles(HttpServletRequest request, @RequestBody UpdateDesiredTitlesRequest body) {
    String username = jwtService.getUsernameFromCookies(request.getCookies());

    meService.updateJobTitles(username, body);
  }

  @PostMapping("/skill")
  public void addSkill(HttpServletRequest request, @RequestBody UpdateSkillsRequest body) {
    String username = jwtService.getUsernameFromCookies(request.getCookies());

    meService.updateSkills(username, body);
  }
}
