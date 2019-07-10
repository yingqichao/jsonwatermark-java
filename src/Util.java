import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.LinkedTreeMap;

import java.io.*;

import java.util.*;
import java.util.regex.Pattern;

public class Util {
    public static double DEFAULT_C = 0.1;
    public static double DEFAULT_DELTA = 0.5;
    public static int DEFAULT_STRLEN = 7;
    public static int DEFAULT_MINLEN = 9;
    public static int DEFAULT_DATALEN = 5;

    public static Map<String,String> JsonMap = new HashMap<>();
    public static int sum = 0;
    public static int valid = 0;

    public static boolean isNumeric(String str){
        return str.matches("-?[0-9]+.*[0-9]*");
    }

    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    public static String toBinary(int num, int digits) {
        int value = 1 << digits | num;
        String bs = Integer.toBinaryString(value); //0x20 | 这个是为了保证这个string长度是6位数
        return  bs.substring(1);
    }

    public static String crc_remainder(String input_bitstring,String polynomial_bitstring, String initial_filler){
//        Calculates the CRC remainder of a string of bits using a chosen polynomial. Initial_filler should be '1' or '0'.
        if(polynomial_bitstring==null)  polynomial_bitstring="101";
        if(initial_filler==null)  initial_filler="0";

        polynomial_bitstring  = polynomial_bitstring.replaceAll("^(0+)", "");

        int len_input = input_bitstring.length();
        String initial_padding = "";
        for(int i=0;i<polynomial_bitstring.length()-1;i++)
            initial_filler += polynomial_bitstring;
        StringBuilder input_padded_array = new StringBuilder(input_bitstring + initial_padding);

        for(int i=0;i<input_padded_array.length();i++){
            if(input_padded_array.charAt(i)=='1'){
                for(int j=0;j<polynomial_bitstring.length();j++){
                    input_padded_array.setCharAt(i+j,(polynomial_bitstring.charAt(j) != input_padded_array.charAt(i + j))?'1':'0');
                }
            }else break;
        }
        return input_padded_array.substring(len_input).toString();
    }

    public static boolean crc_check(String input_bitstring,String polynomial_bitstring){
//        Calculates the CRC check of a string of bits using a chosen polynomial.
        if(polynomial_bitstring==null)  polynomial_bitstring="101";

        polynomial_bitstring  = polynomial_bitstring.replaceAll("^(0+)", "");
        int len_input = input_bitstring.length();
        StringBuilder input_padded_array = new StringBuilder(input_bitstring);

        try{
            for(int i=0;i<input_padded_array.length();i++){
                if(input_padded_array.charAt(i)=='1'){
                    for(int j=0;j<polynomial_bitstring.length();j++){
                        input_padded_array.setCharAt(i+j,(polynomial_bitstring.charAt(j) != input_padded_array.charAt(i + j))?'1':'0');
                    }
                }else break;
            }

            return (input_padded_array.substring(len_input).indexOf('1')<0);
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;

    }

//    # // BKDR
//            # Hash
//# Function
//# unsigned
//# int
//# BKDRHash(char * str)
//# {
//#     unsigned
//# int
//# seed = 131; // 31
//# 131
//# 1313
//# 13131
//# 131313
//# etc..
//#     unsigned
//# int
//# hash = 0;
//#
//# while (*str)
//#     {
//#         hash = hash * seed + (*str + +);
//#     }
//#
//#     return (hash & 0x7FFFFFFF);
//# }

    public static int BKDRHash(String str,Integer seed){
        int hash = 0;if(seed==null) seed = 131;
        for(char ch:str.toCharArray()){
            int ans = 0;
            if(ch>='a' && ch<='z'){
                ans = ch-'a';
            }else if(ch>='A' && ch<='Z'){
                ans = ch-'A';
            }else if(ch>='0' && ch<='9'){
                ans = ch-'0';
            }
            hash = hash * seed + ans;
            hash = hash & 0x7FFFFFFF;
        }
        return hash;
    }

    public static Map<String,String> readFakeJSON(String filename){
        // 使用ArrayList来存储每行读取到的字符串
        Map<String,String> map = new HashMap<>();
        try {
            FileReader fr = new FileReader(filename);
            BufferedReader bf = new BufferedReader(fr);
            String str;
            // 按行读取字符串
            while ((str = bf.readLine()) != null) {
                String[] strs = str.split(":");
                map.put(strs[0],strs[1]);
            }
            bf.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;


    }

    public static String readWatermark(String filename){
        String str = null;
        try {
            FileReader fr = new FileReader(filename);
            BufferedReader bf = new BufferedReader(fr);

            str = bf.readLine();

            bf.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }

    public static void writeFromJSON(Map<String,String> JSON) throws IOException {
        File fout = new File("src/watermarkedJSON.txt");
        FileOutputStream fos = new FileOutputStream(fout);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
        for (String value:JSON.keySet()) {
            bw.write(JSON.get(value));
            bw.newLine();
        }
        bw.close();
    }

    public static Map<String,String> eliminateLevels(JsonObject object){
        sum = 0;valid = 0;

        recursiveHelper(object,"");

        System.out.println("Sum of KEYS: " + sum + ". Sum of Valid: " + valid);
        return JsonMap;
    }

    public static void recursiveHelper(JsonElement object,String prefix){
        if(object instanceof JsonObject){
            // continue the recursion
            for(Map.Entry<String, JsonElement> entry:((JsonObject) object).entrySet()){
                recursiveHelper(entry.getValue(),prefix+entry.getKey());
            }


        }else if(object instanceof JsonArray){

            for (Iterator<JsonElement> iter = ((JsonArray) object).iterator(); iter.hasNext();){
                recursiveHelper(iter.next(),prefix);
            }
        }else{
            // instance of JsonPrimitive
            String value = ((JsonPrimitive)object).getAsString();
            if (value.replaceAll("[A-Za-z0-9]","").length() > DEFAULT_MINLEN){
                //valid
                JsonMap.put(prefix,value);
                valid++;
            }
            sum++;

        }
    }


}
