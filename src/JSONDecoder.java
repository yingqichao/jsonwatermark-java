import GeneralHelper.LtDecoder;
import Setting.Settings;
import Utils.Util;
import com.google.gson.*;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.*;

/**
 * @author Qichao Ying
 * @date 2019/8/12 16:38
 * @Description DEFAULT
 */
public class JSONDecoder extends AbstractDecoder {

    public Map<String,String> JSON = new HashMap<>();
//    public Map<String,String> watermarkedJSON = new HashMap<>();

    public static int sum = 0;
    public static int valid = 0;
    public static int updated = 0;

    public void decode(Map<String,String> JSON,int filesize){
        decoder = new LtDecoder(Settings.DEFAULT_C,Settings.DEFAULT_DELTA);

        for(String key:JSON.keySet()){
            String lt_block = JSON.get(key);
            if(decoder.consume_block(filesize,key,lt_block,1)){
                decoder.received_packs++;
                if(decoder.is_done()){
                    success_time++;
                    System.out.println("--> Decoded Successfully <--... The ExcelWatermarkHelper is now successfully retrieved. Time: "+success_time);
                    List<Integer> buff = decoder.bytes_dump();

                    List<String> res = Utils.StrBinaryTurn.stream2String(buff);
                    //分别保存中英文的可能结果
                    secret_data.add(res.get(0));
                    secret_data_chinese.add(res.get(1));
                    decoder.succeed_and_init();
                }else{
                    System.out.println("Need more Packs...Received: "+decoder.received_packs);
                }
            }
        }

    }

    public void run(JsonObject object,String emb_file) throws Exception{
        //Reads from stream, applying the LT decoding algorithm to incoming encoded blocks until sufficiently many blocks have been received to reconstruct the entire file.
        Set<String> banList = new HashSet<>();
        System.out.println("-----------------------------Extraction---------------------------------------");
        // 解析string
        ByteBuffer buf = ByteBuffer.allocateDirect(10) ;
        UserDefinedFileAttributeView userDefined = Files.getFileAttributeView(Paths.get(emb_file), UserDefinedFileAttributeView.class);
        try {
            userDefined.read("num_packages", buf);
        }catch(Exception e){
            throw new Exception("The file does not contain watermark.");
        }
        buf.flip();
        int filesize = Integer.parseInt(Charset.defaultCharset().decode(buf).toString());
        if(filesize==0){
            throw new Exception("Filesize equals to 0. Please check!");
        }

        eliminateLevels(object,banList);
//        modified_json= Utils.Util.eliminateLevels(JSON, "");
        decode(JSON, filesize);

//        return this.secret_data;
    }

    public void eliminateLevels(JsonObject object, Set<String> banList) throws Exception{
        // clear
        JSON = new HashMap<>();
        sum = 0;valid = 0;
        //get num of packages. It is shown in the root node.
        try {
//            int numpackage = ((JsonPrimitive) ((JsonObject) object).get(Settings.packageNumName)).getAsInt();


            recursiveEliminateHelper(object,"",false,banList);

            System.out.println("Sum of KEYS: " + sum + ". Sum of Valid: " + valid);
            return;
        }catch(NullPointerException e){
            throw new Exception("[Error] No info of packageNum was found in this JSON!");
        }
    }

    public void recursiveEliminateHelper(JsonElement object, String prefix, boolean isArray, Set<String> banList){
        // if the node is in the ban list, return and prohibit further recursion
        if(banList.contains(prefix))
            return;


        // if isArray is true, it indicates parent is an array, then the object must contains a addW key showing the index
        if(object instanceof JsonObject){
            // continue the recursion
            if(isArray){
                int index = ((JsonObject) object).get(Settings.newTagName).getAsInt();
                prefix += index;
            }
            for(Map.Entry<String, JsonElement> entry:((JsonObject) object).entrySet()){
                recursiveEliminateHelper(entry.getValue(),prefix+entry.getKey(),false,banList);
            }
        }else if(object instanceof JsonArray){
            if(isArray){
                System.out.println("[Warning] Recursive structure of JsonArray is currently under tests.");
            }
            for (Iterator<JsonElement> iter = ((JsonArray) object).iterator(); iter.hasNext();){
                recursiveEliminateHelper(iter.next(),prefix,true,banList);
            }
        }else if(!(object instanceof JsonNull)){
            // instance of JsonPrimitive
            String value = ((JsonPrimitive)object).getAsString();
            int lengthQualify = Util.lengthQualify(value,3);
            if (Util.isNumeric(value) && lengthQualify > Settings.DEFAULT_MINLEN){
                //valid
                JSON.put(prefix.replaceAll("[^A-Za-z0-9]",""),value);
//                if(Util.isNumeric(value))
                //新规定要yy求只能在float或者int中嵌入数据
                valid += lengthQualify/Settings.DEFAULT_MINLEN;
            }
            sum++;
        }
    }
}
