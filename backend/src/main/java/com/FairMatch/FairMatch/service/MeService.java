package com.FairMatch.FairMatch.service;

import com.FairMatch.FairMatch.dto.request.UpdateDesiredTitlesRequest;
import com.FairMatch.FairMatch.dto.request.UpdateMeRequest;
import com.FairMatch.FairMatch.dto.request.UpdateSkillsRequest;
import com.FairMatch.FairMatch.dto.response.JobsResponse;
import com.FairMatch.FairMatch.dto.response.UserResponse;
import com.FairMatch.FairMatch.exception.BadRequestException;
import com.FairMatch.FairMatch.model.*;
import com.FairMatch.FairMatch.repository.*;
import jakarta.transaction.Transactional;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MeService {
  private final UserRepository userRepository;
  private final JobsRepository jobsRepository;
  private final JobTitlesRepository jobTitlesRepository;
  private final SkillsRepository skillsRepository;
  private final AuthRepository authRepository;
  private final BCryptPasswordEncoder passwordEncoder;
  private final JobTagsRepository jobTagsRepository;

  @Autowired
  public MeService(UserRepository userRepository,
                   JobsRepository jobsRepository,
                   JobTitlesRepository jobTitlesRepository,
                   SkillsRepository skillsRepository,
                   AuthRepository authRepository,
                   BCryptPasswordEncoder passwordEncoder, JobTagsRepository jobTagsRepository) {
    this.userRepository = userRepository;
    this.jobsRepository = jobsRepository;
    this.jobTitlesRepository = jobTitlesRepository;
    this.skillsRepository = skillsRepository;
    this.authRepository = authRepository;
    this.passwordEncoder = passwordEncoder;
    this.jobTagsRepository = jobTagsRepository;
  }

  public UserResponse getMe(String username) {
    User user =  userRepository.findByEmail(username)
      .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    if (user.getUserType() == UserType.JOB_SEEKER) {
      List<JobTitles> desiredTitles = jobTitlesRepository.findByUserId(user.getId());
      List<Skills> skills = skillsRepository.findByUserId(user.getId());
      return new UserResponse(user, desiredTitles, skills);
    }

    return new UserResponse(user);
  }

  public List<JobsResponse> getMyJobs(String username) {
    User user = userRepository.findByEmail(username)
      .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    if (user.getUserType() == UserType.JOB_SEEKER) {
      return List.of();
    }

    return jobsRepository.findAllByPosterWithApplicants(user.getId())
      .stream()
      .map(it -> {
        List<JobTags> tags = jobTagsRepository.findAllByJobsId(it.getId());
        return new Pair<>(it, tags);
      })
      .map(it -> new JobsResponse(it.a, it.b))
      .toList();
  }

  public UserResponse getById(UUID id) {
    User user = userRepository.findById(id)
     .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    if (user.getUserType() == UserType.JOB_SEEKER) {
      throw new BadRequestException();
    }

    List<Jobs> jobs = jobsRepository.findAllByUser(user);

    return new UserResponse(user, jobs);
  }

  @Transactional
  public Auth updateMe(String username, UpdateMeRequest updateMeRequest) {
    User user = userRepository.findByEmail(username)
      .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    if (updateMeRequest.getName() != null && !updateMeRequest.getName().isBlank())
      user.setName(updateMeRequest.getName());
    if (updateMeRequest.getPhone() != null && !updateMeRequest.getPhone().isBlank())
      user.setPhone(updateMeRequest.getPhone());
    if (updateMeRequest.getEmail() != null && !updateMeRequest.getEmail().isBlank())
      user.setEmail(updateMeRequest.getEmail());

    userRepository.save(user);

    Auth auth = authRepository.findByUsername(username)
      .orElseThrow(() -> new UsernameNotFoundException("Auth not found"));

    if (updateMeRequest.getEmail() != null && !updateMeRequest.getEmail().isBlank())
      auth.setUsername(updateMeRequest.getEmail());
    if (updateMeRequest.getPassword() != null && !updateMeRequest.getPassword().isBlank())
      auth.setPassword(passwordEncoder.encode(updateMeRequest.getPassword()));

    authRepository.save(auth);

    return auth;
  }

  @Transactional
  public void updateJobTitles(String username, UpdateDesiredTitlesRequest body) {
    User user = userRepository.findByEmail(username)
      .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    jobTitlesRepository.deleteAllByUserId(user.getId());

    List<JobTitles> toSave = body.getDesiredTitles()
      .stream()
      .map(it -> JobTitles.builder()
        .user(user)
        .title(it)
        .build()
      ).toList();

    jobTitlesRepository.saveAll(toSave);
  }

  @Transactional
  public void updateSkills(String username, UpdateSkillsRequest body) {
    User user = userRepository.findByEmail(username)
      .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    skillsRepository.deleteAllByUserId(user.getId());

    List<Skills> toSave = body.getSkills()
      .stream()
      .map(it -> Skills.builder()
        .user(user)
        .skillName(it)
        .build()
      ).toList();

    skillsRepository.saveAll(toSave);
  }
}
