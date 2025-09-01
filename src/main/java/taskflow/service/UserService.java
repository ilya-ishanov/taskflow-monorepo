package taskflow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import taskflow.dto.request.UserRequestDto;
import taskflow.dto.response.UserResponseDto;
import taskflow.entity.Project;
import taskflow.entity.User;
import taskflow.exceptions.EntityNotFoundException;
import taskflow.messaging.producer.KafkaEventProducer;
import taskflow.repository.ProjectRepository;
import taskflow.repository.UserRepository;
import taskflow.validator.Validator;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final Validator validator;
    private final KafkaEventProducer kafkaEventProducer;

    @Transactional
    public User createUser(UserRequestDto requestDto) {
        log.info("Сохранение юзера: {}", requestDto);
        User user = saveUser(requestDto);
        kafkaEventProducer.sendUserEvent(user, "USER_CREATED");
        log.info("Юзер сохранен: {}", user);
        return user;
    }

    @Cacheable(value = "users", key = "#id")
    public User findById(Long id) {
        log.info("Поиск юзера по id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь", id));
        log.info("Найдена юзер: {}", user);
        return user;
    }

    @Transactional
    public User update(Long id, UserRequestDto requestDto) {
        log.info("Получение Dto от контроллера : {}", requestDto);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь", id));

        user.setFirstName(requestDto.getFirstName());
        user.setLastName(requestDto.getLastName());
        user.setEmail(requestDto.getEmail());
        user.setActive(requestDto.getActive());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        kafkaEventProducer.sendUserEvent(user, "USER_UPDATED");
        log.info("Update: {}", user);
        return user;
    }

    public void delete(Long id) {
        log.info("Удаление юзера по id: {}", id);
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Проект", id));
        validator.validateEntityExists(id, userRepository, "Пользователь");
        kafkaEventProducer.sendUserEvent(user, "USER_DELETED");
        userRepository.deleteById(id);
        log.error("Юзер успешно удален: id={}", id);
    }

    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserResponseDto::from);
    }

    private User saveUser(UserRequestDto requestDto) {
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new IllegalStateException("Email уже существует");
        }
        User user = new User();
        user.setFirstName(requestDto.getFirstName());
        user.setLastName(requestDto.getLastName());
        user.setEmail(requestDto.getEmail());
        user.setActive(requestDto.getActive());

        List<Project> projects = projectRepository.findAllById(requestDto.getProjectsIds());
        user.setProjects(projects);

        return userRepository.save(user);
    }

    private void eventProjectAction(User user, String eventType) {
        kafkaEventProducer.sendUserEvent(user, eventType);
    }
}
