import javax.print.attribute.IntegerSyntax;
import java.util.*;

public class Encoder {
    private static double DEFAULT_C = 0.1;
    private static double DEFAULT_DELTA = 0.5;
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
    public Map<String,String> JSON = new HashMap<>();
    public Map<String,String> watermarkedJSON = new HashMap<>();

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
        this(1,1,DEFAULT_C,DEFAULT_DELTA,30,f_bytes);
    }

    public void split_file(){
        //Block file byte contents into blocksize chunks, padding last one if necessary
        this.blocks = new int[f_bytes.length()];int i=0;
        for(char c:this.f_bytes.toCharArray()){
            blocks[i] = c-'a';
            i++;
        }
    }

    public String modify(String value,Integer waterSeq){
        //modify the value to embed data
        Character first = null;boolean negative = false;int dotIndex = -1;
        StringBuilder newvalue = new StringBuilder(value);

        //preprocess
        if(Util.isInteger(value)){
            int value_int = Integer.parseInt(value);
            negative = value_int<0;
            if(negative)    newvalue.deleteCharAt(0);
            first = value.charAt(0);newvalue.deleteCharAt(0);
        }else if(Util.isNumeric(value)){
            double value_double = Double.parseDouble(value);
            negative = value_double<0;
            if(negative)    newvalue.deleteCharAt(0);
            first = value.charAt(0);newvalue.deleteCharAt(0);
            dotIndex = value.indexOf('.');
        }

        Set<Integer> duplicateSet = new HashSet<>(0);int embedded = 0;

        String crc_text = Util.toBinary(waterSeq,5);
        crc_text += Util.crc_remainder(crc_text,null,null);

        while(embedded<crc_text.length()){
            int num = this.solitionGenerator.get_next();
            if(!duplicateSet.contains(num)){
                duplicateSet.add(num);
                char ori = newvalue.charAt(num);
                if ((ori >= 'a' && ori <= 'z') || (ori >= 'A' && ori <= 'Z'))
                    //对于小写大写字母：统一向上取结果
                    newvalue.setCharAt(num,(char)(ori + ori % 2 - crc_text.charAt(embedded) + '0'));
                else
                    //对于数字：统一向下取结果
                    newvalue.setCharAt(num,(char)(ori - ori % 2 + crc_text.charAt(embedded) - '0'));
                embedded ++;
            }
        }

        //recovery
        if(Util.isNumeric(value)) newvalue.insert(dotIndex,'.');
        if(first!=null) newvalue.insert(0,first);
        if(negative)    newvalue.insert(0,'-');

        System.out.println("Debug Embed: " + waterSeq + " " + newvalue);
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

        return this.modify(value,block_data);

    }

    public void eliminateLevels(){
        int sum = 0,valid = 0;
        for(String key:this.JSON.keySet()){
            String value = this.JSON.get(key);
            if(value.replaceAll("[^0-9a-zA-Z]","" ).length()>=7){
                String newValue = encoder(key,value);
                watermarkedJSON.put(key,newValue);
                valid += 1;
            }else{
                watermarkedJSON.put(key,value);
            }
            sum += 1;
        }

    }

    public Map<String,String> run(Map<String,String> JSON){
        System.out.println("-----------------------------Embedding---------------------------------------");
        try {
            this.eliminateLevels();

            Util.writeFromJSON(watermarkedJSON);
            System.out.println("-----------------Embedding was conducted successfully...--------------------");


        }catch (Exception e){
            e.printStackTrace();
        }

        return watermarkedJSON;

    }

}
