
package org.navitrace.protocol;

import io.netty.channel.Channel;
import org.navitrace.BaseProtocolDecoder;
import org.navitrace.session.DeviceSession;
import org.navitrace.Protocol;
import org.navitrace.helper.Parser;
import org.navitrace.helper.PatternBuilder;
import org.navitrace.model.Position;

import java.net.SocketAddress;
import java.util.regex.Pattern;

public class ArknavX8ProtocolDecoder extends BaseProtocolDecoder {

    public ArknavX8ProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN_1G = new PatternBuilder()
            .expression("(..),")                 // type
            .number("(dd)(dd)(dd)")              // date (yymmdd)
            .number("(dd)(dd)(dd),")             // time (hhmmss)
            .expression("([AV]),")               // validity
            .number("(d+)(dd.d+)([NS]),")        // latitude
            .number("(d+)(dd.d+)([EW]),")        // longitude
            .number("(d+.d+),")                  // speed
            .number("(d+),")                     // course
            .number("(d+.d+),")                  // hdop
            .number("(d+)")                      // status
            .compile();

    private static final Pattern PATTERN_2G = new PatternBuilder()
            .expression("..,")                   // type
            .number("(dd)(dd)(dd)")              // date (yymmdd)
            .number("(dd)(dd)(dd),")             // time (hhmmss)
            .number("(d+),")                     // satellites
            .number("(d+.d+),")                  // altitude
            .number("(d+.d+),")                  // power
            .number("(d+.d+),")                  // battery
            .number("(d+.d+)")                   // odometer
            .compile();

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        String sentence = (String) msg;

        if (sentence.charAt(2) != ',') {
            getDeviceSession(channel, remoteAddress, sentence.substring(0, 15));
            return null;
        }

        return switch (sentence.substring(0, 2)) {
            case "1G", "1R", "1M" -> decode1G(channel, remoteAddress, sentence);
            case "2G" -> decode2G(channel, remoteAddress, sentence);
            default -> null;
        };
    }

    private Position decode1G(Channel channel, SocketAddress remoteAddress, String sentence) {

        Parser parser = new Parser(PATTERN_1G, sentence);
        if (!parser.matches()) {
            return null;
        }

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress);
        if (deviceSession == null) {
            return null;
        }

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        position.set(Position.KEY_TYPE, parser.next());

        position.setTime(parser.nextDateTime());

        position.setValid(parser.next().equals("A"));
        position.setLatitude(parser.nextCoordinate());
        position.setLongitude(parser.nextCoordinate());
        position.setSpeed(parser.nextDouble(0));
        position.setCourse(parser.nextDouble(0));

        position.set(Position.KEY_HDOP, parser.nextDouble(0));
        position.set(Position.KEY_STATUS, parser.next());

        return position;
    }

    private Position decode2G(Channel channel, SocketAddress remoteAddress, String sentence) {

        Parser parser = new Parser(PATTERN_2G, sentence);
        if (!parser.matches()) {
            return null;
        }

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress);
        if (deviceSession == null) {
            return null;
        }

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        getLastLocation(position, parser.nextDateTime());

        position.set(Position.KEY_SATELLITES, parser.nextInt());
        position.setAltitude(parser.nextDouble());
        position.set(Position.KEY_POWER, parser.nextDouble());
        position.set(Position.KEY_BATTERY, parser.nextDouble());
        position.set(Position.KEY_ODOMETER, parser.nextDouble() * 1852 / 3600);

        return position;
    }

}
