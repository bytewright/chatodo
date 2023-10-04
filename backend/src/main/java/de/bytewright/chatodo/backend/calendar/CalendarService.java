package de.bytewright.chatodo.backend.calendar;

import com.github.caldav4j.CalDAVCollection;
import com.github.caldav4j.exceptions.BadStatusException;
import com.github.caldav4j.exceptions.CalDAV4JException;
import de.bytewright.chatodo.backend.AppSecretsService;
import de.bytewright.chatodo.util.ConnectionSettings;
import de.bytewright.chatodo.util.HttpClientFactory;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.CalendarComponent;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;

@Service
public class CalendarService implements ApplicationListener<ContextRefreshedEvent>, HealthIndicator {
    private static final Logger log = LoggerFactory.getLogger(CalendarService.class);

    private final AppSecretsService appSecretsService;

    @Autowired
    public CalendarService(AppSecretsService appSecretsService) {
        this.appSecretsService = appSecretsService;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("Starting calendar service, attempting to connect...");
        ConnectionSettings connectionSettings = appSecretsService.getCaldavConnectionSettings();

        CalDAVCollection collection = new CalDAVCollection(connectionSettings.url());

        log.info("Col: {}", collection);
        try (CloseableHttpClient httpclient = HttpClientFactory.buildClient(appSecretsService::getCaldavConnectionSettings)) {
            Calendar calendar = collection.getCalendar(httpclient, "");
            log.info("calendar: {}", calendar);
            for (CalendarComponent component : calendar.getComponents()) {
                log.info("calendarCompo name {}: {}", component.getName(), component);
            }

            int statusCode = collection.testConnection(httpclient);
            log.info("testConn: {}", statusCode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (CalDAV4JException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Health health() {
        ConnectionSettings connectionSettings = appSecretsService.getCaldavConnectionSettings();
        CalDAVCollection collection = new CalDAVCollection(connectionSettings.url());
        try (CloseableHttpClient httpclient = HttpClientFactory.buildClient(appSecretsService::getCaldavConnectionSettings)) {
            int testedConnection = collection.testConnection(httpclient);
            log.debug("Connection test returned: {}", testedConnection);
        } catch (BadStatusException e) {
            return Health.down().withDetail("CalDAV-connection", "Connection test failed: " + e.getMessage()).build();
        } catch (Exception e) {
            return Health.down().withDetail("CalDAV-connection", "Connection test threw exception: " + e.getMessage()).build();
        }
        return Health.up().build();
    }

    public void createTodo() {

    }
}
