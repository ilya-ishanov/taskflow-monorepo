package taskflow.elasticsearch.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import taskflow.elasticsearch.entity.CommentIndex;
import taskflow.elasticsearch.repository.CommentSearchRepository;
import taskflow.entity.Comment;
import taskflow.entity.Task;
import taskflow.entity.User;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentSearchService {
    private final CommentSearchRepository commentSearchRepository;
    private final ElasticsearchClient elasticsearchClient;

    public void saveCommentIndex(Comment comment, Task task, User user) {
        CommentIndex commentIndex = commentIndex(comment, task, user);
        commentSearchRepository.save(commentIndex);
    }

    public void deleteCommentIndex(Comment comment) {
        commentSearchRepository.deleteById(comment.getId().toString());
    }

    private CommentIndex commentIndex(Comment comment, Task task, User user) {
        CommentIndex commentIndex = new CommentIndex();
        commentIndex.setId(comment.getId().toString());
        commentIndex.setContent(comment.getContent());
        commentIndex.setTaskId(task.getId().toString());
        commentIndex.setUserId(user.getId().toString());
        commentIndex.setCreatedAt(comment.getCreatedAt().atOffset(ZoneOffset.ofHours(5)));

        return commentIndex;
    }

    public List<CommentIndex> searchComments(String query, String taskId) throws IOException {
        List<Query> mustQueries = new ArrayList<>();
        List<Query> filterQueries = new ArrayList<>();

        // добавляем multi_match
        if (query != null && !query.isBlank()) {
            MultiMatchQuery multiMatchQuery = new MultiMatchQuery.Builder()
                    .query(query) // текст по которому ищем
                    .fields("content") // в каких полях ищем
                    .build(); // собираем готовый объект запроса

            mustQueries.add(new Query.Builder()
                    .multiMatch(multiMatchQuery)
                    .build());
        }

        if (taskId != null) {
            TermQuery termQuery = new TermQuery.Builder()
                    .field("taskId")
                    .value(taskId)
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
                .index("comment-index")
                .query(finalQuery)
                .build();

        SearchResponse<CommentIndex> response = elasticsearchClient.search(request, CommentIndex.class);

        // Обрабатываем результаты
        List<CommentIndex> results = new ArrayList<>();
        for (Hit<CommentIndex> hit : response.hits().hits()) {
            results.add(hit.source());
        }

        return results;
    }
}
