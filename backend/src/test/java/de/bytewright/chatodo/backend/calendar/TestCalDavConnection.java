package de.bytewright.chatodo.backend.calendar;


import com.github.caldav4j.CalDAVCollection;
import com.github.caldav4j.exceptions.CalDAV4JException;
import de.bytewright.chatodo.util.HttpClientFactory;
import org.apache.http.HttpStatus;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;

public class TestCalDavConnection extends BaseChatodoCalenderTest {

    @Test
    void testConnection() throws URISyntaxException, CalDAV4JException {
        try (CloseableHttpClient httpclient = HttpClientFactory.buildClient(this::getConSettings)) {
            CalDAVCollection collection = new CalDAVCollection(getConSettings().url());
            int statusCode = collection.testConnection(httpclient);
            Assertions.assertEquals(HttpStatus.SC_OK, statusCode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
