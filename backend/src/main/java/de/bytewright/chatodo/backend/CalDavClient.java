package de.bytewright.chatodo.backend;

import com.github.caldav4j.CalDAVCollection;
import com.github.caldav4j.exceptions.CalDAV4JException;
import de.bytewright.chatodo.util.AppSecrets;
import de.bytewright.chatodo.util.ConnectionSettings;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthenticationStrategy;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.TargetAuthenticationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;

public class CalDavClient {
    private static final Logger log = LoggerFactory.getLogger(CalDavClient.class);

    public static void main(String[] args) throws URISyntaxException, CalDAV4JException, IOException {
        //https://github.com/caldav4j/caldav4j/wiki/Getting-Started
        AppSecrets appSecrets = new AppSecrets();
        appSecrets.init();
        ConnectionSettings connectionSettings = appSecrets.getCaldavConnectionSettings();

// Instiate variables
        CalDAVCollection collection = new CalDAVCollection(connectionSettings.url());

        log.info("Col: {}", collection);
        try (CloseableHttpClient httpclient = buildClient(appSecrets)) {
            int statusCode = collection.testConnection(httpclient);
            log.info("testConn: {}", statusCode);
        }

// If you want some other server specific settings, then you'll need to do some extra code with HttpClient.
// You can find further examples here: https://hc.apache.org/httpcomponents-client-ga/examples.html

    }

    private static CloseableHttpClient buildClient(AppSecrets appSecrets) {
        ConnectionSettings connectionSettings = appSecrets.getCaldavConnectionSettings();
        HttpHost httpHost = new HttpHost(connectionSettings.url());
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
                connectionSettings.user(),
                connectionSettings.pw());
        CredentialsProvider credProv = new BasicCredentialsProvider();
        credProv.setCredentials(new AuthScope(httpHost), credentials);
        return HttpClients.custom()
                .setTargetAuthenticationStrategy(TargetAuthenticationStrategy.INSTANCE)
                .setDefaultCredentialsProvider(credProv)
                .build();
    }

}