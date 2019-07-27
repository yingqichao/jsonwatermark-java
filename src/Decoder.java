import Utils.Util;
import com.google.gson.*;

import java.util.*;

public class Decoder {
    public int success_time = 0;
    public LtDecoder decoder = null;
    public List<String> secret_data = new LinkedList<>();

    public Map<String,String> JSON = new HashMap<>();
    public Map<String,String> watermarkedJSON = new HashMap<>();

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
                    System.out.println("--> Decoded Successfully <--... The watermark is now successfully retrieved. Time: "+success_time);
                    List<Integer> buff = decoder.bytes_dump();

//                    //Test
//                    for(int i=0;i<buff.size();i+=1){
//                        System.out.print(Util.dec2bin(buff.get(i),4));
//                    }

                    String str = "";
                    for(int i=0;i<buff.size();i+=2){
                        int code = buff.get(i)*16 + buff.get(i+1);
                        str += (char)code;
                    }
//                    for(int i=0;i<buff.size();i++) {
//                        int tmp = buff.get(i);
//                        if(tmp==-1)
//                            str+="?";
//                        else
//                            str += (char)('a'+tmp);
//                    }
                    secret_data.add(str);
                    decoder.succeed_and_init();
                }else{
                    System.out.println("Need more Packs...Received: "+decoder.received_packs);
                }
            }
        }

    }

    public List<String> run(JsonObject object) throws Exception{
        //Reads from stream, applying the LT decoding algorithm to incoming encoded blocks until sufficiently many blocks have been received to reconstruct the entire file.
        System.out.println("-----------------------------Extraction---------------------------------------");
        // 解析string
        int filesize = eliminateLevels(object);
//        modified_json= Utils.Util.eliminateLevels(JSON, "");
        decode(JSON, filesize);

        return this.secret_data;
    }

    public int eliminateLevels(JsonObject object) throws Exception{
        // clear
        JSON = new HashMap<>();
        sum = 0;valid = 0;
        //get num of packages. It is shown in the root node.
        try {
            int numpackage = ((JsonPrimitive) ((JsonObject) object).get(Settings.packageNumName)).getAsInt();


            recursiveEliminateHelper(object,"",false);

            System.out.println("Sum of KEYS: " + sum + ". Sum of Valid: " + valid);
            return numpackage;
        }catch(NullPointerException e){
            throw new Exception("[Error] No info of packageNum was found in this JSON!");
        }
    }

    public void recursiveEliminateHelper(JsonElement object, String prefix,boolean isArray){
        // if isArray is true, it indicates parent is an array, then the object must contains a addW key showing the index
        if(object instanceof JsonObject){
            // continue the recursion
            if(isArray){
                int index = ((JsonObject) object).get(Settings.newTagName).getAsInt();
                prefix += index;
            }
            for(Map.Entry<String, JsonElement> entry:((JsonObject) object).entrySet()){
                recursiveEliminateHelper(entry.getValue(),prefix+entry.getKey(),false);
            }
        }else if(object instanceof JsonArray){
            if(isArray){
                System.out.println("[Warning] Recursive structure of JsonArray is currently under tests.");
            }
            for (Iterator<JsonElement> iter = ((JsonArray) object).iterator(); iter.hasNext();){
                recursiveEliminateHelper(iter.next(),prefix,true);
            }
        }else if(!(object instanceof JsonNull)){
            // instance of JsonPrimitive
            String value = ((JsonPrimitive)object).getAsString();

            if (!Util.isJSON(value) && value.replaceAll("[^A-Za-z0-9]","").length() > Settings.DEFAULT_MINLEN){
                //valid
                JSON.put(prefix.replaceAll("[^A-Za-z0-9]",""),value);
                valid++;
            }
            sum++;
        }
    }



}
