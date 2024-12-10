
package org.navitrace;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.navitrace.helper.NetworkUtil;
import org.navitrace.helper.model.AttributeUtil;
import org.navitrace.model.Command;
import org.navitrace.model.Device;
import org.navitrace.session.cache.CacheManager;

import jakarta.inject.Inject;

public abstract class BaseProtocolEncoder extends ChannelOutboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseProtocolEncoder.class);

    private static final String PROTOCOL_UNKNOWN = "unknown";

    private final Protocol protocol;

    private CacheManager cacheManager;

    private String modelOverride;

    public BaseProtocolEncoder(Protocol protocol) {
        this.protocol = protocol;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    @Inject
    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public String getProtocolName() {
        return protocol != null ? protocol.getName() : PROTOCOL_UNKNOWN;
    }

    protected String getUniqueId(long deviceId) {
        return cacheManager.getObject(Device.class, deviceId).getUniqueId();
    }

    protected void initDevicePassword(Command command, String defaultPassword) {
        if (!command.hasAttribute(Command.KEY_DEVICE_PASSWORD)) {
            String password = AttributeUtil.getDevicePassword(
                    cacheManager, command.getDeviceId(), getProtocolName(), defaultPassword);
            command.set(Command.KEY_DEVICE_PASSWORD, password);
        }
    }

    public void setModelOverride(String modelOverride) {
        this.modelOverride = modelOverride;
    }

    public String getDeviceModel(long deviceId) {
        String model = getCacheManager().getObject(Device.class, deviceId).getModel();
        return modelOverride != null ? modelOverride : model;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

        if (msg instanceof NetworkMessage networkMessage) {
            if (networkMessage.getMessage() instanceof Command command) {

                Object encodedCommand = encodeCommand(ctx.channel(), command);

                StringBuilder s = new StringBuilder();
                s.append("[").append(NetworkUtil.session(ctx.channel())).append("] ");
                s.append("id: ").append(getUniqueId(command.getDeviceId())).append(", ");
                s.append("command type: ").append(command.getType()).append(" ");
                if (encodedCommand != null) {
                    s.append("sent");
                } else {
                    s.append("not sent");
                }
                LOGGER.info(s.toString());

                ctx.write(new NetworkMessage(encodedCommand, networkMessage.getRemoteAddress()), promise);

                return;
            }
        }
        super.write(ctx, msg, promise);
    }

    protected Object encodeCommand(Channel channel, Command command) {
        return encodeCommand(command);
    }

    protected Object encodeCommand(Command command) {
        return null;
    }

}
