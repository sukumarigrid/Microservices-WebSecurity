package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AppTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Test
    void loginIssuesJwtToken() throws Exception {
        mockMvc.perform(post("/api/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alice","password":"alice123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.authorities[0]").value("ROLE_USER"));
    }

    @Test
    void endpointsRejectRequestsWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postOwnershipIsEnforcedForUpdatesAndDeletes() throws Exception {
        String aliceToken = tokenFor("alice", "alice123");
        String bobToken = tokenFor("bob", "bob123");

        JsonNode createdPost = performJson(post("/api/posts")
                .header(HttpHeaders.AUTHORIZATION, bearer(aliceToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title":"Owned by Alice","body":"Original content"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author").value("alice"))
                .andExpect(jsonPath("$.likeCount").value(0))
                .andReturnJson(objectMapper);

        long postId = createdPost.get("id").asLong();

        performJson(put("/api/posts/" + postId)
                .header(HttpHeaders.AUTHORIZATION, bearer(bobToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title":"Bob tried to edit","body":"This should fail"}
                        """))
                .andExpect(status().isForbidden());

        performJson(put("/api/posts/" + postId)
                .header(HttpHeaders.AUTHORIZATION, bearer(aliceToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title":"Alice updated it","body":"New content"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Alice updated it"));

        performJson(delete("/api/posts/" + postId)
                .header(HttpHeaders.AUTHORIZATION, bearer(bobToken)))
                .andExpect(status().isForbidden());

        performJson(delete("/api/posts/" + postId)
                .header(HttpHeaders.AUTHORIZATION, bearer(aliceToken)))
                .andExpect(status().isOk());
    }

    @Test
    void serviceTokenCanCreateInternalNotificationsWhileUserTokenCannot() throws Exception {
        String aliceToken = tokenFor("alice", "alice123");
        String serviceToken = jwtService.issueServiceToken("likes-service", Set.of("ROLE_SERVICE_NOTIFICATION"));

        performJson(post("/api/internal/notifications")
                .header(HttpHeaders.AUTHORIZATION, bearer(aliceToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "postId": 1,
                          "recipientUsername": "alice",
                          "actorUsername": "bob",
                          "sourceService": "likes-service",
                          "message": "bob liked your post"
                        }
                        """))
                .andExpect(status().isForbidden());

        performJson(post("/api/internal/notifications")
                .header(HttpHeaders.AUTHORIZATION, bearer(serviceToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "postId": 1,
                          "recipientUsername": "alice",
                          "actorUsername": "bob",
                          "sourceService": "likes-service",
                          "message": "bob liked your post"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourceService").value("likes-service"));
    }

    @Test
    void adminStatsAreRestrictedToAdmins() throws Exception {
        String userToken = tokenFor("alice", "alice123");
        String adminToken = tokenFor("admin", "admin123");

        performJson(get("/api/admin/stats")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken)))
                .andExpect(status().isForbidden());

        performJson(get("/api/admin/stats")
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userCount", greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.postCount", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.usernames").isArray());
    }

    private String tokenFor(String username, String password) throws Exception {
        JsonNode response = performJson(post("/api/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"username":"%s","password":"%s"}
                        """.formatted(username, password)))
                .andExpect(status().isOk())
                .andReturnJson(objectMapper);

        return response.get("accessToken").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private JsonMvcResultActions performJson(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request)
            throws Exception {
        return new JsonMvcResultActions(mockMvc.perform(request));
    }

    private static final class JsonMvcResultActions {

        private final org.springframework.test.web.servlet.MvcResult result;
        private final org.springframework.test.web.servlet.ResultActions actions;

        private JsonMvcResultActions(org.springframework.test.web.servlet.ResultActions actions) throws Exception {
            this.actions = actions;
            this.result = actions.andReturn();
        }

        private JsonMvcResultActions andExpect(org.springframework.test.web.servlet.ResultMatcher matcher) throws Exception {
            actions.andExpect(matcher);
            return this;
        }

        private JsonNode andReturnJson(ObjectMapper objectMapper) throws Exception {
            return objectMapper.readTree(result.getResponse().getContentAsString());
        }
    }
}
