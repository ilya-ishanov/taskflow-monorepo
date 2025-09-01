package taskflow.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import taskflow.dto.request.ProjectRequestDto;
import taskflow.dto.response.ProjectResponseDto;
import taskflow.service.ProjectService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponseDto> createProject(@RequestBody @Valid ProjectRequestDto requestDto) {
        ProjectResponseDto response = ProjectResponseDto
                .from(projectService.createProject(requestDto));
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ProjectResponseDto>> findAllProjects(Pageable pageable) {
        Page<ProjectResponseDto> page = projectService.getAllProjects(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDto> findByIdProject(@PathVariable Long id) {
        ProjectResponseDto response = ProjectResponseDto
                .from(projectService.findByIdProject(id));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponseDto> updateProject(@PathVariable Long id,
                                                  @RequestBody @Valid ProjectRequestDto requestDto) {
        ProjectResponseDto response = ProjectResponseDto
                .from(projectService.updateProject(id, requestDto));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok("Проект успешно удален");
    }
}
