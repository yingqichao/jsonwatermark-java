
import Utils.Util;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileOutputStream;
import java.io.FileReader;

public class MainEmbed {


    public static void main(String[] args) throws Exception{



        String filename = "test";

//        Map<String,String> JSON = Utils.Util.readFakeJSON("src/fakeJSON.txt");
        JsonParser parser = new JsonParser() ;
        JsonObject object = (JsonObject)parser.parse(new FileReader("src//resources//JSON//"+filename+".json"));

//        // 解析string
//        Map<String,String> map = Utils.Util.eliminateLevels(object);

        String watermark = Util.readWatermark("src//watermark.txt");
        String binarySeq = Util.StreamFromString(watermark);
        System.out.println("Bit Num: "+binarySeq.length());
        Encoder encoder = new Encoder(binarySeq);
        JsonElement jsonElement = encoder.run(object);

        FileOutputStream out=new FileOutputStream("src//embedded_results//"+filename+"_data_"+watermark+".json");
        Util.writeJsonStream(out,jsonElement);

    }

//    // Test Correct
//    public static JsonElement JsonUpdating(JsonObject object){
//        Map<String,String> map = new HashMap<>();
//        map.put("dataversion","1.314");
//        JsonElement jsonElement = Utils.Util.replaceKey(object,map,"","");
//
//        return jsonElement;
//
//    }



}