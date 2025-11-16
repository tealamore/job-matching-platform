package com.FairMatch.FairMatch.e2e;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class E2ETest {
  static void startDockerCompose() throws Exception {
    ProcessBuilder up = new ProcessBuilder("docker", "compose", "up", "-d");
    up.inheritIO();
    Process upProcess = up.start();
    if (upProcess.waitFor() != 0) throw new RuntimeException("Failed to start docker compose");

    boolean connected = false;
    int retries = 0;
    while (!connected && retries < 30) {
      try (Connection conn = DriverManager.getConnection(
        "jdbc:postgresql://localhost:5432/mydatabase", "myuser", "secret")) {
        connected = true;
      } catch (SQLException e) {
        Thread.sleep(2000);
        retries++;
      }
    }
    if (!connected) throw new RuntimeException("Database did not become ready in time");
  }

  static void stopDockerCompose() throws Exception {
    ProcessBuilder down = new ProcessBuilder("docker", "compose", "down");
    down.inheritIO();
    Process downProcess = down.start();
    downProcess.waitFor();
  }

  String getAuthToken(String port, String email, TestRestTemplate restTemplate) {
    String loginUrl = "http://localhost:" + port + "/auth/login";

    String loginRequest = "{" +
      "\"email\":\"" + email + "\"," +
      "\"password\":\"hashedpassword\"" +
      "}";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> request = new HttpEntity<>(loginRequest, headers);

    ResponseEntity<String> response = restTemplate.postForEntity(loginUrl, request, String.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getHeaders().containsKey("Set-Cookie"));

    return Objects.requireNonNull(response.getHeaders().get("Set-Cookie"))
      .stream()
      .filter(it -> it.startsWith("authToken"))
      .map(it -> it.split("=")[1])
      .findFirst()
      .orElseThrow();
  }
}
