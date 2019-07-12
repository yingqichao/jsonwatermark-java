import com.google.gson.*;

import java.util.*;

public class Encoder {

    public int blocksize;
    public int seed;
    public double c;
    public double delta;
    public int numpacks;
    public String f_bytes;
    public int filesize;
    public int[] blocks;
    public int K;
    public Sampler solitionGenerator;
    public TreeMap<String,String> JSON = new TreeMap<>();
    public TreeMap<String,String> watermarkedJSON = new TreeMap<>();

    public static int sum = 0;
    public static int valid = 0;
    public static int updated = 0;

    public Encoder(int blocksize, int seed, double c, double delta, int numpacks, String f_bytes) {
        this.blocksize = blocksize;
        this.seed = seed;
        this.c = c;
        this.delta = delta;
        this.numpacks = numpacks;
        this.f_bytes = f_bytes;
        split_file();
        this.K = this.blocks.length;
        this.solitionGenerator = new Sampler(this.K,delta,c);// Seed is set by interfacing code using set_seed

    }

    public Encoder(String f_bytes){
        this(1,1, Util.DEFAULT_C, Util.DEFAULT_DELTA,30,f_bytes);
    }

    public void split_file(){
        //Block file byte contents into blocksize chunks, padding last one if necessary
        this.blocks = new int[f_bytes.length()];int i=0;
        for(char c:this.f_bytes.toCharArray()){
            blocks[i] = c-'a';
            i++;
        }
    }

    public String modify(String value,String keyname,Integer waterSeq,List<Integer> blocks){
        //modify the value to embed data
        Character first = null;boolean negative = false;int dotIndex = -1;
        StringBuilder newvalue = new StringBuilder(value);
        int startFrom = 0;
        //preprocess
        if(Util.isInteger(value)){
            long value_int = Long.parseLong(value);
            negative = value_int<0;
            if(negative)  {
                newvalue.deleteCharAt(startFrom);
                startFrom++;
            }
            first = value.charAt(startFrom);newvalue.deleteCharAt(0);
        }else if(Util.isNumeric(value)){
            double value_double = Double.parseDouble(value);
            negative = value_double<0;
            if(negative)    {
                newvalue.deleteCharAt(0);
                startFrom++;
            }
            first = value.charAt(startFrom);newvalue.deleteCharAt(0);
            dotIndex = newvalue.indexOf(".");
            newvalue.deleteCharAt(dotIndex);
        }

        Set<Integer> duplicateSet = new HashSet<>(0);int embedded = 0;

        String crc_text = Util.toBinary(waterSeq,5);
        crc_text += Util.crc_remainder(crc_text,null,null);
        int debug = 0;

        while(embedded<crc_text.length()){
            int num = this.solitionGenerator.get_next() % newvalue.length();
            if(embedded==0)
                debug = num;
            if(!duplicateSet.contains(num)){
                duplicateSet.add(num);
                char ori = newvalue.charAt(num);
                if ((ori >= 'a' && ori <= 'z') || (ori >= 'A' && ori <= 'Z')) {
                    //对于小写大写字母：统一向上取结果
                    newvalue.setCharAt(num, (char) (ori + ori % 2 - crc_text.charAt(embedded) + '0'));
                    embedded ++;
                }
                else if(ori >= '0' && ori <= '9') {
                    //对于数字：统一向下取结果
                    newvalue.setCharAt(num, (char) (ori - ori % 2 + crc_text.charAt(embedded) - '0'));
                    embedded ++;
                }
            }
        }

        //recovery
        if(dotIndex!=-1) newvalue.insert(dotIndex,'.');
        if(first!=null) newvalue.insert(0,first);
        if(negative)    newvalue.insert(0,'-');

        System.out.println("Debug Embed: data->" + waterSeq + " seed->" + debug +" " + keyname + " " + newvalue);
        return newvalue.toString();

    }

