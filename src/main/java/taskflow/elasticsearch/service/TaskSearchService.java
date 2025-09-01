package taskflow.elasticsearch.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.aggregations.TermsAggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import taskflow.elasticsearch.dto.StatusPriorityCount;
import taskflow.elasticsearch.dto.TaskSearchResponseDto;
import taskflow.elasticsearch.repository.TaskSearchRepository;
import taskflow.elasticsearch.entity.TaskIndex;
import taskflow.entity.Task;
import taskflow.enums.Priority;
import taskflow.enums.Status;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskSearchService {
    private final TaskSearchRepository taskSearchRepository;
    private final ElasticsearchClient elasticsearchClient;

    public void indexTask(Task task) {
        TaskIndex taskIndex = new TaskIndex();
        taskIndex.setId(task.getId().toString());
        taskIndex.setTitle(task.getTitle());
        taskIndex.setDescription(task.getDescription());
        taskIndex.setStatus(task.getStatus());
        taskIndex.setPriority(task.getPriority());
        taskIndex.setDeadline(task.getDeadline().atOffset(ZoneOffset.ofHours(5)));
        taskIndex.setAssignedUserId(task.getAssignedUser().getId());
        taskIndex.setProjectId(task.getProject().getId());
        log.info("лог TaskIndex отправлен в ElasticSearch ");

        taskSearchRepository.save(taskIndex);
    }

    public void deleteTaskIndex(Task task) {
        taskSearchRepository.deleteById(task.getId().toString());
    }

    public List<TaskSearchResponseDto> searchTasks(String query, Status status, Priority priority) throws IOException {
        List<Query> mustQueries = new ArrayList<>();
        List<Query> filterQueries = new ArrayList<>();

        // добавляем multi_match
        if (query != null && !query.isBlank()) {
            MultiMatchQuery multiMatchQuery = new MultiMatchQuery.Builder()
                    .query(query) // текст по которому ищем
                    .fields("title", "description") // в каких полях ищем
                    .build(); // собираем готовый объект запроса

            mustQueries.add(new Query.Builder()
                    .multiMatch(multiMatchQuery)
                    .build());
        }

        if (status != null) {
            TermQuery termQuery = new TermQuery.Builder()
                    .field("status")
                    .value(status.name())
                    .build();

            filterQueries.add(new Query.Builder()
                    .term(termQuery)
                    .build());
        }

        if (priority != null) {
            TermQuery termQuery = new TermQuery.Builder()
                    .field("priority")
                    .value(priority.name())
                    .build();

            filterQueries.add(new Query.Builder()
                    .term(termQuery)
                    .build());
        }

        BoolQuery boolQuery = new BoolQuery.Builder()
                .must(mustQueries)
                .filter(filterQueries)
                .build();

        Query finalQuery = new Query.Builder()
                .bool(boolQuery)
                .build();

        SearchRequest request = new SearchRequest.Builder()
                .index("task-index")
                .query(finalQuery)
                .build();

        System.out.println("---- Лог запроса в Elastic ----");
        System.out.println("Index: task-index");
        System.out.println("------------------------------");

        // Выполняем поиск
        SearchResponse<TaskIndex> response = elasticsearchClient.search(request, TaskIndex.class);

        // Обрабатываем результаты
        List<TaskIndex> results = new ArrayList<>();
        for (Hit<TaskIndex> hit : response.hits().hits()) {
            results.add(hit.source());
        }

        return results.stream().map(taskIndex -> new TaskSearchResponseDto(
                taskIndex.getId(),
                taskIndex.getTitle(),
                taskIndex.getDescription(),
                taskIndex.getStatus(),
                taskIndex.getPriority(),
                taskIndex.getDeadline()
        )).collect(Collectors.toList());
    }
}

