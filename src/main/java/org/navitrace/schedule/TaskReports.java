
package org.navitrace.schedule;

import com.google.inject.Injector;
import com.google.inject.servlet.RequestScoper;
import com.google.inject.servlet.ServletScopes;
import net.fortuna.ical4j.model.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.navitrace.helper.LogAction;
import org.navitrace.model.BaseModel;
import org.navitrace.model.Calendar;
import org.navitrace.model.Device;
import org.navitrace.model.Group;
import org.navitrace.model.Report;
import org.navitrace.model.User;
import org.navitrace.reports.EventsReportProvider;
import org.navitrace.reports.RouteReportProvider;
import org.navitrace.reports.StopsReportProvider;
import org.navitrace.reports.SummaryReportProvider;
import org.navitrace.reports.TripsReportProvider;
import org.navitrace.reports.common.ReportMailer;
import org.navitrace.storage.Storage;
import org.navitrace.storage.StorageException;
import org.navitrace.storage.query.Columns;
import org.navitrace.storage.query.Condition;
import org.navitrace.storage.query.Request;

import jakarta.inject.Inject;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TaskReports extends SingleScheduleTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskReports.class);

    private static final long CHECK_PERIOD_MINUTES = 15;

    private final Storage storage;
    private final Injector injector;

    @Inject
    public TaskReports(Storage storage, Injector injector) {
        this.storage = storage;
        this.injector = injector;
    }

    @Override
    public void schedule(ScheduledExecutorService executor) {
        executor.scheduleAtFixedRate(this, CHECK_PERIOD_MINUTES, CHECK_PERIOD_MINUTES, TimeUnit.MINUTES);
    }

    @Override
    public void run() {
        Date currentCheck = new Date();
        Date lastCheck = new Date(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(CHECK_PERIOD_MINUTES));

        try {
            for (Report report : storage.getObjects(Report.class, new Request(new Columns.All()))) {
                Calendar calendar = storage.getObject(Calendar.class, new Request(
                        new Columns.All(), new Condition.Equals("id", report.getCalendarId())));

                var lastEvents = calendar.findPeriods(lastCheck);
                var currentEvents = calendar.findPeriods(currentCheck);

                Set<Period<Instant>> finishedEvents = new HashSet<>(lastEvents);
                finishedEvents.removeAll(currentEvents);
                for (Period<Instant> period : finishedEvents) {
                    RequestScoper scope = ServletScopes.scopeRequest(Collections.emptyMap());
                    try (RequestScoper.CloseableScope ignored = scope.open()) {
                        executeReport(report, Date.from(period.getStart()), Date.from(period.getEnd()));
                    }
                }
            }
        } catch (StorageException e) {
            LOGGER.warn("Scheduled reports error", e);
        }
    }

    private void executeReport(Report report, Date from, Date to) throws StorageException {

        var deviceIds = storage.getObjects(Device.class, new Request(
                new Columns.Include("id"),
                new Condition.Permission(Device.class, Report.class, report.getId())))
                .stream().map(BaseModel::getId).collect(Collectors.toList());
        var groupIds = storage.getObjects(Group.class, new Request(
                new Columns.Include("id"),
                new Condition.Permission(Group.class, Report.class, report.getId())))
                .stream().map(BaseModel::getId).collect(Collectors.toList());
        var users = storage.getObjects(User.class, new Request(
                new Columns.Include("id"),
                new Condition.Permission(User.class, Report.class, report.getId())));

        ReportMailer reportMailer = injector.getInstance(ReportMailer.class);

        for (User user : users) {
            LogAction.report(user.getId(), true, report.getType(), from, to, deviceIds, groupIds);
            switch (report.getType()) {
                case "events" -> {
                    var eventsReportProvider = injector.getInstance(EventsReportProvider.class);
                    reportMailer.sendAsync(user.getId(), stream -> eventsReportProvider.getExcel(
                            stream, user.getId(), deviceIds, groupIds, List.of(), from, to));
                }
                case "route" -> {
                    var routeReportProvider = injector.getInstance(RouteReportProvider.class);
                    reportMailer.sendAsync(user.getId(), stream -> routeReportProvider.getExcel(
                            stream, user.getId(), deviceIds, groupIds, from, to));
                }
                case "summary" -> {
                    var summaryReportProvider = injector.getInstance(SummaryReportProvider.class);
                    reportMailer.sendAsync(user.getId(), stream -> summaryReportProvider.getExcel(
                            stream, user.getId(), deviceIds, groupIds, from, to, false));
                }
                case "trips" -> {
                    var tripsReportProvider = injector.getInstance(TripsReportProvider.class);
                    reportMailer.sendAsync(user.getId(), stream -> tripsReportProvider.getExcel(
                            stream, user.getId(), deviceIds, groupIds, from, to));
                }
                case "stops" -> {
                    var stopsReportProvider = injector.getInstance(StopsReportProvider.class);
                    reportMailer.sendAsync(user.getId(), stream -> stopsReportProvider.getExcel(
                            stream, user.getId(), deviceIds, groupIds, from, to));
                }
                default -> LOGGER.warn("Unsupported report type {}", report.getType());
            }
        }
    }

}
