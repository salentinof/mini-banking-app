package com.bankapp.mini_banking_app.controller;

import com.bankapp.mini_banking_app.dto.response.UserResponse;
import com.bankapp.mini_banking_app.entity.User;
import com.bankapp.mini_banking_app.mapper.UserMapper;
import com.bankapp.mini_banking_app.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService service;

    @MockBean
    private UserMapper mapper;

    // ============================
    // POST /users - SUCCESS
    // ============================
    @Test
    void createUser_success() throws Exception {
        User saved = user(1L, "Francesco", "fra@mail.com");
        UserResponse resp = new UserResponse(1L, "Francesco", "fra@mail.com", List.of());

        when(service.createUser(any())).thenReturn(saved);
        when(mapper.responseDto(saved)).thenReturn(resp);

        mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {"name":"Francesco","email":"fra@mail.com"}
                                """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Francesco"))
                .andExpect(jsonPath("$.email").value("fra@mail.com"));
    }

    // ============================
    // GET /users/{id} - SUCCESS
    // ============================
    @Test
    void getUser_success() throws Exception {
        User u = user(1L, "Andrea", "andrea@mail.com");
        UserResponse resp = new UserResponse(1L, "Andrea", "andrea@mail.com", List.of("ACC1"));

        when(service.getUser(1L)).thenReturn(u);
        when(mapper.responseDto(u)).thenReturn(resp);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Andrea"))
                .andExpect(jsonPath("$.email").value("andrea@mail.com"))
                .andExpect(jsonPath("$.accountNumbers[0]").value("ACC1"));
    }

    // ============================
    // GET /users - SUCCESS
    // ============================
    @Test
    void getAllUser_success() throws Exception {
        User u1 = user(1L, "Francesco", "fra@mail.com");
        User u2 = user(2L, "Giovanni", "gio@mail.com");

        UserResponse r1 = new UserResponse(1L, "Francesco", "fra@mail.com", List.of("ACC1"));
        UserResponse r2 = new UserResponse(2L, "Giovanni", "gio@mail.com", List.of("ACC2"));

        when(service.getAllUser()).thenReturn(List.of(u1, u2));
        when(mapper.responseDto(List.of(u1, u2))).thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Francesco"))
                .andExpect(jsonPath("$[0].email").value("fra@mail.com"))
                .andExpect(jsonPath("$[0].accountNumbers[0]").value("ACC1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Giovanni"))
                .andExpect(jsonPath("$[1].email").value("gio@mail.com"))
                .andExpect(jsonPath("$[1].accountNumbers[0]").value("ACC2"));
    }

    // ============================
    // HELPERS
    // ============================
    private User user(Long id, String name, String email) {
        User u = new User();
        u.setId(id);
        u.setName(name);
        u.setEmail(email);
        return u;
    }
}
