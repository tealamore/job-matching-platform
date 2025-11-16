package com.FairMatch.FairMatch.service;

import com.FairMatch.FairMatch.dto.UserResponse;
import com.FairMatch.FairMatch.model.*;
import com.FairMatch.FairMatch.repository.*;
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
  private JobTitlesRepository jobTitlesRepository;
  private SkillsRepository skillsRepository;

  private MeService meService;

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    jobsRepository = mock(JobsRepository.class);
    jobTitlesRepository = mock(JobTitlesRepository.class);
    skillsRepository = mock(SkillsRepository.class);
    meService = new MeService(userRepository, jobsRepository, jobTitlesRepository, skillsRepository);

    mockUser = User.builder()
      .id(UUID.randomUUID())
      .userType(UserType.BUSINESS)
      .build();
  }

  @Test
  void testGetMe_doesntGet_SkillsOrTitles_forBusinessUser() {
    String username = "username";

    when(userRepository.findByEmail(username))
      .thenReturn(Optional.of(mockUser));

    UserResponse user = meService.getMe(username);

    assertEquals(new UserResponse(mockUser), user);

    verifyNoInteractions(skillsRepository, jobTitlesRepository);
  }

  @Test
  void testGetMe_gets_SkillsOrTitles_forApplicantUser() {
    String username = "username";
    mockUser.setUserType(UserType.JOB_SEEKER);
    Skills skill = Skills.builder()
      .id(UUID.randomUUID())
      .skillName("Java")
      .build();

    JobTitles jobTitle = JobTitles.builder()
      .id(UUID.randomUUID())
      .title("Software Engineer")
      .build();

    when(userRepository.findByEmail(username))
      .thenReturn(Optional.of(mockUser));
    when(skillsRepository.findByUserId(mockUser.getId()))
      .thenReturn(List.of(skill));
    when(jobTitlesRepository.findByUserId(mockUser.getId()))
      .thenReturn(List.of(jobTitle));

    UserResponse user = meService.getMe(username);

    assertEquals(new UserResponse(mockUser, List.of(jobTitle), List.of(skill)), user);
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
