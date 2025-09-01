package taskflow.entity;

import jakarta.persistence.*;
import lombok.*;
import taskflow.enums.Priority;
import taskflow.enums.Status;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "tasks")
public class Task extends BaseModel {

    @Column(name = "task_title", nullable = false, length = 128)
    private String title;

    @Column(name = "task_description", nullable = false, length = 128)
    private String description;

    @Column(name = "task_status", nullable = false, length = 128)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "priority", nullable = false, length = 128)
    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Column(name = "deadline", nullable = false, length = 128)
    private LocalDateTime deadline;

    @ManyToOne
    @JoinColumn(name = "assigned_user", nullable = false)
    private User assignedUser;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
}
