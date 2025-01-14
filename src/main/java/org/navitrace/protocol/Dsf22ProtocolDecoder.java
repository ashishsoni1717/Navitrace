
package org.navitrace.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import org.navitrace.BaseProtocolDecoder;
import org.navitrace.session.DeviceSession;
import org.navitrace.NetworkMessage;
import org.navitrace.Protocol;
import org.navitrace.helper.BitUtil;
import org.navitrace.helper.UnitsConverter;
import org.navitrace.model.Position;

import java.net.SocketAddress;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Dsf22ProtocolDecoder extends BaseProtocolDecoder {

    public Dsf22ProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;

        buf.skipBytes(2); // header

        String id = String.valueOf(buf.readUnsignedShortLE());
        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, id);
        if (deviceSession == null) {
            return null;
        }

        List<Position> positions = new LinkedList<>();
        int count = buf.readUnsignedByte();

        for (int i = 0; i < count; i++) {

            Position position = new Position(getProtocolName());
            position.setDeviceId(deviceSession.getDeviceId());

            position.setValid(true);
            position.setLatitude(buf.readIntLE() / 10000000.0);
            position.setLongitude(buf.readIntLE() / 10000000.0);
            position.setTime(new Date(buf.readUnsignedIntLE() * 1000));
            position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedByte()));

            position.set(Position.KEY_FUEL_LEVEL, buf.readUnsignedShortLE() * 0.001);

            int status = buf.readUnsignedByte();
            position.set(Position.KEY_IGNITION, BitUtil.check(status, 0));
            position.set(Position.PREFIX_IN + 1, BitUtil.check(status, 1));
            position.set(Position.PREFIX_OUT + 1, BitUtil.check(status, 4));
            position.set(Position.KEY_ALARM, BitUtil.check(status, 6) ? Position.ALARM_JAMMING : null);
            position.set(Position.KEY_STATUS, status);

            positions.add(position);

        }

        if (channel != null) {
            byte[] response = {0x01};
            channel.writeAndFlush(new NetworkMessage(Unpooled.wrappedBuffer(response), remoteAddress));
        }

        return positions;
    }

}
