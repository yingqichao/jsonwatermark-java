import java.util.*;
import java.io.*;

public class MainEmbed {
    public static void main(String[] args){
        Map<String,String> map = readFakeJSON("fakeJSON.txt");
        String watermark = readWatermark("watermark.txt");


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

}