
package org.navitrace.protocol;

import io.netty.handler.codec.string.StringDecoder;
import org.navitrace.BaseProtocol;
import org.navitrace.CharacterDelimiterFrameDecoder;
import org.navitrace.PipelineBuilder;
import org.navitrace.TrackerServer;
import org.navitrace.config.Config;

import jakarta.inject.Inject;

public class OkoProtocol extends BaseProtocol {

    @Inject
    public OkoProtocol(Config config) {
        addServer(new TrackerServer(config, getName(), false) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline, Config config) {
                pipeline.addLast(new CharacterDelimiterFrameDecoder(1024, '}'));
                pipeline.addLast(new StringDecoder());
                pipeline.addLast(new OkoProtocolDecoder(OkoProtocol.this));
            }
        });
    }

}
