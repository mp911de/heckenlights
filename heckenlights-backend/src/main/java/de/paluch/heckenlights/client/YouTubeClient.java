package de.paluch.heckenlights.client;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.LiveBroadcastListResponse;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Mark Paluch
 */
@Component
@Slf4j
public class YouTubeClient {

    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();
    private static final String CREDENTIALS_DIRECTORY = "oauth-credentials";
    private static final List<String> scopes = Collections.singletonList("https://www.googleapis.com/auth/youtube.readonly");
    private final GoogleClientSecrets clientSecrets;
    private final LocalServerReceiver localReceiver;
    private final LoadingCache<String, String> cache;

    public YouTubeClient(@Value("${server.port}") int serverPort) throws IOException {

        File file = new File("client_secret.json");

        if (file.exists()) {
            clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new FileReader(file));
            localReceiver = new LocalServerReceiver.Builder().setPort(serverPort + 10).build();
        } else {

            log.warn("No {}, YouTubeClient disabled", file);

            clientSecrets = null;
            localReceiver = null;
        }

        cache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build(new CacheLoader<String, String>() {
            @Override
            public String load(String s) throws Exception {
                return fetchStreamingId();
            }
        });
    }

    public Credential authorize() throws IOException {

        FileDataStoreFactory fileDataStoreFactory = new FileDataStoreFactory(new File(CREDENTIALS_DIRECTORY));
        DataStore<StoredCredential> datastore = fileDataStoreFactory.getDataStore("listbroadcasts");

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets,
                scopes).setCredentialDataStore(datastore).build();

        // Authorize.
        return new AuthorizationCodeInstalledApp(flow, localReceiver).authorize("user");
    }

    public String getYouTubeStreamingId() {

        try {
            return cache.get("youtube-streaming-id");
        } catch (ExecutionException e) {
            throw new IllegalStateException(e.getCause());
        }
    }

    private String fetchStreamingId() throws IOException {

        // This object is used to make YouTube Data API requests.
        YouTube youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, authorize()).setApplicationName("heckenlights")
                .build();

        // Create a request to list broadcasts.
        YouTube.LiveBroadcasts.List liveBroadcastRequest = youtube.liveBroadcasts().list("id,status");

        // Indicate that the API response should not filter broadcasts
        // based on their status.
        liveBroadcastRequest.setBroadcastStatus("active");
        liveBroadcastRequest.setBroadcastType("persistent");

        LiveBroadcastListResponse response = liveBroadcastRequest.execute();

        if (response.getItems().isEmpty()) {
            return "";
        }

        return response.getItems().get(0).getId();
    }
}
