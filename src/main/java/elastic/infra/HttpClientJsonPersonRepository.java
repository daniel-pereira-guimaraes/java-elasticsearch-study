package elastic.infra;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
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

    private static final Gson GSON = new Gson();

    private final String indexName;

    public HttpClientJsonPersonRepository(String indexName) {
        this.indexName = indexName;
    }

    @Override
    public void save(Person person) {
        var request = configRequest(new HttpPost(uri(indexName, "_doc", person.id())));
        try {
            request.setEntity(new StringEntity(GSON.toJson(person)));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        person.initialize(documentId(executeRequest(request)));
    }

    @Override
    public Optional<Person> get(String id) {
        var request = configRequest(new HttpGet(uri(indexName, "_doc", id)));
        var result = executeRequest(request);
        return Optional.of(GSON.fromJson(source(result), Person.class));
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


    private static String uri(String... path) {
        var sb = new StringBuilder(ElasticFactory.SERVER_URL);
        Arrays.stream(path)
                .filter(s -> s != null && !s.isEmpty())
                .forEach(s -> sb.append('/').append(s));
        return sb.toString();
    }

    private static String executeRequest(HttpUriRequest request) {
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

    private static void checkStatusCode(HttpResponse response) throws IOException {
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

    private static Optional<String> errorReason(HttpResponse response) throws IOException {
        var json = contentAsString(response);
        var jsonElement = GSON.fromJson(json, JsonElement.class);
        try {
            return Optional.of(jsonElement.getAsJsonObject()
                    .getAsJsonObject("error")
                    .getAsJsonArray("root_cause")
                    .get(0)
                    .getAsJsonObject()
                    .get("reason")
                    .getAsString());
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    private static String contentAsString(HttpResponse response) throws IOException {
        return new String(response.getEntity().getContent().readAllBytes());
    }

    private static String documentId(String json) {
        var jsonElement = JsonParser.parseString(json);
        var id = jsonElement.getAsJsonObject().getAsJsonPrimitive("_id");
        return id.getAsString();
    }

    private static String source(String json) {
        var element = JsonParser.parseString(json);
        var source = element.getAsJsonObject().getAsJsonObject("_source");
        return source.toString();
    }

    private static List<Person> sources(String json) {
        var list = new ArrayList<Person>();
        var jsonElement = JsonParser.parseString(json);
        var hits = jsonElement.getAsJsonObject().getAsJsonObject("hits").getAsJsonArray("hits");
        for (var hit : hits) {
            var hitObject = hit.getAsJsonObject();
            var id = hitObject.get("_id").getAsString();
            var source = hitObject.getAsJsonObject("_source").toString();
            var person = GSON.fromJson(source, Person.class);
            person.initialize(id);
            list.add(person);
        }
        return list;
    }

    private static String urlEncode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

}
