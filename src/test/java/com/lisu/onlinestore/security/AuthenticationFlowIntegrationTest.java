package com.lisu.onlinestore.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthenticationFlowIntegrationTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void migratedRolesAllowRegistrationLoginAndAuthenticatedCart() throws Exception {
        String email = UUID.randomUUID() + "@test.com";
        String registration = """
                {"email":"%s","password":"password123","repeatPassword":"password123",
                 "firstName":"Test","lastName":"Reader"}
                """.formatted(email);
        mvc.perform(post("/auth/registration").contentType(MediaType.APPLICATION_JSON)
                        .content(registration))
                .andExpect(status().isCreated());
        var login = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"password123\"}"))
                .andExpect(status().isOk()).andReturn();
        String token = mapper.readTree(login.getResponse().getContentAsString())
                .get("token").asText();
        mvc.perform(get("/cart").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        assertEquals(2, jdbc.queryForObject(
                "SELECT COUNT(*) FROM roles WHERE name IN ('USER', 'ADMIN')", Integer.class));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM users "
                + "WHERE email = 'admin@example.com' AND is_deleted = false", Integer.class));
    }

    @Test
    void protectedEndpointsRejectMissingAndMalformedCredentials() throws Exception {
        mvc.perform(get("/books")).andExpect(status().isUnauthorized());
        mvc.perform(get("/books").header("Authorization", "Bearer malformed"))
                .andExpect(status().isUnauthorized());
    }
}
