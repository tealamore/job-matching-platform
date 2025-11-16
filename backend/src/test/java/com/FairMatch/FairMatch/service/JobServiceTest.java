package com.FairMatch.FairMatch.service;

import com.FairMatch.FairMatch.dto.CreateJobRequest;
import com.FairMatch.FairMatch.dto.InteractJobRequest;
import com.FairMatch.FairMatch.dto.JobsResponse;
import com.FairMatch.FairMatch.exception.BadRequestException;
import com.FairMatch.FairMatch.model.*;
import com.FairMatch.FairMatch.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.List.*;
import static java.util.Optional.empty;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class JobServiceTest {
  private UserRepository userRepository;
  private JobsRepository jobsRepository;
  private JobTagsRepository jobTagsRepository;
  private JobJobSeekerRepository jobJobSeekerRepository;
  private SkillsRepository skillsRepository;
  private JobTitlesRepository jobTitlesRepository;

  private JobService jobService;

  private User mockUser;

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    jobsRepository = mock(JobsRepository.class);
    jobTagsRepository = mock(JobTagsRepository.class);
    jobJobSeekerRepository = mock(JobJobSeekerRepository.class);
    skillsRepository = mock(SkillsRepository.class);
    jobTitlesRepository = mock(JobTitlesRepository.class);


    jobService = new JobService(jobsRepository, jobTagsRepository,
      userRepository, jobJobSeekerRepository,
      jobTitlesRepository, skillsRepository);

    mockUser = User.builder()
      .id(UUID.randomUUID())
      .userType(UserType.BUSINESS)
      .build();
  }

  @Test
  void testCreateJob_throws_ifUserNotFound() {
    String email = "email";
    when(userRepository.findByEmail(email))
      .thenReturn(empty());

    CreateJobRequest createJobRequest = new CreateJobRequest();

    assertThrows(UsernameNotFoundException.class, () -> jobService.createJob(createJobRequest, email));

    verifyNoInteractions(jobsRepository, jobTagsRepository);
  }

  @Test
  void testCreateJob_successful() {
    String email = "email";
    when(userRepository.findByEmail(email))
      .thenReturn(Optional.of(mockUser));

    CreateJobRequest createJobRequest = new CreateJobRequest();
    createJobRequest.setTitle("Job Title");
    createJobRequest.setDescription("Job Description");
    createJobRequest.setSalary(50000.0);
    createJobRequest.setTags(of("Java", "Spring"));

    jobService.createJob(createJobRequest, email);

    verify(jobsRepository, times(1)).save(any());
    verify(jobTagsRepository, times(1)).saveAll(anyList());
  }

  @Test
  void testCreateJob_throws_ifNotBusinessUser() {
    String email = "email";
    mockUser.setUserType(UserType.JOB_SEEKER);
    when(userRepository.findByEmail(email))
      .thenReturn(Optional.of(mockUser));

    CreateJobRequest createJobRequest = new CreateJobRequest();
    createJobRequest.setTitle("Job Title");
    createJobRequest.setDescription("Job Description");
    createJobRequest.setSalary(50000.0);
    createJobRequest.setTags(of("Java", "Spring"));

    assertThrows(PermissionDeniedDataAccessException.class, () -> jobService.createJob(createJobRequest, email));

    verifyNoInteractions(jobsRepository, jobTagsRepository);
  }

  @Test
  void testInteractJob_throws_ifUserNotFound() {
    String email = "email";
    when(userRepository.findByEmail(email))
      .thenReturn(empty());

    InteractJobRequest interactJobRequest = new InteractJobRequest();

    assertThrows(UsernameNotFoundException.class, () -> jobService.interactJob(interactJobRequest, email));

    verifyNoInteractions(jobsRepository, jobJobSeekerRepository);
  }

  @Test
  void testInteractJob_successful() {
    String email = "email";
    mockUser.setUserType(UserType.JOB_SEEKER);
    when(userRepository.findByEmail(email))
      .thenReturn(Optional.of(mockUser));

    InteractJobRequest interactJobRequest = new InteractJobRequest();
    interactJobRequest.setJobId(UUID.randomUUID());
    interactJobRequest.setSwipeStatus(SwipeStatus.LIKE);

    when(jobsRepository.findById(interactJobRequest.getJobId()))
      .thenReturn(Optional.of(new Jobs()));

    jobService.interactJob(interactJobRequest, email);

    verify(jobsRepository, times(1)).findById(any());
    verify(jobJobSeekerRepository, times(1)).save(any());
  }

  @Test
  void testInteractJob_throws_ifJobId_notFound() {
    String email = "email";
    mockUser.setUserType(UserType.JOB_SEEKER);
    when(userRepository.findByEmail(email))
      .thenReturn(Optional.of(mockUser));

    InteractJobRequest interactJobRequest = new InteractJobRequest();
    interactJobRequest.setJobId(UUID.randomUUID());
    interactJobRequest.setSwipeStatus(SwipeStatus.LIKE);

    assertThrows(BadRequestException.class, () -> jobService.interactJob(interactJobRequest, email));

    verify(jobsRepository, times(1)).findById(any());
  }

  @Test
  void testInteractJob_throws_ifNotApplicantUser() {
    String email = "email";
    mockUser.setUserType(UserType.BUSINESS);

    when(userRepository.findByEmail(email))
      .thenReturn(Optional.of(mockUser));

    InteractJobRequest interactJobRequest = new InteractJobRequest();

    assertThrows(PermissionDeniedDataAccessException.class, () -> jobService.interactJob(interactJobRequest, email));

    verifyNoInteractions(jobsRepository, jobJobSeekerRepository);
  }

  @Test
  void testGetJobById_throws_ifJobNotFound() {
    assertThrows(BadRequestException.class, () -> jobService.getJobById(UUID.randomUUID()));
  }

  @Test
  void testGetJobById_returns_job() {
    when(jobsRepository.findById(any()))
      .thenReturn(Optional.of(new Jobs()));

    jobService.getJobById(UUID.randomUUID());
  }

  @Test
  void testGetJobFeed_HappyPath() {
    mockUser.setUserType(UserType.JOB_SEEKER);

    JobTitles jt = new JobTitles();
    jt.setTitle("Developer");
    jt.setUser(mockUser);
    when(userRepository.findByEmail("seeker@example.com")).thenReturn(Optional.of(mockUser));
    when(jobTitlesRepository.findByUserId(mockUser.getId())).thenReturn(List.of(jt));

    Skills skill = new Skills();
    skill.setSkillName("Java");
    skill.setUser(mockUser);
    when(skillsRepository.findByUserId(mockUser.getId())).thenReturn(List.of(skill));

    Jobs job1 = new Jobs();
    job1.setId(UUID.randomUUID());
    Jobs job2 = new Jobs();
    job2.setId(UUID.randomUUID());

    JobTags tag1 = new JobTags();
    tag1.setSkillName("Java");
    tag1.setJobs(job1);
    JobTags tag2 = new JobTags();
    tag2.setSkillName("Java");
    tag2.setJobs(job2);

    when(jobTagsRepository.findAllBySkillNameIn(List.of("Java"))).thenReturn(List.of(tag1, tag2));
    when(jobsRepository.findAllByTitleIn(List.of("Developer"))).thenReturn(List.of(job1));
    when(jobJobSeekerRepository.findAllByUser(mockUser)).thenReturn(emptyList());

    List<JobsResponse> result = jobService.getJobFeed("seeker@example.com");

    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(r -> r.getId().equals(job1.getId())));
    assertTrue(result.stream().anyMatch(r -> r.getId().equals(job2.getId())));
  }

  @Test
  void testGetJobFeed_UserNotFound() {
    when(userRepository.findByEmail("unknown@example.com")).thenReturn(empty());

    assertThrows(UsernameNotFoundException.class,
      () -> jobService.getJobFeed("unknown@example.com"));
  }

  @Test
  void testGetJobFeed_UserNotJobSeeker() {
    mockUser.setUserType(UserType.BUSINESS);

    when(userRepository.findByEmail("employer@example.com")).thenReturn(Optional.of(mockUser));

    assertThrows(BadRequestException.class,
      () -> jobService.getJobFeed("employer@example.com"));
  }

  @Test
  void testGetJobFeed_AlreadyAppliedExcluded() {
    mockUser.setUserType(UserType.JOB_SEEKER);

    when(userRepository.findByEmail("seeker@example.com")).thenReturn(Optional.of(mockUser));

    JobTitles jt = new JobTitles();
    jt.setTitle("Developer");
    jt.setUser(mockUser);
    when(jobTitlesRepository.findByUserId(mockUser.getId())).thenReturn(List.of(jt));

    Skills skill = new Skills();
    skill.setSkillName("Java");
    skill.setUser(mockUser);
    when(skillsRepository.findByUserId(mockUser.getId())).thenReturn(List.of(skill));

    Jobs job1 = new Jobs();
    job1.setId(UUID.randomUUID());
    Jobs job2 = new Jobs();
    job2.setId(UUID.randomUUID());

    JobTags tag1 = new JobTags();
    tag1.setSkillName("Java");
    tag1.setJobs(job1);
    when(jobTagsRepository.findAllBySkillNameIn(List.of("Java"))).thenReturn(List.of(tag1));
    when(jobsRepository.findAllByTitleIn(List.of("Developer"))).thenReturn(List.of(job2));

    JobJobSeeker applied = new JobJobSeeker();
    applied.setJobs(job1);
    when(jobJobSeekerRepository.findAllByUser(mockUser)).thenReturn(List.of(applied));

    List<JobsResponse> result = jobService.getJobFeed("seeker@example.com");
    assertEquals(1, result.size());
    assertTrue(result.stream().anyMatch(r -> r.getId().equals(job2.getId())));
  }
}
