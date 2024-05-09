package elastic.infra;

import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.fasterxml.jackson.databind.node.ObjectNode;
import elastic.model.Person;
import elastic.model.PersonRepository;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;

public class ElasticClientJsonPersonRepository
        extends ElasticClientPersonRepositoryBase
        implements PersonRepository {

    private static final Logger LOGGER = Logger.getLogger(ElasticClientJsonPersonRepository.class.getName());
    private final Serializer serializer = new Serializer();

    protected ElasticClientJsonPersonRepository(String indexName) {
        super(indexName);
    }

    @Override
    public void save(Person person) {
        var indexRequest = buildIndexRequest(person);
        try {
            var response = esClient.index(indexRequest);
            person.initialize(response.id());
        } catch (IOException e) {
            LOGGER.severe("Save error: " + e.getMessage());
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Optional<Person> get(String id) {
        var request = buildGetRequest(id);
        try {
            var response = esClient.get(request, ObjectNode.class);
            if (!response.found() || response.source() == null) {
                return Optional.empty();
            }
            return Optional.of(personFromNode(response.id(), response.source()));
        } catch (IOException e) {
            LOGGER.severe("Get error: " + e.getMessage());
            throw new UncheckedIOException("Error while getting document from Elasticsearch", e);
        }
    }

    @Override
    public List<Person> getAll(boolean onlyCustomers) {
        var request = buildGetAllRequest(onlyCustomers);
        try {
            var response = esClient.search(request, ObjectNode.class);
            return personsFromResponse(response);
        } catch (IOException e) {
            LOGGER.severe("Get all error: " + e.getMessage());
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public List<Person> queryByName(String name) {
        var request = buildQueryByNameRequest(name);
        try {
            var response = esClient.search(request, ObjectNode.class);
            return personsFromResponse(response);
        } catch (IOException e) {
            LOGGER.severe("Query by name error: " + e.getMessage());
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public List<Person> queryByCreditLimit(BigDecimal minValue, BigDecimal maxValue) {
        var request = buildQueryByCreditLimitRequest(minValue, maxValue);
        try {
            var response = esClient.search(request, ObjectNode.class);
            return personsFromResponse(response);
        } catch (IOException e) {
            LOGGER.severe("Query by name error: " + e.getMessage());
            throw new UncheckedIOException(e);
        }
    }

    private IndexRequest<String> buildIndexRequest(Person person) {
        var personDocument = PersonDocument.of(person);
        var jsonReader = new StringReader(serializer.toJson(personDocument));
        var indexRequestBuilder = new IndexRequest.Builder<String>()
                .index(indexName).withJson(jsonReader);
        var id = person.id();
        if (id != null && !id.isBlank()) {
            indexRequestBuilder.id(id);
        }
        return indexRequestBuilder.build();
    }

    private List<Person> personsFromResponse(SearchResponse<ObjectNode> response) {
        return response.hits().hits().stream()
                .filter(hit -> hit.source() != null)
                .map(hit -> personFromNode(hit.id(), hit.source()))
                .toList();
    }

    private Person personFromNode(String id, ObjectNode node) {
        var personDocument = serializer.fromJson(node.toString(), PersonDocument.class);
        return personDocument.toPerson(id);
    }

}
