package com.FairMatch.FairMatch.controller;

import com.FairMatch.FairMatch.dto.request.CreateJobRequest;
import com.FairMatch.FairMatch.dto.request.InteractJobRequest;
import com.FairMatch.FairMatch.dto.response.JobsResponse;
import com.FairMatch.FairMatch.model.Jobs;
import com.FairMatch.FairMatch.service.JobService;
import com.FairMatch.FairMatch.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
  public JobsResponse getJobById(@PathVariable UUID id) {
    JobsResponse jobs = jobService.getJobById(id);

    System.out.println(jobs);

    return jobs;
  }

  @GetMapping("/feed")
  public List<JobsResponse> getJobFeed(HttpServletRequest request) {
    String username = jwtService.getUsernameFromCookies(request.getCookies());

    return jobService.getJobFeed(username);
  }
}
