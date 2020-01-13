
import Utils.Util;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MainEmbed {


    public static void main(String[] args) throws Exception{

        String[] ban = new String[]{"logDataeventInfodetectStart"};
        Set<String> banList = new HashSet<>();
        for(String str:ban)
            banList.add(str);

        String filename = "file1";

//        Map<String,String> JSON = Utils.Util.readFakeJSON("src/fakeJSON.txt");
        JsonParser parser = new JsonParser() ;
        JsonObject object = (JsonObject)parser.parse(new FileReader("src//resources//JSON//"+filename+".json"));

//        // 解析string
//        Map<String,String> map = Utils.Util.eliminateLevels(object);

        System.out.println("\n================= JSON Embedding from file " + "\"" + filename + "\" =================");

        String watermark = Util.readWatermark("src//watermark.txt");

        String outpath = "src//embedded_results//"+filename+"_data_"+watermark+".json";

        String binarySeq = Util.StreamFromString(watermark);
        System.out.println("Bit Num: "+binarySeq.length());
        JSONEncoder encoder = new JSONEncoder(binarySeq);
        encoder.run(object,outpath,banList);



//        FileOutputStream out=new FileOutputStream("src//embedded_results//"+filename+"_data_"+watermark+".json");
//        Util.writeJsonStream(out,jsonElement);



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