package com.FairMatch.FairMatch.service;

import com.FairMatch.FairMatch.model.User;
import com.FairMatch.FairMatch.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MeService {
  private final UserRepository userRepository;

  @Autowired
  public MeService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public User getMe(String username) {
    User user = userRepository.findByEmail(username)
      .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    user.setId(null);

    return user;
  }
}
