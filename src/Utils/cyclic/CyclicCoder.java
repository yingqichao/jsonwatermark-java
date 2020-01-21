package Utils.cyclic;

import Setting.Settings;

public class CyclicCoder {
	private static final int IRREDUCIBLE_POLYNOMIAL = 0b1011;
	private static final int dig = 3;
	private static final int IRREDUCIBLE_POLYNOMIAL_packageNum = 0b1101;
	//private static final int IRREDUCIBLE_POLYNOMIAL_packageNum = 0b10011;//(10,6),g(x)=x4+x+1
	private static final int dig_packageNum = 3;
	//X^8+X^7+X^6+X^4+1 (15,7)
	private static BinaryDivider binaryDivider = new BinaryDivider();
	

	public static int encode(int signal,int mode) {
		int key = (mode== Settings.DATA_POLYNOMIAL)?IRREDUCIBLE_POLYNOMIAL:IRREDUCIBLE_POLYNOMIAL_packageNum;
		int G = signal << ((mode== Settings.DATA_POLYNOMIAL)?dig:dig_packageNum);
		
		DivResult divResult = binaryDivider.div(G, key);
		int F = G | divResult.getRemainder();
		
		return F;
	}

	public static int decode(int signal,int mode) {
		int sourceSignal = signal;
		int key = (mode== Settings.DATA_POLYNOMIAL)?IRREDUCIBLE_POLYNOMIAL:IRREDUCIBLE_POLYNOMIAL_packageNum;
//		int errorPosition = 0;
//		while (binaryDivider.div(signal, IRREDUCIBLE_POLYNOMIAL).getRemainder() > 1) {
//			int t = signal & 1;
//			signal = signal >> 1;
//			signal = signal | (t << 6);
//			errorPosition++;
//		}
//
//		if (binaryDivider.div(signal, IRREDUCIBLE_POLYNOMIAL).getRemainder() == 1) {
//			sourceSignal ^= (1 << errorPosition);
//		}

		return (binaryDivider.div(signal, key).getRemainder() > 1)?-1:
				(sourceSignal >> ((mode== Settings.DATA_POLYNOMIAL)?dig:dig_packageNum));
	}

}

