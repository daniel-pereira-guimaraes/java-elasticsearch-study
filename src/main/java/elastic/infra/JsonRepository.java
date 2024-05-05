package elastic.infra;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.node.ObjectNode;
import elastic.model.Repository;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.logging.Logger;

public class JsonRepository implements Repository<String> {

    private static final Logger LOGGER = Logger.getLogger(JsonRepository.class.getName());
    private static final String SERVER_URL = "http://localhost:9200";
    private static final ElasticsearchClient esClient = buildElasticClient();

    private final String indexName;

    public JsonRepository(String indexName) {
        this.indexName = indexName;
    }

    @Override
    public String save(String id, String json) {
        var indexRequest = buildIndexRequest(id, json);
        try {
            var response = esClient.index(indexRequest);
            return response.id();
        } catch (IOException e) {
            LOGGER.severe("Save error: " + e.getMessage());
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Optional<String> get(String id) {
        var request = new GetRequest.Builder().index(indexName).id(id).build();
        try {
            var response = esClient.get(request, ObjectNode.class);
            if (response.source() == null) {
                return Optional.empty();
            }
            return Optional.of(response.source().toString());
        } catch (IOException e) {
            LOGGER.severe("Get error: " + e.getMessage());
            throw new UncheckedIOException("Error while getting document from Elasticsearch", e);
        }
    }

    @Override
    public Map<String, String> getAll() {
        var result = new HashMap<String, String>();
        var request = SearchRequest.of(a -> a.index(indexName));
        try {
            var search = esClient.search(request, ObjectNode.class);
            search.hits().hits().forEach(h -> {
                if (h.source() != null) {
                    result.put(h.id(), h.source().toString());
                }
            });
        } catch (IOException e) {
            LOGGER.severe("Get all error: " + e.getMessage());
            throw new UncheckedIOException(e);
        }
        return result;
    }

    @Override
    public Map<String, String> queryByName(String name) {
        var result = new HashMap<String, String>();
        try {
            var response = esClient.search(s -> s
                    .index(indexName)
                    .query(q -> q
                            .match(m -> m
                                    .field("name")
                                    .query(name)
                            )
                    ),
                    ObjectNode.class
            );
            response.hits().hits().stream()
                    .filter(h -> h.source() != null)
                    .forEach(h -> result.put(h.id(), h.source().toString()));
        } catch (IOException e) {
            LOGGER.severe("Query by name error: " + e.getMessage());
            throw new UncheckedIOException(e);
        }
        return result;
    }

    @Override
    public void deleteIndex() {
        var request = new DeleteIndexRequest.Builder().index(indexName).build();
        try {
            esClient.indices().delete(request);
            LOGGER.info("Deleted index: " + indexName);
        } catch (IOException e) {
            LOGGER.severe("Delete index error: " + e.getMessage());
            throw new UncheckedIOException(e);
        }
    }

    private static ElasticsearchClient buildElasticClient() {
        var restClient = RestClient.builder(HttpHost.create(SERVER_URL)).build();
        var transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }

    private IndexRequest<String> buildIndexRequest(String id, String json) {
        var indexRequestBuilder = new IndexRequest.Builder<String>()
                .index(indexName)
                .withJson(new StringReader(json));
        if (id != null && !id.isBlank()) {
            indexRequestBuilder.id(id);
        }
        return indexRequestBuilder.build();
    }

}
