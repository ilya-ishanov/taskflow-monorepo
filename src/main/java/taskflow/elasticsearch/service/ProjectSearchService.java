package taskflow.elasticsearch.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import taskflow.elasticsearch.entity.TaskIndex;
import taskflow.elasticsearch.repository.ProjectSearchRepository;
import taskflow.elasticsearch.entity.ProjectIndex;
import taskflow.entity.Project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectSearchService {
    private final ProjectSearchRepository projectSearchRepository;
    private final ElasticsearchClient elasticsearchClient;

    public void projectIndex(Project project) {
        ProjectIndex projectIndex = new ProjectIndex();
        projectIndex.setId(project.getId().toString());
        projectIndex.setDescription(project.getDescription());
        projectIndex.setStatus(project.getStatus());
        projectIndex.setName(project.getName());

        projectSearchRepository.save(projectIndex);
    }

    public void deleteIndexProject(Project project) {
        projectSearchRepository.deleteById(project.getId().toString());
    }

    public List<ProjectIndex> searchProjects(String query) throws IOException {
        List<Query> mustQueries = new ArrayList<>();

        if (query != null && !query.isBlank()) {
            MultiMatchQuery multiMatchQuery = new MultiMatchQuery.Builder()
                    .query(query)
                    .fields("name", "description")
                    .build();

            mustQueries.add(new Query.Builder()
                    .multiMatch(multiMatchQuery)
                    .build());
        }

        BoolQuery boolQuery = new BoolQuery.Builder()
                .must(mustQueries)
                .build();

        Query finalQuery = new Query.Builder()
                .bool(boolQuery)
                .build();

        SearchRequest request = new SearchRequest.Builder()
                .index("project-index")
                .query(finalQuery)
                .build();

        System.out.println("---- Лог запроса в Elastic ----");
        System.out.println("Index: project-index");
        System.out.println("------------------------------");

        SearchResponse<ProjectIndex> response = elasticsearchClient.search(request, ProjectIndex.class);

        List<ProjectIndex> results = new ArrayList<>();
        for (Hit<ProjectIndex> hit : response.hits().hits()) {
            results.add(hit.source());
        }

        return results;
    }
}
