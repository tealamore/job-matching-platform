package com.FairMatch.FairMatch.service;

import com.FairMatch.FairMatch.dto.CreateJobRequest;
import com.FairMatch.FairMatch.dto.InteractJobRequest;
import com.FairMatch.FairMatch.dto.JobsResponse;
import com.FairMatch.FairMatch.exception.BadRequestException;
import com.FairMatch.FairMatch.model.*;
import com.FairMatch.FairMatch.repository.JobJobSeekerRepository;
import com.FairMatch.FairMatch.repository.JobTagsRepository;
import com.FairMatch.FairMatch.repository.JobsRepository;
import com.FairMatch.FairMatch.repository.UserRepository;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class JobService {
  private final UserRepository userRepository;
  private final JobsRepository jobsRepository;
  private final JobTagsRepository jobTagsRepository;
  private final JobJobSeekerRepository jobJobSeekerRepository;

  public JobService(JobsRepository jobsRepository,
                    JobTagsRepository jobTagsRepository,
                    UserRepository userRepository, JobJobSeekerRepository jobJobSeekerRepository) {
    this.jobsRepository = jobsRepository;
    this.userRepository = userRepository;
    this.jobTagsRepository = jobTagsRepository;
    this.jobJobSeekerRepository = jobJobSeekerRepository;
  }

  public Jobs createJob(CreateJobRequest createJobRequest, String username) {
    User user = userRepository.findByEmail(username)
      .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    if (!user.getUserType().equals(UserType.BUSINESS)) {
      throw new PermissionDeniedDataAccessException("Only employers can create jobs", new Exception());
    }

    Jobs jobs = Jobs.builder()
      .title(createJobRequest.getTitle())
      .description(createJobRequest.getDescription())
      .salary(createJobRequest.getSalary())
      .user(user)
      .build();

    jobsRepository.save(jobs);

    if (createJobRequest.getTags() == null || createJobRequest.getTags().isEmpty()) {
      return jobs;
    }

    jobTagsRepository.saveAll(createJobRequest.getTags()
      .stream()
      .map(it -> JobTags.builder()
        .jobs(jobs)
        .skillName(it)
        .build())
      .toList());

    return jobs;
  }

  public void interactJob(InteractJobRequest jobRequest, String username) {
    User user = userRepository.findByEmail(username)
      .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    if (!user.getUserType().equals(UserType.JOB_SEEKER)) {
      throw new PermissionDeniedDataAccessException("Only Job seekers can swipe on jobs", new Exception());
    }

    Optional<Jobs> jobs = jobsRepository.findById(jobRequest.getJobId());

    if (jobs.isEmpty()) {
      throw new BadRequestException();
    }

    JobJobSeeker jjs = JobJobSeeker.builder()
      .jobs(jobs.get())
      .user(user)
      .status(jobRequest.getSwipeStatus())
      .appliedDate(Date.from(Instant.now()))
      .build();

    jobJobSeekerRepository.save(jjs);
  }

  public JobsResponse getJobById(UUID id) {
    Jobs jobs = jobsRepository.findById(id)
      .orElseThrow(BadRequestException::new);

    return new JobsResponse(jobs);
  }
}
