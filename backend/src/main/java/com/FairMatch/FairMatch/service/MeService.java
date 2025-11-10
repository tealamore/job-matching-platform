package com.FairMatch.FairMatch.service;

import com.FairMatch.FairMatch.model.JobJobSeeker;
import com.FairMatch.FairMatch.model.Jobs;
import com.FairMatch.FairMatch.model.User;
import com.FairMatch.FairMatch.repository.JobJobSeekerRepository;
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
  private final JobJobSeekerRepository jobJobSeekerRepository;

  @Autowired
  public MeService(UserRepository userRepository, JobsRepository jobsRepository, JobJobSeekerRepository jobJobSeekerRepository) {
    this.userRepository = userRepository;
    this.jobsRepository = jobsRepository;
    this.jobJobSeekerRepository = jobJobSeekerRepository;
  }

  public User getMe(String username) {
    User user = userRepository.findByEmail(username)
      .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    user.setId(null);

    return user;
  }

  public List<Jobs> getMyJobs(String username) {
    User user = userRepository.findByEmail(username)
      .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    return jobsRepository.findAllByUser(user);

//    return jobJobSeekerRepository.findAllByJobsIn(jobs);
  }
}
