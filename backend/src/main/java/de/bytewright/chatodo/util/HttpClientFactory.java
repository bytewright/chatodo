package de.bytewright.chatodo.util;

import jakarta.inject.Provider;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.TargetAuthenticationStrategy;

import java.net.URI;
import java.net.URISyntaxException;

public class HttpClientFactory {
    public static CloseableHttpClient buildClient(
            Provider<ConnectionSettings> caldavConnectionSettings)
            throws URISyntaxException {
        ConnectionSettings connectionSettings = caldavConnectionSettings.get();
        HttpHost httpHost = new HttpHost(connectionSettings.url());
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
                connectionSettings.user(),
                connectionSettings.pw());
        CredentialsProvider credProv = new BasicCredentialsProvider();
        URI calServer = new URI(httpHost.toHostString());
        var host = calServer.getHost();
        if (!calServer.getScheme().equalsIgnoreCase("https")) {
            throw new UnsupportedOperationException("Use https server");
        }
        int port = calServer.getPort() == -1 ? 443 : calServer.getPort();
        AuthScope authScope = new AuthScope(host, port);
        credProv.setCredentials(authScope, credentials);
        return HttpClients.custom()
                .setTargetAuthenticationStrategy(TargetAuthenticationStrategy.INSTANCE)
                .setDefaultCredentialsProvider(credProv)
                .build();
    }
}
