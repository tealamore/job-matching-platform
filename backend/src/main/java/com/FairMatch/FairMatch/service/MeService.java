package com.FairMatch.FairMatch.service;

import com.FairMatch.FairMatch.dto.UserDto;
import com.FairMatch.FairMatch.model.*;
import com.FairMatch.FairMatch.repository.JobTitlesRepository;
import com.FairMatch.FairMatch.repository.JobsRepository;
import com.FairMatch.FairMatch.repository.SkillsRepository;
import com.FairMatch.FairMatch.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MeService {
  private final UserRepository userRepository;
  private final JobsRepository jobsRepository;
  private final JobTitlesRepository jobTitlesRepository;
  private final SkillsRepository skillsRepository;

  @Autowired
  public MeService(UserRepository userRepository, JobsRepository jobsRepository, JobTitlesRepository jobTitlesRepository, SkillsRepository skillsRepository) {
    this.userRepository = userRepository;
    this.jobsRepository = jobsRepository;
    this.jobTitlesRepository = jobTitlesRepository;
    this.skillsRepository = skillsRepository;
  }

  public UserDto getMe(String username) {
    User user =  userRepository.findByEmail(username)
      .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    if (user.getUserType() == UserType.JOB_SEEKER) {
      List<JobTitles> desiredTitles = jobTitlesRepository.findByUserId(user.getId());
      List<Skills> skills = skillsRepository.findByUserId(user.getId());
      return new UserDto(user, desiredTitles, skills);
    }

    return new UserDto(user);
  }

  public List<Jobs> getMyJobs(String username) {
    User user = userRepository.findByEmail(username)
      .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    if (user.getUserType() == UserType.JOB_SEEKER) {
      return jobsRepository.findAllByJobJobSeekers_User_Id(user.getId());
    }

    return jobsRepository.findAllByPosterWithApplicants(user.getId());
  }
}
