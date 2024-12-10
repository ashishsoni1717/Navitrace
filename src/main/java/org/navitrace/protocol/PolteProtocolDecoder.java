
package org.navitrace.protocol;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.navitrace.BaseHttpProtocolDecoder;
import org.navitrace.session.DeviceSession;
import org.navitrace.Protocol;
import org.navitrace.model.Position;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import java.io.StringReader;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class PolteProtocolDecoder extends BaseHttpProtocolDecoder {

    public PolteProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        FullHttpRequest request = (FullHttpRequest) msg;
        String content = request.content().toString(StandardCharsets.UTF_8);
        JsonObject json = Json.createReader(new StringReader(content)).readObject();

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, json.getString("ueToken"));
        if (deviceSession == null) {
            sendResponse(channel, HttpResponseStatus.BAD_REQUEST);
            return null;
        }

        if (json.containsKey("location")) {

            Position position = new Position(getProtocolName());
            position.setDeviceId(deviceSession.getDeviceId());

            JsonObject location = json.getJsonObject("location");

            position.setValid(true);
            position.setTime(new Date(location.getInt("detected_at") * 1000L));
            position.setLatitude(location.getJsonNumber("latitude").doubleValue());
            position.setLongitude(location.getJsonNumber("longitude").doubleValue());
            position.setAltitude(location.getJsonNumber("altitude").doubleValue());

            if (json.containsKey("report")) {
                JsonObject report = json.getJsonObject("report");
                position.set(Position.KEY_EVENT, report.getInt("event"));
                if (report.containsKey("battery")) {
                    JsonObject battery = report.getJsonObject("battery");
                    position.set(Position.KEY_BATTERY_LEVEL, battery.getInt("level"));
                    position.set(Position.KEY_BATTERY, battery.getJsonNumber("voltage").doubleValue());
                }
            }

            return position;

        }

        sendResponse(channel, HttpResponseStatus.OK);
        return null;
    }

}
