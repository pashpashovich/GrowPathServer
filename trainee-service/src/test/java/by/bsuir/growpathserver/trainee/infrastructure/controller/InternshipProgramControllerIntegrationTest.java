package by.bsuir.growpathserver.trainee.infrastructure.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import by.bsuir.growpathserver.dto.model.CreateInternshipProgramRequest;
import by.bsuir.growpathserver.dto.model.ProgramGoal;
import by.bsuir.growpathserver.dto.model.UpdateInternshipProgramRequest;
import by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.InternshipProgramStatus;
import by.bsuir.growpathserver.trainee.infrastructure.repository.InternshipProgramRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class InternshipProgramControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InternshipProgramRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    private InternshipProgramEntity testProgram;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        testProgram = new InternshipProgramEntity();
        testProgram.setTitle("Test Program");
        testProgram.setDescription("Test Description");
        testProgram.setStartDate(LocalDate.of(2024, 9, 1));
        testProgram.setDuration(6);
        testProgram.setMaxPlaces(20);
        testProgram.setStatus(InternshipProgramStatus.ACTIVE);
        testProgram.setCreatedBy(1L);
        testProgram.setCreatedAt(LocalDateTime.now());
        testProgram.setUpdatedAt(LocalDateTime.now());
        testProgram = repository.saveAndFlush(testProgram);
    }

    @Test
    void shouldGetInternshipProgramsSuccessfully() throws Exception {
        // When & Then
        mockMvc.perform(get("/internship-programs")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(String.valueOf(testProgram.getId())))
                .andExpect(jsonPath("$.data[0].title").value("Test Program"))
                .andExpect(jsonPath("$.pagination").exists());
    }

    @Test
    void shouldGetInternshipProgramsWithPagination() throws Exception {
        // Given - create more programs
        for (int i = 2; i <= 5; i++) {
            InternshipProgramEntity program = new InternshipProgramEntity();
            program.setTitle("Program " + i);
            program.setDescription("Description " + i);
            program.setStartDate(LocalDate.of(2024, 9, 1));
            program.setDuration(6);
            program.setMaxPlaces(20);
            program.setStatus(InternshipProgramStatus.ACTIVE);
            program.setCreatedBy(1L);
            program.setCreatedAt(LocalDateTime.now());
            program.setUpdatedAt(LocalDateTime.now());
            repository.saveAndFlush(program);
        }

        // When & Then
        mockMvc.perform(get("/internship-programs")
                                .param("page", "1")
                                .param("limit", "2")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.pagination.page").value(1))
                .andExpect(jsonPath("$.pagination.limit").value(2));
    }

    @Test
    void shouldFilterInternshipProgramsByStatus() throws Exception {
        // Given
        InternshipProgramEntity draftProgram = new InternshipProgramEntity();
        draftProgram.setTitle("Draft Program");
        draftProgram.setDescription("Draft Description");
        draftProgram.setStartDate(LocalDate.of(2024, 10, 1));
        draftProgram.setDuration(4);
        draftProgram.setMaxPlaces(15);
        draftProgram.setStatus(InternshipProgramStatus.DRAFT);
        draftProgram.setCreatedBy(1L);
        draftProgram.setCreatedAt(LocalDateTime.now());
        draftProgram.setUpdatedAt(LocalDateTime.now());
        repository.saveAndFlush(draftProgram);

        // When & Then
        mockMvc.perform(get("/internship-programs")
                                .param("status", "draft")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].status").value("draft"));
    }

    @Test
    void shouldSearchInternshipPrograms() throws Exception {
        // Given
        InternshipProgramEntity program2 = new InternshipProgramEntity();
        program2.setTitle("Java Development");
        program2.setDescription("Java Backend Development Program");
        program2.setStartDate(LocalDate.of(2024, 9, 1));
        program2.setDuration(6);
        program2.setMaxPlaces(20);
        program2.setStatus(InternshipProgramStatus.ACTIVE);
        program2.setCreatedBy(1L);
        program2.setCreatedAt(LocalDateTime.now());
        program2.setUpdatedAt(LocalDateTime.now());
        repository.saveAndFlush(program2);

        // When & Then
        mockMvc.perform(get("/internship-programs")
                                .param("search", "Java")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Java Development"));
    }

    @Test
    void shouldGetInternshipProgramByIdSuccessfully() throws Exception {
        // When & Then
        mockMvc.perform(get("/internship-programs/{id}", testProgram.getId())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(String.valueOf(testProgram.getId())))
                .andExpect(jsonPath("$.title").value("Test Program"))
                .andExpect(jsonPath("$.description").value("Test Description"));
    }

    @Test
    void shouldReturnNotFoundWhenProgramDoesNotExist() throws Exception {
        // When & Then
        mockMvc.perform(get("/internship-programs/{id}", 0L)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateInternshipProgramSuccessfully() throws Exception {
        // Given
        CreateInternshipProgramRequest request = new CreateInternshipProgramRequest();
        request.setTitle("New Program");
        request.setDescription("New Description");
        request.setStartDate(LocalDate.of(2025, 1, 1));
        request.setDuration(6);
        request.setMaxPlaces(20);
        request.setStatus(CreateInternshipProgramRequest.StatusEnum.ACTIVE);

        List<String> requirements = new ArrayList<>();
        requirements.add("Java knowledge");
        request.setRequirements(requirements);

        List<Object> goals = new ArrayList<>();
        ProgramGoal goal = new ProgramGoal();
        goal.setTitle("Learn Spring Boot");
        goal.setDescription("Master Spring Boot framework");
        goals.add(goal);
        request.setGoals(goals);

        // When & Then
        mockMvc.perform(post("/internship-programs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("New Program"))
                .andExpect(jsonPath("$.description").value("New Description"))
                .andExpect(jsonPath("$.status").value("active"));
    }

    @Test
    void shouldUpdateInternshipProgramSuccessfully() throws Exception {
        // Given
        UpdateInternshipProgramRequest request = new UpdateInternshipProgramRequest();
        request.setTitle("Updated Program");
        request.setDescription("Updated Description");
        request.setStatus(UpdateInternshipProgramRequest.StatusEnum.COMPLETED);

        // When & Then
        mockMvc.perform(put("/internship-programs/{id}", testProgram.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Program"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.status").value("completed"));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentProgram() throws Exception {
        // Given
        UpdateInternshipProgramRequest request = new UpdateInternshipProgramRequest();
        request.setTitle("Updated Program");

        // When & Then
        mockMvc.perform(put("/internship-programs/{id}", 0L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteInternshipProgramSuccessfully() throws Exception {
        // When & Then
        mockMvc.perform(delete("/internship-programs/{id}", testProgram.getId())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Internship program deleted successfully"));
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentProgram() throws Exception {
        // When & Then
        mockMvc.perform(delete("/internship-programs/{id}", 0L)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
