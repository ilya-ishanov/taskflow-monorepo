package taskflow.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import taskflow.dto.request.ProjectRequestDto;
import taskflow.entity.Project;
import taskflow.entity.User;
import taskflow.enums.ProjectStatus;
import taskflow.exceptions.EntityNotFoundException;
import taskflow.repository.ProjectRepository;
import taskflow.repository.UserRepository;
import taskflow.validator.Validator;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

    @InjectMocks
    private ProjectService projectService;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Validator validator;

    @Test
    public void createProject_validInput_success() {
        ProjectRequestDto dto = new ProjectRequestDto();
        dto.setName("project");
        dto.setDescription("description");
        dto.setStatus(ProjectStatus.ACTIVE);
        dto.setOwnerId(1L);
        dto.setUsers(List.of(1L, 2L, 3L));

        User owner = new User();
        owner.setId(1L);

        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);
        User user3 = new User();
        user3.setId(3L);
        List<User> users = List.of(user1, user2, user3);

        Project project = new Project();
        project.setName("project");
        project.setDescription("description");
        project.setStatus(ProjectStatus.ACTIVE);
        project.setUsers(users);
        project.setOwner(owner);
        project.setUsers(users);

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(userRepository.findAllById(dto.getUsers())).thenReturn(users);
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0));

        Project result = projectService.createProject(dto);

        assertEquals(dto.getName(), result.getName());
        assertEquals(dto.getDescription(), result.getDescription());
        assertEquals(dto.getStatus(), result.getStatus());
        assertEquals(dto.getOwnerId(), result.getOwner().getId());

        List<Long> actualUserIds = result.getUsers().stream()
                .map(User::getId)
                .collect(Collectors.toList());

        assertEquals(dto.getUsers(), actualUserIds);
        verify(projectRepository).save(any());
    }

    @Test
    public void updateProject_validInput_success() {
        ProjectRequestDto dto = new ProjectRequestDto();
        dto.setName("Name");
        dto.setDescription("description");
        dto.setStatus(ProjectStatus.COMPLETED); // задача завершена
        dto.setOwnerId(1L);
        dto.setUsers(List.of(1L, 2L, 3L));

        User owner = new User();
        owner.setId(1L);

        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);
        User user3 = new User();
        user3.setId(3L);
        List<User> users = List.of(user1, user2, user3);

        Project project = new Project();
        project.setName("project");
        project.setDescription("description");
        project.setStatus(ProjectStatus.COMPLETED); // задача завершена
        project.setUsers(users);
        project.setOwner(owner);
        project.setUsers(users);

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findAllById(dto.getUsers())).thenReturn(users);
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0));

        Project result = projectService.updateProject(1L, dto);

        assertEquals(dto.getName(), result.getName());
        assertEquals(dto.getDescription(), result.getDescription());
        assertEquals(dto.getStatus(), result.getStatus());
        assertEquals(dto.getOwnerId(), result.getOwner().getId());

        List<Long> actualUserIds = result.getUsers().stream()
                .map(User::getId)
                .collect(Collectors.toList());

        assertEquals(dto.getUsers(), actualUserIds);
        verify(projectRepository).save(any());
    }

    @Test
    void getProject_byNonexistentId_throwException() {
        Long id = 123L;
        when(projectRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> projectService.findByIdProject(id));
    }
    @Test
    void updateProject_ownerNotFound_throwException() {
        Long id = 1L;
        ProjectRequestDto dto = new ProjectRequestDto();
        dto.setOwnerId(999L);

        Project existingProject = new Project();
        when(projectRepository.findById(id)).thenReturn(Optional.of(existingProject));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> projectService.updateProject(id, dto));
    }

}
