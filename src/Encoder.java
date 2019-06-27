import java.util.*;

public class Encoder {
    public int blocksize;
    public int seed;
    public double c = 0.1;
    public double delta = 0.5;
    public int numpacks;
    public String f_bytes;
    public int filesize;
    public int[] blocks;
    public int K;
    public SolitionGenerator solitionGenerator;
    public Map<String,String> JSON = new HashMap<>();
    public Map<String,String> watermarkedJSON = new HashMap<>();

    public Encoder(int blocksize, int seed, double c, double delta, int numpacks, String f_bytes, int filesize) {
        this.blocksize = blocksize;
        this.seed = seed;
        this.c = c;
        this.delta = delta;
        this.numpacks = numpacks;
        this.f_bytes = f_bytes;
        split_file();
        this.K = this.blocks.length;
        this.solitionGenerator = new SolitionGenerator(this.K,c,delta,0);// Seed is set by interfacing code using set_seed

    }

    public void split_file(){
        //Block file byte contents into blocksize chunks, padding last one if necessary
        this.blocks = new int[f_bytes.length()];int i=0;
        for(char c:this.f_bytes.toCharArray()){
            blocks[i] = c-'a';
            i++;
        }
    }

    public void run(Map<String,String> JSON){
        eliminateLevels();
        //转回JSON

        //写入文件


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

    public String encoder(String key,String value){
//        Generates an infinite sequence of blocks to transmit
//        to the receiver
        this.seed = BKDRHash(key,131);
        this.solitionGenerator.setSeed(this.seed);

        return null;

    }

    public int BKDRHash(String str,int seed){
        int hash = 0;
        for(char ch:str.toCharArray()){
            int ans = 0;
            if(ch>='a' && ch<='z'){
                ans = ch-'a';
            }else if(ch>='A' && ch<='Z'){
                ans = ch-'A';
            }else if(ch>='0' && ch<='9'){
                ans = ch-'0';
            }
            hash = hash * seed + ans;
            hash = hash & 0x7FFFFFFF;
        }

        return hash;
    }
}
