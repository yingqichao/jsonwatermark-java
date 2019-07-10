package GsonTest;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gson.*;

public class TestGson {
    public static Map<String,String> JsonMap = new HashMap<>();
    public static int sum = 0;
    public static int valid = 0;
    public static int DEFAULT_MINLEN = 9;


    public static void main(String []args) throws Exception{
        JsonParser parser = new JsonParser() ;
        JsonObject object = (JsonObject)parser.parse(new FileReader("src//test.json"));
        // 解析string
        Map<String,String> map = eliminateLevels(object);

//        System.out.println("name = "+object.get("name").getAsString());
//        // 解析数组
//        JsonArray array = object.get("like").getAsJsonArray();
//        for(int i=0;i<array.size();i++){
//            JsonObject arrayObject = array.get(i).getAsJsonObject() ;
//            System.out.println("id = "+arrayObject.get("id").getAsInt() + " say = "+arrayObject.get("say").getAsString());
//        }
//        // 解析bool类型
//        System.out.println("key = "+object.get("key").getAsBoolean());
    }

    public static Map<String,String> eliminateLevels(JsonObject object){
        sum = 0;valid = 0;

        recursiveHelper(object,"");

        System.out.println("Sum of KEYS: " + sum + ". Sum of Valid: " + valid);
        return JsonMap;
    }

    public static void recursiveHelper(JsonElement object, String prefix){
        if(object instanceof JsonObject){
            // continue the recursion
            for(Map.Entry<String, JsonElement> entry:((JsonObject) object).entrySet()){
                recursiveHelper(entry.getValue(),prefix+entry.getKey());
            }


        }else if(object instanceof JsonArray){

            for (Iterator<JsonElement> iter = ((JsonArray) object).iterator(); iter.hasNext();){
                recursiveHelper(iter.next(),prefix);
            }
        }else if(object instanceof JsonPrimitive){
            // instance of JsonPrimitive
            String value = ((JsonPrimitive)object).getAsString();
            if (value.replaceAll("![A-Za-z0-9]","").length() > DEFAULT_MINLEN){
                //valid
                JsonMap.put(prefix,value);
                valid++;
            }
            sum++;

        }else{
            // JsonNull
            return;
        }
    }
}
