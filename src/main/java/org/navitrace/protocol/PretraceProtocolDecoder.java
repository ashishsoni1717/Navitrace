
package org.navitrace.protocol;

import io.netty.channel.Channel;
import org.navitrace.BaseProtocolDecoder;
import org.navitrace.session.DeviceSession;
import org.navitrace.Protocol;
import org.navitrace.helper.Parser;
import org.navitrace.helper.PatternBuilder;
import org.navitrace.helper.UnitsConverter;
import org.navitrace.model.Position;

import java.net.SocketAddress;
import java.util.regex.Pattern;

public class PretraceProtocolDecoder extends BaseProtocolDecoder {

    public PretraceProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .text("(")
            .number("(d{15})")                   // imei
            .number("Uddd")                      // type
            .number("d")                         // gps type
            .expression("([AV])")                // validity
            .number("(dd)(dd)(dd)")              // date (yymmdd)
            .number("(dd)(dd)(dd)")              // time (hhmmss)
            .number("(dd)(dd.dddd)")             // latitude
            .expression("([NS])")
            .number("(ddd)(dd.dddd)")            // longitude
            .expression("([EW])")
            .number("(ddd)")                     // speed
            .number("(ddd)")                     // course
            .number("(xxx)")                     // altitude
            .number("(x{8})")                    // odometer
            .number("(x)")                       // satellites
            .number("(dd)")                      // hdop
            .number("(dd)")                      // gsm
            .expression("(.{8}),&")              // state
            .expression("(.+)?")                 // optional data
            .text("^")
            .number("xx")                        // checksum
            .compile();

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        Parser parser = new Parser(PATTERN, (String) msg);
        if (!parser.matches()) {
            return null;
        }

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, parser.next());
        if (deviceSession == null) {
            return null;
        }

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        position.setValid(parser.next().equals("A"));

        position.setTime(parser.nextDateTime());

        position.setLatitude(parser.nextCoordinate());
        position.setLongitude(parser.nextCoordinate());
        position.setSpeed(UnitsConverter.knotsFromKph(parser.nextInt(0)));
        position.setCourse(parser.nextInt(0));
        position.setAltitude(parser.nextHexInt(0));

        position.set(Position.KEY_ODOMETER, parser.nextHexInt(0));
        position.set(Position.KEY_SATELLITES, parser.nextHexInt(0));
        position.set(Position.KEY_HDOP, parser.nextInt(0));
        position.set(Position.KEY_RSSI, parser.nextInt(0));

        parser.next(); // state

        if (parser.hasNext()) {
            for (String value : parser.next().split(",")) {
                switch (value.charAt(0)) {
                    case 'P':
                        if (value.charAt(1) == '1') {
                            if (value.charAt(4) == '%') {
                                position.set(Position.KEY_BATTERY_LEVEL, Integer.parseInt(value.substring(2, 4)));
                            } else {
                                position.set(Position.KEY_BATTERY, Integer.parseInt(value.substring(2), 16) * 0.01);
                            }
                        } else {
                            position.set(Position.KEY_POWER, Integer.parseInt(value.substring(2), 16) * 0.01);
                        }
                        break;
                    case 'T':
                        double temperature = Integer.parseInt(value.substring(2), 16) * 0.25;
                        if (value.charAt(1) == '1') {
                            position.set(Position.KEY_DEVICE_TEMP, temperature);
                        } else {
                            position.set(Position.PREFIX_TEMP + (value.charAt(1) - '0'), temperature);
                        }
                        break;
                    case 'F':
                        position.set("fuel" + (value.charAt(1) - '0'), Integer.parseInt(value.substring(2), 16) * 0.01);
                        break;
                    case 'R':
                        position.set(Position.KEY_DRIVER_UNIQUE_ID, value.substring(3));
                        break;
                    default:
                        break;
                }
            }
        }

        return position;
    }

}