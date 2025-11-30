package com.FairMatch.FairMatch.service;

import com.FairMatch.FairMatch.dto.request.CreateJobRequest;
import com.FairMatch.FairMatch.dto.request.InteractJobRequest;
import com.FairMatch.FairMatch.dto.response.JobsResponse;
import com.FairMatch.FairMatch.exception.BadRequestException;
import com.FairMatch.FairMatch.model.*;
import com.FairMatch.FairMatch.repository.*;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

import static java.util.Collections.emptyList;

@Service
public class JobService {
  private final UserRepository userRepository;
  private final JobsRepository jobsRepository;
  private final JobTagsRepository jobTagsRepository;
  private final JobJobSeekerRepository jobJobSeekerRepository;
  private final JobTitlesRepository jobTitlesRepository;
  private final SkillsRepository skillsRepository;

  public JobService(JobsRepository jobsRepository,
                    JobTagsRepository jobTagsRepository,
                    UserRepository userRepository,
                    JobJobSeekerRepository jobJobSeekerRepository,
                    JobTitlesRepository jobTitlesRepository,
                    SkillsRepository skillsRepository) {
    this.jobsRepository = jobsRepository;
    this.userRepository = userRepository;
    this.jobTagsRepository = jobTagsRepository;
    this.jobJobSeekerRepository = jobJobSeekerRepository;
    this.jobTitlesRepository = jobTitlesRepository;
    this.skillsRepository = skillsRepository;
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

  public List<JobsResponse> getFeed(String username) {
    User user = userRepository.findByEmail(username)
      .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    if (!user.getUserType().equals(UserType.JOB_SEEKER)) {
      System.out.println("User is not a job seeker");
      return getApplicantFeed(user);
    }

    List<String> desiredTitles = jobTitlesRepository.findByUserId(user.getId())
      .stream()
      .map(JobTitles::getTitle)
      .toList();

    List<Jobs> jobsByTitle = jobsRepository.findAllByTitleIn(desiredTitles);

    List<String> skills = skillsRepository.findByUserId(user.getId())
      .stream()
      .map(Skills::getSkillName)
      .toList();

    List<Jobs> jobsBySkills = new ArrayList<>(jobTagsRepository.findAllBySkillNameIn(skills)
      .stream()
      .map(JobTags::getJobs)
      .toList());

    List<UUID> alreadyApplied = jobJobSeekerRepository.findAllByUser(user)
      .stream()
      .map(JobJobSeeker::getJobs)
      .map(Jobs::getId)
      .toList();

    Set<Jobs> uniqueJobs = new HashSet<>(jobsByTitle);
    uniqueJobs.addAll(jobsBySkills);
    uniqueJobs.removeIf(it -> alreadyApplied.contains(it.getId()));

    if (uniqueJobs.isEmpty()) {
      uniqueJobs = new HashSet<>(jobsRepository.findAll());
    }

    return uniqueJobs.stream()
      .map(it -> {
        List<JobTags> tags = jobTagsRepository.findAllByJobsId(it.getId());
        return new Pair<>(it, tags);
      })
      .map(it -> new JobsResponse(it.a, it.b))
      .peek(it -> it.setJobJobSeekers(emptyList()))
      .toList();
  }

  private List<JobsResponse> getApplicantFeed(User user) {
    return jobsRepository.findAllByUserWithApplicants(user.getId())
      .stream()
      .map(JobsResponse::new)
      .toList();
  }
}
