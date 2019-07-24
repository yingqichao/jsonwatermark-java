package cyclic;

public class Test {

	public static void main(String[] args) {
		String encodeResult;
		String encodeInput = "1010";
		
		System.out.println("原文");
		System.out.println(encodeInput);
		
		CyclicCoder cc = new CyclicCoder();
		encodeResult = Integer.toBinaryString(cc.encode(Integer.parseInt(encodeInput, 2)));
		System.out.println("编码");
		System.out.println(encodeResult);
		
		int decodeInput = Integer.parseInt(encodeResult,2);
		System.out.println("译码");
		System.out.println(Integer.toBinaryString(cc.decode(decodeInput)));
	}
}
