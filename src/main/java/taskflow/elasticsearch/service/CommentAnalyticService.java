package taskflow.elasticsearch.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.TermsAggregation;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import taskflow.elasticsearch.dto.StatusProjectCount;
import taskflow.elasticsearch.dto.UserIdCommentCount;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentAnalyticService {
    private final ElasticsearchClient elasticsearchClient;

    private SearchRequest buildAggregationRequest() {   // SearchRequest объект Elasticsearch Java API представляет поисковый запрос

        TermsAggregation byStatusTerms = new TermsAggregation.Builder() //  bucket-агрегация, группирующая документы по уникальным значениям в поле
                .field("userId.keyword")
                .size(10)
                .build();

        Aggregation byUserId = new Aggregation.Builder()
                .terms(byStatusTerms)
                .build();

        return new SearchRequest.Builder() // SearchRequest поисковый запрос в Elasticsearch
                .index("comment-index")
                .aggregations("by_user_id", byUserId) // имя, по которому потом будем искать агрегацию в ответе
                .size(0) //  не возвращай документы, мы хотим только агрегированные данные
                .build();
    }

    public List<UserIdCommentCount> getCommentAnalytics() throws IOException {
        SearchRequest request = buildAggregationRequest(); // "buildAggregationRequest" говорит, что он создаёт и возвращает запрос с агрегациями без query, только аггрегаты
        SearchResponse<Void> response = elasticsearchClient.search(request, Void.class); // SearchResponse результат выполнения запроса (SearchRequest).
        return parseAggs(response); // вызов метода parseAggs Метод принимает SearchResponse<?> и извлекает из него все агрегации.
    }

    public List<UserIdCommentCount> parseAggs(SearchResponse<?> response) {
        var result = new ArrayList<UserIdCommentCount>();

        // блок достаёт все группы по полю status, которые были собраны на уровне агрегации.
        var userIdBuckets = response.aggregations()
                .get("by_user_id") // "by_status" — это имя той агрегации, которую добавили в SearchRequest, получаем объект Aggregate
                .sterms() // terms-агрегация по строковому полю вызываешь .sterms()
                .buckets()
                .array();

        for (var userIdBucket : userIdBuckets) {
            String status = userIdBucket.key().stringValue();
            long count = userIdBucket.docCount();
            result.add(new UserIdCommentCount(status, count));

        }
        return result;
    }
}
