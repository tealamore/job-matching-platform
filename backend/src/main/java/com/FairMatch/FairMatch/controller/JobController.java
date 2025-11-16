package com.FairMatch.FairMatch.controller;

import com.FairMatch.FairMatch.dto.CreateJobRequest;
import com.FairMatch.FairMatch.dto.InteractJobRequest;
import com.FairMatch.FairMatch.dto.JobsDTO;
import com.FairMatch.FairMatch.model.Jobs;
import com.FairMatch.FairMatch.service.JobService;
import com.FairMatch.FairMatch.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/jobs")
@Validated
public class JobController {
  private final JwtService jwtService;
  private final JobService jobService;

  public JobController(JwtService jwtService, JobService jobService) {
    this.jwtService = jwtService;
    this.jobService = jobService;
  }

  @PostMapping
  public Jobs createJob(@Valid @RequestBody CreateJobRequest jobRequest, HttpServletRequest request) {
    String username = jwtService.getUsernameFromCookies(request.getCookies());

    return jobService.createJob(jobRequest, username);
  }

  @PostMapping("/interact")
  public void interactJob(@Valid @RequestBody InteractJobRequest jobRequest, HttpServletRequest request) {
    String username = jwtService.getUsernameFromCookies(request.getCookies());

    jobService.interactJob(jobRequest, username);
  }

  @GetMapping("/{id}")
  public JobsDTO getJobById(@PathVariable UUID id) {
    JobsDTO jobs = jobService.getJobById(id);

    System.out.println(jobs);

    return jobs;
  }
}
