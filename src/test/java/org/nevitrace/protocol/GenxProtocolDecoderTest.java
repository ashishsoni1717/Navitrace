package org.navitrace.protocol;

import org.junit.jupiter.api.Test;
import org.navitrace.ProtocolTest;

public class GenxProtocolDecoderTest extends ProtocolTest {

    @Test
    public void testDecode() throws Exception {

        var decoder = inject(new GenxProtocolDecoder(null));

        decoder.setReportColumns("28,2,3,4,13,17,10,23,27,11,7,8,46,56,59,70,74,75,77,89,90,93,99,107,112,113,114,176,175,178,181,182");

        verifyPosition(decoder, text(
                "000036004133,11/05/2017 00:03:45,45.54767,-73.75547,0,0,63,569.35,118,ON,10687,0,12,O,9,3669.000,95.0,0.0,1,107.9464,0.0065,583.752,43,0.00,28.26,7.60,NA,U,UUU,0,-95.0,U"));

        decoder.setReportColumns("1,2,3,4");

        verifyPosition(decoder, text(
                "000036004130,08/31/2017 17:24:13,45.47275,-73.65491,5,19,117,1.14,147,ON,1462,0,6,N,0,0.000,-95.0,-1.0,0,0.0000,0.0000,0.000,0,0.00,0.00,0.00,NA,U,UUU,0,-95.0,U"));

        verifyPosition(decoder, text(
                "000036004130,08/31/2017 17:24:37,45.47257,-73.65506,3,0,117,1.14,124,ON,1489,0,5,N,0,0.000,-95.0,-1.0,0,0.0000,0.0000,0.000,0,0.00,0.00,0.00,NA,U,UUU,0,-95.0,U"));

        decoder.setReportColumns("1,2,3,4,13,17,10,23,27,11,7,8,46,56,59,70,74,75,77,89,90,93,99,107,112,113,114,176,175,178,181,182");

        verifyPosition(decoder, text(
                "000036035855,04/16/2017 21:19:07,45.46485,-73.65424,24,32,61:213,342.51,157,ON,20984,0,12,O,18,0.000,95.0,24.0,1990,64.0894,0.0219,316.009,71,0.00,16.78,5.10,NA,U,UUU,0,-95.0,U"));

        verifyPosition(decoder, text(
                "000036004129,10/20/2017 00:54:27,43.44638,-79.68616,36,310,6,4954.40,321,ON,35377,0,12,O,13,0.000,85.6,36.0,1573,451.2514,0.0012,5260.953,0,0.00,122.48,33.17,NA,U,UUU,0,-95.0,U"));

    }

}
