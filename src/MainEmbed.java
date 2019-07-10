import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.*;
import java.io.FileReader;

public class MainEmbed {
    public static void main(String[] args) throws Exception{
//        //fastJsonTest
//        System.out.println(Util.isNumeric("12.345"));
//        System.out.println(Util.isNumeric("12345"));
//        System.out.println(Util.isNumeric("-12.345"));
//        System.out.println(Util.isInteger("12.345"));
//        System.out.println(Util.isInteger("-123345"));
//        System.out.println(Util.isInteger("-12.345"));
//        System.out.println(Util.toBinary(2,5));

//        Map<String,String> JSON = Util.readFakeJSON("src/fakeJSON.txt");
        JsonParser parser = new JsonParser() ;
        JsonObject object = (JsonObject)parser.parse(new FileReader("src//test.json"));
        // 解析string
        Map<String,String> map = Util.eliminateLevels(object);

        String watermark = Util.readWatermark("src/watermark.txt");
        Encoder encoder = new Encoder(watermark);
//        encoder.run(JSON);


        Map<String,String> watermarkedJSON = Util.readFakeJSON("src/watermarkedJSON.txt");

    }



}