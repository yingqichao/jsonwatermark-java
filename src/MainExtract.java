import Utils.Util;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.util.*;


public class MainExtract {
    public static void main(String[] args) throws Exception{

        String filename = "test";

//        Map<String,String> JSON = Utils.Util.readFakeJSON("src/watermarkedJSON.txt");
        JsonParser parser = new JsonParser() ;
        String watermark = Util.readWatermark("src/ExcelWatermarkHelper.txt");
        JsonObject object = (JsonObject)parser.parse(new FileReader("src//embedded_results//"+filename+"_data_"+watermark+".json"));

        JSONDecoder decoder = new JSONDecoder();
        List<String> list = decoder.run(object);

        System.out.println("-----------提取得到的信息是------------");
        //打印提取结果
        System.out.println("The ExcelWatermarkHelper is SUCCESSFULLY retrieved "+list.size()+" time(s)！");
        for(String str:list){
            System.out.println(str);
        }

    }
}
