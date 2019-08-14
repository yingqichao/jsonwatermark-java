package Utils;

import com.google.gson.*;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.stream.JsonWriter;

import java.io.*;

import java.util.*;
import java.util.regex.Pattern;

public class Util {

    public static void main(String[] args){
        System.out.println(crc_remainder("1001",null,null));
    }

    public static boolean isJSON(String jsonStr) {
        JsonElement jsonElement;
        try {
            jsonElement = new JsonParser().parse(jsonStr);
        } catch (Exception e) {
            return false;
        }
        if (jsonElement == null) {
            return false;
        }
        if (!jsonElement.isJsonObject()) {
            return false;
        }
        return true;
    }


    public static boolean isNumeric(String str){
        return str.matches("-?[0-9]+\\.[0-9]*");
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
        if(polynomial_bitstring==null)  polynomial_bitstring="1011";
        if(initial_filler==null)  initial_filler="0";

        polynomial_bitstring  = polynomial_bitstring.replaceAll("^(0+)", "");

        int len_input = input_bitstring.length();
        String initial_padding = "";
        for(int i=0;i<polynomial_bitstring.length()-1;i++)
            initial_padding += initial_filler;
        StringBuilder input_padded_array = new StringBuilder(input_bitstring + initial_padding);

        while(input_padded_array.substring(len_input).indexOf("1")!=-1){
            int cur_shift = input_padded_array.indexOf("1");
            for(int j=0;j<polynomial_bitstring.length();j++){
                    input_padded_array.setCharAt(cur_shift+j,(polynomial_bitstring.charAt(j) != input_padded_array.charAt(cur_shift + j))?'1':'0');
            }
        }

// wrong
//        for(int i=0;i<input_padded_array.length();i++){
//            if(input_padded_array.charAt(i)=='1'){
//                for(int j=0;j<polynomial_bitstring.length();j++){
//                    input_padded_array.setCharAt(i+j,(polynomial_bitstring.charAt(j) != input_padded_array.charAt(i + j))?'1':'0');
//                }
//            }else break;
//        }
        return input_padded_array.substring(len_input).toString();
    }

    public static boolean crc_check(String input_bitstring,String polynomial_bitstring){
//        Calculates the CRC check of a string of bits using a chosen polynomial.
        if(polynomial_bitstring==null)  polynomial_bitstring="1011";

        polynomial_bitstring  = polynomial_bitstring.replaceAll("^(0+)", "");
        int len_input = input_bitstring.length();
        StringBuilder input_padded_array = new StringBuilder(input_bitstring);

        try{

            while(input_padded_array.substring(len_input).indexOf("1")!=-1){
                int cur_shift = input_padded_array.indexOf("1");
                for(int j=0;j<polynomial_bitstring.length();j++){
                    input_padded_array.setCharAt(cur_shift+j,(polynomial_bitstring.charAt(j) != input_padded_array.charAt(cur_shift + j))?'1':'0');
                }
            }

// Wrong
//            for(int i=0;i<input_padded_array.length();i++){
//                if(input_padded_array.charAt(i)=='1'){
//                    for(int j=0;j<polynomial_bitstring.length();j++){
//                        input_padded_array.setCharAt(i+j,(polynomial_bitstring.charAt(j) != input_padded_array.charAt(i + j))?'1':'0');
//                    }
//                }else break;
//            }

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

    public static JsonElement replaceKey(JsonElement source,Map<String, String> rep,String prefix,String curr,int arrayCount,String newTagName,String packageNumName,int isRoot) {
        if(arrayCount>0){
            //which indicates the parent object is an array
            //we assert that the child object of an JsonArray can only be either JsonArray or JsonObject
            prefix += arrayCount;
//            arrayCount = 0;
        }
        if (source == null || source.isJsonNull()) {
            return JsonNull.INSTANCE;
        }
        if (source.isJsonPrimitive()) {
            if(arrayCount!=0){
                System.out.println("[Warning] we assert that the child object of an JsonArray can only be either JsonArray or JsonObject. However, JsonPrimitive found. The node is therefore skipped...");
                arrayCount = 0;
                return source;
            }
            JsonElement value = source.getAsJsonPrimitive();
            if (rep.containsKey(prefix.replaceAll("[^A-Za-z0-9]",""))) {
                String newKey = rep.get(prefix.replaceAll("[^A-Za-z0-9]",""));
                JsonPrimitive newJsonObj = new JsonPrimitive(newKey);
                return newJsonObj;
            }
            return source;
        }
        if (source.isJsonArray()) {
            if(arrayCount!=0){
                System.out.println("[Warning] Recursive structure of JsonArray is currently under tests.");
                arrayCount = 0;
            }
            int count = 1;
            JsonArray jsonArr = source.getAsJsonArray();
            JsonArray jsonArray = new JsonArray();
            for(JsonElement item:jsonArr){
                jsonArray.add(replaceKey(item, rep,prefix,curr,count,newTagName,packageNumName,0));
                count++;
            }
            return jsonArray;
        }
        if (source.isJsonObject()) {
            JsonObject jsonObj = source.getAsJsonObject();

            Iterator<Map.Entry<String, JsonElement>> iterator = jsonObj.entrySet().iterator();
            JsonObject newJsonObj = new JsonObject();
            if(isRoot>0)
                newJsonObj.add(packageNumName, new JsonPrimitive(isRoot));
            if(arrayCount!=0){
                newJsonObj.add(newTagName, new JsonPrimitive(arrayCount));
                arrayCount = 0;
            }
            for (; iterator.hasNext();){
                Map.Entry<String, JsonElement> item = iterator.next();
                String key = item.getKey();
                JsonElement value = item.getValue();
//                if (rep.containsKey(prefix+key)) {
//                    String newKey = rep.get(prefix+key);
//                    key = newKey;
//                }
                newJsonObj.add(key, replaceKey(value, rep,prefix+key,key,arrayCount,newTagName,packageNumName,0));
            }

            return newJsonObj;
        }
        return JsonNull.INSTANCE;
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

//    public static void writeFromJSON(Map<String,String> JSON) throws IOException {
//        File fout = new File("src/watermarkedJSON.txt");
//        FileOutputStream fos = new FileOutputStream(fout);
//        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
//        for (String value:JSON.keySet()) {
//            bw.write(JSON.get(value));
//            bw.newLine();
//        }
//        bw.close();
//    }

    public static void writeJsonStream(OutputStream out, JsonElement object) throws IOException {
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
        writer.setIndent(" ");
        Gson gson = new Gson();
        gson.toJson(object,writer);
        // print the result
        gson.toJson(object,System.out);

        writer.close();
    }

    public static String StreamFromString(String str) {
        char[] strChar=str.toCharArray();
        String result="";
        for(int i=0;i<strChar.length;i++){
            String tmp = Integer.toBinaryString(strChar[i]);
            for(int j=tmp.length();j<8;j++)
                tmp = '0'+tmp;
            result += tmp;
        }
//        System.out.println(result);
        return result;
    }

    public static String dec2bin(int in,int digits){
        StringBuilder str = new StringBuilder();
        while(in!=0){
            str.append(in%2);
            in /= 2;
        }
        str.reverse();String res = str.toString();
        for(int i=str.length();i<digits;i++){
            res = "0" + res;
        }

        return res;


    }

    public static int bin2dec(String str){
        int i=0;
        for(char c:str.toCharArray()){
            i<<=1;
            i+=(c=='1')?1:0;

        }
        return i;
    }


}
