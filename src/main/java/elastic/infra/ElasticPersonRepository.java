package elastic.infra;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        var json = new StringReader(toJson(person));
        var indexRequest = new IndexRequest.Builder<String>()
                .index(INDEX_NAME)
                .withJson(json)
                .build();
        try {
            var response = esClient.index(indexRequest);
            person.initialize(response.id());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String toJson(Person person) {
        return new Gson().toJson(person);
    }

    @Override
    public Optional<Person> get(String id) {
        return Optional.empty();
    }

    @Override
    public Collection<Person> getAll() {
        return null;
    }
}
