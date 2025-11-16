package com.FairMatch.FairMatch.service;

import com.FairMatch.FairMatch.dto.CreateJobRequest;
import com.FairMatch.FairMatch.model.JobTags;
import com.FairMatch.FairMatch.model.Jobs;
import com.FairMatch.FairMatch.model.User;
import com.FairMatch.FairMatch.model.UserType;
import com.FairMatch.FairMatch.repository.JobTagsRepository;
import com.FairMatch.FairMatch.repository.JobsRepository;
import com.FairMatch.FairMatch.repository.UserRepository;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class JobService {
  private final UserRepository userRepository;
  private final JobsRepository jobsRepository;
  private final JobTagsRepository jobTagsRepository;

  public JobService(JobsRepository jobsRepository,
                    JobTagsRepository jobTagsRepository,
                    UserRepository userRepository) {
    this.jobsRepository = jobsRepository;
    this.userRepository = userRepository;
    this.jobTagsRepository = jobTagsRepository;
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
}
