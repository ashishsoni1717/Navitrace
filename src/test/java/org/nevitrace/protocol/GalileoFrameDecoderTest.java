package org.navitrace.protocol;

import org.junit.jupiter.api.Test;
import org.navitrace.ProtocolTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GalileoFrameDecoderTest extends ProtocolTest {

    @Test
    public void testDecode() throws Exception {

        var decoder = inject(new GalileoFrameDecoder());

        assertEquals(
                binary("01003f01001c475b166133303035333430363431383437393000001d000064897bb003000b0221c20512a60a0000000802000f209d7b8964300f2536fbfd103c1d01"),
                decoder.decode(null, null, binary("01003f01001c475b166133303035333430363431383437393000001d000064897bb003000b0221c20512a60a0000000802000f209d7b8964300f2536fbfd103c1d01")));

        assertEquals(
                binary("011780011102e603383633353931303238393630323437043200801c"),
                decoder.decode(null, null, binary("011780011102e603383633353931303238393630323437043200801c")));

        assertEquals(
                binary("01d48304320010020520a5829f58300f50dc8a024c0965013300000000344102350740003a41e14b426610431b4459fa672a4500004601a050364c510000520000530000c000000000c100000000c200000000c300000000d80000dd00000000e293000000043200100105202d829f58300f50dc8a024c0965013300000000344102350740003a41d04b426110431b445702882a4500004601a050374c510000520000530000c000000000c100000000c200000000c300000000d80000dd00000000e29400000004320010000520b5819f58300f50dc8a024c0965013300000000344102350740003a419e4b426a10431c4456fab72a4500004601a050434c510000520000530000c000000000c100000000c200000000c300000000d80000dd00000000e29500000004320010ff04203d819f58300f50dc8a024c0965013300000000344102350740003a41874b426310431c4454fe572a4500004601a050334c510000520000530000c000000000c100000000c200000000c300000000d80000dd00000000e29600000004320010fe0420c5809f58300f50dc8a024c0965013300000000344102350840003a41a24b426710431c4457fea72a4500004601a050214c510000520000530000c000000000c100000000c200000000c300000000d80000dd00000000e29700000004320010fd04204d809f58300f50dc8a024c0965013300000000344102350840003a41a34b426310431c4455f6772a4500004601a0502e4c510000520000530000c000000000c100000000c200000000c300000000d80000dd00000000e29900000004320010fc0420d57f9f58300f50dc8a024c0965013300000000344102350840003a41bd4b426510431d4458fe672a4500004601a0501f4c510000520000530000c000000000c100000000c200000000c300000000d80000dd00000000e29700000004320010fb04205d7f9f58300f50dc8a024c0965013300000000344102350840003a41b54b426310431d4456fa772a4500004601a0502d4c510000520000530000c000000000c100000000c200000000c300000000d80000dd00000000e29500000004320010fa0420e57e9f58300f50dc8a024c0965013300000000344102350840003a41b24b426210431e4454fa872a4500004601a050fe4b510000520000530000c000000000c100000000c200000000c300000000d80000dd00000000e29000000004320010f904206d7e9f58300f50dc8a024c0965013300000000344102350a40003a41af4b426710431f4458fea72a4500004601a0500a4c510000520000530000c000000000c100000000c200000000c300000000d80000dd00000000e28900000067c5"),
                decoder.decode(null, null, binary("01d48304320010020520a5829f58300f50dc8a024c0965013300000000344102350740003a41e14b426610431b4459fa672a4500004601a050364c510000520000530000c000000000c100000000c200000000c300000000d80000dd00000000e293000000043200100105202d829f58300f50dc8a024c0965013300000000344102350740003a41d04b426110431b445702882a4500004601a050374c510000520000530000c000000000c100000000c200000000c300000000d80000dd00000000e29400000004320010000520b5819f58300f50dc8a024c0965013300000000344102350740003a419e4b426a10431c4456fab72a4500004601a050434c510000520000530000c000000000c100000000c200000000c300000000d80000dd00000000e29500000004320010ff04203d819f58300f50dc8a024c0965013300000000344102350740003a41874b426310431c4454fe572a4500004601a050334c510000520000530000c000000000c100000000c200000000c300000000d80000dd00000000e29600000004320010fe0420c5809f58300f50dc8a024c0965013300000000344102350840003a41a24b426710431c4457fea72a4500004601a050214c510000520000530000c000000000c100000000c200000000c300000000d80000dd00000000e29700000004320010fd04204d809f58300f50dc8a024c0965013300000000344102350840003a41a34b426310431c4455f6772a4500004601a0502e4c510000520000530000c000000000c100000000c200000000c300000000d80000dd00000000e29900000004320010fc0420d57f9f58300f50dc8a024c0965013300000000344102350840003a41bd4b426510431d4458fe672a4500004601a0501f4c510000520000530000c000000000c100000000c200000000c300000000d80000dd00000000e29700000004320010fb04205d7f9f58300f50dc8a024c0965013300000000344102350840003a41b54b426310431d4456fa772a4500004601a0502d4c510000520000530000c000000000c100000000c200000000c300000000d80000dd00000000e29500000004320010fa0420e57e9f58300f50dc8a024c0965013300000000344102350840003a41b24b426210431e4454fa872a4500004601a050fe4b510000520000530000c000000000c100000000c200000000c300000000d80000dd00000000e29000000004320010f904206d7e9f58300f50dc8a024c0965013300000000344102350a40003a41af4b426710431f4458fea72a4500004601a0500a4c510000520000530000c000000000c100000000c200000000c300000000d80000dd00000000e28900000067c5")));

    }

}
