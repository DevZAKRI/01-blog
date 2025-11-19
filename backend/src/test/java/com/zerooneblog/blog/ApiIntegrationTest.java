// package com.zerooneblog.blog;

// import static org.assertj.core.api.Assertions.assertThat;
// import org.junit.jupiter.api.MethodOrderer;
// import org.junit.jupiter.api.Order;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.TestMethodOrder;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.web.client.TestRestTemplate;
// import org.springframework.boot.test.web.server.LocalServerPort;
// import org.springframework.http.HttpEntity;
// import org.springframework.http.HttpHeaders;
// import org.springframework.http.HttpMethod;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;
// import org.springframework.test.annotation.DirtiesContext;

// import com.fasterxml.jackson.databind.JsonNode;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.zerooneblog.blog.dto.request.RegisterRequest;
// import com.zerooneblog.blog.repository.UserRepository;

// @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
//     properties = {
//         "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
//         "spring.datasource.driver-class-name=org.h2.Driver",
//         "spring.datasource.username=sa",
//         "spring.datasource.password=",
//         "spring.jpa.hibernate.ddl-auto=create-drop"
//     }
// )
// @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
// @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
// public class ApiIntegrationTest {

//     @LocalServerPort
//     private int port;

//     @Autowired
//     private TestRestTemplate restTemplate;

//     @Autowired
//     private ObjectMapper objectMapper;

//     @Autowired
//     private UserRepository userRepository;

//     private String baseUrl() { return "http://localhost:" + port + "/api/v1"; }

//     private String registerAndLogin(String username, String email, String password) throws Exception {
//         RegisterRequest r = new RegisterRequest();
//         r.setUsername(username); r.setEmail(email); r.setPassword(password);
//         ResponseEntity<String> reg = restTemplate.postForEntity(baseUrl() + "/auth/register", r, String.class);
//         assertThat(reg.getStatusCode()).isEqualTo(HttpStatus.OK);
//         JsonNode node = objectMapper.readTree(reg.getBody());
//         assertThat(node.has("token")).isTrue();
//         return node.get("token").asText();
//     }

//     @Test
//     @Order(1)
//     void fullFlow() throws Exception {
//         // register two users
//         String tokenAlice = registerAndLogin("alice", "alice@example.com", "pass1234");
//         String tokenBob = registerAndLogin("bob", "bob@example.com", "pass1234");

//         // Alice creates a post
//         HttpHeaders hAlice = new HttpHeaders(); hAlice.setBearerAuth(tokenAlice); hAlice.setContentType(MediaType.APPLICATION_JSON);
//         String createPostJson = "{\"description\":\"Hello World\",\"mediaUrl\":null}";
//         HttpEntity<String> postReq = new HttpEntity<>(createPostJson, hAlice);
//         ResponseEntity<String> postRes = restTemplate.postForEntity(baseUrl() + "/posts", postReq, String.class);
//         assertThat(postRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);
//         JsonNode postNode = objectMapper.readTree(postRes.getBody());
//         Long postId = postNode.get("id").asLong();

//         // Bob attempts to edit Alice's post -> expect 4xx
//         HttpHeaders hBob = new HttpHeaders(); hBob.setBearerAuth(tokenBob); hBob.setContentType(MediaType.APPLICATION_JSON);
//         String editJson = "{\"description\":\"Hacked\",\"mediaUrl\":null}";
//         ResponseEntity<String> editRes = restTemplate.exchange(baseUrl() + "/posts/" + postId, HttpMethod.PUT, new HttpEntity<>(editJson, hBob), String.class);
//         assertThat(editRes.getStatusCode().is4xxClientError()).isTrue();

//         // Bob likes the post
//         ResponseEntity<Void> likeRes = restTemplate.exchange(baseUrl() + "/posts/" + postId + "/like", HttpMethod.POST, new HttpEntity<>(hBob), Void.class);
//         assertThat(likeRes.getStatusCode().is2xxSuccessful()).isTrue();

//         // Bob comments on the post
//         String commentJson = "{\"text\":\"Nice post!\"}";
//         ResponseEntity<String> commentRes = restTemplate.postForEntity(baseUrl() + "/posts/" + postId + "/comments", new HttpEntity<>(commentJson, hBob), String.class);
//         assertThat(commentRes.getStatusCode()).isEqualTo(HttpStatus.OK);

//         // Alice checks notifications: unread-count should be >=1
//         ResponseEntity<String> unread = restTemplate.exchange(baseUrl() + "/notifications/unread-count", HttpMethod.GET, new HttpEntity<>(hAlice), String.class);
//         assertThat(unread.getStatusCode()).isEqualTo(HttpStatus.OK);
//         JsonNode unreadNode = objectMapper.readTree(unread.getBody());
//         assertThat(unreadNode.has("unread")).isTrue();
//         assertThat(unreadNode.get("unread").asLong()).isGreaterThanOrEqualTo(1L);

//         // Alice marks all read
//         ResponseEntity<Void> markAll = restTemplate.exchange(baseUrl() + "/notifications/mark-all-read", HttpMethod.POST, new HttpEntity<>(hAlice), Void.class);
//         assertThat(markAll.getStatusCode().is2xxSuccessful()).isTrue();

//         // Bob reports Alice
//         Long aliceId = userRepository.findByUsername("alice").get().getId();
//         String reportJson = "{\"reason\":\"spam\"}";
//         ResponseEntity<String> reportRes = restTemplate.postForEntity(baseUrl() + "/users/" + aliceId + "/report", new HttpEntity<>(reportJson, hBob), String.class);
//         assertThat(reportRes.getStatusCode()).isEqualTo(HttpStatus.OK);

//         // Negative tests: unauthorized access
//     ResponseEntity<String> noAuthRes = restTemplate.postForEntity(baseUrl() + "/posts", new HttpEntity<>(createPostJson), String.class);
//     // Server may respond 401 (unauthorized) or 403 (forbidden when anonymous auth exists). Accept any 4xx client error here.
//     assertThat(noAuthRes.getStatusCode().is4xxClientError()).isTrue();
//     }
// }
