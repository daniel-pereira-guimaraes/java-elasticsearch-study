package elastic.infra;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.json.JsonData;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.logging.Logger;

public abstract class ElasticClientPersonRepositoryBase {

    private final Logger logger;
    protected final String indexName;
    protected final ElasticsearchClient esClient = ElasticFactory.buildElasticClient();

    protected ElasticClientPersonRepositoryBase(String indexName) {
        this.logger = Logger.getLogger(getClass().getName());
        this.indexName = indexName;
    }

    protected GetRequest buildGetRequest(String id) {
        return new GetRequest.Builder().index(indexName).id(id).build();
    }

    protected SearchRequest buildGetAllRequest(boolean onlyCustomers) {
        var builder = new SearchRequest.Builder();
        builder.index(indexName);
        if (onlyCustomers) {
            builder.query(q -> q
                    .term(TermQuery.of(t -> t
                            .field("customer").value(true))
                    )
            );
        }
        return builder.build();
    }

    protected SearchRequest buildQueryByNameRequest(String name) {
        return new SearchRequest.Builder()
                .index(indexName)
                .query(q -> q
                        .match(m -> m
                                .field("name")
                                .query(name)
                                .fuzziness("2")
                        )
                )
                .build();
    }

    protected SearchRequest buildQueryByCreditLimitRequest(BigDecimal min, BigDecimal max) {
        var rangeQuery = RangeQuery.of(q -> q
                .field("creditLimit")
                .gte(JsonData.of(min))
                .lte(JsonData.of(max))
        )._toQuery();
        return new SearchRequest.Builder()
                .index(indexName)
                .query(rangeQuery)
                .build();
    }

    public void createIndex() {
        try {
            esClient.indices().create(c -> c.index(indexName));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void deleteIndex() {
        var request = new DeleteIndexRequest.Builder().index(indexName).build();
        try {
            esClient.indices().delete(request);
            logger.info("Deleted index: " + indexName);
        } catch (IOException e) {
            logger.severe("Delete index error: " + e.getMessage());
            throw new UncheckedIOException(e);
        }
    }

}
