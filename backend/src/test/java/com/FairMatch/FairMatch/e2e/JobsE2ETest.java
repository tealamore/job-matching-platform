package com.FairMatch.FairMatch.e2e;

import com.FairMatch.FairMatch.dto.JobsDTO;
import com.FairMatch.FairMatch.model.*;
import com.FairMatch.FairMatch.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class JobsE2ETest extends E2ETest {
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
  void createJob_happyPath_returnsNewJob_forEmployer() {
    String url = "http://localhost:" + port + "/jobs";

    String authCookie = getAuthToken(port, employerEmail, restTemplate);

    String loginRequest = "{" +
      "\"title\":\"job title\"," +
      "\"description\":\"job description\"," +
      "\"salary\":12000.0," +
      "\"tags\":[\"tag 1\", \"tag 2\"]" +
      "}";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Cookie", "authToken=" + authCookie);
    HttpEntity<String> request = new HttpEntity<>(loginRequest, headers);

    ResponseEntity<Jobs> response = restTemplate.postForEntity(url, request, Jobs.class);

    Jobs expectedJob = Jobs.builder()
      .title("job title")
      .description("job description")
      .salary(12000.0)
      .build();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    response.getBody().setId(null);
    response.getBody().setUser(null);
    assertEquals(expectedJob, response.getBody());
  }

  @Test
  void createJob_401_forApplicant() {
    String url = "http://localhost:" + port + "/jobs";

    String authCookie = getAuthToken(port, applicantEmail, restTemplate);

    String loginRequest = "{" +
      "\"title\":\"job title\"," +
      "\"description\":\"job description\"," +
      "\"salary\":12000.0," +
      "\"tags\":[\"tag 1\", \"tag 2\"]" +
      "}";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Cookie", "authToken=" + authCookie);
    HttpEntity<String> request = new HttpEntity<>(loginRequest, headers);

    ResponseEntity<Jobs> response = restTemplate.postForEntity(url, request, Jobs.class);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void interactJob_happyPath_forApplicant() {
    String url = "http://localhost:" + port + "/jobs/interact";

    String authCookie = getAuthToken(port, applicantEmail, restTemplate);

    Jobs job = Jobs.builder()
      .title("job title")
      .description("job description")
      .salary(12000.0)
      .user(employer)
      .build();

    jobsRepository.saveAndFlush(job);
    UUID jobId = job.getId();

    String loginRequest = "{" +
      "\"jobId\":\"" + jobId + "\"," +
      "\"swipeStatus\":\"DISLIKE\"" +
      "}";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Cookie", "authToken=" + authCookie);
    HttpEntity<String> request = new HttpEntity<>(loginRequest, headers);

    ResponseEntity<Jobs> response = restTemplate.postForEntity(url, request, Jobs.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());

    JobJobSeeker swiped = jobJobSeekerRepository.findAllByJobsIn(List.of(job))
      .stream()
      .findFirst()
      .orElseThrow();

    assertEquals(applicant.getId(), swiped.getUser().getId());
    assertEquals(job.getId(), swiped.getJobs().getId());
    assertEquals(SwipeStatus.DISLIKE, swiped.getStatus());
  }

  @Test
  void interactJob_happyPath_liked_forApplicant() {
    String url = "http://localhost:" + port + "/jobs/interact";

    String authCookie = getAuthToken(port, applicantEmail, restTemplate);

    Jobs job = Jobs.builder()
      .title("job title")
      .description("job description")
      .salary(12000.0)
      .user(employer)
      .build();

    jobsRepository.saveAndFlush(job);
    UUID jobId = job.getId();

    String loginRequest = "{" +
      "\"jobId\":\"" + jobId + "\"," +
      "\"swipeStatus\":\"LIKE\"" +
      "}";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Cookie", "authToken=" + authCookie);
    HttpEntity<String> request = new HttpEntity<>(loginRequest, headers);

    ResponseEntity<Jobs> response = restTemplate.postForEntity(url, request, Jobs.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());

    JobJobSeeker swiped = jobJobSeekerRepository.findAllByJobsIn(List.of(job))
      .stream()
      .findFirst()
      .orElseThrow();

    assertEquals(applicant.getId(), swiped.getUser().getId());
    assertEquals(job.getId(), swiped.getJobs().getId());
    assertEquals(SwipeStatus.LIKE, swiped.getStatus());
  }

  @Test
  void interactJob_happyPath_invalidSwipeType_forApplicant() {
    String url = "http://localhost:" + port + "/jobs/interact";

    String authCookie = getAuthToken(port, applicantEmail, restTemplate);

    Jobs job = Jobs.builder()
      .title("job title")
      .description("job description")
      .salary(12000.0)
      .user(employer)
      .build();

    jobsRepository.saveAndFlush(job);
    UUID jobId = job.getId();

    String loginRequest = "{" +
      "\"jobId\":\"" + jobId + "\"," +
      "\"swipeStatus\":\"INVALID_SWIPE\"" +
      "}";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Cookie", "authToken=" + authCookie);
    HttpEntity<String> request = new HttpEntity<>(loginRequest, headers);

    ResponseEntity<Jobs> response = restTemplate.postForEntity(url, request, Jobs.class);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

    assertThat(jobJobSeekerRepository.findAll()).isEmpty();
  }

  @Test
  void interactJob_401_forEmployer() {
    String url = "http://localhost:" + port + "/jobs/interact";

    String authCookie = getAuthToken(port, employerEmail, restTemplate);

    Jobs job = Jobs.builder()
      .title("job title")
      .description("job description")
      .salary(12000.0)
      .user(employer)
      .build();

    jobsRepository.saveAndFlush(job);
    UUID jobId = job.getId();

    String loginRequest = "{" +
      "\"jobId\":\"" + jobId + "\"," +
      "\"swipeStatus\":\"DISLIKE\"" +
      "}";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Cookie", "authToken=" + authCookie);
    HttpEntity<String> request = new HttpEntity<>(loginRequest, headers);

    ResponseEntity<Jobs> response = restTemplate.postForEntity(url, request, Jobs.class);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

    assertThat(jobJobSeekerRepository.findAll()).isEmpty();
  }

  @Test
  void getJobById_happyPath_returnsJob() {
    String authCookie = getAuthToken(port, employerEmail, restTemplate);

    Jobs job = Jobs.builder()
      .title("job title")
      .description("job description")
      .salary(12000.0)
      .user(employer)
      .build();

    jobsRepository.saveAndFlush(job);

    String jobUrl = "http://localhost:" + port + "/jobs/" + job.getId().toString();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Cookie", "authToken=" + authCookie);
    HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

    ResponseEntity<JobsDTO> response = restTemplate.exchange(jobUrl, HttpMethod.GET, requestEntity, JobsDTO.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(response.getBody(), new JobsDTO(job));
  }
}
