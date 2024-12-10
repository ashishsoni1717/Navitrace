
package org.navitrace.web;

import org.h2.server.web.ConnectionInfo;
import org.h2.server.web.JakartaWebServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.navitrace.config.Config;
import org.navitrace.config.Keys;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ConsoleServlet extends JakartaWebServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleServlet.class);

    private final Config config;

    public ConsoleServlet(Config config) {
        this.config = config;
    }

    @Override
    public void init() {
        super.init();

        try {
            Field field = JakartaWebServlet.class.getDeclaredField("server");
            field.setAccessible(true);
            org.h2.server.web.WebServer server = (org.h2.server.web.WebServer) field.get(this);

            ConnectionInfo connectionInfo = new ConnectionInfo("Traccar|"
                    + config.getString(Keys.DATABASE_DRIVER) + "|"
                    + config.getString(Keys.DATABASE_URL) + "|"
                    + config.getString(Keys.DATABASE_USER));

            Method method;

            method = org.h2.server.web.WebServer.class.getDeclaredMethod("updateSetting", ConnectionInfo.class);
            method.setAccessible(true);
            method.invoke(server, connectionInfo);

            method = org.h2.server.web.WebServer.class.getDeclaredMethod("setAllowOthers", boolean.class);
            method.setAccessible(true);
            method.invoke(server, true);

        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            LOGGER.warn("Console reflection error", e);
        }
    }

}
