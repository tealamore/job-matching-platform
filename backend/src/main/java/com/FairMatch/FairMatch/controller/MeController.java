package com.FairMatch.FairMatch.controller;

import com.FairMatch.FairMatch.model.JobJobSeeker;
import com.FairMatch.FairMatch.model.Jobs;
import com.FairMatch.FairMatch.model.User;
import com.FairMatch.FairMatch.service.JwtService;
import com.FairMatch.FairMatch.service.MeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
  public User getMe(HttpServletRequest request, HttpServletResponse response) {
    String username = jwtService.getUsernameFromCookies(request.getCookies());

    return meService.getMe(username);
  }

  @GetMapping
  public List<Jobs> getMyJobs(HttpServletRequest request) {
    String username = jwtService.getUsernameFromCookies(request.getCookies());

    return meService.getMyJobs(username);
  }
}
