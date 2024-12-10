
package org.navitrace.protocol;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.navitrace.BaseHttpProtocolDecoder;
import org.navitrace.session.DeviceSession;
import org.navitrace.Protocol;
import org.navitrace.model.Position;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class LeafSpyProtocolDecoder extends BaseHttpProtocolDecoder {

    public LeafSpyProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        FullHttpRequest request = (FullHttpRequest) msg;
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        Map<String, List<String>> params = decoder.parameters();
        if (params.isEmpty()) {
            decoder = new QueryStringDecoder(request.content().toString(StandardCharsets.US_ASCII), false);
            params = decoder.parameters();
        }

        Position position = new Position(getProtocolName());
        position.setValid(true);

        for (Map.Entry<String, List<String>> entry : params.entrySet()) {
            for (String value : entry.getValue()) {
                switch (entry.getKey()) {
                    case "pass" -> {
                        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, value);
                        if (deviceSession == null) {
                            sendResponse(channel, HttpResponseStatus.BAD_REQUEST);
                            return null;
                        }
                        position.setDeviceId(deviceSession.getDeviceId());
                    }
                    case "Lat" -> position.setLatitude(Double.parseDouble(value));
                    case "Long" -> position.setLongitude(Double.parseDouble(value));
                    case "RPM" -> {
                        position.set(Position.KEY_RPM, Integer.parseInt(value));
                        position.setSpeed(convertSpeed(Double.parseDouble(value) / 63, "kmh"));
                    }
                    case "Elv" -> position.setAltitude(Double.parseDouble(value));
                    case "SOC" -> position.set(Position.KEY_BATTERY_LEVEL, Double.parseDouble(value));
                    case "user" -> position.set(Position.KEY_DRIVER_UNIQUE_ID, value);
                    case "ChrgMode" -> position.set(Position.KEY_CHARGE, Integer.parseInt(value) != 0);
                    case "Odo" -> position.set(Position.KEY_OBD_ODOMETER, Integer.parseInt(value) * 1000);
                    default -> {
                        try {
                            position.set(entry.getKey(), Double.parseDouble(value));
                        } catch (NumberFormatException e) {
                            switch (value) {
                                case "true" -> position.set(entry.getKey(), true);
                                case "false" -> position.set(entry.getKey(), false);
                                default -> position.set(entry.getKey(), value);
                            }
                        }
                    }
                }
            }
        }

        if (position.getFixTime() == null) {
            position.setTime(new Date());
        }

        if (position.getLatitude() == 0 && position.getLongitude() == 0) {
            getLastLocation(position, position.getDeviceTime());
        }

        if (position.getDeviceId() != 0) {
            sendResponse(channel, HttpResponseStatus.OK,
                    Unpooled.copiedBuffer("\"status\":\"0\"", StandardCharsets.US_ASCII));
            return position;
        } else {
            sendResponse(channel, HttpResponseStatus.BAD_REQUEST);
            return null;
        }
    }

}
