package elastic.infra;

import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import elastic.model.Person;
import elastic.model.PersonRepository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class ElasticClientPersonRepository
        extends ElasticClientPersonRepositoryBase
        implements PersonRepository {

    private static final Logger LOGGER = Logger.getLogger(ElasticClientPersonRepository.class.getName());

    protected ElasticClientPersonRepository(String indexName) {
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
            var response = esClient.get(request, PersonDocument.class);
            if (response.source() == null) {
                return Optional.empty();
            }
            return Optional.of(response.source().toPerson(id));
        } catch (IOException e) {
            LOGGER.severe("Get error: " + e.getMessage());
            throw new UncheckedIOException("Error while getting document from Elasticsearch", e);
        }
    }

    @Override
    public List<Person> getAll(boolean onlyCustomers) {
        var request = buildGetAllRequest(onlyCustomers);
        try {
            var response = esClient.search(request, PersonDocument.class);
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
            var response = esClient.search(request, PersonDocument.class);
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
            var response = esClient.search(request, PersonDocument.class);
            return personsFromResponse(response);
        } catch (IOException e) {
            LOGGER.severe("Query by name error: " + e.getMessage());
            throw new UncheckedIOException(e);
        }
    }

    private IndexRequest<PersonDocument> buildIndexRequest(Person person) {
        var personDocument = PersonDocument.of(person);
        var indexRequestBuilder = new IndexRequest.Builder<PersonDocument>()
                .index(indexName)
                .document(personDocument);
        var id = person.id();
        if (id != null && !id.isBlank()) {
            indexRequestBuilder.id(id);
        }
        return indexRequestBuilder.build();
    }

    private static List<Person> personsFromResponse(SearchResponse<PersonDocument> response) {
        return response.hits().hits().stream()
                .filter(hit -> hit.source() != null)
                .map(hit -> hit.source().toPerson(hit.id()))
                .toList();
    }

}
