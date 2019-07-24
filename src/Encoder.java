import Utils.*;
import com.google.gson.*;

import java.util.*;

public class Encoder {


    public int seed;
    public double c;
    public double delta;

    public String f_bytes;
    public int filesize;
    public int[] blocks;
    public int K;
    public int minRequire;
    public Sampler solitionGenerator;
    public TreeMap<String,String> JSON = new TreeMap<>();
    public TreeMap<String,String> watermarkedJSON = new TreeMap<>();

    public static int sum = 0;
    public static int valid = 0;
    public static int updated = 0;

    public Encoder( int seed, double c, double delta, String f_bytes) {
        this.seed = seed;
        this.c = c;
        this.delta = delta;
        this.f_bytes = f_bytes;
        split_file();
        this.K = this.blocks.length;
        this.minRequire = this.K*2;
        System.out.println("packages: "+this.K+". Minimum required blocks: "+this.K*2);
        this.solitionGenerator = new Sampler(this.K,delta,c);// Seed is set by interfacing code using set_seed

    }

    public Encoder(String f_bytes){
        this(1, Settings.DEFAULT_C, Settings.DEFAULT_DELTA,f_bytes);
    }

    public void split_file(){
        //Block file byte contents into blocksize chunks, padding last one if necessary
        this.blocks = new int[f_bytes.length()/Settings.DEFAULT_DATALEN];
        for(int i=0;i<f_bytes.length();i+=Settings.DEFAULT_DATALEN){
            blocks[i/4] = Utils.StrBinaryTurn.binaryToDecimal(f_bytes.substring(i,i+Settings.DEFAULT_DATALEN));
        }
    }

    public String encoder(String key,String value){
//        Generates an infinite sequence of blocks to transmit to the receiver
        Set<Integer> duplicateSet = new HashSet<>();String res = value;
        int innerPackage = value.replaceAll("[^A-Za-z0-9]","").length()/Settings.DEFAULT_MINLEN;
        if(innerPackage>1)
            System.out.println("-- This package is splitted to embed "+innerPackage+" packages --");
        for(int ite=0;ite<innerPackage;ite++){
            this.seed = Util.BKDRHash(key+((innerPackage>1)?ite:""),131);
            this.solitionGenerator.setSeed(this.seed);
            List<Integer> list = this.solitionGenerator.get_src_blocks(null);
            int block_data = 0;
            for(int i=2;i<list.size();i++)
                block_data ^= this.blocks[list.get(i)];

            res = modify(res,key,block_data,list,duplicateSet);

        }



        return res;

    }

    public String modify(String value,String keyname,Integer waterSeq,List<Integer> blocks,Set<Integer> duplicateSet){
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

//        Set<Integer> duplicateSet = new HashSet<>(0);
        int embedded = 0;

//        String crc_text = Util.toBinary(waterSeq,Settings.DEFAULT_DATALEN);
//        crc_text += Util.crc_remainder(crc_text,null,null);

        String crc_text = Util.dec2bin(cyclic.CyclicCoder.encode(waterSeq),Settings.DEFAULT_EMBEDLEN);

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
                //其他的字符全都直接跳过
            }
        }

        //recovery
        if(dotIndex!=-1) newvalue.insert(dotIndex,'.');
        if(first!=null) newvalue.insert(0,first);
        if(negative)    newvalue.insert(0,'-');

        System.out.println("Debug Embed: data->" + waterSeq + " seed->" + debug +" " + keyname + " " + newvalue);
        return newvalue.toString();

    }


    public JsonElement run(JsonObject object){
        System.out.println("-----------------------------Embedding---------------------------------------");
        try {
            // 解析string
            JSON = eliminateLevels(object);
            if(valid<this.minRequire){
                throw new Exception("[Error] Not enough valid packages for watermarking! Please shorter the watermark sequence...");
            }
            //Embedment
            for(String key:this.JSON.keySet()){
                String newValue = encoder(key,this.JSON.get(key));
                watermarkedJSON.put(key,newValue);
            }

            JsonElement newJsonElement = JsonUpdating(object);
//            FileOutputStream out=new FileOutputStream("src//embedded.json");
//            Utils.Util.writeJsonStream(out,object);

//            Utils.Util.writeFromJSON(watermarkedJSON);
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

        recursiveEliminateHelper(object,"",0);

        for(String key:JSON.keySet())
            System.out.println(key+"   "+JSON.get(key));

        System.out.println("-------------------------------------------------");

        System.out.println("Sum of KEYS: " + sum + ". Sum of Valid: " + valid);
        return JSON;
    }

    public void recursiveEliminateHelper(JsonElement object, String prefix,int arrayCount){
        if(arrayCount>0){
            //which indicates the parent object is an array
            prefix += arrayCount;
            arrayCount = 0;
        }
        if(object instanceof JsonObject){
            // continue the recursion
            for(Map.Entry<String, JsonElement> entry:((JsonObject) object).entrySet()){
                recursiveEliminateHelper(entry.getValue(),prefix+entry.getKey(),arrayCount);
            }
        }else if(object instanceof JsonArray){
            int count = 1;
            for (Iterator<JsonElement> iter = ((JsonArray) object).iterator(); iter.hasNext();){
                recursiveEliminateHelper(iter.next(),prefix,count);
                count++;
            }
        }else if(!(object instanceof JsonNull)){
            // instance of JsonPrimitive
            String value = ((JsonPrimitive)object).getAsString();

            if (!Util.isJSON(value) && value.replaceAll("[^A-Za-z0-9]","").length() > Settings.DEFAULT_MINLEN){
                //valid
                JSON.put(prefix.replaceAll("[^A-Za-z0-9]",""),value);
                valid+=value.replaceAll("[^A-Za-z0-9]","").length()/Settings.DEFAULT_MINLEN;
            }
            sum++;
        }
    }

    public JsonElement JsonUpdating(JsonObject object){
        JsonElement jsonElement = Util.replaceKey(object,watermarkedJSON,"","",0,Settings.newTagName,Settings.packageNumName,this.K);

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
