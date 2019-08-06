package watermark.utils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

public class Utils {
    //判断整数（int）
    public boolean isInteger(String str) {
        if (null == str || "".equals(str)) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    //判断浮点数（double和float）
    public boolean isDoubleOrInteger(String str) {
        if (null == str || "".equals(str)) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[-\\+]?[.\\d]*$");
        return pattern.matcher(str).matches();
    }

    //判断浮点数（double和float）
    public boolean isDouble(String str) {
        if (null == str || "".equals(str)) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[-\\+]?[.\\d]*$");
        return (pattern.matcher(str).matches())^(isInteger(str));
    }

    public List<Integer> catArray(List<Integer> a, List<Integer> b){
        List<Integer> c = new LinkedList<>();
        for(int i = 0; i < a.size(); i++){
            c.add(a.get(i));
        }
        for(int i = 0; i < b.size(); i++){
            c.add(b.get(i));
        }
        return c;
    }

    public List<Integer> getShuffleIndex(int length, int seed){
        List<Integer> shuffleIndex = new LinkedList<>();
        for(int i = 0; i < length; i++){
            shuffleIndex.add(i);
        }
        Random rand = new Random(seed);
        Collections.shuffle(shuffleIndex, rand);
        return shuffleIndex;
    }
}

