package taskflow.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import taskflow.dto.request.CommentRequestDto;
import taskflow.dto.response.CommentResponseDto;
import taskflow.service.CommentService;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponseDto> createComment(@RequestBody @Valid CommentRequestDto requestDto) {
        CommentResponseDto comment = CommentResponseDto
                .from(commentService.createComment(requestDto));
        return ResponseEntity.ok(comment);
    }

    @GetMapping
    public ResponseEntity<Page<CommentResponseDto>> findAllComments(Pageable pageable) {
        Page<CommentResponseDto> comment = commentService.getAllComments(pageable);
        return ResponseEntity.ok(comment);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommentResponseDto> findCommentById(@PathVariable Long id) {
        CommentResponseDto comment = CommentResponseDto
                .from(commentService.findCommentById(id));
        return ResponseEntity.ok(comment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentResponseDto> updateComment(@PathVariable Long id,
                                                            @RequestBody @Valid CommentRequestDto requestDto) {
        CommentResponseDto response = CommentResponseDto
                .from(commentService.updateComment(id, requestDto));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.ok("Комментарий успешно удален");
    }
}
