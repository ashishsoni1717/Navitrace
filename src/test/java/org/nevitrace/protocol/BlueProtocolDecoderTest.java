package org.navitrace.protocol;

import org.junit.jupiter.api.Test;
import org.navitrace.ProtocolTest;
import org.navitrace.model.Position;

public class BlueProtocolDecoderTest extends ProtocolTest {

    @Test
    public void testDecode() throws Exception {

        var decoder = inject(new BlueProtocolDecoder(null));

        verifyAttribute(decoder, binary(
                "AA0056860080E3E79E0C811F80000114020207170520011F00407F8005EE1938113B270000000000000000140202071705005AC7A621121F0002000100B7000080110000000000001A3A0000000001F400000000000078"),
                Position.KEY_ALARM, Position.ALARM_SOS);

        verifyAttribute(decoder, binary(
                "AA004A860080E3E79E20015FBE40148005EE193B113B263700000000000000140202080C09005AC7A621125F0002000000BB000000000000000000001A3A0007000001F400000000000008"),
                Position.KEY_IGNITION, true);

        verifyAttribute(decoder, binary(
                "AA004A860080E3E79E200160BE40148005EE193B113B263700000000000000140202080C13005AC7A62112600002000000B7000000110000000000001A3A0007000001F400000000000012"),
                Position.KEY_STATUS, 0x11);

        verifyPosition(decoder, binary(
                "aa00550000813f6f840b840380001032000000002001030040008005ee1938113b26f300000000000000140114082833044d27602112030002000000b70000020000000000000000650000001601f4000000000000e4"));

        verifyPosition(decoder, binary(
                "aa0055860080e3e79e0b840f800010320000000020010f0040008005ee197f113b26e800000000000000130c11091a2b005ac7a621120f0002000000b7000002000000000000001a3a0000000001f40000000000003f"));

    }

}
