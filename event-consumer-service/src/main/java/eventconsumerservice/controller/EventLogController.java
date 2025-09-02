package eventconsumerservice.controller;

import eventconsumerservice.dto.responce.DeadLetterLogResponseDto;
import eventconsumerservice.dto.responce.EventResponseDto;
import eventconsumerservice.service.LogEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/event")
public class EventLogController {
    private final LogEventService logEventService;

    @GetMapping("/processed-events")
    public ResponseEntity<List<EventResponseDto>> getProcessedEvents() {
        List<EventResponseDto> eventLogList = logEventService.getProcessedEvents()
                .stream().map(EventResponseDto::from).toList();
        return ResponseEntity.ok(eventLogList);
    }

    @GetMapping("/errors")
    public ResponseEntity<List<DeadLetterLogResponseDto>> getAllErrors() {
        List<DeadLetterLogResponseDto> deadLetterList = logEventService.getAllErrors()
                .stream().map(DeadLetterLogResponseDto::from).toList();
        return ResponseEntity.ok(deadLetterList);
    }
}
