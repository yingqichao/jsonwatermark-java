package GeneralHelper;

import Setting.Settings;
import Utils.Util;

import java.util.*;

public class LtDecoder {
    public int received_packs = 0;
    public double c = 0.0;
    public double delta = 0.0;
    public int K = 0;
    public int filesize = 0;
    public int blocksize = 0;
    public BlockGraph block_graph = null;
    public Sampler prng = null;
    public boolean initialized = false;
    public boolean done = false;

    public LtDecoder(double c, double delta) {
        this.c = c;
        this.delta = delta;
    }

    public void succeed_and_init(){
        received_packs = 0;
        K = 0;
        block_graph = null;
        prng = null;
        initialized = false;
    }

    public boolean is_done(){
        return done;
    }

    public boolean consume_block(int filesize,String key,String lt_block,Integer blocksize){
        //如果一个包的有效字符长度足够长，那么会被拆成若干个包
        if(blocksize==null) blocksize = 1;
        Set<Integer> duplicateSet = new HashSet<>(0);

        int innerPackage = lt_block.replaceAll("[^A-Za-z0-9]","").length()/Settings.DEFAULT_MINLEN;
        if(innerPackage>1)
            System.out.println("-- This package is splitted to embed "+innerPackage+" packages --");
        for(int ite=0;ite<innerPackage;ite++) {

            if (!initialized) {
                this.filesize = filesize;
                this.blocksize = blocksize;
                this.K = (int) Math.ceil(filesize / blocksize);
                this.block_graph = new BlockGraph(K);
                this.prng = new Sampler(K, Settings.DEFAULT_DELTA, Settings.DEFAULT_C);
                this.initialized = true;
            }

            int blockseed = Util.BKDRHash(key+((innerPackage>1)?ite:""),null);

            this.prng.setSeed(blockseed);
            // Run PRNG with given seed to figure out which blocks were XORed to make received data
            List<Integer> src_blocks = prng.get_src_blocks(null);// or seed=blockseed
            src_blocks.remove(0);//blockseed
            src_blocks.remove(0);//d
            List<Object> blockAndVerify = extract(key, lt_block, duplicateSet);
            String tmpstr = (String) blockAndVerify.get(1);
            if (cyclic.CyclicCoder.decode(Integer.parseInt(tmpstr, 2)) != -1) {
                // If BP is done, stop
                System.out.println("Valid Package.");
                this.done = handle_block(src_blocks, (int) blockAndVerify.get(0));
//                return this.done;
            } else {
                System.out.println("Invalid Package.Skip...");
//                return false;
            }
        }
        return this.done;

    }

    public List<Integer> getSrcBlocks(int filesize,String key,Integer blocksize){
        //简化，没有拆子包的环节，而且能够调用这个函数的string都是合法的，CRC的检验已经交给了ExcelDecoder
        if(blocksize==null) blocksize = 1;
//        Set<Integer> duplicateSet = new HashSet<>(0);

//        int innerPackage = lt_block.replaceAll("[^A-Za-z0-9]","").length()/Settings.DEFAULT_MINLEN;


        if (!initialized) {
            this.filesize = filesize;
            this.blocksize = blocksize;
            this.K = (int) Math.ceil(filesize / blocksize);
            this.block_graph = new BlockGraph(K);
            this.prng = new Sampler(K, Settings.DEFAULT_DELTA, Settings.DEFAULT_C);
            this.initialized = true;
        }

        int blockseed = Util.BKDRHash(key,null);

        this.prng.setSeed(blockseed);
        // Run PRNG with given seed to figure out which blocks were XORed to make received data
        List<Integer> src_blocks = prng.get_src_blocks(null);// or seed=blockseed
        src_blocks.remove(0);//blockseed
        src_blocks.remove(0);//d

        return src_blocks;

    }

    public boolean consume_block_excel(List<Integer> src_blocks,int validValue){
        this.done = handle_block(src_blocks, validValue);
        return this.done;

    }

