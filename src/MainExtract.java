import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.util.*;

public class MainExtract {
    public static void main(String[] args) throws Exception{

        String filename = "array";

//        Map<String,String> JSON = Util.readFakeJSON("src/watermarkedJSON.txt");
        JsonParser parser = new JsonParser() ;
        String watermark = Util.readWatermark("src/watermark.txt");
        JsonObject object = (JsonObject)parser.parse(new FileReader("src//embedded_results//"+filename+"_data_"+watermark+".json"));

        Decoder decoder = new Decoder();
        List<String> list = decoder.run(object, watermark.length());

        System.out.println("-----------提取得到的信息是------------");
        //打印提取结果
        System.out.println("水印信息被成功提取 "+list.size()+" 次！");
        for(String str:list){
            System.out.println(str);
        }

    }
}
