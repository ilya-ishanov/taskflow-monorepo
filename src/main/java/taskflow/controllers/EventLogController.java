package taskflow.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import taskflow.dto.kafka.EventResponseDto;
import taskflow.service.EventLogService;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventLogController {
    private final EventLogService eventLogService;

    @GetMapping
    public ResponseEntity<List<EventResponseDto>> gentAllEventLogs() {
        List<EventResponseDto> eventResponseDtoList = eventLogService.getAllEventLogs()
                .stream().map(EventResponseDto::from).toList();

        return ResponseEntity.ok(eventResponseDtoList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDto> gentEventLog(@PathVariable Long id) {
        EventResponseDto eventResponseDto = EventResponseDto
                .from(eventLogService.gentEventLog(id));
        return ResponseEntity.ok(eventResponseDto);
    }
}
