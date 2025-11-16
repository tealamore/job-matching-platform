package com.FairMatch.FairMatch.controller;

import com.FairMatch.FairMatch.dto.UserResponse;
import com.FairMatch.FairMatch.service.MeService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/business")
@Validated
public class BusinessController {
  private final MeService meService;

  public BusinessController(MeService meService) {
    this.meService = meService;
  }

  @GetMapping("/{id}")
  public UserResponse getBusinessById(@PathVariable UUID id) {
    return meService.getById(id);
  }
}
