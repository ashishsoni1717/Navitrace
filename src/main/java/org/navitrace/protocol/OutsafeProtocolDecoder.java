
package org.navitrace.protocol;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.navitrace.BaseHttpProtocolDecoder;
import org.navitrace.session.DeviceSession;
import org.navitrace.Protocol;
import org.navitrace.model.Position;

import jakarta.json.Json;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import java.io.StringReader;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public class OutsafeProtocolDecoder extends BaseHttpProtocolDecoder {

    public OutsafeProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        FullHttpRequest request = (FullHttpRequest) msg;
        String content = request.content().toString(StandardCharsets.UTF_8);
        JsonObject json = Json.createReader(new StringReader(content)).readObject();

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, json.getString("device"));
        if (deviceSession == null) {
            sendResponse(channel, HttpResponseStatus.BAD_REQUEST);
            return null;
        }

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        position.setTime(new Date());
        position.setValid(true);
        position.setLatitude(json.getJsonNumber("latitude").doubleValue());
        position.setLongitude(json.getJsonNumber("longitude").doubleValue());
        position.setAltitude(json.getJsonNumber("altitude").doubleValue());
        position.setCourse(json.getJsonNumber("heading").intValue());

        position.set(Position.KEY_RSSI, json.getJsonNumber("rssi").intValue());
        position.set("origin", json.getString("origin"));

        JsonObject data = json.getJsonObject("data");
        for (Map.Entry<String, JsonValue> entry : data.entrySet()) {
            decodeUnknownParam(entry.getKey(), entry.getValue(), position);
        }

        sendResponse(channel, HttpResponseStatus.OK);
        return position;
    }

    private void decodeUnknownParam(String name, JsonValue value, Position position) {
        if (value instanceof JsonNumber jsonNumber) {
            position.set(name, jsonNumber.doubleValue());
        } else if (value instanceof JsonString jsonString) {
            position.set(name, jsonString.getString());
        } else if (value == JsonValue.TRUE || value == JsonValue.FALSE) {
            position.set(name, value == JsonValue.TRUE);
        }
    }

}
