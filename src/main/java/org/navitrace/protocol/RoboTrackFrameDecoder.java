
package org.navitrace.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.navitrace.BaseFrameDecoder;

public class RoboTrackFrameDecoder extends BaseFrameDecoder {

    private int messageLength(ByteBuf buf) {
        return switch (buf.getUnsignedByte(buf.readerIndex())) {
            case RoboTrackProtocolDecoder.MSG_ID -> 69;
            case RoboTrackProtocolDecoder.MSG_ACK -> 3;
            case RoboTrackProtocolDecoder.MSG_GPS, RoboTrackProtocolDecoder.MSG_GSM,
                 RoboTrackProtocolDecoder.MSG_IMAGE_START -> 24;
            case RoboTrackProtocolDecoder.MSG_IMAGE_DATA -> 8 + buf.getUnsignedShortLE(buf.readerIndex() + 1);
            case RoboTrackProtocolDecoder.MSG_IMAGE_END -> 6;
            default -> Integer.MAX_VALUE;
        };
    }

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        int length = messageLength(buf);

        if (buf.readableBytes() >= length) {
            return buf.readRetainedSlice(length);
        }

        return null;
    }

}
