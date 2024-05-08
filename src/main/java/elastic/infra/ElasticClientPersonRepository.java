package elastic.infra;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.json.JsonData;
import elastic.model.Person;
import elastic.model.PersonRepository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class ElasticClientPersonRepository implements PersonRepository {

    private static final Logger LOGGER = Logger.getLogger(ElasticClientPersonRepository.class.getName());

    private final ElasticsearchClient esClient = ElasticFactory.buildElasticClient();
    private final String indexName;

    public ElasticClientPersonRepository(String indexName) {
        this.indexName = indexName;
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
        var request = new GetRequest.Builder().index(indexName).id(id).build();
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
    public List<Person> getAll() {
        var request = SearchRequest.of(a -> a.index(indexName));
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
        try {
            var response = esClient.search(s -> s
                    .index(indexName)
                    .query(q -> q
                            .match(m -> m
                                    .field("name")
                                    .query(name)
                                    .fuzziness("2")
                            )
                    ),
                    PersonDocument.class
            );
            return personsFromResponse(response);
        } catch (IOException e) {
            LOGGER.severe("Query by name error: " + e.getMessage());
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public List<Person> queryByCreditLimit(BigDecimal minValue, BigDecimal maxValue) {
        try {
            var rangeQuery = RangeQuery.of(rq -> rq
                    .field("creditLimit")
                    .gte(JsonData.of(minValue))
                    .lte(JsonData.of(maxValue))
            )._toQuery();

            var response = esClient.search(s -> s
                    .index(indexName)
                    .query(rangeQuery),
                    PersonDocument.class
            );

            return personsFromResponse(response);
        } catch (IOException e) {
            LOGGER.severe("Query by name error: " + e.getMessage());
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void createIndex() {
        try {
            esClient.indices().create(c -> c
                    .index(indexName)
            );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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
