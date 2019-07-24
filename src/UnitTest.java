import cyclic.CyclicCoder;

public class UnitTest {

    public static void main(String[] args) {
        String encodeResult;
        String encodeInput = "1011";

        System.out.println("原文");
        System.out.println(encodeInput);

        CyclicCoder cc = new CyclicCoder();
        encodeResult = Integer.toBinaryString(cc.encode(Integer.parseInt(encodeInput, 2)));
        System.out.println("编码");
        System.out.println(encodeResult);

        encodeResult = "1001000";
        System.out.println("篡改为");
        System.out.println(encodeResult);

        int decodeInput = Integer.parseInt(encodeResult,2);
        System.out.println("译码");
        int decode = cc.decode(decodeInput);
        System.out.println((decode==-1)?"Invalid":Integer.toBinaryString(decode));
    }
}
