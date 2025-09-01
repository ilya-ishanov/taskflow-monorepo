package taskflow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import taskflow.dto.kafka.CommentLogContext;
import taskflow.dto.kafka.ProjectLogContext;
import taskflow.dto.request.CommentRequestDto;
import taskflow.dto.response.CommentResponseDto;
import taskflow.elasticsearch.service.CommentSearchService;
import taskflow.entity.Comment;
import taskflow.entity.Project;
import taskflow.entity.Task;
import taskflow.entity.User;
import taskflow.exceptions.EntityNotFoundException;
import taskflow.messaging.producer.KafkaEventProducer;
import taskflow.messaging.producer.KafkaLogProducer;
import taskflow.repository.CommentRepository;
import taskflow.repository.TaskRepository;
import taskflow.repository.UserRepository;
import taskflow.validator.Validator;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final Validator validator;
    private final KafkaLogProducer kafkaLogProducer;
    private final KafkaEventProducer kafkaEventProducer;
    private final EventLogService eventLogService;
    private final CommentSearchService commentSearchService;

    @Transactional
    public Comment createComment(CommentRequestDto requestDto) {
        log.info("Сохранение комментария: {}", requestDto);
        Comment comment = saveComment(requestDto);
        log.info("Комментарий сохранен: {}", comment);
        logCommentAction(comment, "Создание коммента");
        eventCommentAction(comment, "COMMENT_CREATED");
        eventLogService.commentEventLog(comment, "COMMENT_CREATED");
        return comment;
    }

    public Comment findCommentById(Long id) {
        log.info("Поиск комменатрий по id: {}", id);
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Комменатрий", id));
        log.info("Найден комменатрий: {}", comment);
        return comment;
    }

    @Transactional
    public Comment updateComment(Long id, CommentRequestDto requestDto) {
        log.info("Получение Dto от контроллера : {}", requestDto);
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Комментарий", id));

        Long userId = requestDto.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new EntityNotFoundException("Пользователь", userId));

        Long taskId = requestDto.getTaskId();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(()-> new EntityNotFoundException("Таск", taskId));

        comment.setContent(requestDto.getContent());
        comment.setUser(user);
        comment.setTask(task);

        log.info("Update: {}", comment);
        logCommentAction(comment, "Коммент изменен");
        eventCommentAction(comment, "COMMENT_UPDATED");
        eventLogService.commentEventLog(comment, "COMMENT_UPDATED");
        commentSearchService.saveCommentIndex(comment, task, user); // elastic
        return comment;
    }

    public void deleteComment(Long id) {
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Коммент", id));
        log.info("Удаление комментария по id: {}", id);
        validator.validateEntityExists(id, commentRepository, "Комментарий");
        commentRepository.deleteById(id);
        logCommentAction(comment, "Коммент удален");
        eventCommentAction(comment, "COMMENT_DELETED");
        eventLogService.commentEventLog(comment, "COMMENT_DELETED");
        commentSearchService.deleteCommentIndex(comment); // ELASTIC
        log.error("Комментарий успешно удален: id={}", id);
    }

    public Page<CommentResponseDto> getAllComments(Pageable pageable) {
        return commentRepository.findAll(pageable).map(CommentResponseDto::from);
    }

    private Comment saveComment(CommentRequestDto requestDto) {
        Long userId = requestDto.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new EntityNotFoundException("Пользователь", userId));

        Long taskId = requestDto.getTaskId();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(()-> new EntityNotFoundException("Таск", taskId));

        Comment comment = new Comment();
        comment.setContent(requestDto.getContent());
        comment.setUser(user);
        comment.setTask(task);
        Comment result = commentRepository.save(comment);
        commentSearchService.saveCommentIndex(comment, task, user); // elastic
        return result;
    }

    private void logCommentAction(Comment comment, String message) {
        CommentLogContext commentLog = new CommentLogContext();
        commentLog.setCommentId(comment.getId());
        commentLog.setUserId(comment.getUser().getId());
        commentLog.setTaskId(comment.getTask().getId());

        kafkaLogProducer.sendLog("INFO", message, commentLog);
        log.info("Sending comment log in kafka: {}", commentLog);
    }

    private void eventCommentAction(Comment comment, String eventType) {
        kafkaEventProducer.sendCommentEvent(comment, eventType);
    }
}