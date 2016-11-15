package biz.paluch.heckenlights.messagebox;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParamBean;
import org.apache.http.params.HttpParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;

@SpringBootApplication
@Import({ TwitterConfiguration.class })
@ImportResource("classpath:META-INF/applicationContext.xml")
@EnableConfigurationProperties(MongoProperties.class)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    ThreadSafeClientConnManager threadSafeClientConnManager() {
        return new ThreadSafeClientConnManager();
    }

    @Bean
    BasicHttpParams httpConnectionParams(@Value("${midirelay.connectTimeout:500}") int connectTimeout,
            @Value("${midirelay.readTimeout:500}") int readTimeout) {

        BasicHttpParams params = new BasicHttpParams();
        HttpConnectionParamBean bean = new HttpConnectionParamBean(new BasicHttpParams());

        bean.setConnectionTimeout(connectTimeout);
        bean.setSoTimeout(readTimeout);

        return params;
    }

    @Bean
    DefaultHttpClient httpClient(ClientConnectionManager clientConnectionManager, HttpParams httpParams) {
        return new DefaultHttpClient(clientConnectionManager, httpParams);
    }

    @Bean
    MongoClient reactiveMongoClient(MongoProperties mongoProperties) {
        return MongoClients.create(String.format("mongodb://%s:%d/%s", mongoProperties.getHost(), mongoProperties.getPort(),
                mongoProperties.getDatabase()));
    }

    @Bean
    ReactiveMongoTemplate reactiveMongoTemplate(MongoClient mongoClient, MongoProperties mongoProperties) {
        return new ReactiveMongoTemplate(mongoClient, mongoProperties.getDatabase());
    }
}
