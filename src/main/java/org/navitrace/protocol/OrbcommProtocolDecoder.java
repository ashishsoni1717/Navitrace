
package org.navitrace.protocol;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpResponse;
import org.navitrace.BasePipelineFactory;
import org.navitrace.BaseProtocolDecoder;
import org.navitrace.session.DeviceSession;
import org.navitrace.Protocol;
import org.navitrace.helper.UnitsConverter;
import org.navitrace.model.Position;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import java.io.StringReader;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.TimeZone;

public class OrbcommProtocolDecoder extends BaseProtocolDecoder {

    public OrbcommProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        FullHttpResponse response = (FullHttpResponse) msg;
        String content = response.content().toString(StandardCharsets.UTF_8);
        JsonObject json = Json.createReader(new StringReader(content)).readObject();

        if (channel != null && !json.getString("NextStartUTC").isEmpty()) {
            OrbcommProtocolPoller poller =
                    BasePipelineFactory.getHandler(channel.pipeline(), OrbcommProtocolPoller.class);
            if (poller != null) {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                poller.setStartTime(dateFormat.parse(json.getString("NextStartUTC")));
            }
        }

        if (json.get("Messages").getValueType() == JsonValue.ValueType.NULL) {
            return null;
        }

        LinkedList<Position> positions = new LinkedList<>();

        JsonArray messages = json.getJsonArray("Messages");
        for (int i = 0; i < messages.size(); i++) {
            JsonObject message = messages.getJsonObject(i);
            DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, message.getString("MobileID"));
            if (deviceSession != null) {

                Position position = new Position(getProtocolName());
                position.setDeviceId(deviceSession.getDeviceId());

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                position.setDeviceTime(dateFormat.parse(message.getString("MessageUTC")));

                JsonArray fields = message.getJsonObject("Payload").getJsonArray("Fields");
                for (int j = 0; j < fields.size(); j++) {
                    JsonObject field = fields.getJsonObject(j);
                    String value = field.getString("Value");
                    switch (field.getString("Name").toLowerCase()) {
                        case "eventtime" -> position.setDeviceTime(new Date(Long.parseLong(value) * 1000));
                        case "latitude" -> position.setLatitude(Integer.parseInt(value) / 60000.0);
                        case "longitude" -> position.setLongitude(Integer.parseInt(value) / 60000.0);
                        case "speed" -> position.setSpeed(UnitsConverter.knotsFromKph(Integer.parseInt(value)));
                        case "heading" -> {
                            int heading = Integer.parseInt(value);
                            position.setCourse(heading <= 360 ? heading : 0);
                        }
                    }
                }

                if (position.getLatitude() != 0 && position.getLongitude() != 0) {
                    position.setValid(true);
                    position.setFixTime(position.getDeviceTime());
                } else {
                    getLastLocation(position, position.getDeviceTime());
                }

                positions.add(position);

            }
        }

        return positions.isEmpty() ? null : positions;
    }

}
