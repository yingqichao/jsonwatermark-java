
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileOutputStream;
import java.util.*;
import java.io.FileReader;

public class MainEmbed {
    public static void main(String[] args) throws Exception{
        // Test
//        JsonParser parser = new JsonParser() ;
//        JsonObject object = (JsonObject)parser.parse(new FileReader("src//simpleJsonTest.json"));
//        JsonElement newJsonElement = JsonUpdating(object);
//
//        FileOutputStream out=new FileOutputStream("src//embedded.json");
//        Util.writeJsonStream(out,newJsonElement);
        //End Test


        String filename = "array";

//        Map<String,String> JSON = Util.readFakeJSON("src/fakeJSON.txt");
        JsonParser parser = new JsonParser() ;
        JsonObject object = (JsonObject)parser.parse(new FileReader("src//resources//"+filename+".json"));

//        // 解析string
//        Map<String,String> map = Util.eliminateLevels(object);

        String watermark = Util.readWatermark("src//watermark.txt");
        Encoder encoder = new Encoder(watermark);
        JsonElement jsonElement = encoder.run(object);

        FileOutputStream out=new FileOutputStream("src//embedded_results//"+filename+"_data_"+watermark+".json");
        Util.writeJsonStream(out,jsonElement);

    }

//    // Test Correct
//    public static JsonElement JsonUpdating(JsonObject object){
//        Map<String,String> map = new HashMap<>();
//        map.put("dataversion","1.314");
//        JsonElement jsonElement = Util.replaceKey(object,map,"","");
//
//        return jsonElement;
//
//    }



}