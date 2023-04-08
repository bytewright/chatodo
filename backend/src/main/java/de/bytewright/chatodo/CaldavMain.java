import com.github.caldav4j.CalDAVCollection;
import com.github.caldav4j.CalDAVConstants;
import com.github.caldav4j.CalDAVResource;
import com.github.caldav4j.exceptions.CalDAV4JException;
import com.github.caldav4j.methods.HttpCalDAVReportMethod;
import com.github.caldav4j.model.request.CalendarQuery;
import com.github.caldav4j.model.request.CompFilter;
import com.github.caldav4j.model.request.PropFilter;
import com.github.caldav4j.model.response.CalendarDataProperty;
import com.github.caldav4j.util.GenerateQuery;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class CalDavClient {
    public static void main(String[] args) throws URISyntaxException, CalDAV4JException, IOException {
        String url = "https://example.com/caldav";
        String username = "username";
        String password = "password";

        CalDAVCollection collection = new CalDAVCollection(
                new URI(url), username, password);
        collection.setCalendarDescription("My Calendar");
        collection.setDisplayName("My Calendar");

        HttpCalDAVReportMethod reportMethod = new HttpCalDAVReportMethod();
        CalendarQuery calendarQuery = new CalendarQuery();
        CompFilter compFilter = new CompFilter(
                CompFilter.Name.VCALENDAR.toString(),
                new PropFilter(
                        PropFilter.Name.UID.toString(),
                        ".*",
                        PropFilter.TextMatchType.CONTAINS
                ),
                generateTimeRange()
        );
        calendarQuery.setCompFilter(compFilter);

        reportMethod.setReportRequestEntity(new CalendarDataProperty(), calendarQuery);
        CalDAVResource[] resources = collection.report(reportMethod);

        for (CalDAVResource resource : resources) {
            InputStream inputStream = resource.getCalendarData().getInputStream();
            CalendarBuilder builder = new CalendarBuilder();
            Calendar calendar = builder.build(inputStream);
            System.out.println(calendar);
        }
    }

    private static GenerateQuery.TimeRange generateTimeRange() {
        GenerateQuery.TimeRange timeRange = new GenerateQuery.TimeRange();
        timeRange.setStart("20220101T000000Z");
        timeRange.setEnd("20221231T000000Z");
        return timeRange;
    }
}