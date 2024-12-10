package org.navitrace.protocol;

import org.junit.jupiter.api.Test;
import org.navitrace.ProtocolTest;

public class BceFrameDecoderTest extends ProtocolTest {

    @Test
    public void testDecode() throws Exception {

        var decoder = inject(new BceFrameDecoder());

        verifyNull(
                decoder.decode(null, null, binary("23424345230d0a")));

        verifyFrame(
                binary("18ed450cf31403001501a5050a66207bde6442534451380a66207bde6440534451380a66207bde6414534451380a66207bde6416534451380a96207bde6415534451386247277bde03c0ffffc081400069f934418ce94b42001c88ee0000000000908c060103025d19ab004b0000000000000000000000000000000000000000000000000000000000000000000000000000000000000017b5c40000000000000001010000000aa6277bde008f5344513862b7277bde03c0ffffc081400051f8344185e94b420c1c85fa0000000000d18c060103025d19ab00470000000000000000000000000000000000000000000000000000000000000000000000000000000000000017b5c40000000000000001010400000ab6277bde009153445138a0"),
                decoder.decode(null, null, binary("18ed450cf31403001501a5050a66207bde6442534451380a66207bde6440534451380a66207bde6414534451380a66207bde6416534451380a96207bde6415534451386247277bde03c0ffffc081400069f934418ce94b42001c88ee0000000000908c060103025d19ab004b0000000000000000000000000000000000000000000000000000000000000000000000000000000000000017b5c40000000000000001010000000aa6277bde008f5344513862b7277bde03c0ffffc081400051f8344185e94b420c1c85fa0000000000d18c060103025d19ab00470000000000000000000000000000000000000000000000000000000000000000000000000000000000000017b5c40000000000000001010400000ab6277bde009153445138a0")));

        verifyFrame(
                binary("18ed450cf31403000200a5070e"),
                decoder.decode(null, null, binary("18ed450cf31403000200a5070e")));

    }

}