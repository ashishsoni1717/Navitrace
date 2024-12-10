
package org.navitrace.protocol;

import org.navitrace.StringProtocolEncoder;
import org.navitrace.model.Command;
import org.navitrace.Protocol;

public class XexunProtocolEncoder extends StringProtocolEncoder {

    public XexunProtocolEncoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected Object encodeCommand(Command command) {

        initDevicePassword(command, "123456");

        switch (command.getType()) {
            case Command.TYPE_ENGINE_STOP:
                return formatCommand(command, "powercar%s 11", Command.KEY_DEVICE_PASSWORD);
            case Command.TYPE_ENGINE_RESUME:
                return formatCommand(command, "powercar%s 00", Command.KEY_DEVICE_PASSWORD);
            default:
                return null;
        }
    }

}
