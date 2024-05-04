package elastic.infra;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import elastic.model.Person;
import elastic.model.PersonRepository;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.*;

public class ElasticPersonRepository implements PersonRepository {

    private static final Gson GSON = new Gson();
    private static final String SERVER_URL = "http://localhost:9200";
    public static final String INDEX_NAME = "persons";

    private final RestClient restClient = RestClient
            .builder(HttpHost.create(SERVER_URL))
            .build();
    private final ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
    private final ElasticsearchClient esClient = new ElasticsearchClient(transport);

    public ElasticPersonRepository() {
        var objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }


    @Override
    public void save(Person person) {
        var indexRequest = buildIndexRequestWithJson(person);
        try {
            var response = esClient.index(indexRequest);
            person.initialize(response.id());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static IndexRequest<String> buildIndexRequestWithJson(Person person) {
        var indexRequestBuilder = new IndexRequest.Builder<String>()
                .index(INDEX_NAME)
                .withJson(new StringReader(toJson(person)));
        if (person.id() != null) {
            indexRequestBuilder.id(person.id());
        }
        return indexRequestBuilder.build();
    }

    @Override
    public Optional<Person> get(String id) {
        var request = new GetRequest.Builder().index(INDEX_NAME).id(id).build();
        try {
            var response = esClient.get(request, ObjectNode.class);
            if (response.source() == null) {
                return Optional.empty();
            }
            return Optional.of(fromJson(response.source().toString()));
        } catch (IOException e) {
            throw new UncheckedIOException("Error while getting document from Elasticsearch", e);
        }
    }

    @Override
    public Collection<Person> getAll() {
        return null;
    }

    private static String toJson(Person person) {
        return GSON.toJson(person);
    }

    private Person fromJson(String json) {
        return GSON.fromJson(json, Person.class);
    }

}
