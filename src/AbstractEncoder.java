import GeneralHelper.Sampler;
import Setting.Settings;

import java.util.*;

public abstract class AbstractEncoder {


    public int seed;
    public double c;
    public double delta;

    public String f_bytes;
    public int filesize;
    public int[] blocks;
    public int K;
    public int minRequire;
    public Sampler solitionGenerator;

    public static int sum = 0;
    public static int valid = 0;
    public static int updated = 0;

    public AbstractEncoder(int seed, double c, double delta, String f_bytes) {
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

    public AbstractEncoder(String f_bytes){
        this(1, Settings.DEFAULT_C, Settings.DEFAULT_DELTA,f_bytes);
    }

    public void split_file(){
        //Block file byte contents into blocksize chunks, padding last one if necessary
        this.blocks = new int[f_bytes.length()/Settings.DEFAULT_DATALEN];
        for(int i=0;i<f_bytes.length();i+=Settings.DEFAULT_DATALEN){
            blocks[i/4] = Utils.StrBinaryTurn.binaryToDecimal(f_bytes.substring(i,i+Settings.DEFAULT_DATALEN));
        }
    }

}
