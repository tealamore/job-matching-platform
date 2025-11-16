package com.FairMatch.FairMatch.service;

import com.FairMatch.FairMatch.model.Jobs;
import com.FairMatch.FairMatch.model.User;
import com.FairMatch.FairMatch.model.UserType;
import com.FairMatch.FairMatch.repository.JobsRepository;
import com.FairMatch.FairMatch.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MeService {
  private final UserRepository userRepository;
  private final JobsRepository jobsRepository;

  @Autowired
  public MeService(UserRepository userRepository, JobsRepository jobsRepository) {
    this.userRepository = userRepository;
    this.jobsRepository = jobsRepository;
  }

  public User getMe(String username) {
    return userRepository.findByEmail(username)
      .orElseThrow(() -> new UsernameNotFoundException("User not found"));
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
