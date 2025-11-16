package com.FairMatch.FairMatch.service;

import com.FairMatch.FairMatch.model.Jobs;
import com.FairMatch.FairMatch.model.User;
import com.FairMatch.FairMatch.model.UserType;
import com.FairMatch.FairMatch.repository.JobJobSeekerRepository;
import com.FairMatch.FairMatch.repository.JobsRepository;
import com.FairMatch.FairMatch.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MeServiceTest {
  private User mockUser;
  private UserRepository userRepository;
  private JobsRepository jobsRepository;
  private JobJobSeekerRepository jobJobSeekerRepository;

  private MeService meService;

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    jobsRepository = mock(JobsRepository.class);
    meService = new MeService(userRepository, jobsRepository);

    mockUser = User.builder()
      .id(UUID.randomUUID())
      .userType(UserType.BUSINESS)
      .build();
  }

  @Test
  void testGetMe() {
    String username = "username";
    when(userRepository.findByEmail(username))
      .thenReturn(Optional.of(mockUser));

    User user = meService.getMe(username);

    assertEquals(mockUser, user);
  }

  @Test
  void testGetMe_throwsWhenUserNotFound() {
    String username = "username";
    when(userRepository.findByEmail(username))
      .thenReturn(empty());

    assertThrows(UsernameNotFoundException.class, () -> meService.getMe(username));
  }

  @Test
  void testGetMyJobs_employer() {
    String username = "username";
    when(userRepository.findByEmail(username))
      .thenReturn(Optional.of(mockUser));
    when(jobsRepository.findAllByPosterWithApplicants(mockUser.getId()))
      .thenReturn(emptyList());

    List<Jobs> response = meService.getMyJobs(username);

    assertEquals(emptyList(), response);
    verify(jobsRepository, times(0)).findAllByJobJobSeekers_User_Id(mockUser.getId());
  }

  @Test
  void testGetMyJobs_applicant() {
    String username = "username";
    mockUser.setUserType(UserType.JOB_SEEKER);
    when(userRepository.findByEmail(username))
      .thenReturn(Optional.of(mockUser));
    when(jobsRepository.findAllByJobJobSeekers_User_Id(mockUser.getId()))
      .thenReturn(emptyList());

    List<Jobs> response = meService.getMyJobs(username);

    assertEquals(emptyList(), response);
    verify(jobsRepository, times(0)).findAllByPosterWithApplicants(mockUser.getId());
  }

  @Test
  void testGetMyJobs_userNotFound() {
    String username = "username";
    when(userRepository.findByEmail(username))
      .thenReturn(Optional.empty());

    assertThrows(UsernameNotFoundException.class,
      () -> meService.getMyJobs(username));

    verifyNoInteractions(jobsRepository);
  }
}