    public String encoder(String key,String value){
//        Generates an infinite sequence of blocks to transmit to the receiver
        this.seed = Util.BKDRHash(key,131);
        this.solitionGenerator.setSeed(this.seed);
        List<Integer> list = this.solitionGenerator.get_src_blocks(null);
        int block_data = 0;
        for(int i=2;i<list.size();i++)
            block_data ^= this.blocks[list.get(i)];

        return this.modify(value,key,block_data,list);

    }

    public JsonElement run(JsonObject object){
        System.out.println("-----------------------------Embedding---------------------------------------");
        try {
            // 解析string
            JSON = eliminateLevels(object);

            //Embedment
            for(String key:this.JSON.keySet()){
                String newValue = encoder(key,this.JSON.get(key));
                watermarkedJSON.put(key,newValue);
            }

            JsonElement newJsonElement = JsonUpdating(object);
//            FileOutputStream out=new FileOutputStream("src//embedded.json");
//            Util.writeJsonStream(out,object);

//            Util.writeFromJSON(watermarkedJSON);
            // Writing Json



            System.out.println("-----------------Embedding was conducted successfully...--------------------");

            return newJsonElement;
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;

    }

    //    public void eliminateLevels(){
////        int sum = 0,valid = 0;
//        for(String key:this.JSON.keySet()){
////            String value = this.JSON.get(key);
////            if(value.replaceAll("[^0-9a-zA-Z]","" ).length()>=7){
//                String newValue = encoder(key,value);
//                watermarkedJSON.put(key,newValue);
////                valid += 1;
////            }else{
////                watermarkedJSON.put(key,value);
////            }
////            sum += 1;
//        }
//
//    }

    public TreeMap<String,String> eliminateLevels(JsonObject object){
        // clear
        JSON = new TreeMap<>();
        sum = 0;valid = 0;

        recursiveEliminateHelper(object,"");

        for(String key:JSON.keySet())
            System.out.println(key+"   "+JSON.get(key));

        System.out.println("-------------------------------------------------");

        System.out.println("Sum of KEYS: " + sum + ". Sum of Valid: " + valid);
        return JSON;
    }

    public void recursiveEliminateHelper(JsonElement object, String prefix){
        if(object instanceof JsonObject){
            // continue the recursion
            for(Map.Entry<String, JsonElement> entry:((JsonObject) object).entrySet()){
                recursiveEliminateHelper(entry.getValue(),prefix+entry.getKey());
            }
        }else if(object instanceof JsonArray){
            for (Iterator<JsonElement> iter = ((JsonArray) object).iterator(); iter.hasNext();){
                recursiveEliminateHelper(iter.next(),prefix);
            }
        }else if(!(object instanceof JsonNull)){
            // instance of JsonPrimitive
            String value = ((JsonPrimitive)object).getAsString();

            if (!Util.isJSON(value) && value.replaceAll("[^A-Za-z0-9]","").length() > Util.DEFAULT_MINLEN){
                //valid
                JSON.put(prefix.replaceAll("[^A-Za-z0-9]",""),value);
                valid++;
            }
            sum++;
        }
    }

    public JsonElement JsonUpdating(JsonObject object){
        JsonElement jsonElement = Util.replaceKey(object,watermarkedJSON,"","");

        System.out.println("Successfully updated!......");

        return jsonElement;

    }

//    public void recursiveUpdatingHelper(JsonElement object, String prefix){
//        if(object instanceof JsonObject){
//            // continue the recursion
//            for(Map.Entry<String, JsonElement> entry:((JsonObject) object).entrySet()){
//                recursiveEliminateHelper(entry.getValue(),prefix+entry.getKey());
//            }
//        }else if(object instanceof JsonArray){
//            for (Iterator<JsonElement> iter = ((JsonArray) object).iterator(); iter.hasNext();){
//                recursiveEliminateHelper(iter.next(),prefix);
//            }
//        }else{
//            // instance of JsonPrimitive
//            String value = ((JsonPrimitive)object).getAsString();
//            if (watermarkedJSON.containsKey(prefix)){
//                //update
//                JsonPrimitive jsonPrimitive = (JsonPrimitive)object;
//                jsonPrimitive.setValue(watermarkedJSON.get(prefix));
//                updated++;
//            }
//            sum++;
//        }
//    }

}
