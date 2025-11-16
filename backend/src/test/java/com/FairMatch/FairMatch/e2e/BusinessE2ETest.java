package com.FairMatch.FairMatch.e2e;

import com.FairMatch.FairMatch.dto.JobsResponse;
import com.FairMatch.FairMatch.dto.UserResponse;
import com.FairMatch.FairMatch.model.Auth;
import com.FairMatch.FairMatch.model.Jobs;
import com.FairMatch.FairMatch.model.User;
import com.FairMatch.FairMatch.model.UserType;
import com.FairMatch.FairMatch.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BusinessE2ETest extends E2ETest {
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
  private JobTagsRepository jobTagsRepository;
  @Autowired
  private SkillsRepository skillsRepository;
  @Autowired
  private JobTitlesRepository jobTitlesRepository;

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
    jobTitlesRepository.deleteAll();
    skillsRepository.deleteAll();
    jobJobSeekerRepository.deleteAll();
    jobTagsRepository.deleteAll();
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
    jobTitlesRepository.deleteAll();
    skillsRepository.deleteAll();
    jobJobSeekerRepository.deleteAll();
    jobTagsRepository.deleteAll();
    jobsRepository.deleteAll();
    authRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  void testGetById_businessUser_success() {
    String authCookie = getAuthToken(port, employerEmail, restTemplate);

    Jobs jobs = Jobs.builder()
      .title("Software Engineer")
      .description("Develop software applications.")
      .salary(12000.0)
      .user(employer)
      .build();
    jobsRepository.saveAndFlush(jobs);

    String url = "http://localhost:" + port + "/business/" + employer.getId().toString();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Cookie", "authToken=" + authCookie);
    HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

    ResponseEntity<UserResponse> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, UserResponse.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());

    UserResponse returnedUser = response.getBody();

    assertEquals(employer.getId(), returnedUser.getId());
    assertEquals(employer.getName(), returnedUser.getName());
    assertEquals(employer.getEmail(), returnedUser.getEmail());
    assertEquals(employer.getPhone(), returnedUser.getPhone());
    assertEquals(employer.getUserType(), returnedUser.getUserType());

    assertEquals(1, returnedUser.getJobs().size());

    JobsResponse jobsResponse = returnedUser.getJobs().get(0);

    assertEquals(jobs.getTitle(), jobsResponse.getTitle());
    assertEquals(jobs.getId(), jobsResponse.getId());
    assertEquals(jobs.getDescription(), jobsResponse.getDescription());
    assertEquals(jobs.getSalary(), jobsResponse.getSalary());
    assertThat(jobsResponse.getJobJobSeekers()).isEmpty();
    assertNull(jobsResponse.getPostedBy());
  }

  @Test
  void testGetById_applicantUser_badRequest() {
    String authCookie = getAuthToken(port, employerEmail, restTemplate);

    String url = "http://localhost:" + port + "/business/" + applicant.getId().toString();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Cookie", "authToken=" + authCookie);
    HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

    ResponseEntity<JobsResponse> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, JobsResponse.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }



}
