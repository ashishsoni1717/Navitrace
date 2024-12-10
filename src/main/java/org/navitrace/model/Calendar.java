
package org.navitrace.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;
import org.navitrace.storage.QueryIgnore;
import org.navitrace.storage.StorageName;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@StorageName("tc_calendars")
public class Calendar extends ExtendedModel {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private byte[] data;

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) throws IOException, ParserException {
        CalendarBuilder builder = new CalendarBuilder();
        calendar = builder.build(new ByteArrayInputStream(data));
        this.data = data;
    }

    private net.fortuna.ical4j.model.Calendar calendar;

    @QueryIgnore
    @JsonIgnore
    public net.fortuna.ical4j.model.Calendar getCalendar() {
        return calendar;
    }

    public Set<Period<Instant>> findPeriods(Date date) {
        if (calendar != null) {
            var period = new Period<>(date.toInstant(), Duration.ZERO);
            return calendar.<VEvent>getComponents(CalendarComponent.VEVENT).stream()
                    .flatMap(c -> c.<Instant>calculateRecurrenceSet(period).stream())
                    .collect(Collectors.toUnmodifiableSet());
        } else {
            return Set.of();
        }
    }

    public boolean checkMoment(Date date) {
        return !findPeriods(date).isEmpty();
    }

}
