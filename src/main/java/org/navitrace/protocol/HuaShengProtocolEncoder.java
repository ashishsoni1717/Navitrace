
package org.navitrace.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.navitrace.BaseProtocolEncoder;
import org.navitrace.Protocol;
import org.navitrace.model.Command;

public class HuaShengProtocolEncoder extends BaseProtocolEncoder {

    public HuaShengProtocolEncoder(Protocol protocol) {
        super(protocol);
    }

    private ByteBuf encodeContent(int type, ByteBuf content) {

        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(0xC0);
        buf.writeShort(0x0000); // flag and version
        buf.writeShort(12 + content.readableBytes());
        buf.writeShort(type);
        buf.writeShort(0); // checksum
        buf.writeInt(1); // index
        buf.writeBytes(content);
        content.release();
        buf.writeByte(0xC0);

        return buf;
    }

    @Override
    protected Object encodeCommand(Command command) {

        ByteBuf content = Unpooled.buffer(0);
        switch (command.getType()) {
            case Command.TYPE_POSITION_PERIODIC:
                content.writeShort(0x0002);
                content.writeShort(6); // length
                content.writeShort(command.getInteger(Command.KEY_FREQUENCY));
                return encodeContent(HuaShengProtocolDecoder.MSG_SET_REQ, content);
            case Command.TYPE_OUTPUT_CONTROL:
                content.writeByte(
                        (command.getInteger(Command.KEY_INDEX) - 1) * 2
                        + (2 - command.getInteger(Command.KEY_DATA)));
                return encodeContent(HuaShengProtocolDecoder.MSG_CTRL_REQ, content);
            case Command.TYPE_ALARM_ARM:
            case Command.TYPE_ALARM_DISARM:
                content.writeShort(0x0001);
                content.writeShort(5); // length
                content.writeByte(command.getType().equals(Command.TYPE_ALARM_ARM) ? 1 : 0);
                return encodeContent(HuaShengProtocolDecoder.MSG_SET_REQ, content);
            case Command.TYPE_SET_SPEED_LIMIT:
                content.writeShort(0x0004);
                content.writeShort(6); // length
                content.writeShort(command.getInteger(Command.KEY_DATA));
                return encodeContent(HuaShengProtocolDecoder.MSG_SET_REQ, content);
            default:
                return null;
        }
    }

}
