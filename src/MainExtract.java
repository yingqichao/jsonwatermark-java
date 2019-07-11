import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.util.*;

public class MainExtract {
    public static void main(String[] args) throws Exception{

//        Map<String,String> JSON = Util.readFakeJSON("src/watermarkedJSON.txt");
        JsonParser parser = new JsonParser() ;
        JsonObject object = (JsonObject)parser.parse(new FileReader("src//embedded.json"));

        String watermark = Util.readWatermark("src/watermark.txt");
        Decoder decoder = new Decoder();
        List<String> list = decoder.run(object, watermark.length());

        System.out.println("-----------提取得到的信息是------------");
        //打印提取结果
        for(String str:list){
            System.out.println(str);
        }

    }
}
