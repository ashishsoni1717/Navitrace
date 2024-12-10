
package org.navitrace.protocol;

import io.netty.channel.Channel;
import org.navitrace.BaseProtocolDecoder;
import org.navitrace.session.DeviceSession;
import org.navitrace.NetworkMessage;
import org.navitrace.Protocol;
import org.navitrace.helper.Parser;
import org.navitrace.helper.PatternBuilder;
import org.navitrace.model.Position;

import java.net.SocketAddress;
import java.util.regex.Pattern;

public class NeosProtocolDecoder extends BaseProtocolDecoder {

    public NeosProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .text(">")
            .number("(d{8}),")                   // id
            .number("d+,")                       // status
            .number("([01]),")                   // valid
            .number("(dd)(dd)(dd),")             // date (yymmdd)
            .number("(dd)(dd)(dd),")             // time (hhmmss)
            .expression("([EW])")
            .number("(d+)(dd.d+),")              // longitude
            .expression("([NS])")
            .number("(d+)(dd.d+),")              // latitude
            .expression("[^,]*,")                // response
            .number("(d+),")                     // speed
            .number("(d+),")                     // course
            .number("(d+),")                     // rssi
            .expression("[^,]*,")                // event data
            .number("(d+)-")                     // adc
            .number("(d+),")                     // battery
            .number("0,")
            .number("d,")
            .number("([01]{8})")                 // input
            .text("*")
            .number("xx!")
            .any()
            .compile();

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        if (channel != null) {
            channel.writeAndFlush(new NetworkMessage("$OK!", remoteAddress));
        }

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

        position.setValid(parser.nextInt() > 0);
        position.setTime(parser.nextDateTime());
        position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN));
        position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN));
        position.setSpeed(parser.nextInt());
        position.setCourse(parser.nextInt());

        position.set(Position.KEY_RSSI, parser.nextInt());
        position.set(Position.PREFIX_ADC + 1, parser.nextInt());
        position.set(Position.KEY_BATTERY_LEVEL, parser.nextInt());
        position.set(Position.KEY_INPUT, parser.nextBinInt());

        return position;
    }

}
