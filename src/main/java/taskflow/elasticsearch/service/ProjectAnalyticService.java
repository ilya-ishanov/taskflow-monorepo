package taskflow.elasticsearch.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.TermsAggregation;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import taskflow.elasticsearch.dto.StatusPriorityCount;
import taskflow.elasticsearch.dto.StatusProjectCount;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectAnalyticService {
    private final ElasticsearchClient elasticsearchClient;

    private SearchRequest buildAggregationRequest() {   // SearchRequest объект Elasticsearch Java API представляет поисковый запрос

        TermsAggregation byStatusTerms = new TermsAggregation.Builder() //  bucket-агрегация, группирующая документы по уникальным значениям в поле
                .field("status.keyword")
                .size(10)
                .build();

        Aggregation byStatus = new Aggregation.Builder()
                .terms(byStatusTerms)
                .build();

        return new SearchRequest.Builder() // SearchRequest поисковый запрос в Elasticsearch
                .index("project-index")
                .aggregations("by_status", byStatus) // имя, по которому потом будем искать агрегацию в ответе
                .size(0) //  не возвращай документы, мы хотим только агрегированные данные
                .build();
    }

    public List<StatusProjectCount> getProjectAnalytics() throws IOException {
        SearchRequest request = buildAggregationRequest(); // "buildAggregationRequest" говорит, что он создаёт и возвращает запрос с агрегациями без query, только аггрегаты
        SearchResponse<Void> response = elasticsearchClient.search(request, Void.class); // SearchResponse результат выполнения запроса (SearchRequest).
        return parseAggs(response); // вызов метода parseAggs Метод принимает SearchResponse<?> и извлекает из него все агрегации.
    }

    public List<StatusProjectCount> parseAggs(SearchResponse<?> response) {
        var result = new ArrayList<StatusProjectCount>();

        // блок достаёт все группы по полю status, которые были собраны на уровне агрегации.
        var statusBuckets = response.aggregations()
                .get("by_status") // "by_status" — это имя той агрегации, которую добавили в SearchRequest, получаем объект Aggregate
                .sterms() // terms-агрегация по строковому полю вызываешь .sterms()
                .buckets()
                .array();

        for (var statusBucket : statusBuckets) {
            String status = statusBucket.key().stringValue();
            long count = statusBucket.docCount();
            result.add(new StatusProjectCount(status, count));

        }
        return result;
    }
}
