
package org.navitrace.protocol;

import io.netty.channel.Channel;
import org.navitrace.BaseProtocolDecoder;
import org.navitrace.session.DeviceSession;
import org.navitrace.Protocol;
import org.navitrace.helper.BitUtil;
import org.navitrace.helper.DateBuilder;
import org.navitrace.helper.Parser;
import org.navitrace.helper.PatternBuilder;
import org.navitrace.model.Position;

import java.net.SocketAddress;
import java.util.regex.Pattern;

public class AutoGradeProtocolDecoder extends BaseProtocolDecoder {

    public AutoGradeProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .text("(")
            .number("d{12}")                     // index
            .number("(d{15})")                   // imei
            .number("(dd)(dd)(dd)")              // date (ddmmyy)
            .expression("([AV])")                // validity
            .number("(d+)(dd.d+)([NS])")         // latitude
            .number("(d+)(dd.d+)([EW])")         // longitude
            .number("([d.]{5})")                 // speed
            .number("(dd)(dd)(dd)")              // time (hhmmss)
            .number("([d.]{6})")                 // course
            .expression("(.)")                   // status
            .number("A(xxxx)")
            .number("B(xxxx)")
            .number("C(xxxx)")
            .number("D(xxxx)")
            .number("E(xxxx)")
            .number("K(xxxx)")
            .number("L(xxxx)")
            .number("M(xxxx)")
            .number("N(xxxx)")
            .number("O(xxxx)")
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

        DateBuilder dateBuilder = new DateBuilder()
                .setDateReverse(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));

        position.setValid(parser.next().equals("A"));
        position.setLatitude(parser.nextCoordinate());
        position.setLongitude(parser.nextCoordinate());
        position.setSpeed(parser.nextDouble(0));

        dateBuilder.setTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
        position.setTime(dateBuilder.getDate());

        position.setCourse(parser.nextDouble(0));

        int status = parser.next().charAt(0);
        position.set(Position.KEY_STATUS, status);
        position.set(Position.KEY_IGNITION, BitUtil.check(status, 0));

        for (int i = 1; i <= 5; i++) {
            position.set(Position.PREFIX_ADC + i, parser.next());
        }

        for (int i = 1; i <= 5; i++) {
            position.set("can" + i, parser.next());
        }

        return position;
    }

}
