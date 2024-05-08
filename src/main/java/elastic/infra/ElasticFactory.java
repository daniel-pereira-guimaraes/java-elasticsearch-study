package elastic.infra;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;

import java.util.logging.Logger;

public class ElasticFactory {
    private static final Logger LOGGER = Logger.getLogger(ElasticFactory.class.getName());

    public static final String SERVER_URL = "http://localhost:9200";
    public static final String USERNAME = System.getenv("ES_USERNAME");
    public static final String PASSWORD = System.getenv("ES_PASSWORD");

    private ElasticFactory() {

    }

    public static ElasticsearchClient buildElasticClient() {
        LOGGER.info("Connecting to " + SERVER_URL);
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(USERNAME, PASSWORD));
        var restClient = RestClient.builder(HttpHost.create(SERVER_URL))
                .setHttpClientConfigCallback(httpClientBuilder ->
                        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
                .build();
        var transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }

}
