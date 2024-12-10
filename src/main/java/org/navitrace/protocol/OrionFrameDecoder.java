
package org.navitrace.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.navitrace.BaseFrameDecoder;

public class OrionFrameDecoder extends BaseFrameDecoder {

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        int length = 6;

        if (buf.readableBytes() >= length) {

            int type = buf.getUnsignedByte(buf.readerIndex() + 2) & 0x0f;

            if (type == OrionProtocolDecoder.MSG_USERLOG && buf.readableBytes() >= length + 5) {

                int index = buf.readerIndex() + 3;
                int count = buf.getUnsignedByte(index) & 0x0f;
                index += 5;
                length += 5;

                for (int i = 0; i < count; i++) {
                    if (buf.readableBytes() < length) {
                        return null;
                    }
                    int logLength = buf.getUnsignedByte(index + 1);
                    index += logLength;
                    length += logLength;
                }

                if (buf.readableBytes() >= length) {
                    return buf.readRetainedSlice(length);
                }

            } else if (type == OrionProtocolDecoder.MSG_SYSLOG && buf.readableBytes() >= length + 12) {

                length += buf.getUnsignedShortLE(buf.readerIndex() + 8);
                if (buf.readableBytes() >= length) {
                    return buf.readRetainedSlice(length);
                }

            }
        }

        return null;
    }

}