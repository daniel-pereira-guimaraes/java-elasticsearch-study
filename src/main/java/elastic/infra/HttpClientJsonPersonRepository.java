package elastic.infra;

import elastic.model.Person;
import elastic.model.PersonRepository;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HttpClientJsonPersonRepository implements PersonRepository {

    private final Serializer serializer = new Serializer();
    private final String indexName;

    public HttpClientJsonPersonRepository(String indexName) {
        this.indexName = indexName;
    }

    @Override
    public void save(Person person) {
        var request = configRequest(new HttpPost(uri(indexName, "_doc", person.id())));
        try {
            var personDocument = PersonDocument.of(person);
            request.setEntity(new StringEntity(serializer.toJson(personDocument)));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        person.initialize(documentId(executeRequest(request)));
    }

    @Override
    public Optional<Person> get(String id) {
        var request = configRequest(new HttpGet(uri(indexName, "_doc", id)));
        var result = executeRequest(request);
        return Optional.of(source(result).toPerson(id));
    }

    @Override
    public List<Person> getAll() {
        var request = configRequest(new HttpGet(uri(indexName, "_search")));
        return sources(executeRequest(request));
    }

    @Override
    public List<Person> queryByName(String name) {
        var queryParam = "_search?q=name:" + urlEncode(name);
        var request = configRequest(new HttpGet(uri(indexName, queryParam)));
        return sources(executeRequest(request));
    }

    @Override
    public List<Person> queryByCreditLimit(BigDecimal minValue, BigDecimal maxValue) {
        var queryParam = "_search?q=creditLimit:[" + minValue + "+TO+" + maxValue + "]";
        var request = configRequest(new HttpGet(uri(indexName, queryParam)));
        return sources(executeRequest(request));
    }

    @Override
    public void createIndex() {
        executeRequest(configRequest(new HttpPut(uri(indexName))));
    }

    @Override
    public void deleteIndex() {
        executeRequest(configRequest(new HttpDelete(uri(indexName))));
    }

    private static <T extends HttpUriRequest> T configRequest(T request) {
        request.setHeader("Content-Type", "application/json");
        return request;
    }


    private String uri(String... path) {
        var sb = new StringBuilder(ElasticFactory.SERVER_URL);
        Arrays.stream(path)
                .filter(s -> s != null && !s.isEmpty())
                .forEach(s -> sb.append('/').append(s));
        return sb.toString();
    }

    private String executeRequest(HttpUriRequest request) {
        var credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(
                ElasticFactory.USERNAME, ElasticFactory.PASSWORD));

        try (var client = HttpClients.custom()
                .setDefaultCredentialsProvider(credentialsProvider)
                .build()) {

            var response = client.execute(request);
            checkStatusCode(response);
            var entity = response.getEntity();
            return new String(entity.getContent().readAllBytes());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void checkStatusCode(HttpResponse response) throws IOException {
        var status = response.getStatusLine().getStatusCode();
        if (status < 200 || status > 299) {
            var message = "Request error! Status code: " + status;
            var reason = errorReason(response);
            if (reason.isPresent()) {
                message += "\nReason: " + reason.get();
            }
            throw new RuntimeException(message);
        }
    }

    private Optional<String> errorReason(HttpResponse response) throws IOException {
        var json = contentAsString(response);
        var errorResponse = serializer.fromJson(json, ErrorResponse.class);
        if (errorResponse.error == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(errorResponse.error.reason);
    }

    private String contentAsString(HttpResponse response) throws IOException {
        return new String(response.getEntity().getContent().readAllBytes());
    }

    private String documentId(String json) {
        return serializer.fromJson(json, IndexResponse.class)._id;
    }

    private PersonDocument source(String json) {
        return serializer.fromJson(json, PersonResponse.class)._source;
    }

    private List<Person> sources(String json) {
        var searchResponse = serializer.fromJson(json, SearchResponse.class);
        return searchResponse.hits.hits.stream()
                .map(hit -> hit._source.toPerson(hit._id))
                .toList();
    }

    private static String urlEncode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    public static class IndexResponse {
        public String _id;
    }

    public static class PersonResponse {
        public PersonDocument _source;
    }

    public static class SearchResponse {
        public Hits hits;

        public static class Hits {
            public List<Hit> hits;

            public static class Hit {
                public String _id;
                public PersonDocument _source;
            }
        }
    }

    public static class ErrorResponse {
        public ErrorDetail error;

        public static class ErrorDetail {
            public String reason;
        }
    }

}
