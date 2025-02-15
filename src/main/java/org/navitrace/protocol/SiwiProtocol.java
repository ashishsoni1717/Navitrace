
package org.navitrace.protocol;

import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import org.navitrace.BaseProtocol;
import org.navitrace.PipelineBuilder;
import org.navitrace.TrackerServer;
import org.navitrace.config.Config;

import jakarta.inject.Inject;

public class SiwiProtocol extends BaseProtocol {

    @Inject
    public SiwiProtocol(Config config) {
        addServer(new TrackerServer(config, getName(), false) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline, Config config) {
                pipeline.addLast(new LineBasedFrameDecoder(1024));
                pipeline.addLast(new StringDecoder());
                pipeline.addLast(new SiwiProtocolDecoder(SiwiProtocol.this));
            }
        });
    }

}
