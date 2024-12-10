
package org.navitrace.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import org.navitrace.BaseProtocolDecoder;
import org.navitrace.session.DeviceSession;
import org.navitrace.NetworkMessage;
import org.navitrace.Protocol;
import org.navitrace.helper.UnitsConverter;
import org.navitrace.model.Position;
import org.navitrace.protobuf.dolphin.Messages.DolphinMessages;

import java.net.SocketAddress;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class DolphinProtocolDecoder extends BaseProtocolDecoder {

    public DolphinProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;

        buf.readUnsignedShort(); // header
        int index = (int) buf.readUnsignedIntLE();
        buf.readUnsignedShort(); // version
        buf.readUnsignedShort(); // flags
        int type = buf.readUnsignedShortLE();

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, String.valueOf(buf.readLongLE()));
        if (deviceSession == null) {
            return null;
        }

        int length = (int) buf.readUnsignedIntLE();
        buf.readUnsignedInt(); // reserved

        if (type == DolphinMessages.MessageType.DataPack_Request.getNumber()) {

            DolphinMessages.DataPackRequest message = DolphinMessages.DataPackRequest.parseFrom(
                    ByteBufUtil.getBytes(buf, buf.readerIndex(), length, false));

            if (channel != null) {
                byte[] responseData = DolphinMessages.DataPackResponse.newBuilder()
                        .setResponse(DolphinMessages.DataPackResponseCode.DataPack_OK)
                        .build()
                        .toByteArray();

                ByteBuf response = Unpooled.buffer();
                response.writeShort(0xABAB); // header
                response.writeIntLE(index);
                response.writeShort(0); // flags
                response.writeShortLE(DolphinMessages.MessageType.DataPack_Response.getNumber());
                response.writeIntLE(responseData.length);
                response.writeIntLE(0); // reserved
                response.writeBytes(responseData);

                channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
            }

            List<Position> positions = new LinkedList<>();

            for (int i = 0; i < message.getPointsCount(); i++) {

                Position position = new Position(getProtocolName());
                position.setDeviceId(deviceSession.getDeviceId());

                DolphinMessages.DataPoint point = message.getPoints(i);

                position.setValid(true);
                position.setTime(new Date(point.getTimestamp() * 1000L));
                position.setLatitude(point.getLatitude());
                position.setLongitude(point.getLongitude());
                position.setAltitude(point.getAltitude());
                position.setSpeed(UnitsConverter.knotsFromKph(point.getSpeed()));
                position.setCourse(point.getBearing());

                position.set(Position.KEY_SATELLITES, point.getSatellites());
                position.set(Position.KEY_HDOP, point.getHDOP());

                for (int j = 0; j < point.getIOListIDCount(); j++) {
                    position.set(Position.PREFIX_IO + point.getIOListIDValue(j), point.getIOListValue(j));
                }

                positions.add(position);

            }

            return positions;

        }

        return null;
    }

}
