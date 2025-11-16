package com.FairMatch.FairMatch.e2e;

import com.FairMatch.FairMatch.dto.UserResponse;
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

import static org.junit.jupiter.api.Assertions.*;

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
  private SkillsRepository skillsRepository;
  @Autowired
  private JobTitlesRepository jobTitlesRepository;
  @Autowired
  private JobTagsRepository jobTagsRepository;

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
  void getMe_happyPath_returnsUser_forEmployer() {
    String meUrl = "http://localhost:" + port + "/me";

    String authCookie = getAuthToken(port, employerEmail, restTemplate);

    Skills skill = Skills.builder()
      .skillName("Java")
      .user(employer)
      .build();
    skillsRepository.saveAndFlush(skill);

    JobTitles jobTitle = JobTitles.builder()
      .title("Software Engineer")
      .user(employer)
      .build();
    jobTitlesRepository.saveAndFlush(jobTitle);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Cookie", "authToken=" + authCookie);
    HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

    ResponseEntity<UserResponse> response = restTemplate.exchange(meUrl, HttpMethod.GET, requestEntity, UserResponse.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());

    UserResponse returnedUser = response.getBody();

    assertNotNull(returnedUser);

    assertEquals(employer.getId(), returnedUser.getId());
    assertEquals(employer.getName(), returnedUser.getName());
    assertEquals(employer.getEmail(), returnedUser.getEmail());
    assertEquals(employer.getPhone(), returnedUser.getPhone());
    assertEquals(employer.getUserType(), returnedUser.getUserType());

    assertNull(returnedUser.getDesiredTitles());
    assertNull(returnedUser.getSkills());
  }

  @Test
  void getMe_happyPath_returnsUser_forApplicant() {
    String meUrl = "http://localhost:" + port + "/me";

    String authCookie = getAuthToken(port, applicantEmail, restTemplate);

    Skills skill = Skills.builder()
      .skillName("Java")
      .user(applicant)
      .build();
    skillsRepository.saveAndFlush(skill);

    JobTitles jobTitle = JobTitles.builder()
      .title("Software Engineer")
      .user(applicant)
      .build();
    jobTitlesRepository.saveAndFlush(jobTitle);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Cookie", "authToken=" + authCookie);
    HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

    ResponseEntity<UserResponse> response = restTemplate.exchange(meUrl, HttpMethod.GET, requestEntity, UserResponse.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());

    UserResponse returnedUser = response.getBody();

    assertNotNull(returnedUser);

    assertEquals(applicant.getId(), returnedUser.getId());
    assertEquals(applicant.getName(), returnedUser.getName());
    assertEquals(applicant.getEmail(), returnedUser.getEmail());
    assertEquals(applicant.getPhone(), returnedUser.getPhone());
    assertEquals(applicant.getUserType(), returnedUser.getUserType());

    assertEquals(1, returnedUser.getDesiredTitles().size());
    assertEquals(1, returnedUser.getSkills().size());

    assertEquals(jobTitle.getTitle(), returnedUser.getDesiredTitles().get(0).getTitle());
    assertEquals(jobTitle.getId(), returnedUser.getDesiredTitles().get(0).getId());

    assertEquals(skill.getSkillName(), returnedUser.getSkills().get(0).getSkillName());
    assertEquals(skill.getId(), returnedUser.getSkills().get(0).getId());
  }


}
