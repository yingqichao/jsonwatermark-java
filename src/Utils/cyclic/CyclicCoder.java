package Utils.cyclic;

import Setting.Settings;

public class CyclicCoder {
	private static final int IRREDUCIBLE_POLYNOMIAL = 0b1011;
	private static final int dig_short = 3;
	private static final int IRREDUCIBLE_POLYNOMIAL_SHORTMODE = 0b1101;
	private static final int IRREDUCIBLE_POLYNOMIAL_LONGMODE = 0b10011;//(10,6),g(x)=x4+x+1

	private static final int dig_long = 4;
	//X^8+X^7+X^6+X^4+1 (15,7)
	private static BinaryDivider binaryDivider = new BinaryDivider();
	

	public static int encode(int signal,boolean isNormal,boolean isLong) {
		int polynomial;
		if(isNormal)
			polynomial = IRREDUCIBLE_POLYNOMIAL;
		else if(isLong)
			polynomial = IRREDUCIBLE_POLYNOMIAL_LONGMODE;
		else
			polynomial = IRREDUCIBLE_POLYNOMIAL_SHORTMODE;
		int digit = (!isNormal && isLong)?dig_long:dig_short;
		int G = signal << digit;
		
		DivResult divResult = binaryDivider.div(G, polynomial);
		int F = G | divResult.getRemainder();
		
		return F;
	}

	public static int decode(int signal,boolean isNormal,boolean isLong) {
		int sourceSignal = signal;
		int polynomial;
		if(isNormal)
			polynomial = IRREDUCIBLE_POLYNOMIAL;
		else if(isLong)
			polynomial = IRREDUCIBLE_POLYNOMIAL_LONGMODE;
		else
			polynomial = IRREDUCIBLE_POLYNOMIAL_SHORTMODE;
		int digit = (!isNormal && isLong)?dig_long:dig_short;

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

		return (binaryDivider.div(signal, polynomial).getRemainder() > 1)?-1:(sourceSignal >> digit);
	}

	public static int decode_without_correction(int signal,boolean isNormal,boolean isLong) {
		int polynomial;
		if(isNormal)
			polynomial = IRREDUCIBLE_POLYNOMIAL;
		else if(isLong)
			polynomial = IRREDUCIBLE_POLYNOMIAL_LONGMODE;
		else
			polynomial = IRREDUCIBLE_POLYNOMIAL_SHORTMODE;
		int digit = (!isNormal && isLong)?dig_long:dig_short;

		int remain = signal;
		int time = 0;
		while(remain>=polynomial){
			time++;
			if(time>100)	return -1;
			int divider = polynomial;
			while((divider<<1)<=remain)
				divider<<=1;
			remain = remain^divider;
		}

		return (remain!=0)?-1:(signal >> digit);
	}

}

