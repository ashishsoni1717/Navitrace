
package org.navitrace.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.navitrace.BaseFrameDecoder;

public class H02FrameDecoder extends BaseFrameDecoder {

    private static final int MESSAGE_SHORT = 32;
    private static final int MESSAGE_LONG = 45;

    private int messageLength;

    public H02FrameDecoder(int messageLength) {
        this.messageLength = messageLength;
    }

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        char marker = (char) buf.getByte(buf.readerIndex());

        while (marker != '*' && marker != '$' && marker != 'X' && buf.readableBytes() > 0) {
            buf.skipBytes(1);
            if (buf.readableBytes() > 0) {
                marker = (char) buf.getByte(buf.readerIndex());
            }
        }

        switch (marker) {
            case '*':

                // Return text message
                int index = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte) '#');
                if (index != -1) {
                    ByteBuf result = buf.readRetainedSlice(index + 1 - buf.readerIndex());
                    while (buf.isReadable()
                            && (buf.getByte(buf.readerIndex()) == '\r' || buf.getByte(buf.readerIndex()) == '\n')) {
                        buf.readByte(); // skip new line
                    }
                    return result;
                }

                break;

            case '$':

                if (messageLength == 0) {
                    if (buf.readableBytes() == MESSAGE_LONG) {
                        messageLength = MESSAGE_LONG;
                    } else {
                        messageLength = MESSAGE_SHORT;
                    }
                }

                if (buf.readableBytes() >= messageLength) {
                    return buf.readRetainedSlice(messageLength);
                }

                break;

            case 'X':

                if (buf.readableBytes() >= MESSAGE_SHORT) {
                    return buf.readRetainedSlice(MESSAGE_SHORT);
                }

                break;

            default:

                return null;
        }

        return null;
    }

}
