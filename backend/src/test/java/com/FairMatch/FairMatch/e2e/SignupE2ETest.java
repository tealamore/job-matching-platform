package com.FairMatch.FairMatch.e2e;

import com.FairMatch.FairMatch.model.Auth;
import com.FairMatch.FairMatch.model.User;
import com.FairMatch.FairMatch.model.UserType;
import com.FairMatch.FairMatch.repository.AuthRepository;
import com.FairMatch.FairMatch.repository.JobJobSeekerRepository;
import com.FairMatch.FairMatch.repository.JobsRepository;
import com.FairMatch.FairMatch.repository.UserRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SignupE2ETest extends E2ETest {
    @LocalServerPort
    private int port;

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
    }

    @AfterEach
    void tearDown() {
      jobJobSeekerRepository.deleteAll();
      jobsRepository.deleteAll();
      authRepository.deleteAll();
      userRepository.deleteAll();
    }

    @Test
    void signup_happyPath_createsUserAndAuth() {
        String url = "http://localhost:" + port + "/auth/signup";
        String body = "{" +
                "\"email\":\"e2euser@example.com\"," +
                "\"password\":\"password123\"," +
                "\"name\":\"E2E User\"," +
                "\"phone\":\"5551234567\"," +
                "\"userType\":\"JOB_SEEKER\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getHeaders().containsKey("Set-Cookie"));

        Optional<User> userOpt = userRepository.findByEmail("e2euser@example.com");
        assertTrue(userOpt.isPresent());
        User user = userOpt.get();
        assertEquals("E2E User", user.getName());
        assertEquals("5551234567", user.getPhone());
        assertEquals(UserType.JOB_SEEKER, user.getUserType());

        Auth auth = authRepository.findAll().stream()
                .filter(a -> a.getUsername().equals("e2euser@example.com"))
                .findFirst().orElse(null);
        assertNotNull(auth);
        assertEquals(user.getId(), auth.getUser().getId());
    }

    @Test
    void signup_duplicateEmail_returnsConflict() {
        User user = User.builder()
                .name("E2E User")
                .email("e2euser@example.com")
                .phone("5551234567")
                .userType(UserType.JOB_SEEKER)
                .build();
        user = userRepository.saveAndFlush(user);
        Auth auth = Auth.builder()
                .user(user)
                .username("e2euser@example.com")
                .password("hashed")
                .role("USER")
                .build();
        authRepository.saveAndFlush(auth);

        String url = "http://localhost:" + port + "/auth/signup";
        String body = "{" +
                "\"email\":\"e2euser@example.com\"," +
                "\"password\":\"password123\"," +
                "\"name\":\"E2E User\"," +
                "\"phone\":\"5551234567\"," +
                "\"userType\":\"JOB_SEEKER\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }
}
