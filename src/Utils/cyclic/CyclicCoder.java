package Utils.cyclic;

import Setting.Settings;

public class CyclicCoder {
	private static final int IRREDUCIBLE_POLYNOMIAL = 0b1011;
	private static final int IRREDUCIBLE_POLYNOMIAL_packageNum = 0b1101;
	private static BinaryDivider binaryDivider = new BinaryDivider();
	

	public static int encode(int signal,int mode) {
		int key = (mode== Settings.DATA_POLYNOMIAL)?IRREDUCIBLE_POLYNOMIAL:IRREDUCIBLE_POLYNOMIAL_packageNum;
		int G = signal << 3;
		
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

		return (binaryDivider.div(signal, key).getRemainder() > 1)?-1:(sourceSignal >> 3);
	}

}

