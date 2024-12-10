
package org.navitrace.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.navitrace.helper.model.PositionUtil;
import org.navitrace.model.Device;
import org.navitrace.model.Event;
import org.navitrace.model.LogRecord;
import org.navitrace.model.Position;
import org.navitrace.session.ConnectionManager;
import org.navitrace.storage.Storage;
import org.navitrace.storage.StorageException;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AsyncSocket extends WebSocketAdapter implements ConnectionManager.UpdateListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncSocket.class);

    private static final String KEY_DEVICES = "devices";
    private static final String KEY_POSITIONS = "positions";
    private static final String KEY_EVENTS = "events";
    private static final String KEY_LOGS = "logs";

    private final ObjectMapper objectMapper;
    private final ConnectionManager connectionManager;
    private final Storage storage;
    private final long userId;

    private boolean includeLogs;

    public AsyncSocket(ObjectMapper objectMapper, ConnectionManager connectionManager, Storage storage, long userId) {
        this.objectMapper = objectMapper;
        this.connectionManager = connectionManager;
        this.storage = storage;
        this.userId = userId;
    }

    @Override
    public void onWebSocketConnect(Session session) {
        super.onWebSocketConnect(session);

        try {
            Map<String, Collection<?>> data = new HashMap<>();
            data.put(KEY_POSITIONS, PositionUtil.getLatestPositions(storage, userId));
            sendData(data);
            connectionManager.addListener(userId, this);
        } catch (StorageException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        super.onWebSocketClose(statusCode, reason);

        connectionManager.removeListener(userId, this);
    }

    @Override
    public void onWebSocketText(String message) {
        super.onWebSocketText(message);

        try {
            includeLogs = objectMapper.readTree(message).get("logs").asBoolean();
        } catch (JsonProcessingException e) {
            LOGGER.warn("Socket JSON parsing error", e);
        }
    }

    @Override
    public void onKeepalive() {
        sendData(new HashMap<>());
    }

    @Override
    public void onUpdateDevice(Device device) {
        sendData(Map.of(KEY_DEVICES, List.of(device)));
    }

    @Override
    public void onUpdatePosition(Position position) {
        sendData(Map.of(KEY_POSITIONS, List.of(position)));
    }

    @Override
    public void onUpdateEvent(Event event) {
        sendData(Map.of(KEY_EVENTS, List.of(event)));
    }

    @Override
    public void onUpdateLog(LogRecord record) {
        if (includeLogs) {
            sendData(Map.of(KEY_LOGS, List.of(record)));
        }
    }

    private void sendData(Map<String, Collection<?>> data) {
        if (isConnected()) {
            try {
                getRemote().sendString(objectMapper.writeValueAsString(data), null);
            } catch (JsonProcessingException e) {
                LOGGER.warn("Socket JSON formatting error", e);
            }
        }
    }
}
