
package org.navitrace;

import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.navitrace.config.Config;
import org.navitrace.config.Keys;
import org.navitrace.helper.ClassScanner;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.net.BindException;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class ServerManager implements LifecycleObject {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerManager.class);

    private final List<TrackerConnector> connectorList = new LinkedList<>();
    private final Map<String, BaseProtocol> protocolList = new ConcurrentHashMap<>();

    @Inject
    public ServerManager(
            Injector injector, Config config) throws IOException, URISyntaxException, ReflectiveOperationException {
        Set<String> enabledProtocols = null;
        if (config.hasKey(Keys.PROTOCOLS_ENABLE)) {
            enabledProtocols = new HashSet<>(Arrays.asList(config.getString(Keys.PROTOCOLS_ENABLE).split("[, ]")));
        }
        for (Class<?> protocolClass : ClassScanner.findSubclasses(BaseProtocol.class, "org.navitrace.protocol")) {
            String protocolName = BaseProtocol.nameFromClass(protocolClass);
            if (enabledProtocols == null || enabledProtocols.contains(protocolName)) {
                if (config.getInteger(Keys.PROTOCOL_PORT.withPrefix(protocolName)) > 0) {
                    BaseProtocol protocol = (BaseProtocol) injector.getInstance(protocolClass);
                    connectorList.addAll(protocol.getConnectorList());
                    protocolList.put(protocol.getName(), protocol);
                }
            }
        }
    }

    public BaseProtocol getProtocol(String name) {
        return protocolList.get(name);
    }

    @Override
    public void start() throws Exception {
        for (TrackerConnector connector: connectorList) {
            try {
                connector.start();
            } catch (BindException e) {
                LOGGER.warn("Port disabled due to conflict", e);
            } catch (ConnectException e) {
                LOGGER.warn("Connection failed", e);
            }
        }
    }

    @Override
    public void stop() throws Exception {
        for (TrackerConnector connector : connectorList) {
            connector.stop();
        }
    }

}
