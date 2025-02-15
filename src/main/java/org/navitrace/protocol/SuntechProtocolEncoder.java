
package org.navitrace.protocol;

import io.netty.channel.Channel;
import org.navitrace.BasePipelineFactory;
import org.navitrace.StringProtocolEncoder;
import org.navitrace.model.Command;
import org.navitrace.Protocol;

public class SuntechProtocolEncoder extends StringProtocolEncoder {

    public SuntechProtocolEncoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected Object encodeCommand(Channel channel, Command command) {

        boolean universal = false;
        String prefix = "SA200";
        if (channel != null) {
            SuntechProtocolDecoder protocolDecoder =
                    BasePipelineFactory.getHandler(channel.pipeline(), SuntechProtocolDecoder.class);
            if (protocolDecoder != null) {
                universal = protocolDecoder.getUniversal();
                String decoderPrefix = protocolDecoder.getPrefix();
                if (decoderPrefix != null && decoderPrefix.length() > 5) {
                    prefix = decoderPrefix.substring(0, decoderPrefix.length() - 3);
                }
            }
        }

        if (universal) {
            return encodeUniversalCommand(command);
        } else {
            return encodeLegacyCommand(prefix, command);
        }
    }

    protected Object encodeUniversalCommand(Command command) {
        return switch (command.getType()) {
            case Command.TYPE_REBOOT_DEVICE -> formatCommand(command, "CMD;%s;03;03\r", Command.KEY_UNIQUE_ID);
            case Command.TYPE_POSITION_SINGLE -> formatCommand(command, "CMD;%s;03;01\r", Command.KEY_UNIQUE_ID);
            case Command.TYPE_OUTPUT_CONTROL -> {
                if (command.getAttributes().get(Command.KEY_DATA).equals("1")) {
                    yield switch (command.getInteger(Command.KEY_INDEX)) {
                        case 1 -> formatCommand(command, "CMD;%s;04;01\r", Command.KEY_UNIQUE_ID);
                        case 2 -> formatCommand(command, "CMD;%s;04;03\r", Command.KEY_UNIQUE_ID);
                        case 3 -> formatCommand(command, "CMD;%s;04;09\r", Command.KEY_UNIQUE_ID);
                        default -> null;
                    };
                } else {
                    yield switch (command.getInteger(Command.KEY_INDEX)) {
                        case 1 -> formatCommand(command, "CMD;%s;04;02\r", Command.KEY_UNIQUE_ID);
                        case 2 -> formatCommand(command, "CMD;%s;04;04\r", Command.KEY_UNIQUE_ID);
                        case 3 -> formatCommand(command, "CMD;%s;04;10\r", Command.KEY_UNIQUE_ID);
                        default -> null;
                    };
                }
            }
            case Command.TYPE_ENGINE_STOP -> formatCommand(command, "CMD;%s;04;01\r", Command.KEY_UNIQUE_ID);
            case Command.TYPE_ENGINE_RESUME -> formatCommand(command, "CMD;%s;04;02\r", Command.KEY_UNIQUE_ID);
            case Command.TYPE_ALARM_ARM -> formatCommand(command, "CMD;%s;04;03\r", Command.KEY_UNIQUE_ID);
            case Command.TYPE_ALARM_DISARM -> formatCommand(command, "CMD;%s;04;04\r", Command.KEY_UNIQUE_ID);
            default -> null;
        };
    }

    protected Object encodeLegacyCommand(String prefix, Command command) {
        return switch (command.getType()) {
            case Command.TYPE_REBOOT_DEVICE ->
                    formatCommand(command, prefix + "CMD;%s;02;Reboot\r", Command.KEY_UNIQUE_ID);
            case Command.TYPE_POSITION_SINGLE ->
                    formatCommand(command, prefix + "CMD;%s;02;StatusReq\r", Command.KEY_UNIQUE_ID);
            case Command.TYPE_OUTPUT_CONTROL -> {
                if (command.getAttributes().get(Command.KEY_DATA).equals("1")) {
                    yield formatCommand(command, prefix + "CMD;%s;02;Enable%s\r",
                            Command.KEY_UNIQUE_ID, Command.KEY_INDEX);
                } else {
                    yield formatCommand(command, prefix + "CMD;%s;02;Disable%s\r",
                            Command.KEY_UNIQUE_ID, Command.KEY_INDEX);
                }
            }
            case Command.TYPE_ENGINE_STOP ->
                    formatCommand(command, prefix + "CMD;%s;02;Enable1\r", Command.KEY_UNIQUE_ID);
            case Command.TYPE_ENGINE_RESUME ->
                    formatCommand(command, prefix + "CMD;%s;02;Disable1\r", Command.KEY_UNIQUE_ID);
            case Command.TYPE_ALARM_ARM ->
                    formatCommand(command, prefix + "CMD;%s;02;Enable2\r", Command.KEY_UNIQUE_ID);
            case Command.TYPE_ALARM_DISARM ->
                    formatCommand(command, prefix + "CMD;%s;02;Disable2\r", Command.KEY_UNIQUE_ID);
            default -> null;
        };
    }

}
