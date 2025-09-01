package taskflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import taskflow.enums.ProjectStatus;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "projects")
public class Project extends BaseModel {

    @Column(name = "project_name", nullable = false, length = 128)
    private String name;

    @Column(name = "project_description", nullable = false, length = 128)
    private String description;

    @Column(name = "project_status", nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    private ProjectStatus status;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToMany(mappedBy = "projects")
    private List<User> users;
}
