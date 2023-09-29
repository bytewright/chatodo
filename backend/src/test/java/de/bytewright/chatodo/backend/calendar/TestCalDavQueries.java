package de.bytewright.chatodo.backend.calendar;


import com.github.caldav4j.CalDAVCollection;
import com.github.caldav4j.exceptions.CalDAV4JException;
import com.github.caldav4j.model.request.CalendarQuery;
import com.github.caldav4j.util.GenerateQuery;
import de.bytewright.chatodo.util.HttpClientFactory;
import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Status;
import org.apache.http.HttpStatus;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

public class TestCalDavQueries extends BaseChatodoCalenderTest {

    private static final Logger log = LoggerFactory.getLogger(TestCalDavQueries.class);

    @Test
    void testFetchNextEvent() throws URISyntaxException, CalDAV4JException {
        try (CloseableHttpClient httpclient = HttpClientFactory.buildClient(this::getConSettings)) {
            CalDAVCollection collection = new CalDAVCollection(getConSettings().url());
            runQuery(httpclient, collection);
            int statusCode = collection.testConnection(httpclient);
            Assertions.assertEquals(HttpStatus.SC_OK, statusCode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testFetchNextTodo() throws URISyntaxException, CalDAV4JException {
        try (CloseableHttpClient httpclient = HttpClientFactory.buildClient(this::getConSettings)) {
            CalDAVCollection collection = new CalDAVCollection(getConSettings().url());
            runTodoQuery(httpclient, collection);
            int statusCode = collection.testConnection(httpclient);
            Assertions.assertEquals(HttpStatus.SC_OK, statusCode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void runTodoQuery(CloseableHttpClient httpclient, CalDAVCollection collection) throws CalDAV4JException {
        GenerateQuery gq = new GenerateQuery();
        List<String> props = new LinkedList<>();
        props.add(Property.STATUS + "!=" + Status.VTODO_CANCELLED);
       // props.add(Property.COMPLETED + "==UNDEF");
        props.add(Property.DTSTART + "==[;20220810]");
        gq.setFilter(Component.VTODO, props);
        CalendarQuery query = gq.generate();
        log.info(gq.prettyPrint());
        List<Calendar> calendars = collection.queryCalendars(httpclient, query);
        printCalendars(calendars);
    }

    private void runQuery(CloseableHttpClient httpclient, CalDAVCollection collection) throws CalDAV4JException {
        GenerateQuery gq = new GenerateQuery();
        gq.setComponent(Component.VEVENT);
        // retrieve the given properties
        gq.setRequestedComponentProperties(List.of(Property.UID, Property.SUMMARY, Property.ATTENDEE, Property.DTSTART, Property.DTEND));
        gq.setTimeRange(new DateTime(Date.from(Instant.now())), null);
        CalendarQuery query = gq.generate();
        log.info(gq.prettyPrint());
        List<Calendar> calendars = collection.queryCalendars(httpclient, query);
        log.info("----------------------------------------------------");
        log.info("got {} results from query", calendars.size());
        printCalendars(calendars);
    }

    private void printCalendars(List<Calendar> calendars) {
        for (Calendar calendar : calendars) {
            log.info("queryresult for calendar with these components: '{}'", calendar.getComponents());
            for (CalendarComponent component : calendar.getComponents()) {
                log.info("CalendarComponent: {}", component.getName());
                if (Component.VEVENT.equals(component.getName())) {
                    VEvent vEvent = (VEvent) component;
                    DtStart startDate = vEvent.getStartDate();
                    TimeZone timeZone = startDate.getTimeZone();
                    log.info("Got VEvent: {}", vEvent.getSummary());
                    if (timeZone != null) {
                        LocalDateTime localDateTime = LocalDateTime.ofInstant(startDate.getDate().toInstant(), timeZone.toZoneId());
                        log.info("it starts at: {}", localDateTime);
                    }
                }
            }

        }
    }
}
