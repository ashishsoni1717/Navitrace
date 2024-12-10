
package org.navitrace.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import org.navitrace.BaseProtocolDecoder;
import org.navitrace.session.DeviceSession;
import org.navitrace.Protocol;
import org.navitrace.helper.BitUtil;
import org.navitrace.helper.DateBuilder;
import org.navitrace.helper.UnitsConverter;
import org.navitrace.model.Position;

import java.net.SocketAddress;

public class SolarPoweredProtocolDecoder extends BaseProtocolDecoder {

    public SolarPoweredProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    public static final int MSG_ACTIVE_REPORTING = 0x11;

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;

        buf.readUnsignedByte(); // start marker

        String imei = ByteBufUtil.hexDump(buf.readSlice(8)).substring(0, 15);
        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, imei);
        if (deviceSession == null) {
            return null;
        }

        int type = buf.readUnsignedByte();
        buf.readUnsignedShort(); // attributes

        if (type == MSG_ACTIVE_REPORTING) {

            Position position = new Position(getProtocolName());
            position.setDeviceId(deviceSession.getDeviceId());

            while (buf.readableBytes() > 2) {
                int tag = buf.readUnsignedByte();
                int length = buf.readUnsignedByte();
                switch (tag) {
                    case 0x81:
                        int status = buf.readUnsignedByte();
                        DateBuilder dateBuilder = new DateBuilder()
                                .setDate(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte())
                                .setTime(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte());
                        position.setTime(dateBuilder.getDate());
                        position.setLatitude(buf.readUnsignedInt() * 0.000001);
                        if (BitUtil.check(status, 3)) {
                            position.setLatitude(-position.getLatitude());
                        }
                        position.setLongitude(buf.readUnsignedInt() * 0.000001);
                        if (BitUtil.check(status, 2)) {
                            position.setLongitude(-position.getLongitude());
                        }
                        position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedByte()));
                        int temperature = buf.readUnsignedByte();
                        if (BitUtil.check(temperature, 7)) {
                            position.set(Position.KEY_DEVICE_TEMP, -BitUtil.to(temperature, 7));
                        } else {
                            position.set(Position.KEY_DEVICE_TEMP, BitUtil.to(temperature, 7));
                        }
                        position.set(Position.KEY_BATTERY, buf.readUnsignedByte() * 0.02);
                        position.setCourse(buf.readUnsignedByte());
                        break;
                    case 0x82:
                        int alarmMask = buf.readUnsignedByte();
                        int alarm = buf.readUnsignedByte();
                        if (BitUtil.check(alarmMask, 0) && BitUtil.check(alarm, 0)) {
                            position.set(Position.KEY_ALARM, Position.ALARM_TAMPERING);
                        }
                        if (BitUtil.check(alarmMask, 1) && BitUtil.check(alarm, 1)) {
                            position.set(Position.KEY_ALARM, Position.ALARM_LOW_POWER);
                        }
                        if (BitUtil.check(alarmMask, 2) && BitUtil.check(alarm, 2)) {
                            position.set(Position.KEY_ALARM, Position.ALARM_SOS);
                        }
                        if (BitUtil.check(alarmMask, 3) && BitUtil.check(alarm, 3)) {
                            position.set(Position.KEY_ALARM, Position.ALARM_FALL_DOWN);
                        }
                        if (BitUtil.check(alarmMask, 4)) {
                            position.set(Position.KEY_MOTION, BitUtil.check(alarm, 4));
                        }
                        break;
                    case 0x83:
                        buf.readUnsignedInt(); // uptime
                        buf.readUnsignedInt(); // gps count
                        buf.readUnsignedInt(); // gsm count
                        buf.readUnsignedByte(); // positioning time
                        buf.readUnsignedByte(); // registration time
                        buf.readUnsignedByte(); // connection time
                        position.set(Position.KEY_RSSI, buf.readUnsignedByte());
                        break;
                    default:
                        buf.skipBytes(length);
                        break;
                }
            }

            return position;

        }

        return null;
    }

}
