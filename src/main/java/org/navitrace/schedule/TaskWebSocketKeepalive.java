
package org.navitrace.schedule;

import org.navitrace.session.ConnectionManager;

import jakarta.inject.Inject;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TaskWebSocketKeepalive implements ScheduleTask {

    private static final long PERIOD_SECONDS = 55;

    private final ConnectionManager connectionManager;

    @Inject
    public TaskWebSocketKeepalive(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public void schedule(ScheduledExecutorService executor) {
        executor.scheduleAtFixedRate(this, PERIOD_SECONDS, PERIOD_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        connectionManager.sendKeepalive();
    }

}