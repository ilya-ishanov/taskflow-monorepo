package taskflow.elasticsearch.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.TermsAggregation;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import taskflow.elasticsearch.dto.StatusPriorityCount;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskAnalyticService {
    private final ElasticsearchClient elasticsearchClient;

    private SearchRequest buildAggregationRequest() {   // SearchRequest объект Elasticsearch Java API представляет поисковый запрос
        TermsAggregation byPriorityTerms = new TermsAggregation.Builder() //  группирует документы по уникальным значениям в поле
                .field("priority") // группировать документы по значению поля "priority"
                .size(10)
                .build();

        Aggregation byPriority = new Aggregation.Builder() // универсальный контейнер, в который можно завернуть bucket-агрегации (terms, range, date_histogram)
                .terms(byPriorityTerms)
                .build();

        TermsAggregation byStatusTerms = new TermsAggregation.Builder() //  bucket-агрегация, группирующая документы по уникальным значениям в поле
                .field("status")
                .size(10)
                .build();

        Aggregation byStatus = new Aggregation.Builder()
                .terms(byStatusTerms)
                .aggregations("by_priority", byPriority) // добавляем вложенную агрегацию по приоритета
                .build();

        return new SearchRequest.Builder() // SearchRequest поисковый запрос в Elasticsearch
                .index("task-index")
                .aggregations("by_status", byStatus) // имя, по которому потом будем искать агрегацию в ответе
                .size(0) //  не возвращай документы, мы хотим только агрегированные данные
                .build();
    }

    public List<StatusPriorityCount> getTaskAnalytics() throws IOException {
        SearchRequest request = buildAggregationRequest(); // "buildAggregationRequest" говорит, что он создаёт и возвращает запрос с агрегациями без query, только аггрегаты
        SearchResponse<Void> response = elasticsearchClient.search(request, Void.class); // SearchResponse результат выполнения запроса (SearchRequest).
        return parseAggs(response); // вызов метода parseAggs Метод принимает SearchResponse<?> и извлекает из него все агрегации.
    }

    public List<StatusPriorityCount> parseAggs(SearchResponse<?> response) {
        var result = new ArrayList<StatusPriorityCount>();

        // блок достаёт все группы по полю status, которые были собраны на уровне агрегации.
        var statusBuckets = response.aggregations()
                .get("by_status") // "by_status" — это имя той агрегации, которую добавили в SearchRequest, получаем объект Aggregate
                .sterms() // terms-агрегация по строковому полю вызываешь .sterms()
                .buckets()
                .array();

        for (var statusBucket : statusBuckets) {
            String status = statusBucket.key().stringValue();

            var priorityBuckets = statusBucket.aggregations()
                    .get("by_priority")
                    .sterms()
                    .buckets()
                    .array();

            for (var priorityBucket : priorityBuckets) {
                String priority = priorityBucket.key().stringValue();
                long count = priorityBucket.docCount();
                result.add(new StatusPriorityCount(status, priority, count));
            }
        }
        return result;
    }
}
