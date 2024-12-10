package org.navitrace.protocol;

import org.junit.jupiter.api.Test;
import org.navitrace.ProtocolTest;
import org.navitrace.model.Position;

public class T800xProtocolDecoderTest extends ProtocolTest {

    @Test
    public void testDecode() throws Exception {

        var decoder = inject(new T800xProtocolDecoder(null));

        verifyAttributes(decoder, binary(
                "25251300594a1b0869738060144917003c0e101e03e85a2dc8c00005070000410000000000000000000000005b000003a5b45e00230919102252e3a5094288fabfc0e98b15420000010403921352ffff0000001cffffffffff25251300594a1c0869738060144917003c0e101e03e85a2ac7c00005070000410000000000000000000000002d000003a5b48b002309191023522d320642abfebfc0e98b15420000010c03921345ffff0000001bffffffffff25251300594a1d0869738060144917003c0e101e03e85a1ac9c000050700004100000000000000000000000024000003a5b4af00230919102452b81ef9410002c0c0ec8b15420108011403911345ffff0000001dffffffffff25251300594a1e0869738060144917003c0e101e03e85a3ec7c00005070000410000000000000000000000000e000003a5b4bd002309191025060ad7ec41da02c0c0058c15420084016303921345ffff0000001cffffffffff25251300594a1f0869738060144917003c0e101e03e85a3ec7c020050700004100000000000000000000000005000003a5b4c2002309191025090e2deb410203c0c0108c15420089014303921338ffff0000001dffffffffff25251300594a200869738060144917003c0e101e03e85a1ec5c000050700004100000000000000000000000020000003a5b4e20023091910260948e1bc412205c0c0458c15420040013603921355ffff0000001bffffffffff25251300594a210869738060144917003c0e101e03e85a00c5c020050700004100000000000000000000000000000003a5b4e20023091910270948e1bc412205c0c0458c15420040013603911332ffff0000001dffffffffff"));

        verifyAttributes(decoder, binary(
                "272704004901380864112055585747c612230321220006000036435fc8acc2ee600f420000000000000000909019003900001356a18000012c0000a8c00000001e20d4800000c00000"));

        verifyAttributes(decoder, binary(
                "2525110055000208677300508924902206262035310c540045004c00430045004c0004454447450847534d20313930300f323134303734323036373835323839143839333430373131373930303936383037363846"));

        verifyAttributes(decoder, binary(
                "27271000247bd00860112047066487210407034238000005d7d17365e625ff640a730148"));

        verifyAttributes(decoder, binary(
                "27271000277bb30860112047066487210407022840000004e6215130c50fff620a0c1518000156"));

        verifyPosition(decoder, binary(
                "252514005901c00867730050941347001e46501e03e80064f2c0001401000041000000000000000000ffffffff160000034ec40021100719073800000000c2fb90c21291fd400000000003961237ffff0000002effffffffff"));

        verifyPosition(decoder, binary(
                "27270200497d880860112047066487470021040702270500006442d4e2e342f671b441000000008000008080881dff3900000384700640003c0000001e1e00641e30d2800000000000"));

        verifyAttributes(decoder, binary(
                "252510003100180865284041080544201221191023000003ffff9702eff820014700000000912a6ac26dff09c200000000"));

        verifyAttribute(decoder, binary(
                "2727020049052e086528404072393849002008060310110000000068b7c8c286eaa441000000008000008100001617410700019ce782b0001e000002581e00000530d4801f00000000"),
                Position.KEY_BATTERY_LEVEL, 100);

        verifyPosition(decoder, binary(
                "262602005308090865284040309670000f000f0f0000005a47c000050100000020000000008bfd0020022505185300004041dcc9d6c243b3c6410000012712400000000009e2ffffffffffffffffffffffff09"));

        verifyPosition(decoder, binary(
                "2727040049001b0866425039645728c916190604005240000000007739d2c25b681f420000000080000081000020174105000005458216001e000000f01e00001e30d0000000000000"));

        verifyAttribute(decoder, binary(
                "272705005e000108664250328807851905301107481054002d004d006f00620069006c006500074341542d4e42310a4c54452042414e4420340f333130323430323030303032333030143839303132343032303531303030323330303746"),
                Position.KEY_OPERATOR, "T-Mobile");

        verifyPosition(decoder, binary(
                "272702004904a90866425032880785c800190530080350000000000705eec29bf50842000000000008008090502a003700000a9e358002003c000003841900001e3f90000000000000272702004904aa0866425032880785c800190530081851000000000705eec29bf50842000000000008008090602e003700000a9e358002003c000003841900001e3f90000000000000"));

        verifyNull(decoder, binary(
                "2727010017000108806168988888881016010207110111"));

        verifyNull(decoder, binary(
                "252501001504050880061689888888111111250350"));

        verifyAttribute(decoder, binary(
                "2525810128000108664250328959160149004d00450049003a003800360036003400320035003000330032003800390035003900310036002c005300450054002000560045005200530049004f004e0020004f004b002c00560065007200730069006f006e003a00420061007300690063003a00560031002e0030002e0030002c004100500050003a00560034002e0032002e0033002c004200550049004c0044003a0032003000310039002d00300033002d00330030002c00300038003a00300035002c0050004c0054003a0032003500300033004100560045002c00480057003a00560032002e0031002c004d004f00440045004c003a002c004d004f00440045004d003a0042003900470036004d0041005200300032004100300037004d00310047002300"),
                Position.KEY_RESULT, "IMEI:866425032895916,SET VERSION OK,Version:Basic:V1.0.0,APP:V4.2.3,BUILD:2019-03-30,08:05,PLT:2503AVE,HW:V2.1,MODEL:,MODEM:B9G6MAR02A07M1G#");

        verifyPosition(decoder, binary(
                "2525020044a66d0862522030401350001403841409c40064edc000051100960000071701370000003ea7ee0019032010581300000000aad3e1bda6f24d42000000001281"));

        verifyPosition(decoder, binary(
                "252502004400010880616898888888000A00FF2001000020409600989910101010055501550000101005050005051010050558866B4276D6E342912AB441111500051010"));

        verifyNull(decoder, binary(
                "232301001500000880316890202968140197625020"));

        verifyNull(decoder, binary(
                "232303000f00000880316890202968"));

        verifyAttributes(decoder, binary(
                "232302004200000880316890202968001e02582d00000000000000050000320000018901920000001dc1e2001601081154255d0202005a0053875a00a57e5a00af80"));

        verifyNull(decoder, binary(
                "232301001500020357367031063979150208625010"));

        verifyNull(decoder, binary(
                "232303000f00000357367031063979"));

        verifyPosition(decoder, binary(
                "232304004200030357367031063979003c03842307d00000c80000050100008000008900890100000017b100151022121648b8ef0c4422969342cec5944100000110"));

        verifyPosition(decoder, binary(
                "232302004200150357367031063979003c03842307d000004a0000050100004001009500940000000285ab001510281350477f710d4452819342d1ba944101160038"));

        verifyAttributes(decoder, binary(
                "232302004200000357367031063979003c03842307d000008000000501000000010094009400000002a0b90015102814590694015a00620cf698620cf49e620cf498"));

    }

}
