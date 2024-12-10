package org.navitrace.protocol;

import org.junit.jupiter.api.Test;
import org.navitrace.ProtocolTest;
import org.navitrace.model.Command;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ItsProtocolEncoderTest extends ProtocolTest {

    @Test
    public void testEncode() throws Exception {

        var encoder = inject(new ItsProtocolEncoder(null));

        Command command = new Command();
        command.setDeviceId(1);
        command.setType(Command.TYPE_ENGINE_STOP);

        assertEquals("@SET#RLP,OP1,", encoder.encodeCommand(command));

    }

}
