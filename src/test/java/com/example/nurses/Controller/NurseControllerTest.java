package com.example.nurses.Controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;

import com.example.nurses.Entity.Nurse;
import com.example.nurses.Repository.NurseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class NurseControllerTest {

	@Autowired
    private MockMvc mockMvc;

    @Autowired
    private NurseRepository nurseRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Nurse nurse;

    @BeforeEach
    void setUp() {
    	nurseRepository.deleteAll();
        nurse = new Nurse("Luciana", "Gómez", "luciana@hospital.com", "luciana", "1234");
        nurseRepository.save(nurse);
    }

    @Test
    void shouldGetAllNurses() throws Exception {
        mockMvc.perform(get("/nurse/index"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetNurseById() throws Exception {
        mockMvc.perform(get("/nurse/" + nurse.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("luciana@hospital.com"));
    }

    @Test
    void shouldReturnNotFoundForUnknownId() throws Exception {
        mockMvc.perform(get("/nurse/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNurse() throws Exception {
        Nurse newNurse = new Nurse("Ana", "López", "ana@hospital.com", "ana", "5678");

        mockMvc.perform(post("/nurse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newNurse)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Ana"))
                .andExpect(jsonPath("$.id").isNumber());

        assertThat(nurseRepository.findAll()).hasSize(2);
    }

    @Test
    void shouldUpdateNurse() throws Exception {
        Nurse update = new Nurse();
        update.setEmail("nuevo@hospital.com");

        mockMvc.perform(put("/nurse/" + nurse.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk());

        Nurse updated = nurseRepository.findById(nurse.getId()).orElseThrow();
        assertThat(updated.getEmail()).isEqualTo("nuevo@hospital.com");
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingUnknownNurse() throws Exception {
        Nurse update = new Nurse();
        update.setEmail("x@x.com");

        mockMvc.perform(put("/nurse/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
    	Nurse login = new Nurse();
        login.setUser("luciana");
        login.setPass("1234");

        mockMvc.perform(post("/nurse/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user").value("luciana"))
                .andExpect(jsonPath("$.pass").value("1234"));
    }

    @Test
    void shouldFailLoginWithInvalidCredentials() throws Exception {
    	Nurse login = new Nurse();
        login.setUser("luciana");
        login.setPass("wrong");

        mockMvc.perform(post("/nurse/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldFindByName() throws Exception {
        mockMvc.perform(get("/nurse/name").param("name", "Luciana"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Luciana"));
    }

    @Test
    void shouldReturnNotFoundForUnknownName() throws Exception {
        mockMvc.perform(get("/nurse/name").param("name", "Desconocida"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void shouldDeleteNurse() throws Exception {
        mockMvc.perform(delete("/nurse/" + nurse.getId()))
                .andExpect(status().isOk());
        assertThat(nurseRepository.existsById(nurse.getId())).isFalse();
    }

    @Test
    void shouldReturnNotFoundWhenDeletingUnknownNurse() throws Exception {
        mockMvc.perform(delete("/nurse/999"))
                .andExpect(status().isNotFound());
    }

}
