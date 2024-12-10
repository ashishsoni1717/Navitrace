package org.navitrace.protocol;

import org.junit.jupiter.api.Test;
import org.navitrace.ProtocolTest;

public class TmgFrameDecoderTest extends ProtocolTest {

    @Test
    public void testDecode() throws Exception {

        var decoder = inject(new TmgFrameDecoder());

        verifyNull(
                decoder.decode(null, null, binary("2424242424")));

        verifyFrame(
                binary("24696f662c3836343530323033373939393630342c323238323133323031372c383132343238302c302c3239393133363231362e2d3438323235383537362ca52c313337393234353339382e3831383733343637362c142c2d36393936393937332e302c313135333435343433372e2d313938363833343039322c3439323039373739392c32302c302c2d3332302c302c4c4c4c4c2c4e4e544e2c48482c302e31372c332e30312c3330313131363030312c302c56455230302e3161"),
                decoder.decode(null, null, binary("111538360b383634353032303337393939363034eb0b1c8d00ffff23000000000000000000000000000000001c9401008c320c00188901000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000044ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff00000000000000000000000000001d940100c8950100bd0024696f662c3836343530323033373939393630342c323238323133323031372c383132343238302c302c3239393133363231362e2d3438323235383537362ca52c313337393234353339382e3831383733343637362c142c2d36393936393937332e302c313135333435343433372e2d313938363833343039322c3439323039373739392c32302c302c2d3332302c302c4c4c4c4c2c4e4e544e2c48482c302e31372c332e30312c3330313131363030312c302c56455230302e31610a000195010090960100bd0024746d702c3836343530323033373939393630342c323238323133323031372c383132343238302c302c3239393133363231362e2d3438323235383537362ca52c313337393234353339382e3831383733343637362c142c2d36393936393937332e302c313135333435343433372e2d313938363833343039322c3439323039373739392c32302c302c2d3237372c302c4c4c4c4c2c4e4e544e2c48482c302e31372c332e30312c3330313131363030312c302c56455230302e31610a00c9950100289701008b002462616b2c3836343530323033373939393630342c32313131323031372c3132303230312c312c323832362e353938312c4e2c30373731382e363436352c452c3030302e302c3239382e35322c35323834372c32322c332c2d3233392c302c4c4c4c4c2c4e4e544e2c48482c302e32342c332e30332c3330313131363030312c302c56455230302e31610a00000091960100c09701008b002462616b2c3836343530323033373939393630342c32313131323031372c3132303231322c312c323832362e353938312c4e2c30373731382e363437322c452c3030302e302c3037392e34322c35323834372c32332c332c2d3230352c302c4c4c4c4c2c4e4e544e2c48482c302e31352c332e30332c3330313131363030312c302c56455230302e31610a00000029970100589801008a002462616b2c3836343530323033373939393630342c32313131323031372c3132303232322c312c323832362e353938302c4e2c30373731382e363438352c452c3030302e302c3037372e332c35323834372c32332c332c2d3137342c302c4c4c4c4c2c4e4e544e2c48482c302e31382c332e30332c3330313131363030312c302c56455230302e31610a00000000c1970100f09801008a002462616b2c3836343530323033373939393630342c32313131323031372c3132303233312c312c323832362e353936342c4e2c30373731382e363437312c452c3030302e302c3133312e352c35323834372c32322c342c2d3134362c302c4c4c4c4c2c4e4e544e2c48482c302e31392c332e30332c3330313131363030312c302c56455230302e31610a0000000059980100889901008b002462616b2c3836343530323033373939393630342c32313131323031372c3132303234332c312c323832362e353935382c4e2c30373731382e363436382c452c3030302e302c3133392e36362c35323834372c32322c342c2d3132312c302c4c4c4c4c2c4e4e544e2c48482c302e31322c332e30332c3330313131363030312c302c56455230302e31610a000000f1980100209a01008a002462616b2c3836343530323033373939393630342c32313131323031372c3132303235332c312c323832362e353934392c4e2c30373731382e363436")));

        verifyFrame(
                binary("246c6f632c3836343530323033303335323734342c32393131323031372c3038333034392c312c323533342e363733312c4e2c30383733342e363735352c452c3033382e302c3037372e31392c35343234312c362c31312c3130302c302c48484c4c2c4e4e4e4e2c48482c302e31342c332e31312c3330313131363030312c332c56455230302e3161"),
                decoder.decode(null, null, binary("246c6f632c3836343530323033303335323734342c32393131323031372c3038333034392c312c323533342e363733312c4e2c30383733342e363735352c452c3033382e302c3037372e31392c35343234312c362c31312c3130302c302c48484c4c2c4e4e4e4e2c48482c302e31342c332e31312c3330313131363030312c332c56455230302e31610a246c6f632c3836343530323033303335323734342c32393131323031372c3038333035392c312c323533342e363634322c4e2c30383733342e373333352c452c3032392e302c3132322e37302c35343234312c362c31312c3130302c302c48484c4c2c4e4e4e4e2c48482c302e31342c332e31312c3330313131363030312c342c56455230302e31610a246c6f632c3836343530323033303335323734342c32393131323031372c3038333130392c312c323533342e363531362c4e2c30383733342e373938312c452c3034342e302c3039342e39382c36303631322c31312c31312c3130302c302c48484c4c2c4e4e4e4e2c48482c302e31372c332e31302c3330313131363030312c342c56455230302e31610a246c6f632c3836343530323033303335323734342c32393131323031372c3038333131392c312c323533342e363432312c4e2c30383733342e383639312c452c3033352e302c3130312e37332c36303631322c31322c31312c3130302c302c48484c4c2c4e4e4e4e2c48482c302e31342c332e31312c3330313131363030312c342c56455230302e31610a246c6f632c3836343530323033303335323734342c32393131323031372c3038333132392c312c323533342e363335352c4e2c30383733342e393135322c452c3032302e302c3131312e31322c36303631322c31322c31312c3130302c302c48484c4c2c4e4e4e4e2c48482c302e31362c332e31312c3330313131363030312c342c56455230302e31610a")));

    }

}