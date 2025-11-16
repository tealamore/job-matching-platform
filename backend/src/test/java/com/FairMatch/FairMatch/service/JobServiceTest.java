package com.FairMatch.FairMatch.service;

import com.FairMatch.FairMatch.dto.CreateJobRequest;
import com.FairMatch.FairMatch.dto.InteractJobRequest;
import com.FairMatch.FairMatch.exception.BadRequestException;
import com.FairMatch.FairMatch.model.Jobs;
import com.FairMatch.FairMatch.model.SwipeStatus;
import com.FairMatch.FairMatch.model.User;
import com.FairMatch.FairMatch.model.UserType;
import com.FairMatch.FairMatch.repository.JobJobSeekerRepository;
import com.FairMatch.FairMatch.repository.JobTagsRepository;
import com.FairMatch.FairMatch.repository.JobsRepository;
import com.FairMatch.FairMatch.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static java.util.List.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class JobServiceTest {
  private UserRepository userRepository;
  private JobsRepository jobsRepository;
  private JobTagsRepository jobTagsRepository;
  private JobJobSeekerRepository jobJobSeekerRepository;

  private JobService jobService;

  private User mockUser;

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    jobsRepository = mock(JobsRepository.class);
    jobTagsRepository = mock(JobTagsRepository.class);
    jobJobSeekerRepository = mock(JobJobSeekerRepository.class);

    jobService = new JobService(jobsRepository, jobTagsRepository, userRepository, jobJobSeekerRepository);

    mockUser = User.builder()
      .id(UUID.randomUUID())
      .userType(UserType.BUSINESS)
      .build();
  }

  @Test
  void testCreateJob_throws_ifUserNotFound() {
    String email = "email";
    when(userRepository.findByEmail(email))
      .thenReturn(Optional.empty());

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
      .thenReturn(Optional.empty());

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
}
