package org.navitrace.protocol;

import org.junit.jupiter.api.Test;
import org.navitrace.ProtocolTest;

public class AdmProtocolDecoderTest extends ProtocolTest {

    @Test
    public void testDecode() throws Exception {

        var decoder = inject(new AdmProtocolDecoder(null));

        verifyPosition(decoder, binary(
                "38363931353330343235323337383400003728e000001402441d5f42c3711642930d000000c7000a461954f25fd82ed508000000000000000044000000010000000000140000"));

        verifyNull(decoder, binary(
                "000042033836393135333034323532333738340000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000078"));

        verifyPosition(decoder, binary(
                "00003728e000001402591d5f42e8711642f100000000df0007483223f35fe02eab0800000000000000004400000001000000000018000000003728e000001402591d5f42e8711642f100000000df0006496e23f35fe22e9a0800000000000000004400000001000000000017000000003728e000001402591d5f42e8711642f100000000df000748aa23f35fe22e4a07000000000000000043000100010000000000180000"));

        verifyNull(decoder, binary(
                "010042033836313331313030323639343838320501000000000000000000000000000000000000000000000000000000000000000000000000000000000000000073"));

        verifyPosition(decoder, binary(
                "01002e40041c0744009dfe6742c6c860427402000000f4ff077752c8f55b000000000b4132010213430100041e"));

        verifyPosition(decoder, binary(
                "01002680336510002062A34C423DCF8E42A50B1700005801140767E30F568F2534107D220000"));

        verifyPosition(decoder, binary(
                "010022003300072020000000000000000044062A330000000000107F10565D4A8310"));

        verifyPosition(decoder, binary(
                "0100268033641080207AA34C424CCF8E4239030800005B01140755E30F560000F00F70220000"));

        verifyPosition(decoder, binary(
                "01002680336510002062A34C423DCF8E42A50B1700005801140767E30F568F2534107D220000"));

        verifyPosition(decoder, binary(
                "01002200333508202000000000000000007F0D9F030000000000E39A1056E24A8210"));

        verifyNotNull(decoder, binary(
                "01008449443d3120536f66743d30783531204750533d313036382054696d653d30383a35393a32302031302e30392e31372056616c3d30204c61743d36312e36373738204c6f6e3d35302e3832343520563d3020536174436e743d342b3720537461743d30783030313020496e5f616c61726d3d30783030000000000000000000000000"));
    }

}