    public List<Object> extract_excel(String key,String ori_block,int strlen){
        //根据分配到的每个单元格的嵌入长度来做提取，没有检验的环节
        boolean negative;String verify = "";
        int extracted = 0;StringBuilder lt_block = new StringBuilder(ori_block);

        //preprocess
        if(Util.isInteger(ori_block)){
            long value_int = Long.parseLong(ori_block);
            negative = value_int<0;
            if(negative)    lt_block.deleteCharAt(0);
            lt_block.deleteCharAt(0);
        }else if(Util.isNumeric(ori_block)){
            double value_double = Double.parseDouble(ori_block);
            negative = value_double<0;
            if(negative)    lt_block.deleteCharAt(0);
            lt_block.deleteCharAt(0);
            lt_block.deleteCharAt(lt_block.indexOf("."));
        }

        int buff = -1;Set<Integer> duplicateSet = new HashSet<>();
        String debug = new String();
        int ind = 0;
        while(ind<strlen){
            int num = prng.get_next() % lt_block.length();
            if(ind==0)  buff = num;
            if(!duplicateSet.contains(num)) {
                duplicateSet.add(num);
                char ori = lt_block.charAt(num);
                debug += ori;
                if ((ori >= 97 && ori <=122)||(ori >= 65 && ori <= 90) ||(ori >= 48 && ori <= 57)){

                    extracted *= 2;
                    extracted += (ori % 2);// * pow(2, ind)

                    verify += ori % 2;
                    ind += 1;
                }
            }

        }

        List<Object> list = new LinkedList<>();list.add(extracted);list.add(debug);

//        System.out.println("Debug Extract: data->" + extracted + " seed->" + buff + " " + key + " " + ori_block);
        return list;
    }

    public List<Object> extract(String key,String ori_block,Set<Integer> duplicateSet){
        List<Object> list = new LinkedList<>();
        int strlen = Settings.DEFAULT_EMBEDLEN;boolean negative;String verify = "";
        int extracted = 0;StringBuilder lt_block = new StringBuilder(ori_block);

        //preprocess
        if(Util.isInteger(ori_block)){
            long value_int = Long.parseLong(ori_block);
            negative = value_int<0;
            if(negative)    lt_block.deleteCharAt(0);
            lt_block.deleteCharAt(0);
        }else if(Util.isNumeric(ori_block)){
            double value_double = Double.parseDouble(ori_block);
            negative = value_double<0;
            if(negative)    lt_block.deleteCharAt(0);
            lt_block.deleteCharAt(0);
            lt_block.deleteCharAt(lt_block.indexOf("."));
        }

        int buff = -1;

        int ind = 0;
        while(ind<strlen){
            int num = prng.get_next() % lt_block.length();
            if(ind==0)  buff = num;
            if(!duplicateSet.contains(num)) {
                duplicateSet.add(num);
                char ori = lt_block.charAt(num);
                if ((ori >= 97 && ori <=122)||(ori >= 65 && ori <= 90) ||(ori >= 48 && ori <= 57)){
                    if (ind < Settings.DEFAULT_DATALEN) {
                        extracted *= 2;
                        extracted += (ori % 2);// * pow(2, ind)
                    }
                    verify += ori % 2;
                    ind += 1;
                }
            }

        }

        System.out.println("Debug Extract: data->" + extracted + " seed->" + buff + " " + key + " " + ori_block);
        list.add(extracted);list.add(verify);
        return list;
    }

    public List<Integer> bytes_dump(){
        return stream_dump();
    }

    public List<Integer> stream_dump(){
        List<Integer> res = new LinkedList<>();
        int[] data = new int[filesize];
        for(int key:block_graph.eliminated.keySet()){
            data[key] = block_graph.eliminated.get(key);
        }
        for(int i=0;i<filesize;i++)
            res.add(data[i]);
        return res;

    }

    public boolean handle_block(List<Integer> src_blocks, int block){
        // What to do with new block: add check and pass messages in graph
        return block_graph.add_block(new HashSet<>(src_blocks), block);
    }

}
