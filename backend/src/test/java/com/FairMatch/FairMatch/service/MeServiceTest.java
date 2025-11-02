package com.FairMatch.FairMatch.service;

import com.FairMatch.FairMatch.model.User;
import com.FairMatch.FairMatch.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MeServiceTest {
  private User mockUser;
  private UserRepository userRepository;
  private MeService meService;

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    meService = new MeService(userRepository);

    mockUser = User.builder()
      .id(UUID.randomUUID())
      .build();
  }

  @Test
  void testGetMe() {
    String username = "username";
    when(userRepository.findByEmail(username))
      .thenReturn(Optional.of(mockUser));

    User user = meService.getMe(username);

    assertEquals(mockUser, user);
    assertNull(user.getId());
  }

  @Test
  void testGetMe_throwsWhenUserNotFound() {
    String username = "username";
    when(userRepository.findByEmail(username))
      .thenReturn(Optional.empty());

    assertThrows(UsernameNotFoundException.class, () -> meService.getMe(username));
  }

}
