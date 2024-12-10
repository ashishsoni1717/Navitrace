
package org.navitrace.protocol;

import io.netty.channel.Channel;
import org.navitrace.BaseProtocolDecoder;
import org.navitrace.session.DeviceSession;
import org.navitrace.Protocol;
import org.navitrace.helper.Parser;
import org.navitrace.helper.PatternBuilder;
import org.navitrace.helper.UnitsConverter;
import org.navitrace.model.CellTower;
import org.navitrace.model.Network;
import org.navitrace.model.Position;

import java.net.SocketAddress;
import java.util.regex.Pattern;

public class AvemaProtocolDecoder extends BaseProtocolDecoder {

    public AvemaProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .number("(d+),")                     // device id
            .number("(dddd)(dd)(dd)")            // date (yyyymmdd)
            .number("(dd)(dd)(dd),")             // time (hhmmss)
            .number("(-?d+.d+),")                // longitude
            .number("(-?d+.d+),")                // latitude
            .number("(d+),")                     // speed
            .number("(d+),")                     // course
            .number("(-?d+),")                   // altitude
            .number("(d+),")                     // satellites
            .number("(d+),")                     // event
            .number("(d+.d+),")                  // odometer
            .number("(d+),")                     // input
            .number("(d+.d+)V?,")                // adc 1
            .number("(d+.d+)V?,")                // adc 2
            .number("(d+),")                     // output
            .number("(d),")                      // roaming
            .number("(d+),")                     // rssi
            .number("d,")                        // communication system
            .number("(ddd)-?")                   // mcc
            .number("(d+),")                     // mnc
            .number("(x+),")                     // lac
            .number("(x+),")                     // cid
            .number("(d+.d+),").optional()       // battery
            .number("([^,]+)?")                  // rfid
            .any()
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

        position.setValid(true);
        position.setTime(parser.nextDateTime());
        position.setLongitude(parser.nextDouble());
        position.setLatitude(parser.nextDouble());
        position.setSpeed(UnitsConverter.knotsFromKph(parser.nextInt()));
        position.setCourse(parser.nextInt());
        position.setAltitude(parser.nextInt());

        position.set(Position.KEY_SATELLITES, parser.nextInt());
        position.set(Position.KEY_EVENT, parser.nextInt());
        position.set(Position.KEY_ODOMETER, parser.nextDouble() * 1000);
        position.set(Position.KEY_INPUT, parser.nextInt());
        position.set(Position.PREFIX_ADC + 1, parser.nextDouble());
        position.set(Position.PREFIX_ADC + 2, parser.nextDouble());
        position.set(Position.KEY_OUTPUT, parser.nextInt());
        position.set(Position.KEY_ROAMING, parser.nextInt() == 1);

        int rssi = parser.nextInt();
        position.setNetwork(new Network(CellTower.from(
                parser.nextInt(), parser.nextInt(), parser.nextHexInt(), parser.nextHexInt(), rssi)));

        position.set(Position.KEY_BATTERY, parser.nextDouble());
        position.set(Position.KEY_DRIVER_UNIQUE_ID, parser.next());

        return position;
    }

}
