
package org.navitrace.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.navitrace.BaseProtocolDecoder;
import org.navitrace.Protocol;
import org.navitrace.helper.BitUtil;
import org.navitrace.helper.DateBuilder;
import org.navitrace.helper.UnitsConverter;
import org.navitrace.model.Position;
import org.navitrace.session.DeviceSession;

import java.net.SocketAddress;
import java.util.Date;

public class PositrexProtocolDecoder extends BaseProtocolDecoder {

    public PositrexProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    public static final int MSG_PING = 0x2E;

    private Date readTime(ByteBuf buf) {
        long time = buf.readUnsignedInt();
        DateBuilder dateBuilder = new DateBuilder();
        dateBuilder.setSecond((int) (time % 60));
        time /= 60;
        dateBuilder.setMinute((int) (time % 60));
        time /= 60;
        dateBuilder.setHour((int) (time % 24));
        time /= 24;
        dateBuilder.setDay((int) (time % 32));
        time /= 32;
        dateBuilder.setMonth((int) (time % 13));
        dateBuilder.setYear((int) (2000 + time / 13));
        return dateBuilder.getDate();
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;

        int first = buf.getUnsignedByte(buf.readerIndex());
        long deviceId;
        if (BitUtil.check(first, 7)) {
            if (BitUtil.check(first, 6)) {
                deviceId = 73000000 + BitUtil.to(buf.readUnsignedInt(), 30);
            } else if (!BitUtil.check(first, 5) && !BitUtil.check(first, 4)) {
                deviceId = 7590000 + BitUtil.to(buf.readUnsignedMedium(), 20);
            } else {
                deviceId = 70000000 + BitUtil.to(buf.readUnsignedMedium(), 20);
            }
        } else {
            deviceId = 7560000 + buf.readUnsignedShort();
        }

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, String.valueOf(deviceId));
        if (deviceSession == null) {
            return null;
        }

        int service = buf.readUnsignedByte();
        if (service == MSG_PING) {

            Position position = new Position(getProtocolName());
            position.setDeviceId(deviceSession.getDeviceId());

            position.setTime(readTime(buf));

            int latitude = buf.readMedium();
            int longitude = buf.readMedium();

            position.setValid(BitUtil.check(latitude, 23));
            position.setLatitude(BitUtil.to(latitude, 23) * 0.000025);
            position.setLongitude(longitude * 0.000025);

            position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedByte()));
            position.setCourse(buf.readUnsignedByte() * 2);

            position.set(Position.PREFIX_IO, buf.readUnsignedByte());
            position.set(Position.KEY_ODOMETER, buf.readUnsignedInt());
            buf.readUnsignedInt(); // report begin
            buf.readUnsignedInt(); // report end
            buf.readUnsignedInt(); // number of records

            if (buf.isReadable()) {
                position.set(Position.KEY_POWER, buf.readUnsignedShort() * 0.01);
                position.set(Position.KEY_BATTERY, buf.readUnsignedShort() * 0.01);
            }

            return position;

        }

        return null;
    }

}
