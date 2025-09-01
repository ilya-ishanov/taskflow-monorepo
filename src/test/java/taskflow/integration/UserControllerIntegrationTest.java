package taskflow.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import taskflow.controllers.UserController;
import taskflow.dto.request.TaskRequestDto;
import taskflow.dto.request.UserRequestDto;
import taskflow.entity.Project;
import taskflow.entity.User;
import taskflow.enums.Priority;
import taskflow.enums.ProjectStatus;
import taskflow.enums.Status;
import taskflow.repository.ProjectRepository;
import taskflow.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import static org.hamcrest.Matchers.hasSize;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserController userController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Transactional
    public void create_validInput_success() throws Exception {
        // Создаём пустого владельца для проекта
        User owner = new User();
        owner.setFirstName("Owner");
        owner.setLastName("Smith");
        owner.setEmail("owner@test.com");
        owner.setActive(true);
        owner = userRepository.save(owner);

        // Сохраняем проект с этим владельцем
        Project project = new Project();
        project.setName("Project");
        project.setDescription("Some description");
        project.setStatus(ProjectStatus.ACTIVE);
        project.setOwner(owner); // обязательное поле!
        project = projectRepository.save(project);

        // Формируем DTO
        UserRequestDto dto = new UserRequestDto();
        dto.setFirstName("Den");
        dto.setLastName("Smirnov");
        dto.setEmail("test@mail.ru");
        dto.setActive(true);
        dto.setProjectsIds(List.of(project.getId())); // ← корректный ID

        // Отправляем POST запрос
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@mail.ru"));

        // Проверяем в БД
        List<User> users = userRepository.findAll();
        assertTrue(users.stream().anyMatch(u -> "test@mail.ru".equals(u.getEmail())));
    }

    @Test
    public void create_missingEmail_400() throws Exception {
        UserRequestDto dto = new UserRequestDto();
        dto.setFirstName("Den");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void get_allUsers_success() throws Exception {
        User firstUser = new User();
        firstUser.setFirstName("Bob");
        firstUser.setLastName("Gil");
        firstUser.setEmail("bob@mail.ru");
        firstUser.setActive(true);

        User secondUser = new User();
        secondUser.setFirstName("Kate");
        secondUser.setLastName("Klu");
        secondUser.setEmail("kate@mail.ru");
        secondUser.setActive(true);

        userRepository.save(firstUser);
        userRepository.save(secondUser);

        Project firstProject = new Project();
        firstProject.setName("First Project");
        firstProject.setDescription("Description1");
        firstProject.setStatus(ProjectStatus.ACTIVE);
        firstProject.setOwner(firstUser);

        Project secondProject = new Project();
        secondProject.setName("Second Project");
        secondProject.setDescription("Description2");
        secondProject.setStatus(ProjectStatus.ACTIVE);
        secondProject.setOwner(firstUser);

        Project thirdProject = new Project();
        thirdProject.setName("Third Project");
        thirdProject.setDescription("Description3");
        thirdProject.setStatus(ProjectStatus.ACTIVE);
        thirdProject.setOwner(secondUser);

        Project fourthProject = new Project();
        fourthProject.setName("Fourth Project");
        fourthProject.setDescription("Description4");
        fourthProject.setStatus(ProjectStatus.ACTIVE);
        fourthProject.setOwner(secondUser);

        firstUser.setProjects(List.of(firstProject, secondProject));
        secondUser.setProjects(List.of(thirdProject, fourthProject));

        projectRepository.save(firstProject);
        projectRepository.save(secondProject);

        projectRepository.save(thirdProject);
        projectRepository.save(fourthProject);

        mockMvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].firstName").value("Bob"))
                .andExpect(jsonPath("$.content[1].firstName").value("Kate"));

    }

    @Test
    public void get_byId_success() throws Exception {
        User firstUser = new User();
        firstUser.setFirstName("Bob");
        firstUser.setLastName("Gil");
        firstUser.setEmail("bob@mail.ru");
        firstUser.setActive(true);
        User savedUser = userRepository.save(firstUser);

        Project firstProject = new Project();
        firstProject.setName("First Project");
        firstProject.setDescription("Description1");
        firstProject.setStatus(ProjectStatus.ACTIVE);
        firstProject.setOwner(savedUser);

        Project secondProject = new Project();
        secondProject.setName("Second Project");
        secondProject.setDescription("Description2");
        secondProject.setStatus(ProjectStatus.ACTIVE);
        secondProject.setOwner(savedUser);

        projectRepository.save(firstProject);
        projectRepository.save(secondProject);

        firstUser.setProjects(List.of(firstProject, secondProject));
        userRepository.save(savedUser);

        mockMvc.perform(get("/users/{id}", savedUser.getId()))
                .andExpect(jsonPath("$.firstName").value("Bob"))
                .andExpect(jsonPath("$.lastName").value("Gil"))
                .andExpect(jsonPath("$.email").value("bob@mail.ru"))
                .andExpect(jsonPath("$.active").value(true));
    }


    @Test
    public void get_userNotFound_404() throws Exception {
        User user = new User();
        user.setId(1L);

        mockMvc.perform(get("/users/{id}", 777L))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("не найден")));
    }

    @Test
    public void update_validInput_success() throws Exception {
        User user = new User();
        user.setFirstName("Ivan");
        user.setLastName("Petrov");
        user.setEmail("ivan@mail.ru");
        user.setActive(true);
        user = userRepository.saveAndFlush(user);

        Project project = new Project();
        project.setName("Project 1");
        project.setDescription("Test Project");
        project.setStatus(ProjectStatus.ACTIVE);
        project.setOwner(user);
        project = projectRepository.saveAndFlush(project);

        user.setProjects(List.of(project));
        user = userRepository.saveAndFlush(user);

        String body = """
    {
        "firstName": "Petr",
        "lastName": "Ivanov",
        "email": "petr@mail.ru",
        "active": false,
        "projectsIds": [%d]
    }
    """.formatted(project.getId());

        mockMvc.perform(put("/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Petr"))
                .andExpect(jsonPath("$.lastName").value("Ivanov"))
                .andExpect(jsonPath("$.email").value("petr@mail.ru"))
                .andExpect(jsonPath("$.active").value(false));

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertEquals("Petr", updated.getFirstName());
        assertEquals("Ivanov", updated.getLastName());
        assertEquals("petr@mail.ru", updated.getEmail());
        assertFalse(Boolean.TRUE.equals(updated.getActive()));
    }

    @Test
    public void delete_byId_success() throws Exception {
        User user = new User();
        user.setFirstName("Alex");
        user.setLastName("Ivanov");
        user.setEmail("alex@mail.ru");
        user.setActive(true);
        user = userRepository.saveAndFlush(user);

        mockMvc.perform(delete("/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("User успешно удален"));

        Optional<User> deleted = userRepository.findById(user.getId());
        assertTrue(deleted.isEmpty(), "Пользователь должен быть удалён");
    }

    @Test
    public void delete_userNotFound_404() throws Exception {
        Long nonexistentId = 9999L;

        mockMvc.perform(delete("/users/{id}", nonexistentId))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("не найден")));
    }

    @Test
    void create_blankTitle_400() throws Exception {
        TaskRequestDto dto = new TaskRequestDto();
        dto.setTitle(""); // пусто — нарушает @NotBlank
        dto.setStatus(Status.TODO);
        dto.setPriority(Priority.HIGH);
        dto.setUserId(1L);
        dto.setProjectId(1L);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}
