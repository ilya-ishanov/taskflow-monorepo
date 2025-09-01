package taskflow.util;


import taskflow.entity.Project;
import taskflow.entity.User;
import taskflow.enums.ProjectStatus;
import taskflow.enums.Status;

import java.util.List;

public class TestEntities {
    public static User dummyUser() {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john" + System.nanoTime() + "@test.com");
        user.setActive(true);
        return user;
    }

    public static Project dummyProject(User user) {
        Project project = new Project();
        project.setName("Test Project");
        project.setDescription("Cache testing project");
        project.setStatus(ProjectStatus.COMPLETED);
        project.setOwner(user);
        project.setUsers(List.of(user));
        return project;
    }
}
