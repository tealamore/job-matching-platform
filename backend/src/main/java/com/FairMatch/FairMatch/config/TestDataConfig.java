package com.FairMatch.FairMatch.config;

import com.FairMatch.FairMatch.model.*;
import com.FairMatch.FairMatch.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;
import java.util.Date;

@Configuration
@Profile("!prod")
public class TestDataConfig {
    @Bean
    @Transactional
    CommandLineRunner insertTestData(AuthRepository authRepo,
                                     UserRepository userRepo,
                                     BCryptPasswordEncoder encoder,
                                     JobsRepository jobsRepository,
                                     JobJobSeekerRepository jobJobSeekerRepository,
                                     JobTitlesRepository jobTitlesRepository,
                                     SkillsRepository skillsRepository,
                                     JobTagsRepository jobTagsRepository
                                     ) {
        return args -> {
          String employerEmail = "user1@example.com";
          jobTagsRepository.deleteAll();
          skillsRepository.deleteAll();
          jobTitlesRepository.deleteAll();
          jobJobSeekerRepository.deleteAll();
          jobsRepository.deleteAll();
          authRepo.deleteAll();
          userRepo.deleteAll();

          if (!userRepo.existsByEmail(employerEmail) && !authRepo.existsByUsername(employerEmail)) {
              User user = User.builder()
                  .name("Test User")
                  .email(employerEmail)
                  .phone("1234567890")
                  .userType(UserType.BUSINESS)
                  .build();
              user = userRepo.saveAndFlush(user);

              Auth auth = Auth.builder()
                  .user(user)
                  .username(employerEmail)
                  .password(encoder.encode("password1"))
                  .role("BUSINESS")
                  .build();
              authRepo.saveAndFlush(auth);
          }

          String[] applicantNames = {"Alice Johnson", "Bob Smith", "Charlie Davis", "Diana Martinez", "Ethan Brown"};
          String[] applicantEmails = {"alice@example.com", "bob@example.com", "charlie@example.com", "diana@example.com", "ethan@example.com"};

          for (int i = 0; i < applicantNames.length; i++) {
            String aplicantEmail = applicantEmails[i];
            if (!userRepo.existsByEmail(aplicantEmail) && !authRepo.existsByUsername(aplicantEmail)) {
              User user = User.builder()
                .name(applicantNames[i])
                .email(aplicantEmail)
                .phone("1234567890")
                .userType(UserType.JOB_SEEKER)
                .build();
              user = userRepo.saveAndFlush(user);

              Auth auth = Auth.builder()
                .user(user)
                .username(aplicantEmail)
                .password(encoder.encode("password1"))
                .role("JOB_SEEKER")
                .build();
              authRepo.saveAndFlush(auth);
            }
          }

          if (jobsRepository.findAllByUser(userRepo.findByEmail(employerEmail).orElseThrow()).isEmpty()) {
            Jobs jobs = Jobs.builder()
              .description("Test Job")
              .title("Software Engineer")
              .salary(75000.0)
              .user(userRepo.findByEmail(employerEmail).orElseThrow())
              .build();
            jobsRepository.saveAndFlush(jobs);
          }

          if (jobJobSeekerRepository.findAll().isEmpty()) {
            Jobs jobs = jobsRepository.findAllByUser(userRepo.findByEmail(employerEmail).orElseThrow()).get(0);

            for (String applicant_email : applicantEmails) {
              JobJobSeeker jobJobSeeker = JobJobSeeker.builder()
                .appliedDate(Date.from(Instant.now()))
                .jobs(jobs)
                .status(SwipeStatus.LIKE)
                .user(userRepo.findByEmail(applicant_email).orElseThrow())
                .build();

              jobJobSeekerRepository.saveAndFlush(jobJobSeeker);
            }
          }

          if (jobTitlesRepository.findAll().isEmpty()) {
            User applicant = userRepo.findByEmail(applicantEmails[0]).orElseThrow();

            JobTitles jobTitle1 = JobTitles.builder()
              .title("Software Engineer")
              .user(applicant)
              .build();
            JobTitles jobTitle2 = JobTitles.builder()
              .title("Data Scientist")
              .user(applicant)
              .build();
            jobTitlesRepository.saveAndFlush(jobTitle1);
            jobTitlesRepository.saveAndFlush(jobTitle2);
          }

          if (skillsRepository.findAll().isEmpty()) {
            User applicant = userRepo.findByEmail(applicantEmails[0]).orElseThrow();

            Skills skill1 = Skills.builder()
              .skillName("Java")
              .user(applicant)
              .build();
            Skills skill2 = Skills.builder()
              .skillName("Python")
              .user(applicant)
              .build();
            skillsRepository.saveAndFlush(skill1);
            skillsRepository.saveAndFlush(skill2);
          }
        };
    }
}
