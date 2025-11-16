package com.FairMatch.FairMatch.e2e;

import com.FairMatch.FairMatch.dto.JobsResponse;
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

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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

    ResponseEntity<JobsResponse> response = restTemplate.exchange(jobUrl, HttpMethod.GET, requestEntity, JobsResponse.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(response.getBody(), new JobsResponse(job));
  }

  @Test
  void getJobFeed_happyPath_returnsRecommendedJobs() {
    JobTitles jt = JobTitles.builder()
      .title("Developer")
      .user(applicant)
      .build();
    jobTitlesRepository.saveAndFlush(jt);

    Skills skill = Skills.builder()
      .skillName("Java")
      .user(applicant)
      .build();
    skillsRepository.saveAndFlush(skill);

    Jobs job1 = Jobs.builder()
      .title("Some other title")
      .description("Job 1")
      .salary(10000.0)
      .user(employer)
      .build();
    Jobs job2 = Jobs.builder()
      .title("Another title")
      .description("Job 2")
      .salary(12000.0)
      .user(employer)
      .build();
    jobsRepository.saveAll(List.of(job1, job2));

    JobTags tag1 = JobTags.builder()
      .skillName("Java")
      .jobs(job1)
      .build();
    JobTags tag2 = JobTags.builder()
      .skillName("Java")
      .jobs(job2)
      .build();
    jobTagsRepository.saveAll(List.of(tag1, tag2));

    String authCookie = getAuthToken(port, applicantEmail, restTemplate);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Cookie", "authToken=" + authCookie);

    HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
    String url = "http://localhost:" + port + "/jobs/feed";

    ResponseEntity<JobsResponse[]> response = restTemplate.exchange(
      url,
      HttpMethod.GET,
      requestEntity,
      JobsResponse[].class
    );

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());

    List<UUID> returnedIds = Arrays.stream(response.getBody())
      .map(JobsResponse::getId)
      .toList();

    assertTrue(returnedIds.contains(job1.getId()));
    assertTrue(returnedIds.contains(job2.getId()));
  }

  @Test
  void getJobFeed_happyPath_returnsRecommendedJobs_filtersItems() {
    JobTitles jt = JobTitles.builder()
      .title("Developer")
      .user(applicant)
      .build();
    jobTitlesRepository.saveAndFlush(jt);

    Skills skill = Skills.builder()
      .skillName("Java")
      .user(applicant)
      .build();
    skillsRepository.saveAndFlush(skill);

    Jobs job1 = Jobs.builder()
      .title("Some other title")
      .description("Job 1")
      .salary(10000.0)
      .user(employer)
      .build();
    Jobs job2 = Jobs.builder()
      .title("Another title")
      .description("Job 2")
      .salary(12000.0)
      .user(employer)
      .build();
    jobsRepository.saveAll(List.of(job1, job2));

    JobTags tag1 = JobTags.builder()
      .skillName("Java")
      .jobs(job1)
      .build();
    JobTags tag2 = JobTags.builder()
      .skillName("Java")
      .jobs(job2)
      .build();
    jobTagsRepository.saveAll(List.of(tag1, tag2));

    String authCookie = getAuthToken(port, applicantEmail, restTemplate);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Cookie", "authToken=" + authCookie);

    HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
    String url = "http://localhost:" + port + "/jobs/feed";

    ResponseEntity<JobsResponse[]> response = restTemplate.exchange(
      url,
      HttpMethod.GET,
      requestEntity,
      JobsResponse[].class
    );

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());

    List<UUID> returnedIds = Arrays.stream(response.getBody())
      .map(JobsResponse::getId)
      .toList();

    assertTrue(returnedIds.contains(job1.getId()));
    assertTrue(returnedIds.contains(job2.getId()));
  }
}
