package elastic.infra;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import elastic.model.Repository;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

public class HttpClientRepository implements Repository<String> {

    private static final Gson gson = new Gson();

    private final String indexName;

    public HttpClientRepository(String indexName) {
        this.indexName = indexName;
    }

    @Override
    public String save(String id, String value) {
        return null;
    }

    @Override
    public Optional<String> get(String id) {
        return Optional.empty();
    }

    @Override
    public Map<String, String> getAll() {
        return null;
    }

    @Override
    public Map<String, String> queryByName(String name) {
        return null;
    }

    @Override
    public Map<String, String> queryByCreditLimit(BigDecimal minValue, BigDecimal maxValue) {
        return null;
    }

    @Override
    public void createIndex() {
        executeRequest(configRequest(new HttpPut(uri("/" + indexName))));
    }

    @Override
    public void deleteIndex() {
        executeRequest(configRequest(new HttpDelete(uri("/" + indexName))));
    }

    private static HttpUriRequest configRequest(HttpUriRequest request) {
        request.setHeader("Content-Type", "application/json");
        return request;
    }

    private static String uri(String path) {
        return ElasticFactory.SERVER_URL + path;
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
        var jsonElement = gson.fromJson(json, JsonElement.class);
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

}
