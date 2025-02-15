
package org.navitrace.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.navitrace.BaseProtocol;
import org.navitrace.PipelineBuilder;
import org.navitrace.TrackerServer;
import org.navitrace.config.Config;

import java.nio.ByteOrder;
import jakarta.inject.Inject;

public class RitiProtocol extends BaseProtocol {

    @Inject
    public RitiProtocol(Config config) {
        addServer(new TrackerServer(config, getName(), false) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline, Config config) {
                pipeline.addLast(new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, 1024, 105, 2, 3, 0, true));
                pipeline.addLast(new RitiProtocolDecoder(RitiProtocol.this));
            }
        });
    }

}
