import Utils.Util;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.util.*;


public class MainExtract {
    public static void main(String[] args) throws Exception{

        String filename = "test";


        System.out.println("\n================= JSON Extract from file " + "\"" + filename + "\" =================");

//        Map<String,String> JSON = Utils.Util.readFakeJSON("src/watermarkedJSON.txt");
        JsonParser parser = new JsonParser() ;
        String watermark = Util.readWatermark("src/ExcelWatermarkHelper.txt");
        JsonObject object = (JsonObject)parser.parse(new FileReader("src//embedded_results//"+filename+"_data_"+watermark+".json"));

        JSONDecoder decoder = new JSONDecoder();
        decoder.run(object);

        List<String> list = decoder.getEnglishResult();
        List<String> chinese_list = decoder.getEnglishResult();

        System.out.println("-----------提取得到的信息是------------");
        //打印提取结果
        System.out.println("The ExcelWatermarkHelper is SUCCESSFULLY retrieved "+list.size()+" time(s)！");
        for(String str:list){
            System.out.println(str);
        }

        System.out.println("----如果您发现上面的解析内容是乱码，那么也可以参考以下gbk中文解码的水印内容----");
        for(String str:chinese_list){
            System.out.println(str);
        }
    }
}
