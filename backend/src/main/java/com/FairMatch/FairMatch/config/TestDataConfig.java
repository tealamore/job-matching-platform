package com.FairMatch.FairMatch.config;

import com.FairMatch.FairMatch.model.Auth;
import com.FairMatch.FairMatch.model.User;
import com.FairMatch.FairMatch.model.UserType;
import com.FairMatch.FairMatch.repository.AuthRepository;
import com.FairMatch.FairMatch.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@Profile("!prod")
public class TestDataConfig {
    @Bean
    @Transactional
    CommandLineRunner insertTestData(AuthRepository authRepo, UserRepository userRepo, BCryptPasswordEncoder encoder) {
        return args -> {
            String email = "user1@example.com";
            if (!userRepo.existsByEmail(email) && !authRepo.existsByUsername(email)) {
                User user = User.builder()
                    .name("Test User")
                    .email(email)
                    .phone("1234567890")
                    .userType(UserType.JOB_SEEKER)
                    .build();
                user = userRepo.saveAndFlush(user);

                Auth auth = Auth.builder()
                    .user(user)
                    .username(email)
                    .password(encoder.encode("password1"))
                    .role("USER")
                    .build();
                authRepo.saveAndFlush(auth);
            }
        };
    }
}
