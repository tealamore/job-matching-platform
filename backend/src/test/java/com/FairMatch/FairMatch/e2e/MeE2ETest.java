package com.FairMatch.FairMatch.e2e;

import com.FairMatch.FairMatch.model.Auth;
import com.FairMatch.FairMatch.model.User;
import com.FairMatch.FairMatch.model.UserType;
import com.FairMatch.FairMatch.repository.AuthRepository;
import com.FairMatch.FairMatch.repository.JobJobSeekerRepository;
import com.FairMatch.FairMatch.repository.JobsRepository;
import com.FairMatch.FairMatch.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class MeE2ETest extends E2ETest {
  @LocalServerPort
  private String port;

  @Autowired
  private TestRestTemplate restTemplate;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private AuthRepository authRepository;
  @Autowired
  private JobsRepository jobsRepository;
  @Autowired
  private JobJobSeekerRepository jobJobSeekerRepository;
  @Autowired
  private BCryptPasswordEncoder passwordEncoder;

  User employer;
  User applicant;

  private final String applicantEmail = "applicant@email.com";
  private final String employerEmail = "employer@email.com";

  @BeforeAll
  static void beforeAll() throws Exception {
    startDockerCompose();
  }

  @AfterAll
  static void afterAll() throws Exception {
    stopDockerCompose();
  }

  @BeforeEach
  void setUp() {
    jobJobSeekerRepository.deleteAll();
    jobsRepository.deleteAll();
    authRepository.deleteAll();
    userRepository.deleteAll();

    applicant = User.builder()
      .name("Applicant User")
      .email(applicantEmail)
      .phone("5551112222")
      .userType(UserType.JOB_SEEKER)
      .build();
    userRepository.saveAndFlush(applicant);
    Auth applicantAuth = Auth.builder()
      .user(applicant)
      .username(applicantEmail)
      .password(passwordEncoder.encode("hashedpassword"))
      .role("JOB_SEEKER")
      .build();
    authRepository.saveAndFlush(applicantAuth);

    employer = User.builder()
      .name("Employer User")
      .email(employerEmail)
      .phone("5553334444")
      .userType(UserType.BUSINESS)
      .build();
    userRepository.saveAndFlush(employer);
    Auth employerAuth = Auth.builder()
      .user(employer)
      .username(employerEmail)
      .password(passwordEncoder.encode("hashedpassword"))
      .role("EMPLOYER")
      .build();
    authRepository.saveAndFlush(employerAuth);
  }

  @AfterEach
  void tearDown() {
    jobJobSeekerRepository.deleteAll();
    jobsRepository.deleteAll();
    authRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  void getMe_happyPath_returnsUser_forEmployer() {
    String meUrl = "http://localhost:" + port + "/me";

    String authCookie = getAuthToken(port, employerEmail, restTemplate);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Cookie", "authToken=" + authCookie);
    HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

    ResponseEntity<User> response = restTemplate.exchange(meUrl, HttpMethod.GET, requestEntity, User.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(response.getBody(), employer);
  }

  @Test
  void getMe_happyPath_returnsUser_forApplicant() {
    String meUrl = "http://localhost:" + port + "/me";

    String authCookie = getAuthToken(port, applicantEmail, restTemplate);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Cookie", "authToken=" + authCookie);
    HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

    ResponseEntity<User> response = restTemplate.exchange(meUrl, HttpMethod.GET, requestEntity, User.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(response.getBody(), applicant);
  }


}
